package com.example.a1201766_1201086_courseproject;

public class Task {
    private int id;
    private String title;
    private String description;
    private String dueDate;
    private String priority;
    private String status;
    private String reminder;

    // Constructor
    public Task(int id, String title, String description, String dueDate, String priority, String status, String reminder) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.reminder = reminder;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDueDate() { return dueDate; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getReminder() { return reminder; }
}
