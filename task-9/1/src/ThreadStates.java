public class ThreadStates {

    public static void demonstrateThreadStates() {
        final Object lock = new Object();

        Thread demoThread = new Thread(() -> {

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        System.out.println("1. NEW: " + demoThread.getState());

        demoThread.start();
        System.out.println("2. RUNNABLE: " + demoThread.getState());

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("3. TIMED_WAITING: " + demoThread.getState());

        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("4. WAITING: " + demoThread.getState());

        Thread blockedDemo = createBlockedState();
        System.out.println("5. BLOCKED: " + blockedDemo.getState());

        synchronized (lock) {
            lock.notify();
        }

        try {
            demoThread.join();
            System.out.println("6. TERMINATED: " + demoThread.getState());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println();
    }

    private static Thread createBlockedState() {
        final Object lock = new Object();

        Thread thread1 = new Thread(() -> {
            synchronized (lock) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (lock) {
            }
        });

        thread1.start();
        thread2.start();

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return thread2;
    }
}