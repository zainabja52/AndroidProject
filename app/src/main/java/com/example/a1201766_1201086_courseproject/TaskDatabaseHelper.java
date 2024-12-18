package com.example.a1201766_1201086_courseproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    private static final String COL_USER_EMAIL = "user_email"; // Added column for user-specific tasks


    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
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
                COL_USER_EMAIL + " TEXT, " +
                "UNIQUE(" + COL_TITLE + ", " + COL_DUE_DATE + "))";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertTask(String userEmail,String title, String description, String dueDate, String priority, String status, String reminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_DUE_DATE, dueDate);
        values.put(COL_PRIORITY, priority);
        values.put(COL_STATUS, status);
        values.put(COL_REMINDER, reminder);
        values.put(COL_USER_EMAIL, userEmail);

        try {
            long result = db.insertOrThrow(TABLE_NAME, null, values);
            return result != -1;
        } catch (Exception e) {
            return false; // Task already exists
        }
    }

    public Cursor getAllTasks(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE user_email = ? ORDER BY due_date", new String[]{userEmail});
    }

    public Cursor getCompletedTasks(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_USER_EMAIL + " = ? AND " + COL_STATUS + "='completed' ORDER BY " + COL_DUE_DATE, new String[]{userEmail});
    }

    public Cursor searchTasks(String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_DUE_DATE + " BETWEEN ? AND ?", new String[]{startDate, endDate});
    }

    public boolean updateTask(int id, String title, String description, String dueDate, String priority, String status, String reminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_DUE_DATE, dueDate);
        values.put(COL_PRIORITY, priority);
        values.put(COL_STATUS, status);
        values.put(COL_REMINDER, reminder);
        int result = db.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteTaskById(int id) {
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

    public Cursor getAllTasksGroupedByDate() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id, title, description, due_date, priority, status, reminder FROM " + TABLE_NAME + " ORDER BY due_date ASC", null);
    }

    public Cursor getCompletedTasksGroupedByDate() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM tasks WHERE status = 'completed' ORDER BY due_date ASC", null);
    }


}

