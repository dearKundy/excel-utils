package com.kundy.excelutils.utils;

/**
 * @author kundy
 * @create 2019/2/18 4:33 PM
 */
public class ThreadLocalExample {

    public static class MyRunnable implements Runnable {

        private ThreadLocal threadLocal = new ThreadLocal();
        private int num;

        public MyRunnable(int num) {
            this.num = num;
        }

        @Override

        public void run() {
            threadLocal.set(num);
            System.out.println(threadLocal.get());
        }
    }


    public static void main(String[] args) {
        MyRunnable sharedRunnableInstance = new MyRunnable(1);
        Thread thread1 = new Thread(sharedRunnableInstance);
        Thread thread2 = new Thread(sharedRunnableInstance);
        thread1.start();
        thread2.start();
    }

}
