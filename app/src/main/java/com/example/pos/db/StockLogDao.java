package com.example.pos.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.pos.model.StockLog;
import java.util.ArrayList;
import java.util.List;

public class StockLogDao {
    private UserDatabaseHelper dbHelper;

    public StockLogDao(Context context) {
        dbHelper = new UserDatabaseHelper(context);
    }

    public long addStockLog(StockLog log) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("product_id", log.getProductId());
        values.put("quantity", log.getQuantity());
        values.put("user_id", log.getUserId());
        values.put("timestamp", log.getTimestamp());
        values.put("reason", log.getReason());
        values.put("type", log.getType());
        long id = db.insert("stock_logs", null, values);
        db.close();
        return id;
    }

    public List<StockLog> getLogsForProduct(int productId) {
        List<StockLog> logs = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("stock_logs", null, "product_id=?", new String[]{String.valueOf(productId)}, null, null, "timestamp DESC");
        while (cursor.moveToNext()) {
            logs.add(new StockLog(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("product_id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                cursor.getString(cursor.getColumnIndexOrThrow("reason")),
                cursor.getString(cursor.getColumnIndexOrThrow("type"))
            ));
        }
        cursor.close();
        db.close();
        return logs;
    }
} 