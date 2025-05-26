package com.example.pos.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "bizc_pos.db";
    private static final int DATABASE_VERSION = 2;

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "username TEXT NOT NULL UNIQUE," +
            "password_hash TEXT NOT NULL," +
            "user_type TEXT NOT NULL" +
            ");"
        );
        db.execSQL(
            "CREATE TABLE products (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "barcode TEXT NOT NULL UNIQUE," +
            "price REAL NOT NULL," +
            "stock INTEGER NOT NULL," +
            "category TEXT," +
            "supplier TEXT," +
            "cost REAL," +
            "image_uri TEXT" +
            ");"
        );
        db.execSQL(
            "CREATE TABLE transactions (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "datetime TEXT NOT NULL," +
            "total REAL," +
            "tax REAL," +
            "discount REAL," +
            "grand_total REAL," +
            "payment_type TEXT," +
            "cashier_id INTEGER," +
            "FOREIGN KEY(cashier_id) REFERENCES users(id)" +
            ");"
        );
        db.execSQL(
            "CREATE TABLE transaction_items (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "transaction_id INTEGER," +
            "product_id INTEGER," +
            "quantity INTEGER," +
            "price REAL," +
            "subtotal REAL," +
            "FOREIGN KEY(transaction_id) REFERENCES transactions(id)," +
            "FOREIGN KEY(product_id) REFERENCES products(id)" +
            ");"
        );
        db.execSQL(
            "CREATE TABLE stock_logs (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "product_id INTEGER," +
            "quantity INTEGER," +
            "user_id INTEGER," +
            "timestamp TEXT," +
            "reason TEXT," +
            "type TEXT," +
            "FOREIGN KEY(product_id) REFERENCES products(id)," +
            "FOREIGN KEY(user_id) REFERENCES users(id)" +
            ");"
        );
        db.execSQL(
            "CREATE TABLE audit_logs (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER," +
            "action TEXT," +
            "timestamp TEXT," +
            "details TEXT," +
            "FOREIGN KEY(user_id) REFERENCES users(id)" +
            ");"
        );
        db.execSQL(
            "CREATE TABLE business_settings (" +
            "key TEXT PRIMARY KEY," +
            "value TEXT" +
            ");"
        );
        // Insert default admin and cashier
        db.execSQL("INSERT INTO users (username, password_hash, user_type) VALUES ('admin', '" + hashPassword("admin123") + "', 'admin');");
        db.execSQL("INSERT INTO users (username, password_hash, user_type) VALUES ('cashier', '" + hashPassword("cashier123") + "', 'cashier');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS transaction_items");
        db.execSQL("DROP TABLE IF EXISTS transactions");
        db.execSQL("DROP TABLE IF EXISTS products");
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS stock_logs");
        db.execSQL("DROP TABLE IF EXISTS audit_logs");
        db.execSQL("DROP TABLE IF EXISTS business_settings");
        onCreate(db);
    }

    // Static password hashing utility
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
} 