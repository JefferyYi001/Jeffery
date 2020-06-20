# 1.  Spark 集群启动流程分析

## 1.1 sbin/start-all.sh

```bash
# sbin/start-all.sh 中会调用 start-master.sh、start-slaves.sh
    "${SPARK_HOME}/sbin"/start-master.sh
    "${SPARK_HOME}/sbin"/start-slaves.sh
# -----------------------------------------------------------------------------
# start-master.sh 
# （1）配置 ${SPARK_HOME} 下的配置脚本（如果没有设置 SPARK_HOME 环境变量，将当前脚本的上一级目录作为 SPARK_HOME）
    "${SPARK_HOME}/sbin/spark-config.sh"
    "${SPARK_HOME}/bin/load-spark-env.sh"
# （2）配置主机名（默认当前主机名）、通信端口（默认7077）、webUI 端口（默认8080）
# （3）调用 spark-daemon.sh 脚本，传参 org.apache.spark.deploy.master.Master
    CLASS="org.apache.spark.deploy.master.Master"
        "${SPARK_HOME}/sbin"/spark-daemon.sh start $CLASS 1 \
        --host $SPARK_MASTER_HOST  \
        --port $SPARK_MASTER_PORT  \
        --webui-port $SPARK_MASTER_WEBUI_PORT \
        $ORIGINAL_ARGS
# ------------------------------------------------------------------------------
# spark-daemon.sh 
# 配置日志目录，记录日志
# 上一步的参数会进入 run_command 函数
    $option=start
    command=$1

    run_command class "$@"

    mode="$1"  # mode=class
# 进而进入 execute_command 函数，运行 spark-class 脚本
    execute_command nice -n "$SPARK_NICENESS" "${SPARK_HOME}"/bin/spark-class "$command" "$@"
    nohup -- "$@" >> $log 2>&1 < /dev/null &

# ------------------------------------------------------------------------------
# spark-class 
# 最终执行的 "${CMD[@]}" 命令在控制台打出后是下方的一长串：
    CMD=("${CMD[@]:0:$LAST}")
    exec "${CMD[@]}"

    /opt/module/jdk1.8.0_144/bin/java 
    -cp /opt/module/spark-standalone/conf/:/opt/module/spark-standalone/jars/* -Dspark.deploy.recoveryMode=ZOOKEEPER -Dspark.deploy.zookeeper.url=hadoop102:2181,hadoop103:2181,hadoop104:2181 -Dspark.deploy.zookeeper.dir=/spark1128 -Xmx1g 
    org.apache.spark.deploy.master.Master 
    --host hadoop102 --port 7077 --webui-port 8080
# 至此，完成 Master 类的启动
#--------------------------------------------------------------------------------
# Worker 类的启动
# start-slaves.sh
    "${SPARK_HOME}/sbin"/start-slaves.sh
#--------------------------------------------------------------------------------
# 随后调用 ssh 调用各个节点上的 start-slave.sh
# 实际是执行的java -cp Worker
# 后面的步骤与 Master 类的启动类似，此处不再赘述。最后执行的内容为：
opt/module/jdk1.8.0_144/bin/java 
-cp /opt/module/spark-standalone/conf/:/opt/module/spark-standalone/jars/* 
-Dspark.deploy.recoveryMode=ZOOKEEPER 
-Dspark.deploy.zookeeper.url=hadoop102:2181,hadoop103:2181,hadoop104:2181 
-Dspark.deploy.zookeeper.dir=/spark1128 -Xmx1g 
org.apache.spark.deploy.worker.Worker --webui-port 8081 spark://hadoop102:7077

```

## 1.2 启动 Master

