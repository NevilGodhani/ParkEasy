package com.example.parkeasy;

public class User {
    private String first,last,email,pass,key;

    public User(){

    }

    public User(String first,String last,String email,String pass, String key){
        this.first=first;
        this.last=last;
        this.email=email;
        this.pass=pass;
        this.key=key;
    }
    public String getKey(){
        return key;
    }
    public String getFirst(){
        return first;
    }
    public String getLast(){
        return last;
    }
    public String getEmail(){
        return email;
    }
    public String getPass(){
        return pass;
    }
    public void setEmail(String email){
        this.email=email;
    }
    public void setPass(String pass){
        this.pass=pass;
    }
    public void setFirst(String first){
        this.first=first;
    }
    public void setLast(String last){
        this.last=last;
    }
    public void setKey(String key){
        this.key=key;
    }
}
