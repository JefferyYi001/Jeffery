## 1.Lock 标志位

使用 int 作为标志位，在 ReentrantLock 对象的多个 Condition 之间进行切换。

```java
public class Test {
    public static void main(String[] args) {
        ThreadTest threadTest = new ThreadTest();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                threadTest.print5();
            }
        },"A").start();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                threadTest.print10();
            }
        },"B").start();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                threadTest.print15();
            }
        },"C").start();
    }
}

public class ThreadTest {
    private int number = 1;
    private Lock lock = new ReentrantLock();
    private Condition condition1 = lock.newCondition();
    private Condition condition2 = lock.newCondition();
    private Condition condition3 = lock.newCondition();

    public void print5() {
        lock.lock();
        try {
            //1. 判断
            while (number != 1) {
                condition1.await();
            }
            //2. 干活
            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
            //3. 通知
            number = 2;
            condition2.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void print10() {
        lock.lock();
        try {
            //1. 判断
            while (number != 2) {
                condition2.await();
            }
            //2. 干活
            for (int i = 0; i < 10; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
            //3. 通知
            number = 3;
            condition3.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void print15() {
        lock.lock();
        try {
            //1. 判断
            while (number != 3) {
                condition3.await();
            }
            //2. 干活
            for (int i = 0; i < 15; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
            //3. 通知
            number = 1;
            condition1.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
```