```scala
//  The life-cycle of an endpoint is:
//  constructor -> onStart -> receive* -> onStop
// constructor -----------------------------------------------------------------
// 伴生对象中的 main 根据通信参数创建 Master 端的 RpcEnv 环境, 并启动 RpcEnv
val rpcEnv: RpcEnv = RpcEnv.create(SYSTEM_NAME, host, port, conf, securityMgr)
// 本质是 NettyRpcEnv

// 根据 RpcEnv 创建一个 Master 对象
val masterEndpoint: RpcEndpointRef = rpcEnv.setupEndpoint(ENDPOINT_NAME,
            new Master(rpcEnv, rpcEnv.address, webUiPort, securityMgr, conf)) 

// java 中的线程池：只能跑一个后台线程的线程池（ Master 对象属性）
private val forwardMessageThread: ScheduledExecutorService =
    ThreadUtils.newDaemonSingleThreadScheduledExecutor("master-forward-message-thread")
// onStart ----------------------------------------------------------------------
// 传递的线程, 会每隔60s执行一次!
checkForWorkerTimeOutTask = forwardMessageThread.scheduleAtFixedRate(new Runnable {
            override def run(): Unit = Utils.tryLogNonFatalError {
                self.send(CheckForWorkerTimeOut) // 给自己发送信息
            }
        }, 0, WORKER_TIMEOUT_MS, TimeUnit.MILLISECONDS)
// 0: 延迟执行的时间
// WORKER_TIMEOUT_MS:执行频率（60*1000）
            case CheckForWorkerTimeOut =>
                        // 移除超时的workers
                        timeOutDeadWorkers()
// 所有注册成功的 worker 创建一个 set 集合（后序过程中会再创建两个 Map 集合）
val workers = new HashSet[WorkerInfo]
    private val idToWorker = new HashMap[String, WorkerInfo]
    private val addressToWorker = new HashMap[RpcAddress, WorkerInfo]
// timeOutDeadWorkers() 每60秒判断一次 work 是否超时(失联60s), 如果超时, 则异常. 通过上次的心跳时间来确定
/*
总结: 
    创建一个单例线程，每 60 秒判断一次 work 是否超时(失联60s), 如果超时, 则异常（通过上次的心跳时间来确定）。
*/
```

## 1.3 启动 Worker

```scala
org.apache.spark.deploy.worker.Worke
// The life-cycle of an endpoint is:
// constructor -> onStart -> receive* -> onStop
// constructor -----------------------------------------------------------------
// 前面步骤与 Master 基本一致，需要注意的是启动参数中包含所有 Master URL（高可用），会将其解析为一个字符串数组。
// 伴生对象中的 main 根据通信参数创建 Worker 端的 RpcEnv 环境, 并启动 RpcEnv

// onStart ----------------------------------------------------------------------
// 向 master 注册 worker
registerWithMaster()
    // 尝试去向 master 注册, 但是有可能注册失败
    // 比如 master 还没有启动成功, 或者网络有问题
	// 首先定义注册策略：向所有的 master 注册
    registerMasterFutures = tryRegisterAllMasters()
// ------------tryRegisterAllMasters() ------------------------------------------
        masterRpcAddresses.map { masterAddress =>
            registerMasterThreadPool.submit(new Runnable {
                override def run(): Unit = {
                    try {
                        logInfo("Connecting to master " + masterAddress + "...")
    // 根据构造器中 Master 的地址得到一个 Master 的 RpcEndpointRef, 然后就可以和 Master 进行通讯了.
                        val masterEndpoint: RpcEndpointRef = rpcEnv.setupEndpointRef(masterAddress, Master.ENDPOINT_NAME)
    // -------------------------- 向 Master 注册 -------------------------------
             registerWithMaster(masterEndpoint)
            // -------------- registerWithMaster(masterEndpoint) --------------
                  // 给master发送信息:RegisterWorker（包含 work 的信息）---------
             masterEndpoint.ask[RegisterWorkerResponse](RegisterWorker(
             workerId, host, port, self, cores, memory, workerWebUiUrl))
                    } catch {
                        case ie: InterruptedException => // Cancelled
                        case NonFatal(e) => logWarning(s"Failed to connect to master $masterAddress", e)
                    }
                }
            })
    				// Master 端收到注册信息后会对其进行处理-------------------
         case RegisterWorker(
        id, workerHost, workerPort, workerRef, cores, memory, workerWebUiUrl) =>
            logInfo("Registering worker %s:%d with %d cores, %s RAM".format(
                workerHost, workerPort, cores, Utils.megabytesToString(memory)))
            if (state == RecoveryState.STANDBY) { // state 为 Master 当前的状态
                // 给发送者回应消息.  对方的 receive 方法会收到这个信息
                context.reply(MasterInStandby)
            } else if (idToWorker.contains(id)) { // 如果要注册的 Worker 已经存在
                context.reply(RegisterWorkerFailed("Duplicate worker ID"))
            } else {
                // 根据传来的信息封装 WorkerInfo
                val worker = new WorkerInfo(id, workerHost, workerPort, cores, memory,
                    workerRef, workerWebUiUrl)
                // 进行注册：registerWorker(worker) 有 3 步：清理 deadWorker；将新注册的 worker 存入到 workers；把 id 和 worker 的映射存入到 HashMap 中。
                if (registerWorker(worker)) { // 注册成功
                    persistenceEngine.addWorker(worker)
                    // 发送响应信息给 Worker，包含当前 master 的信息
                    context.reply(RegisteredWorker(self, masterWebUiUrl))
                    schedule()
                } else {...}
            }
    // --------------Worker 端收到注册成功信息后会对其进行处理-------------------
   	 msg match {
            case RegisteredWorker(masterRef, masterWebUiUrl) =>
                logInfo("Successfully registered with master " + masterRef.address.toSparkURL)
                // 更新注册状态
                registered = true
                // 更新 Master
                changeMaster(masterRef, masterWebUiUrl)
                // 起一个单例线程池，给 Master 发送心跳信息，默认 1 分钟 4 次
                forwordMessageScheduler.scheduleAtFixedRate(new Runnable {
                    override def run(): Unit = Utils.tryLogNonFatalError {
                        // 先是自己给自己发，收到信息后再给 Master 发送心跳信息
                        self.send(SendHeartbeat)
                    }
                }, 0, HEARTBEAT_MILLIS, TimeUnit.MILLISECONDS)
         // ------------Master 端收到注册信息后更新心跳时间-------------------
         case Heartbeat(workerId, worker) =>
            idToWorker.get(workerId) match {
                case Some(workerInfo) =>
                    // 记录该 Worker 的最新心跳   每 15秒更新一次
                    workerInfo.lastHeartbeat = System.currentTimeMillis()
                
    // 以上只是策略，还没有实际执行！！！
	// 从线程池中启动单例线程来执行 Worker 向 Master 的注册，若注册失败则采用随机的策略尝试注册。
            registrationRetryTimer = Some(forwordMessageScheduler.scheduleAtFixedRate(
                    new Runnable {
                        override def run(): Unit = Utils.tryLogNonFatalError {
                            Option(self).foreach(_.send(ReregisterWithMaster))
                        }
                    },
                    // [0.5, 1.5) * 10
                    INITIAL_REGISTRATION_RETRY_INTERVAL_SECONDS,
                    INITIAL_REGISTRATION_RETRY_INTERVAL_SECONDS,
                    TimeUnit.SECONDS))
        }
```

