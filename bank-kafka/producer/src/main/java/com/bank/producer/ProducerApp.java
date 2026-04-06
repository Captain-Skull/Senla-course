package com.bank.producer;

import com.bank.producer.config.AppConfig;
import com.bank.producer.services.TransferProducerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.CountDownLatch;

public class ProducerApp {

    private static final Logger log = LogManager.getLogger(ProducerApp.class);

    public static void main(String[] args) {
        log.info("Запуск приложения Producer...");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        context.registerShutdownHook();

        TransferProducerService producerService = context.getBean(TransferProducerService.class);
        producerService.init();
        log.info("Приложение Producer запущено успешно.");

        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown));

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("Приложение Producer было прервано", e);
            Thread.currentThread().interrupt();
        }
    }
}
