package com.example.pos.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.pos.model.Product;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {
    private UserDatabaseHelper dbHelper;

    public ProductDao(Context context) {
        dbHelper = new UserDatabaseHelper(context);
    }

    public long addProduct(Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("barcode", product.getBarcode());
        values.put("price", product.getPrice());
        values.put("stock",  product.getStock());
        values.put("category", product.getCategory());
        values.put("supplier", product.getSupplier());
        values.put("cost", product.getCost());
        values.put("image_uri", product.getImageUri());
        long id = db.insert("products", null, values);
        db.close();
        return id;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("products", null, null, null, null, null, "name ASC");
        while (cursor.moveToNext()) {
            Product p = new Product(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("barcode")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("price"))
            );
            p.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
            p.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
            p.setSupplier(cursor.getString(cursor.getColumnIndexOrThrow("supplier")));
            p.setCost(cursor.getDouble(cursor.getColumnIndexOrThrow("cost")));
            p.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow("image_uri")));
            products.add(p);
        }
        cursor.close();
        db.close();
        return products;
    }

    public Product getProductByBarcode(String barcode) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("products", null, "barcode=?", new String[]{barcode}, null, null, null);
        Product p = null;
        if (cursor.moveToFirst()) {
            p = new Product(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("barcode")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("price"))
            );
            p.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
        }
        cursor.close();
        db.close();
        return p;
    }

    public void updateStock(int productId, int newStock) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("stock", newStock);
        db.update("products", values, "id=?", new String[]{String.valueOf(productId)});
        db.close();
    }

    public void updateProduct(Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("barcode", product.getBarcode());
        values.put("price", product.getPrice());
        values.put("stock", product.getStock());
        values.put("category", product.getCategory());
        values.put("supplier", product.getSupplier());
        values.put("cost", product.getCost());
        values.put("image_uri", product.getImageUri());
        db.update("products", values, "id=?", new String[]{String.valueOf(product.getId())});
        db.close();
    }

    public void deleteProduct(int productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("products", "id=?", new String[]{String.valueOf(productId)});
        db.close();
    }
} 