## 1.4 总结

![image-20200520205405410](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200520205405410.png)

```scala
1.	start-all.sh脚本，实际是执行java -cp Master和java -cp Worker；
2.	Master启动时首先创建一个RpcEnv对象，负责管理所有通信逻辑；
3.	Master通过RpcEnv对象创建一个Endpoint，Master就是一个Endpoint，Worker可以与其进行通信；
4.	Worker启动时也是创建一个RpcEnv对象；
5.	Worker通过RpcEnv对象创建一个Endpoint；
6.	Worker通过RpcEnv对象建立到Master的连接，获取到一个RpcEndpointRef对象，通过该对象可以与Master通信；
7.	Worker向Master注册，注册内容包括主机名、端口、CPU Core数量、内存数量；
8.	Master接收到Worker的注册，将注册信息维护在内存中的Table中，其中还包含了一个到Worker的RpcEndpointRef对象引用；
9.	Master回复Worker已经接收到注册，告知Worker已经注册成功；
10.	Worker端收到成功注册响应后，开始周期性向Master发送心跳。
```

# 2. YARN 模式启动流程分析

## 2.1  YARN Cluster 模式

YARN Cluster 模式下总共有 3 种进程：

```
SparkSubmit
ApplicationMaster
CoarseGrainedExecutorBackend
```

### 2.1.1 spark-submit.sh

```bash
# 同样是先配置 ${SPARK_HOME} 相关变量
# 之后运行 spark-class 脚本，执行 org.apache.spark.deploy.SparkSubmit 类
exec "${SPARK_HOME}"/bin/spark-class org.apache.spark.deploy.SparkSubmit "$@"
# spark-class -----------------------------------------------------------------
# 最终执行的是下面这行命令：
/opt/module/jdk1.8.0_144/bin/java 
-cp /opt/module/spark-yarn/conf/:/opt/module/spark-yarn/jars/*:/opt/module/hadoop-2.7.2/etc/hadoop/ 
org.apache.spark.deploy.SparkSubmit 
--master yarn 
--deploy-mode cluster 
--class org.apache.spark.examples.SparkPi 
./examples/jars/spark-examples_2.11-2.1.1.jar 1000
```

### 2.1.2 SparkSubmit 类/ 进程

