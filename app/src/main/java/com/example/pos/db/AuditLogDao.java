package com.example.pos.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.pos.model.AuditLog;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDao {
    private UserDatabaseHelper dbHelper;

    public AuditLogDao(Context context) {
        dbHelper = new UserDatabaseHelper(context);
    }

    public long addAuditLog(AuditLog log) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", log.getUserId());
        values.put("action", log.getAction());
        values.put("timestamp", log.getTimestamp());
        values.put("details", log.getDetails());
        long id = db.insert("audit_logs", null, values);
        db.close();
        return id;
    }

    public List<AuditLog> getLogsForUser(int userId) {
        List<AuditLog> logs = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("audit_logs", null, "user_id=?", new String[]{String.valueOf(userId)}, null, null, "timestamp DESC");
        while (cursor.moveToNext()) {
            logs.add(new AuditLog(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("action")),
                cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                cursor.getString(cursor.getColumnIndexOrThrow("details"))
            ));
        }
        cursor.close();
        db.close();
        return logs;
    }
} 