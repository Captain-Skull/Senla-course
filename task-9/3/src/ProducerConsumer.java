import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class ProducerConsumer {
    private static final int BUFFER_SIZE = 5;
    private static final Queue<Integer> buffer = new LinkedList<>();
    private static final Object lock = new Object();
    private static boolean productionFinished = false;

    public static void demonstrateProducerConsumer() {
        System.out.println("Размер буфера: " + BUFFER_SIZE);

        Thread producer = new Thread(() -> {
            Random random = new Random();

            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(random.nextInt(500));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (lock) {
                    while (buffer.size() == BUFFER_SIZE) {
                        try {
                            System.out.println("Производитель ждет... Буфер полон.");
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    int number = random.nextInt(100);
                    buffer.add(number);
                    System.out.println("Производитель создал: " + number +
                            " (в буфере: " + buffer.size() + "/" + BUFFER_SIZE + ")");

                    lock.notifyAll();
                }
            }

            synchronized (lock) {
                productionFinished = true;
                lock.notifyAll();
                System.out.println("Производитель завершил работу.");
            }
        });

        Thread consumer = new Thread(() -> {
            Random random = new Random();

            while (true) {
                try {
                    Thread.sleep(random.nextInt(800));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (lock) {
                    while (buffer.isEmpty() && !productionFinished) {
                        try {
                            System.out.println("Потребитель ждет... Буфер пуст.");
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (buffer.isEmpty() && productionFinished) {
                        System.out.println("Потребитель завершил работу.");
                        break;
                    }

                    if (!buffer.isEmpty()) {
                        int number = buffer.poll();
                        System.out.println("Потребитель забрал: " + number +
                                " (в буфере: " + buffer.size() + "/" + BUFFER_SIZE + ")");
                        lock.notifyAll();
                    }
                }
            }
        });

        producer.setName("Producer");
        consumer.setName("Consumer");

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println();
    }
}
