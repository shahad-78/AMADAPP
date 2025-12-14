package com.example.amadapp.Model;

public class DegradedArea {

    String userID;
    String image;
    String date;
    String description;
    String address;
    String lat;
    String lng;
    String Status;

    public DegradedArea() {
    }

    public DegradedArea(String userID, String image, String date, String description, String address, String lat, String lng, String status) {
        this.userID = userID;
        this.image = image;
        this.date = date;
        this.description = description;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        Status = status;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