```scala
// SparkSubmit Object 的 main 中先完成参数的解析，之后连同解析后的参数提交
appArgs.action match {
            // 如果没有指定 action, 则 action 的默认值是: Submit
            case SparkSubmitAction.SUBMIT => submit(appArgs)
            case SparkSubmitAction.KILL => kill(appArgs)
            case SparkSubmitAction.REQUEST_STATUS => requestStatus(appArgs)
        }
// submit()----------------------------------------------------------------------
// 准备提交环境
// 当是 yarn-cluster 模式: childMainClass = org.apache.spark.deploy.yarn.Client
// 当是 yarn-client 模式:  childMainClass = args.mainClass
	val (childArgs, childClasspath, sysProps, childMainClass) = prepareSubmitEnvironment(args)
	// prepareSubmitEnvironment()-----------------------------------------------
	// 首先会根据传参进行各种模式判断：Standlone/ Yarn/ Mesos、cluster/ client。期间会对相关的成员变量进行赋值，并校验出不合理的组合。
	// 之后对各个返回值进行赋值，重点关注 childMainClass
	// childMainClass 最终值: org.apache.spark.deploy.yarn.Client
        childMainClass = args.mainClass   // 第一次：用户主类
        // 在 yarn 集群模式下, 会进行第二次赋值：yarn.Client
        childMainClass = "org.apache.spark.deploy.yarn.Client"
	// 返回后执行 runMain 函数
	runMain(childArgs, childClasspath, sysProps, childMainClass, args.verbose)
	// runMain()----------------------------------------------------------------
	// 使用反射的方式加载 childMainClass = "org.apache.spark.deploy.yarn.Client"
	// （在 client模式下, childMainClass 直接指向用户的类）
	mainClass = Utils.classForName(childMainClass)  
	// 反射获取 mainClass 的 main 方法
    val mainMethod: Method = mainClass.getMethod("main", new Array[String](0).getClass)
	// 执行 main 方法
	mainMethod.invoke(null, childArgs.toArray)
    // --------------------执行 Client 类的 main 方法----------------------------
		// Client 类 main 方法中的核心方法：submitApplication()
		launcherBackend.connect()	// 连接到 yarn.
		// 初始化 yarn 客户端
		yarnClient.init(yarnConf)
         // 启动 yarn 客户端
         yarnClient.start()
		// 从 RM 创建一个应用程序
         val newApp = yarnClient.createApplication()
         val newAppResponse = newApp.getNewApplicationResponse()
		// 设置正确的上下文对象来启动 ApplicationMaster 
         val containerContext = createContainerLaunchContext(newAppResponse)
         // 创建应用程序提交任务上下文
         val appContext = createApplicationSubmissionContext(newApp, containerContext)
		// createApplicationSubmissionContext核心代码: 确定 ApplicationMaster 类
            val amClass =
            if (isClusterMode) { // 如果是 cluster 模式
     Utils.classForName("org.apache.spark.deploy.yarn.ApplicationMaster").getName
            } else { // 如果是 client 模式
     Utils.classForName("org.apache.spark.deploy.yarn.ExecutorLauncher").getName
            }
            // 生成 ApplicationMaster/ ExecutorLauncher 启动命令
            val commands = prefixEnv ++ Seq(
                YarnSparkHadoopUtil.expandEnvironment(Environment.JAVA_HOME) + "/bin/java", "-server"
            ) ++ javaOpts ++ amArgs ++
                Seq(
                    "1>", ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout",
                    "2>", ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr")
	// 创建应用程序提交任务上下文
	val appContext = createApplicationSubmissionContext(newApp, containerContext)
    // 提交应用给 ResourceManager 启动 ApplicationMaster
    // org.apache.spark.deploy.yarn.ApplicationMaster
    yarnClient.submitApplication(appContext)
	// 最后返回从 RM 获取的 appId
```

### 2.1.3 ApplicationMaster  类/ 进程

