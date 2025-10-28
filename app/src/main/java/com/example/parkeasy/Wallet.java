package com.example.parkeasy;
public class Wallet {
    private String userUid;
    private double balance;

    // Empty constructor required for Firebase
    public Wallet() {
    }

    public Wallet(String userUid, double balance) {
        this.userUid = userUid;
        this.balance = balance;
    }

    // Getters and Setters
    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}