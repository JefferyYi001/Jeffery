---
typora-root-url: images
---

# Java SE 核心编程

##### 1. 实现两个整数的加减乘除以及取余算法

```java
		//定义变量接受两个整数
		int num1,num2;
		//提示用户输入第一个整数
		Scanner scan = new Scanner(System.in);
		System.out.println("请输入第一个整数：");

		while (true)
		{
			if(scan.hasNextInt()){
				num1 = scan.nextInt();
				break;
			}else{
				System.out.println("请输入第一个整数！整数！整数！\n重要的事情说三遍！！！");
				scan = new Scanner(System.in);
			}
		}
		//提示用户输入第二个整数
		System.out.println("请输入第二个整数：");
		scan = new Scanner(System.in);
		
		while (true){
			if(scan.hasNextInt()){
				num2 = scan.nextInt();
				break;
			}else{
				System.out.println("请输入第二个整数！整数！整数！\n重要的事情说三遍！！！");
				scan = new Scanner(System.in);
			}
		}
		
		//计算并输出结果
		System.out.println(num1 + "+" + num2 + "=" + (num1+num2));
		System.out.println(num1 + "-" + num2 + "=" + (num1-num2));
		System.out.println(num1 + "*" + num2 + "=" + (num1*num2));
		System.out.println(num1 + "/" + num2 + "=" + ((double)num1/num2));
```
##### 2. 不使用中间变量交换两个数的值

解法一：

```java
		int num1 = 20;
		int num2 = 30;
		
		num1 = num1 + num2;
		num2 = num1 - num2;
		num1 = num1 - num2;
		
		System.out.println("num1 = "+ num1);
		System.out.println("num2 = "+ num2);
```

解法二：
```java
		int num3 = 70;
		int num4 = 80;

		num3 = num3 ^ num4;
		num4 = num3 ^ num4;
		num3 = num3 ^ num4;

		System.out.println("num3 = "+ num3);
		System.out.println("num4 = "+ num4);
```
##### 3. 判断一个年份是否为闰年

```java
	int year;
	Scanner scan = new Scanner(System.in);
	System.out.println("请输入一个年份");

	while (true)
		{
			if(scan.hasNextInt()){
				year = scan.nextInt();
				break;
			}else{
				System.out.println("请输入一个整数！整数！整数！\n重要的事情说三遍！！！");
				scan = new Scanner(System.in);
			}
		}
	
	if ((year % 4 == 0)&&(year % 400 != 0) || (year % 400 == 0)){
		System.out.println(year + "是闰年");
	}else{
		System.out.println(year + "不是闰年");
	}
```
##### 4. 判断一个整数是否是水仙花数

```java
//所谓水仙花数是指一个3位数，其各个位上数字立方和等于其本身，例如： 153 = 1*1*1 + 3*3*3 + 5*5*5。
	int num;
	Scanner scan = new Scanner(System.in);
	System.out.println("请输入一个三位整数");

	while (true)
		{
			if(scan.hasNextInt()){
				num = scan.nextInt();
				if (num >100 || num <999){
					break;
				}else{
					System.out.println("请输入一个三位整数！三位整数！三位整数！\n重要的事情说三遍！！！");
					scan = new Scanner(System.in);
				}
				
			}else{
				System.out.println("请输入一个三位整数！三位整数！三位整数！\n重要的事情说三遍！！！");
				scan = new Scanner(System.in);
			}
		}
	//获取各个数位值
	int bai = num / 100;
	int shi = num % 100 / 10;
	int ge  = num %10;
	
	if (bai * bai * bai + shi * shi * shi + ge * ge * ge == num){
		System.out.println(num + "是水仙花数");
	}else{
		System.out.println(num + "不是水仙花数");
	}
```
##### 5. 根据输入的月份和年份，求出该月的天数

