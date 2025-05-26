package com.example.pos.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.pos.model.Product;
import com.example.pos.model.CartItem;
import java.util.ArrayList;
import java.util.List;
import com.example.pos.model.TransactionSummary;

public class TransactionDao {
    private UserDatabaseHelper dbHelper;

    public TransactionDao(Context context) {
        dbHelper = new UserDatabaseHelper(context);
    }

    public long saveTransaction(String datetime, double total, double tax, double discount, double grandTotal, String paymentType, int cashierId, List<CartItem> cartItems) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("total", total);
        values.put("tax", tax);
        values.put("discount", discount);
        values.put("grand_total", grandTotal);
        values.put("payment_type", paymentType);
        values.put("cashier_id", cashierId);
        long transactionId = db.insert("transactions", null, values);
        for (CartItem item : cartItems) {
            ContentValues itemValues = new ContentValues();
            itemValues.put("transaction_id", transactionId);
            itemValues.put("product_id", item.getProduct().getId());
            itemValues.put("quantity", item.getQuantity());
            itemValues.put("price", item.getProduct().getPrice());
            itemValues.put("subtotal", item.getSubtotal());
            db.insert("transaction_items", null, itemValues);
        }
        db.close();
        return transactionId;
    }

    public List<TransactionSummary> getAllTransactionsSummary() {
        List<TransactionSummary> summaries = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT t.id, t.datetime, t.grand_total, t.payment_type, u.username FROM transactions t LEFT JOIN users u ON t.cashier_id = u.id ORDER BY t.datetime DESC", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String date = cursor.getString(1);
            double total = cursor.getDouble(2);
            String cashierName = cursor.getString(4);
            summaries.add(new TransactionSummary(id, date, total, cashierName));
        }
        cursor.close();
        db.close();
        return summaries;
    }

    /**
     * Get all transactions for a specific date (YYYY-MM-DD)
     */
    public List<TransactionSummary> getTransactionsSummaryByDate(String date) {
        List<TransactionSummary> summaries = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT t.id, t.datetime, t.grand_total, t.payment_type, u.username " +
            "FROM transactions t LEFT JOIN users u ON t.cashier_id = u.id " +
            "WHERE date(t.datetime) = ? ORDER BY t.datetime DESC",
            new String[]{date}
        );
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String datetime = cursor.getString(1);
            double total = cursor.getDouble(2);
            String cashierName = cursor.getString(4);
            summaries.add(new TransactionSummary(id, datetime, total, cashierName));
        }
        cursor.close();
        db.close();
        return summaries;
    }

    /**
     * Get all transactions for a specific date and cashier
     */
    public List<TransactionSummary> getTransactionsSummaryByDateAndCashier(String date, int cashierId) {
        List<TransactionSummary> summaries = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT t.id, t.datetime, t.grand_total, t.payment_type, u.username " +
            "FROM transactions t LEFT JOIN users u ON t.cashier_id = u.id " +
            "WHERE date(t.datetime) = ? AND t.cashier_id = ? ORDER BY t.datetime DESC",
            new String[]{date, String.valueOf(cashierId)}
        );
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String datetime = cursor.getString(1);
            double total = cursor.getDouble(2);
            String cashierName = cursor.getString(4);
            summaries.add(new TransactionSummary(id, datetime, total, cashierName));
        }
        cursor.close();
        db.close();
        return summaries;
    }

    /**
     * Get total sales for a specific date
     */
    public double getTotalSalesByDate(String date) {
        double total = 0.0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT SUM(grand_total) FROM transactions WHERE date(datetime) = ?",
            new String[]{date}
        );
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    /**
     * Get total sales for a specific date and cashier
     */
    public double getTotalSalesByDateAndCashier(String date, int cashierId) {
        double total = 0.0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT SUM(grand_total) FROM transactions WHERE date(datetime) = ? AND cashier_id = ?",
            new String[]{date, String.valueOf(cashierId)}
        );
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    /**
     * Get best-selling product for a specific date
     */
    public String getBestSellerByDate(String date) {
        String bestSeller = "-";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT p.name, SUM(ti.quantity) as total_qty " +
            "FROM transaction_items ti " +
            "JOIN transactions t ON ti.transaction_id = t.id " +
            "JOIN products p ON ti.product_id = p.id " +
            "WHERE date(t.datetime) = ? " +
            "GROUP BY p.name ORDER BY total_qty DESC LIMIT 1",
            new String[]{date}
        );
        if (cursor.moveToFirst()) {
            bestSeller = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return bestSeller;
    }

    /**
     * Get best-selling product for a specific date and cashier
     */
    public String getBestSellerByDateAndCashier(String date, int cashierId) {
        String bestSeller = "-";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT p.name, SUM(ti.quantity) as total_qty " +
            "FROM transaction_items ti " +
            "JOIN transactions t ON ti.transaction_id = t.id " +
            "JOIN products p ON ti.product_id = p.id " +
            "WHERE date(t.datetime) = ? AND t.cashier_id = ? " +
            "GROUP BY p.name ORDER BY total_qty DESC LIMIT 1",
            new String[]{date, String.valueOf(cashierId)}
        );
        if (cursor.moveToFirst()) {
            bestSeller = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return bestSeller;
    }

    /**
     * Get hourly sales totals for a specific date (returns double[24] for each hour)
     */
    public double[] getHourlySalesByDate(String date) {
        double[] hourlySales = new double[24];
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT strftime('%H', datetime) as hour, SUM(grand_total) " +
            "FROM transactions WHERE date(datetime) = ? GROUP BY hour",
            new String[]{date}
        );
        while (cursor.moveToNext()) {
            int hour = Integer.parseInt(cursor.getString(0));
            double total = cursor.getDouble(1);
            hourlySales[hour] = total;
        }
        cursor.close();
        db.close();
        return hourlySales;
    }

    /**
     * Get hourly sales totals for a specific date and cashier (returns double[24] for each hour)
     */
    public double[] getHourlySalesByDateAndCashier(String date, int cashierId) {
        double[] hourlySales = new double[24];
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT strftime('%H', datetime) as hour, SUM(grand_total) " +
            "FROM transactions WHERE date(datetime) = ? AND cashier_id = ? GROUP BY hour",
            new String[]{date, String.valueOf(cashierId)}
        );
        while (cursor.moveToNext()) {
            int hour = Integer.parseInt(cursor.getString(0));
            double total = cursor.getDouble(1);
            hourlySales[hour] = total;
        }
        cursor.close();
        db.close();
        return hourlySales;
    }
} 