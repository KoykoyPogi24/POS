package com.example.pos.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.pos.model.BusinessSetting;
import java.util.ArrayList;
import java.util.List;

public class BusinessSettingDao {
    private UserDatabaseHelper dbHelper;

    public BusinessSettingDao(Context context) {
        dbHelper = new UserDatabaseHelper(context);
    }

    public void setSetting(String key, String value) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        db.insertWithOnConflict("business_settings", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public String getSetting(String key) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("business_settings", new String[]{"value"}, "key=?", new String[]{key}, null, null, null);
        String value = null;
        if (cursor.moveToFirst()) {
            value = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return value;
    }

    public List<BusinessSetting> getAllSettings() {
        List<BusinessSetting> settings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("business_settings", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            settings.add(new BusinessSetting(
                cursor.getString(cursor.getColumnIndexOrThrow("key")),
                cursor.getString(cursor.getColumnIndexOrThrow("value"))
            ));
        }
        cursor.close();
        db.close();
        return settings;
    }
} 