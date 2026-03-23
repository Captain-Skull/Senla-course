package com.bank.producer.services;

import com.bank.producer.dao.AccountDao;
import com.bank.producer.dao.TransferDao;
import com.bank.producer.model.Account;
import com.bank.producer.model.TransferMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TransferProducerService {
    private static final Logger log = LogManager.getLogger(TransferProducerService.class);
    private final AtomicLong transferIdCounter = new AtomicLong(1);

    private final AccountDao accountDao;
    private final TransferDao transferDao;
    private final KafkaProducer<String, String> kafkaProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kafka.topic}")
    private String topicName;

    private final Map<Long, Account> accountMap = new HashMap<>();
    private final List<Long> accountIds = new ArrayList<>();
    private final Random random = new Random();

    @Autowired
    public TransferProducerService(AccountDao accountDao, TransferDao transferDao, KafkaProducer<String, String> kafkaProducer) {
        this.accountDao = accountDao;
        this.transferDao = transferDao;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional
    public void init() {
        long count = accountDao.count();
        if (count == 0) {
            log.info("Таблица счетов пуста. Инициализация тестовыми данными...");
            for (int i = 1; i <= 1000; i++) {
                int balance = 1000 + random.nextInt(9000);
                Account account = new Account(i, balance);
                accountDao.save(account);
                accountMap.put(account.getId(), account);
                accountIds.add(account.getId());
            }
            log.info("Инициализация завершена. Создано 1000 счетов.");
        } else {
            log.info("Загрузка счетов из базы данных...");
            List<Account> accounts = accountDao.findAll();
            for (Account account : accounts) {
                accountMap.put(account.getId(), account);
                accountIds.add(account.getId());
            }
            log.info("Загрузка завершена. Найдено {} счетов.", accounts.size());
        }

        long maxTransferId = transferDao.findMaxTransferId();
        transferIdCounter.set(maxTransferId + 1);
    }

    @Scheduled(fixedDelayString = "${producer.send.delay:200}")
    public void generateAndSendTransfer() {
        if (accountIds.size() < 2) {
            log.warn("Недостаточно счетов для генерации перевода.");
            return;
        }

        int idx1 = random.nextInt(accountIds.size());
        int idx2;
        do {
            idx2 = random.nextInt(accountIds.size());
        } while (idx2 == idx1);

        long fromAccountId = accountIds.get(idx1);
        long toAccountId = accountIds.get(idx2);
        int amount = 1 + random.nextInt(1000);
        long transferId = transferIdCounter.getAndIncrement();

        TransferMessage transfer = new TransferMessage(transferId, fromAccountId, toAccountId, amount);

        try {
            String messageJson = objectMapper.writeValueAsString(transfer);
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, String.valueOf(transferId), messageJson);

            kafkaProducer.beginTransaction();
            Future<RecordMetadata> future = kafkaProducer.send(record);
            kafkaProducer.commitTransaction();

            RecordMetadata metadata = future.get();
            log.info("Sent transfer: {} to Kafka topic {} at partition {}, offset {}", transfer, topicName, metadata.partition(), metadata.offset());

        } catch (Exception e) {
            log.error("Failed to send message to Kafka: {}", e.getMessage(), e);
            try {
                kafkaProducer.abortTransaction();
            } catch (Exception ex) {
                log.error("Failed to abort Kafka transaction: {}", ex.getMessage(), ex);
            }
        }
    }
}
