package com.bank.consumer;

import com.bank.consumer.config.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.CountDownLatch;

public class ConsumerApp {

    private static final Logger log = LogManager.getLogger(ConsumerApp.class);

    public static void main(String[] args) {
        log.info("Starting Consumer application...");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        context.registerShutdownHook();
        log.info("Consumer application started successfully.");

        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown));

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Consumer application was interrupted", e);
        }
    }
}