```scala
// 进入 ApplicationMaster 的 main 方法，对传进去的参数进行封装后构建 ApplicationMaster 实例
SparkHadoopUtil.get.runAsSparkUser { () =>
    // 构建 ApplicationMaster 实例,   ApplicationMaster 需要与 RM 通讯
    master = new ApplicationMaster(amArgs, new YarnRMClient)
    // 运行 ApplicationMaster 的 run 方法
    // run 方法结束之后, 结束 ApplicationMaster 进程
    System.exit(master.run())
    	// master.run() 方法 --------------------------------------------------
        // 关键核心代码
        if (isClusterMode) {  // cluster 模式
            runDriver(securityMgr)
        } else { // client 模式
            runExecutorLauncher(securityMgr)
        }
    		// runDriver() 方法
    		// 开始执行用户类. 启动一个子线程来执行用户类的 main 方法.  返回值就是运行用户类的子线程.
			// 线程名就叫"Driver"，说driver是一个进程(process)也对, 指的是ApplicationMaster这个进程
        	userClassThread = startUserApplication()
    		// startUserApplication() 中得到用户类的 main 方法 -----------------
        	val mainMethod: Method = userClassLoader.loadClass(args.userClass)
            .getMethod("main", classOf[Array[String]])
            // 在子线程内执行用户类的 main 方法
            val userThread: Thread = new Thread {
                override def run() {
                    try {
                        // 调用用户类的 main 方法
                        mainMethod.invoke(null, userArgs.toArray)
                        finish(FinalApplicationStatus.SUCCEEDED, ApplicationMaster.EXIT_SUCCESS)
                        logDebug("Done running users class")
    
    	// 在 main 线程中注册 ApplicationMaster , 其实就是请求资源 -------
        registerAM(sc.getConf, rpcEnv, driverRef, sc.ui.map(_.appUIAddress).getOrElse(""), securityMgr)
			// registerAM() ----------------------------------------------
             // 向 RM 注册, 得到 YarnAllocator
                allocator = client.register(driverUrl,
                    driverRef,
                    yarnConf,
                    _sparkConf,
                    uiAddress,
                    historyAddress,
                    securityMgr,
                    localResources)
                // 请求分配资源，期间涉及向 RM 提出分配请求、从 RM 获得容器、处理获得的容器（核心代码: 运行分配的容器 runAllocatedContainers(containersToUse)）
                allocator.allocateResources()
                        // 先对每个容器开一个线程，创建 nmClient 通讯，在通讯中生成 excutor 启动命令，去启动容器中的 excutor 进程
                        handleAllocatedContainers(allocatedContainers.asScala)
                        runAllocatedContainers(containersToUse)
                        new ExecutorRunnable(
                                    Some(container),
                                    conf,
                                    sparkConf,
                                    driverUrl,
                                    executorId,
                                    executorHostname,
                                    executorMemory,
                                    executorCores,
                                    appAttemptId.getApplicationId.toString,
                                    securityMgr,
                                    localResources
                                ).run()
                             startContainer()
                             val commands = prepareCommand()
// excutor 类名 org.apache.spark.executor.CoarseGrainedExecutorBackend

                        // nm启动容器: 其实就是启动一个进程: CoarseGrainedExecutorBackend
                        nmClient.startContainer(container.get, ctx)
                // ----------------------------------------------------------
    	// 线程 join: 把 userClassThread 线程执行完毕之后再继续执行当前线程.
            userClassThread.join()  // yield 礼让给 driver
}
```

### 2.1.4 CoarseGrainedExecutorBackend 类/ 进程

```scala
// 在 main 方法中先运行的是 run 方法
run(driverUrl, executorId, hostname, cores, appId, workerUrl, userClassPath)
	// 创建 SparkEnv 对象，内部封装了一个 EndPointEnv 对象。作为 Spark 内部的一个通讯环境，Driver 在用，Excutor 也用。
    val env: SparkEnv = SparkEnv.createExecutorEnv(
        driverConf, executorId, hostname, port, cores, cfg.ioEncryptionKey, isLocal = false)
	// 在上述环境中调用 CoarseGrainedExecutorBackend 的构造器创建之
    new CoarseGrainedExecutorBackend(env.rpcEnv, driverUrl, executorId, hostname, cores, userClassPath, env
        // 在 CoarseGrainedExecutorBackend.onStart() 方法中获得 driver 的 RPCref，并进行反向注册。
        ref.ask[Boolean](RegisterExecutor(executorId, self, hostname, cores, extractLogUrls))

    // 成功之后，Driver 端的 CoarseGrainedSchedulerBackend（伴随着 context 启动） 将 excutorData 添加到 hashMap 中，和 standalone 模式下的 workInfo 集合类似。
       val data = new ExecutorData(executorRef, executorRef.address, hostname,
                        cores, cores, logUrls)        
       // 给 Executor 发送注册成功的消息
       executorRef.send(RegisteredExecutor)
    // Executor 收到注册成功的消息后 new 一个 Executor
    executor = new Executor(executorId, hostname, env, userClassPath, isLocal = false)
```

### 2.1.5 总结

![image-20200521173128209](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200521173128209.png)

```scala
1.	执行脚本提交任务，实际是启动一个 SparkSubmit 的 JVM 进程；
2.	SparkSubmit 类中的 main方法反射调用Client的main方法；
3.	Client创建Yarn客户端，然后向Yarn发送执行指令：bin/java ApplicationMaster；
4.	Yarn框架收到指令后会在指定的NM中启动ApplicationMaster；
5.	ApplicationMaster启动Driver线程，执行用户的作业；
6.	AM向RM注册，申请资源；
7.	获取资源后AM向NM发送指令：bin/java CoarseGrainedExecutorBacken；
8.	启动ExecutorBackend, 并向driver注册.
9.	注册成功后, ExecutorBackend会创建一个Executor对象.
10.	Driver会给ExecutorBackend分配任务, 并监控任务的执行.
注意:
•	SparkSubmit、ApplicationMaster和CoarseGrainedExecutorBacken是独立的进程；
•	Client和Driver是独立的线程；
•	Executor是一个对象。

```

