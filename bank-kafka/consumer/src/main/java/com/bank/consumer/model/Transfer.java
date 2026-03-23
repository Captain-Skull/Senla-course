package com.bank.consumer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "sender_account_id", nullable = false)
    private long senderAccountId;

    @Column(name = "recipient_account_id", nullable = false)
    private long recipientAccountId;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "status", nullable = false)
    private String status;

    public Transfer() {
    }

    public Transfer(long id, long senderAccountId, long recipientAccountId, int amount, String status) {
        this.id = id;
        this.senderAccountId = senderAccountId;
        this.recipientAccountId = recipientAccountId;
        this.amount = amount;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSenderAccountId() {
        return senderAccountId;
    }

    public void setSenderAccountId(long senderAccountId) {
        this.senderAccountId = senderAccountId;
    }

    public long getRecipientAccountId() {
        return recipientAccountId;
    }

    public void setRecipientAccountId(long recipientAccountId) {
        this.recipientAccountId = recipientAccountId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Transfer{id='" + id + "', sender=" + senderAccountId +
                ", recipient=" + recipientAccountId + ", amount=" + amount +
                ", status='" + status + "'}";
    }
}