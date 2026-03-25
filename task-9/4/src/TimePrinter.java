import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimePrinter {
    public static class TimePrinterThread extends Thread {
        private final int intervalSeconds;
        private volatile boolean running = true;

        public TimePrinterThread(int intervalSeconds) {
            super("TimePrinter");
            this.intervalSeconds = intervalSeconds;
            setDaemon(true);
        }

        @Override
        public void run() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            while (running) {
                LocalTime currentTime = LocalTime.now();
                System.out.println("Текущее время: " +
                        currentTime.format(formatter));

                try {
                    Thread.sleep(intervalSeconds * 1000L);
                } catch (InterruptedException e) {
                    System.out.println("Поток времени был прерван.");
                    break;
                }
            }

            System.out.println("Поток времени завершил работу.");
        }

        public void stopPrinter() {
            running = false;
            this.interrupt();
        }
    }

    public static void demonstrateTimePrinter() {
        TimePrinterThread timePrinter = new TimePrinterThread(3);
        timePrinter.start();

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        timePrinter.stopPrinter();

        try {
            timePrinter.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println();
    }
}