```java
	short year = 0;
	byte month = 0;
	byte day = 0;
	Scanner myScan = new Scanner(System.in);

	System.out.println("请输入年份 月份，例如1993 12");
	year = myScan.nextShort();
	month = myScan.nextByte();
	
	switch (month){
	case 1:
	case 3:
	case 5:
	case 7:
	case 8:
	case 10:
	case 12:
		day = 31;
		break;
	case 4:
	case 6:
	case 9:
	case 11:
		day = 30;
		break;
	case 2:
		if ((year % 4 == 0)&&(year % 400 != 0) || (year % 400 == 0)){
			day = 29;
		}else{
			day = 28;
		}
		break;
	default:
		System.out.println("请正确输入年份 月份，例如1993 12");
		return;
	}

	System.out.println(year + "年" + month + "月共有" + day +"天");
```
##### 6. 输入年月日，判断该日是当年的第几天

```java
short year = 0;
byte month = 0;
byte monthCount = 0;
byte day = 0;
short daySeq = 0;

Scanner myScan = new Scanner(System.in);

System.out.println("请输入日期，例如2019 12 03");
year  = myScan.nextShort();
month = myScan.nextByte();
monthCount = month;
day   = myScan.nextByte();

for (; monthCount-- > 1; ){
	switch (monthCount){
	case 1:
	case 3:
	case 5:
	case 7:
	case 8:
	case 10:
	case 12:
		daySeq += 31;
		break;
	case 4:
	case 6:
	case 9:
	case 11:
		daySeq += 30;
		break;
	case 2:
		if ((year % 4 == 0)&&(year % 400 != 0) || (year % 400 == 0)){
			daySeq += 29;
		}else{
			daySeq += 28;
		}
		break;
	default:
		System.out.println("请正确输入日期，例如2019 12 03");
		return;
	}
}
daySeq += day;

System.out.println(year + "年" + month + "月" + day + "日是当年的第" + daySeq + "天");
```
##### 7. 循环打印输入的月份的天数

```java
//【使用continue实现】
// 包含输入的月份是否错误的语句 
	short year = 0;
	byte month = 0;
	byte day = 0;
	Scanner myScan = new Scanner(System.in);
	
	while (true){
		System.out.print("请输入年：");
		year = myScan.nextShort();
		System.out.print("请输入月：");
		month = myScan.nextByte();
		
		switch (month){
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			day = 31;
			break;
		case 4:
		case 6:
		case 9:
		case 11:
			day = 30;
			break;
		case 2:
			if ((year % 4 == 0)&&(year % 400 != 0) || (year % 400 == 0)){
				day = 29;
			}else{
				day = 28;
			}
			break;
		default:
			System.out.println("月份不正确");
			continue;
		}

		System.out.println(year + "年" + month + "月共有" + day +"天");
	}
```

##### 8. 输出100以内的所有素数(只能被1和自己整除的数)，每行显示5个

```java
		//ifPrime 计是否为素数
		//count 计素数个数，用于换行
		boolean ifPrime = true;
		byte count = 0;
		//外层循环2~100的数i
		//内层循环2~i的数j，作为除数
		for (byte i = 2; i <= 100 ; i++){
			ifPrime = true;
			for (byte j = 2; j < i; j++){
				if (i % j == 0){
					ifPrime = false;
					break;
				}
			}
			if (ifPrime){
				System.out.print(i + "\t");
				count++;
				if (count % 5 == 0){
					System.out.println();
				}
			}
			
		}
```
##### 9. 三天打渔两天晒网问题

```java
	//定义初始日期1990.01.01 和 当前日期（用户输入）
	short startYear = 1990;
	byte startMonth = 1;
	byte startDay = 1;

	short endYear;
	byte endMonth;
	byte endDay;
	// 定义变量dayNum记录天数
	int dayNum = 0;

	Scanner myScan = new Scanner(System.in);

	System.out.println("请输入当前日期，例如：2019 12 04");
	
	endYear  = myScan.nextShort();
	endMonth = myScan.nextByte();
	endDay   = myScan.nextByte();

	for (int year = startYear; year < endYear; year++){
		if ((year % 4 == 0 && year % 400 != 0)|| (year % 400 == 0)){
			dayNum += 366;
		}else{
			dayNum += 365;
		}
	}

	switch (endMonth){
		case 12:
			dayNum += 30;
		case 11:
			dayNum += 31;
		case 10:
			dayNum += 30;
		case 9:
			dayNum += 31;
		case 8:
			dayNum += 31;
		case 7:
			dayNum += 30;
		case 6:
			dayNum += 31;
		case 5:
			dayNum += 30;
		case 4:
			dayNum += 31;
		case 3:
			if ((endYear % 4 == 0 && endYear % 400 != 0)|| endYear % 400 == 0){
				dayNum += 29;
			}else{
				dayNum += 28;
			}
		case 2:
			dayNum += 31;
		case 1:
			dayNum += endDay;
			break;
		default:
			System.out.println("请输入正确的月份！！");
			return;
	}
	
	System.out.println("距离" + startYear + "年" + startMonth + "月" + startDay + "日的第" + dayNum +"天");
	
	switch (dayNum % 5){
		case 0:
		case 1:
		case 2:
			System.out.println("今天打渔~");
			break;
		default:
			System.out.println("今天晒网~");
			break;
	}
```
##### 10. 最佳评委与最差评委

