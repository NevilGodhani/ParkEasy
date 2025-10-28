package com.example.parkeasy;

public class Ticket {
    private String date;
    private String vehicleType;
    private String duration;
    private String etime;
    private String key;
    private String near;
    private String parkingName;
    private String slot;
    private String stime;
    private String totalPrice;
    private String vehicleNo;

    // Default Constructor (Needed for Firebase)
    public Ticket() {
    }

    // Parameterized Constructor
    public Ticket(String date, String duration, String etime, String key, String near,
                          String parkingName, String slot, String stime, String totalPrice, String vehicleNo,String vehicleType) {
        this.date = date;
        this.duration = duration;
        this.etime = etime;
        this.key = key;
        this.near = near;
        this.parkingName = parkingName;
        this.slot = slot;
        this.stime = stime;
        this.totalPrice = totalPrice;
        this.vehicleNo = vehicleNo;
        this.vehicleType=vehicleType;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }
    public String getVehicleType() {
        return vehicleType;
    }
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getEtime() {
        return etime;
    }

    public void setEtime(String etime) {
        this.etime = etime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNear() {
        return near;
    }

    public void setNear(String near) {
        this.near = near;
    }

    public String getParkingName() {
        return parkingName;
    }

    public void setParkingName(String parkingName) {
        this.parkingName = parkingName;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getStime() {
        return stime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }
}

