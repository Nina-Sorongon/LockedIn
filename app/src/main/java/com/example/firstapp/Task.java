package com.example.firstapp;

public class Task {
    private String title;
    private long deadline;
    private String status;

    public Task() {
        // Default constructor for Firebase
    }

    public Task(String title, long deadline, String status) {
        this.title = title;
        this.deadline = deadline;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public long getDeadline() {
        return deadline;
    }

    public String getStatus() {
        return status;
    }
}
