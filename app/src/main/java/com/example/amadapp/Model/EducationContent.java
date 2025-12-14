package com.example.amadapp.Model;

public class EducationContent {

    String title;
    String content;
    String date;
    String url;
    String image;

    public EducationContent() {
    }

    public EducationContent(String title, String content, String date, String url, String image) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.url = url;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
