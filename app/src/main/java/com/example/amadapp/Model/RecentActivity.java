package com.example.amadapp.Model;

public class RecentActivity implements Comparable<RecentActivity> {
    private String title;
    private String date;
    private String type; // "tree" or "report"

    public RecentActivity(String title, String date, String type) {
        this.title = title;
        this.date = date;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    // Helper to sort by date (simple string comparison for yyyy-MM-dd works)
    @Override
    public int compareTo(RecentActivity o) {
        return o.getDate().compareTo(this.getDate()); // Descending order
    }
}