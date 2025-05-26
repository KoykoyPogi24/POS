package com.example.pos.model;

public class StockLog {
    private int id;
    private int productId;
    private int quantity;
    private int userId;
    private String timestamp;
    private String reason;
    private String type; // "in" or "out"

    public StockLog(int id, int productId, int quantity, int userId, String timestamp, String reason, String type) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.userId = userId;
        this.timestamp = timestamp;
        this.reason = reason;
        this.type = type;
    }

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public int getUserId() { return userId; }
    public String getTimestamp() { return timestamp; }
    public String getReason() { return reason; }
    public String getType() { return type; }

    public void setId(int id) { this.id = id; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setReason(String reason) { this.reason = reason; }
    public void setType(String type) { this.type = type; }
} 