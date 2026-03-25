package com.bank.consumer.services;

import com.bank.consumer.dao.AccountDao;
import com.bank.consumer.dao.TransferDao;
import com.bank.consumer.model.Account;
import com.bank.consumer.model.Transfer;
import com.bank.consumer.model.TransferMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class TransferConsumerService {

    private static final Logger log = LogManager.getLogger(TransferConsumerService.class);

    public static final String STATUS_DONE = "готово";
    public static final String STATUS_ERROR = "завершилось с ошибкой";

    private final KafkaConsumer<String, String> kafkaConsumer;
    private final AccountDao accountDao;
    private final TransferDao transferDao;
    private final PlatformTransactionManager transactionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Thread consumerThread;

    public TransferConsumerService(KafkaConsumer<String, String> kafkaConsumer,
                                   AccountDao accountDao,
                                   TransferDao transferDao,
                                   PlatformTransactionManager transactionManager) {
        this.kafkaConsumer = kafkaConsumer;
        this.accountDao = accountDao;
        this.transferDao = transferDao;
        this.transactionManager = transactionManager;
    }

    @PostConstruct
    public void start() {
        consumerThread = new Thread(this::pollLoop, "kafka-consumer-thread");
        consumerThread.setDaemon(true);
        consumerThread.start();
        log.info("Consumer service started, polling for messages...");
    }

    @PreDestroy
    public void stop() {
        log.info("Shutting down consumer service...");
        running.set(false);
        if (consumerThread != null) {
            consumerThread.interrupt();
            try {
                consumerThread.join(5000);
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for consumer thread to finish", e);
                Thread.currentThread().interrupt();
            }
        }
        kafkaConsumer.wakeup();
        log.info("Consumer service stopped.");
    }

    private void pollLoop() {
        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));

                if (!records.isEmpty()) {
                    log.info("Received {} records from Kafka", records.count());

                    for (ConsumerRecord<String, String> record : records) {
                        processRecord(record);
                    }

                    kafkaConsumer.commitSync();
                }
            }
        } catch (WakeupException e) {
            if (running.get()) throw e;
        } finally {
            kafkaConsumer.close();
            log.info("Kafka consumer closed.");
        }
    }

    private void processRecord(ConsumerRecord<String, String> record) {
        log.info("Start processing record: key={}, partition={}, offset={}", record.key(), record.partition(), record.offset());

        TransferMessage message;
        try {
            message = objectMapper.readValue(record.value(), TransferMessage.class);
        } catch (Exception e) {
            log.error("Failed to deserialize message: {}", record.value(), e);
            return;
        }

        log.info("Received transfer message: {}", message);

        Optional<Transfer> existing = transferDao.findById(message.getTransferId());
        if (existing.isPresent()) {
            log.info("Transfer with ID {} already processed, skipping", message.getTransferId());
            return;
        }

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
        TransactionStatus txStatus = transactionManager.getTransaction(txDef);

        try {
            Optional<Account> fromOpt = accountDao.findByIdForUpdate(message.getSenderAccountId());
            if (fromOpt.isEmpty()) {
                log.error("Validation failed: sender account {} not found", message.getSenderAccountId());
                transactionManager.rollback(txStatus);
                return;
            }

            Optional<Account> toOpt = accountDao.findByIdForUpdate(message.getRecipientAccountId());
            if (toOpt.isEmpty()) {
                log.error("Validation failed: recipient account {} not found", message.getRecipientAccountId());
                transactionManager.rollback(txStatus);
                return;
            }

            Account senderAccount = fromOpt.get();
            Account recipientAccount = toOpt.get();
            int amount = message.getAmount();

            if (senderAccount.getBalance() < amount) {
                log.error("Validation failed: insufficient funds in sender account {}. Available: {}, required: {}",
                        senderAccount.getId(), senderAccount.getBalance(), amount);
                transactionManager.rollback(txStatus);
                return;
            }

            senderAccount.setBalance(senderAccount.getBalance() - amount);
            recipientAccount.setBalance(recipientAccount.getBalance() + amount);
            accountDao.update(senderAccount);
            accountDao.update(recipientAccount);
            Transfer transfer = new Transfer(message.getTransferId(), senderAccount.getId(), recipientAccount.getId(), amount, STATUS_DONE);
            transferDao.save(transfer);
            transactionManager.commit(txStatus);
            log.info("Successfully processed transfer: {}", transfer);

        } catch (Exception e) {
            log.error("Error transfer transaction {}: {}", message.getTransferId(),  e.getMessage(), e);

            try {
                transactionManager.rollback(txStatus);
            } catch (Exception ex) {
                log.error("Failed to rollback transaction for transfer: {}", ex.getMessage(), ex);
            }

            saveErrorTransfer(message);
        }
    }

    private void saveErrorTransfer(TransferMessage message) {
        DefaultTransactionDefinition errorTxDef = new DefaultTransactionDefinition();
        TransactionStatus errorTxStatus = transactionManager.getTransaction(errorTxDef);
        try {
            Transfer errorTransfer = new Transfer(
                    message.getTransferId(),
                    message.getSenderAccountId(),
                    message.getRecipientAccountId(),
                    message.getAmount(),
                    STATUS_ERROR
            );
            transferDao.save(errorTransfer);
            transactionManager.commit(errorTxStatus);
            log.info("Saved error transfer record: {}", errorTransfer);
        } catch (Exception e) {
            try {
                transactionManager.rollback(errorTxStatus);
            } catch (Exception ex) {
                log.error("Failed to rollback error transfer transaction: {}", ex.getMessage(), ex);
            }

            log.error("Failed to save error transfer record for message {}: {}", message, e.getMessage(), e);
        }
    }
}
