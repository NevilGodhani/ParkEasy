package com.example.parkeasy;

public class HistoryItem {
    private String date, duration, etime, near, parkingName, slot, stime, totalPrice, vehicleNo, vehicleType;

    public HistoryItem() {
        // Required empty constructor for Firebase
    }

    public HistoryItem(String date, String duration, String etime, String near, String parkingName, String slot, String stime, String totalPrice, String vehicleNo, String vehicleType) {
        this.date = date;
        this.duration = duration;
        this.etime = etime;
        this.near = near;
        this.parkingName = parkingName;
        this.slot = slot;
        this.stime = stime;
        this.totalPrice = totalPrice;
        this.vehicleNo = vehicleNo;
        this.vehicleType = vehicleType;
    }

    public String getDate() { return date; }
    public String getDuration() { return duration; }
    public String getEtime() { return etime; }
    public String getNear() { return near; }
    public String getParkingName() { return parkingName; }
    public String getSlot() { return slot; }
    public String getStime() { return stime; }
    public String getTotalPrice() { return totalPrice; }
    public String getVehicleNo() { return vehicleNo; }
    public String getVehicleType() { return vehicleType; }
}

