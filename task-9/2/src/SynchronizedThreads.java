public class SynchronizedThreads {
    private static final Object lock = new Object();
    private static boolean firstThreadTurn = true;

    public static void demonstrateSynchornizedThreads() {
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 6; i++) {
                synchronized (lock) {
                    while (!firstThreadTurn) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println(Thread.currentThread().getName());
                    firstThreadTurn = false;
                    lock.notifyAll();
                }
            }
        }, "1 поток");

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 6; i++) {
                synchronized (lock) {
                    while (firstThreadTurn) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println(Thread.currentThread().getName());
                    firstThreadTurn = true;
                    lock.notifyAll();
                }
            }
        }, "2 поток");

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println();
    }
}