## 2.2 Yarn Client 模式

YARN Client 模式下总共有 3 种进程：

```
SparkSubmit    
ExecutorLauncher   (ApplicationMaster)
CoarseGrainedExecutorBackend  
```

### 2.2.1 SparkSubmit 类/进程

```scala
// 前面的过程 Client 模式与 Cluster 模式并无二致。
// 到 runMain 函数时，反射并运行的实际上是用户主类的 main 函数
    childMainClass = args.mainClass  // 用户主类

    mainMethod.invoke(null, childArgs.toArray)

// 重点：创建 SparkContext 类 -------------------------------------------------
SparkContext
	// 重要属性
	// 用来与其他组件通讯用  *******
    private var _schedulerBackend: SchedulerBackend = _
    // 任务调度器, 是调度系统中的重要组件之一.
    // TaskScheduler 按照调度算法对集群管理器已经分配给应用程序的资源进行二次调度后分配给任务
    // TaskScheduler 调度的 Task 是由 DAGScheduler 创建的, 所以 DAGScheduler 是 TaskScheduler的前置调度器
    private var _taskScheduler: TaskScheduler = _
    // 用来接收 Executor 的💛跳信息
    private var _heartbeatReceiver: RpcEndpointRef = _
    // DAG 调度器, 是调度系统的中的中的重要组件之一, 负责创建 Job, 将 DAG 中的 RDD 划分到不同的 Stage, 提交 Stage 等.
    // SparkUI 中有关 Job 和 Stage 的监控数据都来自 DAGScheduler
    @volatile private var _dagScheduler: DAGScheduler = _


	// 构造器，首先对读取 conf 中的各项配置并校验，例如判断 appName、master 是否设置
	// Executor内存, 默认 1G
	// 接下来的重点是创建 TaskScheduler
	val (sched, ts): (SchedulerBackend, TaskScheduler) = SparkContext.createTaskScheduler(this, master, deployMode)
	// 内部执行如下代码--------------------------------------------
		// 获取外部集群管理器
		val cm: ExternalClusterManager = getClusterManager(masterUrl) 
        try {
            // 创建 TaskScheduler. 实际类型是: YarnScheduler
            val scheduler: TaskScheduler = cm.createTaskScheduler(sc, masterUrl)
            // 创建 SchedulerBackend. 实际类型是: YarnClientSchedulerBackend
            val backend: SchedulerBackend = cm.createSchedulerBackend(sc, masterUrl, scheduler)
            // 初始化 TaskScheduler 和 SchedulerBackend
            cm.initialize(scheduler, backend)
            // 返回 SchedulerBackend 和 TaskScheduler
            (backend, scheduler)

	_schedulerBackend = sched
    _taskScheduler = ts
    // 创建 DAGScheduler  stage的划分, 任务的划分, 把任务封装成 TaskSet 交给TaskScheduler调度
    _dagScheduler = new DAGScheduler(this)
    _heartbeatReceiver.ask[Boolean](TaskSchedulerIsSet)
    // 启动 TaskSheduler
    _taskScheduler.start()
    // 内部执行如下代码--------------------------------------------
    	backend.start() // 实际调用的是 YarnClientSchedulerBackend.start()
            // 执行如下命令
            // 命令中的 client 类还是 cluster 模式下的 client 类
            bindToYarn(client.submitApplication(), None)
            // submitApplication() 还是 cluster 模式下的 client 类
            	createContainerLaunchContext(newAppResponse) // -> 核心代码
            	// 核心代码: 确定 ApplicationMaster 类
        		val amClass =
            	if (isClusterMode) { 
                    // 如果是 cluster 模式
Utils.classForName("org.apache.spark.deploy.yarn.ApplicationMaster").getName
            	} else { // 如果是 client 模式，执行该条指令
Utils.classForName("org.apache.spark.deploy.yarn.ExecutorLauncher").getName
            	}
// 最后生成 command 提交给 RM
```

### 2.2.2 ExecutorLauncher 类/ 进程

