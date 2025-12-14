package com.example.amadapp.Model;

/**
 * Data class for a single item in the "My Trees" RecyclerView.
 */
public class UserTree {
    private String lng;
    private String lat;
    private String treeID;
    private String image;
    private String date;

    public UserTree() {
    }

    public UserTree(String lng, String lat, String treeID, String image, String date) {
        this.lng = lng;
        this.lat = lat;
        this.treeID = treeID;
        this.image = image;
        this.date = date;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getTreeID() {
        return treeID;
    }

    public void setTreeID(String treeID) {
        this.treeID = treeID;
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
}