```java
//数组初始化
double scores[] = {9.3, 7.8, 8.8, 9.2, 9.9, 6.5, 8.4, 8.25};
//声明总分、平均分
double sum = 0, average = 0;
//声明数组存放评委偏差绝对值，以及最大值、最大值索引，最小值、最小值索引
double absBias[] = new double[8];
double maxBias = 0.0;
double minBias = 10.0;
byte maxBiasIndex = 0;
byte minBiasIndex = 0;
//求总分、平均分
for (byte i = 0; i < 8; i++){
	sum += scores[i];
}
average = sum / 8;
//求偏差绝对值，并取最大值、最小值及其索引
for (byte i = 0; i < 8; i++){
	absBias[i] = Math.abs(scores[i] - average);
	if (maxBias < absBias[i]){
		maxBias = absBias[i];
		maxBiasIndex = i;
	}
	if (minBias > absBias[i]){
		minBias = absBias[i];
		minBiasIndex = i;
	}
}
//打印结论
System.out.println("选手最后得分为：" + average +"分");
System.out.println("最佳评委的索引号为：" + minBiasIndex + "，Ta 的打分为：" + scores[minBiasIndex]);
System.out.println("最差评委的索引号为：" + maxBiasIndex + "，Ta 的打分为：" + scores[maxBiasIndex]);
```

##### 11. 冒泡排序

//定义原数组与中间变量

```java
	//定义原数组与中间变量
	int arr[] = {24, 69, 80, 57, 13};
	int temp = 0;
	boolean flag = false;
	//打印原数组
	for (int i = 0; i < arr.length; i++){
		System.out.print(arr[i] + "\t");
	}
	//冒泡排序核心程序
	for (int i = 0; i < arr.length - 1; i++){
		for (int j = 0; j < arr.length - i - 1; j++){
			if (arr[j] > arr[j + 1]){
				temp = arr[j];
				arr[j] = arr[j + 1];
				arr[j + 1] = temp;
			}
		}
	}
	//打印排序后的数组
	System.out.println();
	for (int i = 0; i < arr.length; i++){
		System.out.print(arr[i] + "\t");
	}
```

##### 12. 二分查找

int[] arr = {1,8, 10, 89, 1000, 1234};//有序[从小->大]
​		

```java
	//先定义几个变量
	int findVal = -1;
	int leftIndex = 0; //数组左边的下标
	int index = -1; //表示查找的数，在数组中的下标，初始化-1
	int rightIndex = arr.length - 1; //数组右边的下标
	int midIndex = 0; //记录中间数下标

	while(leftIndex <= rightIndex) {//分析	, 逻辑-》分析， 见多识广	

		midIndex = (leftIndex + rightIndex) / 2; //中间的数的下标				

		//多分支
		if(arr[midIndex] == findVal) {
			index = midIndex;	
			break;
		} else if( arr[midIndex] > findVal) {
			rightIndex = midIndex - 1;
		} else {
			leftIndex = midIndex + 1;
		}
	
	}

	//判断是否找到
	if(index != -1) {
		System.out.println("找到 index=" + index);
	} else {
		System.out.println("没有找到");
	}
```
##### 13. 有序表增加元素