```scala
object ExecutorLauncher {
    // 其实就是一个披着 ExecutorLauncher 外皮的 ApplicationMaster
    def main(args: Array[String]): Unit = {
        ApplicationMaster.main(args)
    }
}
// 接下来的步骤与 cluster 模式一致，直到 run() 方法的以下代码
        // 关键核心代码
            if (isClusterMode) {  // cluster 模式
                runDriver(securityMgr)
            } else { // client 模式
                runExecutorLauncher(securityMgr)
            }
		// runExecutorLauncher 内部执行 registerAM，接下来与 cluster 模式完全一致
			registerAM(sparkConf, rpcEnv, driverRef, sparkConf.get("spark.driver.appUIAddress", ""),
            securityMgr)
```

### 2.2.3 总结

![image-20200521203337361](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200521203337361.png)

```scala
1.	执行脚本提交任务，实际是启动一个SparkSubmit的 JVM 进程；
2.	SparkSubmit伴生对象中的main方法反射调用用户代码的main方法；
3.	启动Driver，执行用户的作业，并创建ScheduleBackend；
4.	YarnClientSchedulerBackend向RM发送指令：bin/java ExecutorLauncher；
5.	Yarn框架收到指令后会在指定的NM中启动ExecutorLauncher（实际上还是调用ApplicationMaster的main方法）；
object ExecutorLauncher {

  def main(args: Array[String]): Unit = {
    ApplicationMaster.main(args)
  }

}
6.	AM向RM注册，申请资源；
7.	获取资源后AM向NM发送指令：bin/java CoarseGrainedExecutorBacken；
8.	后面和cluster模式一致
注意：
- SparkSubmit、ExecutorLauncher和CoarseGrainedExecutorBacken是独立的进程；
- driver不是一个子线程,而是直接运行在SparkSubmit进程的main线程中, 所以sparkSubmit进程不能退出.

```

# 3. 任务调度

## 3.1 Stage 层级的调度

```scala
// 以 collect() 算子为例
// Stage 类有两个子类 ———— ResultStage + ShuffleMapStage
def collect(): Array[T] = withScope {
        val results = sc.runJob(this, (iter: Iterator[T]) => iter.toArray)
        Array.concat(results: _*)
    }
// 最终会调用 dagScheduler.runJob()
dagScheduler.runJob(rdd, cleanedFunc, partitions, callSite, resultHandler, localProperties.get)
    // runJob() 中运行 submitJob
    val waiter: JobWaiter[U] = submitJob(rdd, func, partitions, callSite, resultHandler, properties)
		// 向内部事件循环器发送消息 JobSubmitted
// eventProcessLoop: DAGSchedulerEventProcessLoop = new DAGSchedulerEventProcessLoop(this)
		
        eventProcessLoop.post(JobSubmitted(
            jobId, rdd, func2, partitions.toArray, callSite, waiter,
            SerializationUtils.clone(properties)))
				// post 方法就是将上述 JobSubmitted 对象放入队列
				def post(event: E): Unit = {
   	 				eventQueue.put(event)
  				}
		// 父类中还有一个 eventQueue.take(event) 方法。对象加载到最后会启动一个线程死循环执行 eventQueue.take(event) 方法。
		// 拿到提交的 job 后会对其进行处理
		case JobSubmitted(...) =>
            dagScheduler.handleJobSubmitted(jobId, rdd, func, partitions, callSite, listener, properties)
		// 先划分 finalStage: ResultStage 并提交
		// createResultStage 过程中会获取 shuffleMapStage
		// 这个过程采用递归的方式进行深度优先遍历，获取 shuffleMapStage，直到获取到第一个 shuffleMapStage 之后才会返回 finalStage。
	finalStage = createResultStage(finalRDD, func, partitions, jobId, callSite)
	submitStage(finalStage)

        // submitStage(finalStage)-------------------------------------
		// 提交时还是会采用深度优先遍历的思想，先提交父 Stage，再提交当前 Stage
        // 1. 找到没有提交的父 Stage
            val missing: List[Stage] = getMissingParentStages(stage).sortBy(_.id)
		   if (missing.isEmpty) { // 2. 如果为空, 则从当前 stage 直接提交
               submitMissingTasks(stage, jobId.get)
            } else { // 3. 如果不为空, 则递归的向上查找
               for (parent <- missing) {
                  submitStage(parent)
               }
             // 4. 当前 stage 加入到等待提交的 stage 对列中
             waitingStages += stage
```

## 3.2 task 层级的调度

