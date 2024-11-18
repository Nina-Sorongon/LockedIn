package com.example.firstapp;

public class TaskGroup {
    private String title;
    private String imageUrl;
    private int completedTasks;
    private int totalTasks;

    public TaskGroup(String title, String imageUrl, int completedTasks, int totalTasks) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.completedTasks = completedTasks;
        this.totalTasks = totalTasks;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public int getTotalTasks() {
        return totalTasks;
    }
}
