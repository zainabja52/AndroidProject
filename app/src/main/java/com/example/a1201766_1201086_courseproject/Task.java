package com.example.a1201766_1201086_courseproject;

public class Task {
    private int id;
    private String title;
    private String description;
    private String dueDate;
    private String priority;
    private String status;
    private String reminder;
    private String customNotificationTime;
    private int snoozeDuration;
    private boolean defaultReminderEnabled;

    // Default constructor
    public Task() {
        this.id = 0;
        this.title = "";
        this.description = "";
        this.dueDate = "";
        this.priority = "Medium";
        this.status = "pending";
        this.reminder = "";
        this.customNotificationTime = null;
        this.snoozeDuration = 0;
        this.defaultReminderEnabled = false;
    }

    // Parameterized constructor
    public Task(int id, String title, String description, String dueDate, String priority, String status, String reminder, String customNotificationTime, int snoozeDuration, boolean defaultReminderEnabled) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.reminder = reminder;
        this.customNotificationTime = customNotificationTime;
        this.snoozeDuration = snoozeDuration;
        this.defaultReminderEnabled = defaultReminderEnabled;
    }

    // Setters
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

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCustomNotificationTime(String customNotificationTime) {
        this.customNotificationTime = customNotificationTime;
    }

    public void setSnoozeDuration(int snoozeDuration) {
        this.snoozeDuration = snoozeDuration;
    }

    public void setDefaultReminderEnabled(boolean defaultReminderEnabled) {
        this.defaultReminderEnabled = defaultReminderEnabled;
    }


    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDueDate() {
        return dueDate;
    }


    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public String getReminder() {
        return reminder;
    }

    public String getCustomNotificationTime() {
        return customNotificationTime;
    }

    public int getSnoozeDuration() {
        return snoozeDuration;
    }

    public boolean getDefaultReminderEnabled() {
        return defaultReminderEnabled;
    }

    public String getDueTime() {
        if (dueDate != null && dueDate.contains(" ")) {
            String[] parts = dueDate.split(" ");
            return parts.length > 1 ? parts[1] : "00:00"; // Default to 00:00 if no time is specified
        }
        return "00:00";
    }

}
