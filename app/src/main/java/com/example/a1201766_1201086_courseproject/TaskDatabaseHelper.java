package com.example.a1201766_1201086_courseproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TaskDatabase.db";
    private static final String TABLE_NAME = "tasks";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_DUE_DATE = "due_date";
    private static final String COL_PRIORITY = "priority";
    private static final String COL_STATUS = "status";
    private static final String COL_REMINDER = "reminder";
    private static final String COL_USER_EMAIL = "user_email";
    private static final String COL_CUSTOM_NOTIFICATION_TIME = "custom_notification_time";
    private static final String COL_SNOOZE_DURATION = "snooze_duration";
    private static final String COL_DEFAULT_REMINDER_ENABLED = "default_reminder_enabled";

    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_DUE_DATE + " TEXT, " +
                COL_PRIORITY + " TEXT, " +
                COL_STATUS + " TEXT, " +
                COL_REMINDER + " TEXT, " +
                COL_CUSTOM_NOTIFICATION_TIME + " TEXT, " +
                COL_SNOOZE_DURATION + " INTEGER, " +
                COL_DEFAULT_REMINDER_ENABLED + " INTEGER DEFAULT 0, " +
                COL_USER_EMAIL + " TEXT, " +
                "UNIQUE(" + COL_TITLE + ", " + COL_DUE_DATE + "))";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertTask(String userEmail, String title, String description, String dueDate, String priority, String status, String reminder, String customNotificationTime, int snoozeDuration, boolean defaultReminderEnabled) {
        if (!isValidDueDate(dueDate)) {
            System.out.println("Invalid due_date format: " + dueDate);
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_DUE_DATE, dueDate);
        values.put(COL_PRIORITY, priority);
        values.put(COL_STATUS, status);
        values.put(COL_REMINDER, reminder);
        values.put(COL_CUSTOM_NOTIFICATION_TIME, customNotificationTime);
        values.put(COL_SNOOZE_DURATION, snoozeDuration);
        values.put(COL_DEFAULT_REMINDER_ENABLED, defaultReminderEnabled ? 1 : 0);
        values.put(COL_USER_EMAIL, userEmail);

        try {
            long result = db.insertOrThrow(TABLE_NAME, null, values);
            return result != -1;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidDueDate(String dueDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(dueDate);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Cursor getAllTasks(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE user_email = ? ORDER BY due_date", new String[]{userEmail});
    }

    public boolean updateTask(int id, String title, String description, String dueDate, String priority, String status, String reminder, String customNotificationTime, int snoozeDuration, boolean defaultReminderEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (title != null) values.put(COL_TITLE, title);
        if (description != null) values.put(COL_DESCRIPTION, description);
        if (dueDate != null) values.put(COL_DUE_DATE, dueDate);
        if (priority != null) values.put(COL_PRIORITY, priority);
        if (status != null) values.put(COL_STATUS, status);
        if (reminder != null) values.put(COL_REMINDER, reminder);
        if (customNotificationTime != null) {
            values.put(COL_CUSTOM_NOTIFICATION_TIME, customNotificationTime);
        } else {
            values.putNull(COL_CUSTOM_NOTIFICATION_TIME);
        }
        values.put(COL_SNOOZE_DURATION, snoozeDuration);
        values.put(COL_DEFAULT_REMINDER_ENABLED, defaultReminderEnabled ? 1 : 0);

        int result = db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }


    public boolean updateTaskStatus(int id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        Log.d("TaskDatabaseHelper", "Attempting to update task ID: " + id + " with status: " + status);

        int result = db.update("tasks", values, "id = ?", new String[]{String.valueOf(id)});

        if (result > 0) {
            Log.d("TaskDatabaseHelper", "Successfully updated task ID: " + id + " to status: " + status);
        } else {
            Log.e("TaskDatabaseHelper", "Failed to update task ID: " + id + " to status: " + status);
        }

        return result > 0;
    }


    public boolean deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }


    public boolean deleteTaskByTitleAndDate(String title, String dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, COL_TITLE + "=? AND " + COL_DUE_DATE + "=?", new String[]{title, dueDate});
        return result > 0;
    }


    public Cursor getTaskByTitleAndDate(String title, String dueDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " +
                COL_TITLE + " = ? AND " + COL_DUE_DATE + " = ?", new String[]{title, dueDate});
    }

    public Cursor getCompletedTasksGroupedByDate() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM tasks WHERE status = 'completed' ORDER BY due_date ASC", null);
    }

    public Cursor getTasksForToday(String userEmail, String todayDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM tasks WHERE user_email = ? AND due_date LIKE ?",
                new String[]{userEmail, todayDate + "%"});
    }
    public boolean updateTaskDefaultReminderState(String title, String dueDateTime, boolean isDefaultReminderEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DEFAULT_REMINDER_ENABLED, isDefaultReminderEnabled ? 1 : 0);

        int result = db.update(TABLE_NAME, values, "title = ? AND due_date = ?", new String[]{title, dueDateTime});
        return result > 0;
    }



}