```java
//1.  创建一个arr数组 {10， 12， 45， 90}
//2.  输入一个 insertNum = 23;
//3.  创建一个新的数组 arr2 大小 arr.length + 1
//4.  将 arr 数据拷贝到 arr2 来 arr = {10， 12， 45， 90, 0}
//5.  重点 的 insertNum 插入的下标得到 insertIndex
///    5.1 遍历 arr2 , 如果 arr2[i] > insertNum 成立，说明insertIndex = i;  
//6. 整体，后移 arr2 从 insertIndex 数据开始后移
//7. arr[insertIndex] = insertNum;
// arr = arr2; // 原来的 arr数组被销毁
//创建一个arr数组 {10， 12， 45， 90}
		int[] arr = {10, 12, 45, 90};
		int insertNum = 23;
		int insertIndex = -1; // 插入位置
		//创建一个新的数组 arr2 大小 arr.length + 1
		int[] arr2 = new int[arr.length + 1];
		//将 arr 数据拷贝到 arr2 来 arr2 = {10， 12， 45， 90, 0}
		for(int i = 0; i < arr.length ; i++) {
			arr2[i] = arr[i];
		}
		//重点 的 insertNum 插入的下标得到 insertIndex
		for( int i = 0; i < arr2.length - 1; i++) {
			if( arr2[i]  >  insertNum) {
			insertIndex = i;
			break;
			}
		}
		if(insertIndex == -1) {
      	//应该放到arr2的最后
      		insertIndex = arr2.length - 1;
		}
		//整体，后移 arr2 从 insertIndex 数据开始后移
		for(int i = arr2.length - 2; i >= insertIndex ; i--){
			arr2[i+1] = arr2[i];
		}
		//arr2[insertIndex] = insertNum;插入数组
		arr2[insertIndex] = insertNum;
		arr = arr2;
		//遍历最后arr,看看效果
		System.out.println("arr插入后的数据如下");
		for( int i = 0; i < arr.length; i++) {
			System.out.print(arr[i] + " ");
		}  
```

##### 14. 打印杨辉三角

```java
		/*
		【提示】
		 1. 第一行有 1 个元素, 第 n 行有 n 个元素
		 2. 每一行的第一个元素和最后一个元素都是 1
		 3. 从第三行开始, 对于非第一个元素和最后一个元素的元素的值. arr[i][j] 
		 arr[i][j]=arr[i-1][j-1]+arr[i-1][j]; //自己推

		*/

		int [][] yangHui = new int[10][];

		//根据杨辉三角特点，来创建各个一维数组大小
		for(int i = 0; i < yangHui.length; i++) {
			//先给每个一维数组分配空间, yangHui[i]  是一维数组
			yangHui[i] = new int[i+1];
			//赋值
			//每一行的第一个元素和最后一个元素都是 1
			for( int j = 0; j < yangHui[i].length ; j++) {
				if(j == 0 ||  j == yangHui[i].length -1) { //该行第一个元素，或者最后元素
					yangHui[i][j] = 1;
				} else {
					yangHui[i][j]=yangHui[i-1][j-1]+yangHui[i-1][j];
				}
			}
		}

		//遍历杨辉
		for(int i = 0; i < yangHui.length; i++) {
			for(int j = 0; j < yangHui[i].length; j++) {
				System.out.print(yangHui[i][j] + "\t");
			}
			//完成一行，输出一个换行
			System.out.println();
		}
```

##### 15. 数组反转

```java
//数组定义好
		int arr1[] = {1,2,3,4,5};
		int temp = 0;
		for( int i = 0; i < arr1.length / 2; i++){
		
				temp = arr1[i];
				arr1[i] = arr1[arr1.length - 1 - i]; 
				arr1[arr1.length - 1 - i] = temp;

		}

		//输出看看处理后的结果
		for(int i=0; i < arr1.length; i++) {
			
				System.out.print(arr1[i] + "\t");
		}

```

##### 16. equals()方法重写

```java
/*
 * 编写Order类，有int型的orderId，String型的OrderName，private属性。
 * 相应的getter()和setter()方法，两个参数的构造器，重写父类的equals()方法： public boolean equals(Object
 * obj)， 并判断测试类中创建的两个对象是否相等。相等就是判断属性是否相同.
 * 
 */

class Order {
	private int orderId;
	private String orderName;

	public Order(int orderId, String orderName) {
		super();
		this.orderId = orderId;
		this.orderName = orderName;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public String getOrderName() {
		return orderName;
	}

	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}

	// 判断属性是否
	@Override
	public boolean equals(Object obj) {
		// 判断
		if (this == obj) {
			return true;
		}

		// 验证类型
		if (!(obj instanceof Order)) {
			return false;
		}
		// 向下转型
		Order order = (Order) obj;

		return this.orderName.equals(order.getOrderName()) && 
          this.orderId == order.getOrderId();
	}

}

```

