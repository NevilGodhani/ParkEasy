package com.example.parkeasy;

public class Transaction {
    private String amount;
    private String type;
    private String timestamp;
    private String paymentID;

    // Required empty constructor for Firebase
    public Transaction() {}

    public Transaction(String amount, String type, String timestamp, String paymentID) {
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.paymentID = paymentID;
    }

    public String getAmount() { return amount; }
    public String getType() { return type; }
    public String  getTimestamp() { return timestamp; }
    public String getPaymentID() { return paymentID; }
}

