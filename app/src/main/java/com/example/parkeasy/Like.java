package com.example.parkeasy;

public class Like {
    public String parkingName,near,stime,etime;
    public String imageurl;

    public Like() {
        // Default constructor required for calls to DataSnapshot.getValue(Upload.class)
    }
    public Like(String parkingName,String near, String imageurl,String stime,String etime) {
        this.parkingName = parkingName;
        this.near=near;
        this.imageurl = imageurl;
        this.etime=etime;
        this.stime=stime;
    }
    public String getStime() {
        return stime;
    }
    public String getEtime() {
        return etime;
    }
    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
    public void setStime(String stime) {
        this.stime = stime;
    }
    public void setEtime(String etime) {
        this.etime = etime;
    }


    public String getParkingName() {
        return parkingName;
    }

    public void setParkingName(String parkingName) {
        this.parkingName = parkingName;
    }

    public String getNear() {
        return near;
    }

    public void setNear(String near) {
        this.near = near;
    }
}
