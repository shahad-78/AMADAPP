package com.example.amadapp.Model;

public class Tree {
    private String name;
    private String scientificName;
    private String description;
    private String region;
    private String imageUrl;

    // Empty constructor required for Firebase
    public Tree() {
    }

    public Tree(String name, String scientificName, String description, String region, String imageUrl) {
        this.name = name;
        this.scientificName = scientificName;
        this.description = description;
        this.region = region;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}