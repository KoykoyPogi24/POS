package com.example.pos.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.pos.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private UserDatabaseHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new UserDatabaseHelper(context);
    }

    // Hash password using SHA-256
    private String hashPassword(String password) {
        return UserDatabaseHelper.hashPassword(password);
    }

    // Register user
    public boolean registerUser(String username, String password, String userType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password_hash", hashPassword(password));
        values.put("user_type", userType);

        long result = db.insert("users", null, values);
        db.close();
        return result != -1;
    }

    // Login user
    public User loginUser(String username, String password, String userType) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {"id", "username", "password_hash", "user_type"};
        String selection = "username=? AND password_hash=? AND user_type=?";
        String[] selectionArgs = {username, hashPassword(password), userType};

        Cursor cursor = db.query("users", columns, selection, selectionArgs, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(0));
            user.setUsername(cursor.getString(1));
            user.setPasswordHash(cursor.getString(2));
            user.setUserType(cursor.getString(3));
        }
        cursor.close();
        db.close();
        return user;
    }

    // Get all users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"id", "username", "password_hash", "user_type"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            User user = new User();
            user.setId(cursor.getInt(0));
            user.setUsername(cursor.getString(1));
            user.setPasswordHash(cursor.getString(2));
            user.setUserType(cursor.getString(3));
            users.add(user);
        }
        cursor.close();
        db.close();
        return users;
    }

    // Get all cashiers
    public List<User> getAllCashiers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"id", "username", "password_hash", "user_type"}, "user_type=?", new String[]{"cashier"}, null, null, null);
        while (cursor.moveToNext()) {
            User user = new User();
            user.setId(cursor.getInt(0));
            user.setUsername(cursor.getString(1));
            user.setPasswordHash(cursor.getString(2));
            user.setUserType(cursor.getString(3));
            users.add(user);
        }
        cursor.close();
        db.close();
        return users;
    }

    // Delete user by id
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("users", "id=?", new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    // Update user (username and userType)
    public boolean updateUser(int userId, String newUsername, String newUserType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", newUsername);
        values.put("user_type", newUserType);
        int rows = db.update("users", values, "id=?", new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }
} 