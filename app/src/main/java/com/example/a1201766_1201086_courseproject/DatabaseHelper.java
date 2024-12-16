package com.example.a1201766_1201086_courseproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final String TABLE_NAME = "users";
    private static final String COL_EMAIL = "email";
    private static final String COL_FIRST_NAME = "first_name";
    private static final String COL_LAST_NAME = "last_name";
    private static final String COL_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_EMAIL + " TEXT PRIMARY KEY, " +
                COL_FIRST_NAME + " TEXT, " +
                COL_LAST_NAME + " TEXT, " +
                COL_PASSWORD + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertUser(String email, String firstName, String lastName, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, email);
        values.put(COL_FIRST_NAME, firstName);
        values.put(COL_LAST_NAME, lastName);
        values.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL_EMAIL + "=? AND " + COL_PASSWORD + "=?", new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    public boolean updateUserProfile(String oldEmail, String newEmail, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, newEmail);
        values.put(COL_PASSWORD, newPassword);

        int result = db.update(TABLE_NAME, values, COL_EMAIL + "=?", new String[]{oldEmail});
        return result > 0;
    }

    public Cursor getUserDetails(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_EMAIL + "=?", new String[]{email});
    }

}