##### 17. IO流实现文件复制

```java
public void test() {
		FileInputStream fis =null;
		FileOutputStream fos = null;
		try {
			// 1. create File object
			File reader = new File("C:\\Users\\Jeffery\\Desktop\\aaaa.txt");
			File writer = new File("C:\\Users\\Jeffery\\Desktop\\bbbb.txt");
			// 2. create Stream object
			fis = new FileInputStream(reader);
			fos = new FileOutputStream(writer);
			// 3. writing while reading
			byte[] b = new byte[200];
			int len = 0;
			while ((len  = fis.read(b)) != -1) {
				fos.write(b, 0, len);
			}
			// 4. close Stream
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try{
				if (fis != null) {
					fis.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
```

##### 18. 线程安全的懒汉式

```java
class Computer {
	// 私有化构造体
	private Computer() {
	}
	// 声明成员属性为空
	private static Computer computer = null;
	// 对外开放 get（）方法
	public static Computer getInstance() {
		if (computer == null) {
			synchronized (Computer.class) {
				if (computer == null) {
					computer = new Computer();
				}
			}
		}
        // 返回成员属性 computer
		return computer;
	}
}

```

##### 19. Callable 接口用法

```java
public class CallableTest {

	public static void main(String[] args) throws Exception, Exception {

		// 4.创建Callable实现类的对象
		MyCallable mc = new MyCallable();
		// 5.创建FutureTask对象并将Callable实现类的对象作为实参传到FutureTask的构造器中
		FutureTask<String> ft = new FutureTask<>(mc);
		// 6.创建Thread的对象并将FutureTask对象作为实参传到Thread的构造器中
		Thread thread = new Thread(ft);
		// 7.调用start方法
		thread.start();

		for (int i = 0; i < 100; i++) {
			System.out.println("aaaaaaaaaaaaaaaaaaaaaaa====" + i);
		}
		// 获取分线程的返回值
		String str = ft.get(); // 调用get()方法后，当前线程阻塞，等待分线程执行结束，然后继续向下执行。
		System.out.println(str);

		for (int i = 0; i < 100; i++) {
			System.out.println(Thread.currentThread().getName() + "====" + i);
		}
	}
}

// 1.自定义类并实现Callable接口
class MyCallable implements Callable<String> {

	// 2.重写call方法
	@Override
	public String call() throws Exception {
		// 3.在call方法中实现需要在分线程中实现的功能
		for (int i = 0; i < 100; i++) {
			System.out.println(Thread.currentThread().getName() + "====" + i);
		}
		return "ccc";
	}
}
```

##### 20. 单例线程池

```java
public class ThreadPoolTest {
	// 类是单例的
	private ThreadPoolTest() {
	}

	private static ThreadPoolTest tpt = new ThreadPoolTest();

	// 属性 - 当前类只有一个对象，即然只有一个对象那么该属性只有一份
	private ExecutorService service = Executors.newCachedThreadPool();

	public static ThreadPoolTest getInstatnce() {
		return tpt;
	}

	/**
	 * 二次封装 - 更灵活
	 * 
	 * 如果需要更换方法的底层的实现，那么只需要修改当前方法中的实现即可，其它调用此方法的类都不需要修改
	 */
	public void execute(Runnable r) {
		service.execute(r);
	}
}
```

##### 21. 通过类加载器获取流读取文件内容

```java
	public void test2() throws Exception{
		//创建Properties对象
		Properties properties = new Properties();
		//创建流的对象 - 默认的路径是当前工程下
//		FileInputStream fis = new FileInputStream("user.properties");
		//默认路径是当前工程的bin目录下
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream("user.properties");
		//加载流
		properties.load(is);
		//获取内容
		String name = properties.getProperty("name");
		System.out.println(name);
		//关流
		is.close();
	}
```

