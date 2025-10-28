package com.example.parkeasy;

public class Parking {
    private String parkingName,near,parkingAddress,slot2,slot4,stime,etime,contact,charging,air,price2,price4;
    public String imageurl;
    public Parking(){

    }
    public Parking(String parkingName,String near,String imageurl,String parkingAddress,String slot2,String slot4, String stime, String etime,String contact,String charging,String air,String price2,String price4){
        this.parkingName=parkingName;
        this.near=near;
        this.parkingAddress=parkingAddress;
        this.slot2=slot2;
        this.slot4=slot4;
        this.stime=stime;
        this.etime=etime;
        this.contact=contact;
        this.charging=charging;
        this.air=air;
        this.price2=price2;
        this.price4=price4;
        this.imageurl = imageurl;
    }
    public String getImageurl() {
        return imageurl;
    }
    public String getNear() {
        return near;
    }
    public void setNear(String near) {
        this.near = near;
    }
    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
    public String getEtime() {
        return etime;
    }

    public String getStime() {
        return stime;
    }

    public void setEtime(String etime) {
        this.etime = etime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public String getSlot2() {
        return slot2;
    }

    public String getSlot4() {
        return slot4;
    }

    public void setSlot2(String slot2) {
        this.slot2 = slot2;
    }

    public void setSlot4(String slot4) {
        this.slot4 = slot4;
    }

    public String getAir() {
        return air;
    }

    public String getCharging() {
        return charging;
    }

    public String getContact() {
        return contact;
    }

    public String getParkingAddress() {
        return parkingAddress;
    }

    public String getParkingName() {
        return parkingName;
    }

    public String getPrice2() {
        return price2;
    }

    public String getPrice4() {
        return price4;
    }

    public void setAir(String air) {
        this.air = air;
    }

    public void setCharging(String charging) {
        this.charging = charging;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setParkingAddress(String parkingAddress) {
        this.parkingAddress = parkingAddress;
    }

    public void setParkingName(String parkingName) {
        this.parkingName = parkingName;
    }

    public void setPrice2(String price2) {
        this.price2 = price2;
    }

    public void setPrice4(String price4) {
        this.price4 = price4;
    }

}
