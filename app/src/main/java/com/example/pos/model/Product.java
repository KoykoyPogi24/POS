package com.example.pos.model;

public class Product {
    private int id;
    private String name;
    private String barcode;
    private double price;
    private int stock;
    private String category;
    private String supplier;
    private double cost;
    private String imageUri;

    public Product(int id, String name, String barcode, double price) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.price = price;
        this.stock = 0;
        this.category = "";
        this.supplier = "";
        this.cost = 0.0;
        this.imageUri = null;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getBarcode() { return barcode; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getCategory() { return category; }
    public String getSupplier() { return supplier; }
    public double getCost() { return cost; }
    public String getImageUri() { return imageUri; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setCategory(String category) { this.category = category; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public void setCost(double cost) { this.cost = cost; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
} 