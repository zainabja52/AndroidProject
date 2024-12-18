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

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public void setStatus(String completed) {
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
