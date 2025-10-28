package com.example.parkeasy;

public class Check {
    private String email, pass, key;

    // Default constructor required for Firebase
    public Check() {
    }

    // Parameterized constructor
    public Check(String email, String pass, String key) {
        this.email = email;
        this.pass = pass;
        this.key = key;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getPass() {
        return pass;
    }

    public String getKey() {
        return key;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