```scala
// Task 类有两个子类 ———— ResultTask + ShuffleMapTask
def submitMissingTasks(stage: Stage, jobId: Int){
    ...
    stage match {
            case s: ShuffleMapStage =>
                outputCommitCoordinator.stageStart(stage = s.id, maxPartitionId = s.numPartitions - 1)
            case s: ResultStage =>
                outputCommitCoordinator.stageStart(
                    stage = s.id, maxPartitionId = s.rdd.partitions.length - 1)
        }
    // 先根据 stage 划分 task
    val tasks: Seq[Task[_]] = try {
            stage match {
                case stage: ShuffleMapStage =>
                    partitionsToCompute.map { id =>
                        val locs = taskIdToLocations(id)
                        val part = stage.rdd.partitions(id)
                        // ShuffleMapTask
                        new ShuffleMapTask(stage.id, stage.latestInfo.attemptId,
                            taskBinary, part, locs, stage.latestInfo.taskMetrics, properties, Option(jobId),
                            Option(sc.applicationId), sc.applicationAttemptId)
                    }
                
                case stage: ResultStage =>
                    partitionsToCompute.map { id =>
                        val p: Int = stage.partitions(id)
                        val part = stage.rdd.partitions(p)
                        val locs = taskIdToLocations(id)
                        // ResultTask
                        new ResultTask(stage.id, stage.latestInfo.attemptId,
                            taskBinary, part, locs, id, properties, stage.latestInfo.taskMetrics,
                            Option(jobId), Option(sc.applicationId), sc.applicationAttemptId)
                    }
            }
        
     // 提交 task
     // 把 tasks 封装到 TaskSet 中, 然后交给 taskScheduler 来提交
        // 每个 TaskSet 最后交给一个 TaskSetManager 进行调度
	taskScheduler.submitTasks(new TaskSet(
                tasks.toArray, stage.id, stage.latestInfo.attemptId, jobId, properties))
        // submitTasks 中把任务交给任务调度池来调度 ------------------------
        // SchedulableBuilder 有两个子类 ———— FIFO + Fair
            schedulableBuilder.addTaskSetManager(manager, manager.taskSet.properties)
        // 通知 SchedulerBackend 给自己(Driver)发送信息:ReviveOffers
        backend.reviveOffers()
        // 自己(Driver)收到后相应---------------------------------------------
        case ReviveOffers => makeOffers()
        	// makeOffers() ----------------------------------------------
        	private def makeOffers() {
            // Filter out executors under killing
            // 过滤出 Active 的 Executor
            val activeExecutors: collection.Map[String, ExecutorData] = executorDataMap.filterKeys(executorIsAlive)
            // 封装资源
            val workOffers: immutable.IndexedSeq[WorkerOffer] = activeExecutors.map { case (id, executorData) =>
                new WorkerOffer(id, executorData.executorHost, executorData.freeCores)
            }.toIndexedSeq
            // 启动任务
            launchTasks(scheduler.resourceOffers(workOffers))
                // launchTasks() -------------------------------------------
                private def launchTasks(tasks: Seq[Seq[TaskDescription]]) {
            		for (task <- tasks.flatten) {
                	// 序列化任务
                	val serializedTask: ByteBuffer = ser.serialize(task)
                    // 发送任务到 Executor.CoarseGrainedExecutorBackend
                    executorData.executorEndpoint.send(LaunchTask(new SerializableBuffer(serializedTask)))
                // Executor 接收到任务后执行 ---------------------------------
            case LaunchTask(data) =>
            if (executor == null) {
                exitExecutor(1, "Received LaunchTask command but executor was null")
            } else {
                // 封装任务
                val taskDesc: TaskDescription = ser.deserialize[TaskDescription](data.value)
                logInfo("Got assigned task " + taskDesc.taskId)
                // 启动任务
                executor.launchTask(this, taskId = taskDesc.taskId, attemptNumber = taskDesc.attemptNumber,taskDesc.name, taskDesc.serializedTask)
                // launchTask() ----------------------------------------------
                def launchTask(
                      context: ExecutorBackend,
                      taskId: Long,
                      attemptNumber: Int,
                      taskName: String,
                      serializedTask: ByteBuffer): Unit = {
        		// Runnable 接口的对象.
       			val tr = new TaskRunner(context, taskId = taskId, attemptNumber = attemptNumber, taskName,serializedTask)
                 // runningTasks：正在运动的 task 集合
                 runningTasks.put(taskId, tr)
                 // 在线程池中执行 task，所以 task 的执行级别是线程级
                 threadPool.execute(tr)
                    // tr: TaskRunner 的 run() 方法：更新 task 状态、反序列化、执行
                    // 核心代码
                            val value = try {
                            // 开始运行 task，在 task.run() 中执行那些匿名函数
                            val res = task.run(
                                taskAttemptId = taskId,
                                attemptNumber = attemptNumber,
                                metricsSystem = env.metricsSystem)
                            threwException = false
                            res
                }
  			}
         }
     }
}

```









