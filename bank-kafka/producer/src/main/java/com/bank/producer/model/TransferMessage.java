package com.bank.producer.model;

public class TransferMessage {
    private long transferId;
    private long senderAccountId;
    private long recipientAccountId;
    private int amount;

    public TransferMessage() {
    }

    public TransferMessage(long transferId, long senderAccountId, long recipientAccountId, int amount) {
        this.transferId = transferId;
        this.senderAccountId = senderAccountId;
        this.recipientAccountId = recipientAccountId;
        this.amount = amount;
    }

    public long getTransferId() {
        return transferId;
    }

    public long getSenderAccountId() {
        return senderAccountId;
    }

    public long getRecipientAccountId() {
        return recipientAccountId;
    }

    public int getAmount() {
        return amount;
    }

    public void setTransferId(long transferId) {
        this.transferId = transferId;
    }

    public void setSenderAccountId(long senderAccountId) {
        this.senderAccountId = senderAccountId;
    }

    public void setRecipientAccountId(long recipientAccountId) {
        this.recipientAccountId = recipientAccountId;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "TransferMessage{transferId='" + transferId + "', sender=" + senderAccountId +
                ", recipient=" + recipientAccountId + ", amount=" + amount + '}';
    }
}
