package com.example.pos.model;

public class AuditLog {
    private int id;
    private int userId;
    private String action;
    private String timestamp;
    private String details;

    public AuditLog(int id, int userId, String action, String timestamp, String details) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.timestamp = timestamp;
        this.details = details;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getAction() { return action; }
    public String getTimestamp() { return timestamp; }
    public String getDetails() { return details; }

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setAction(String action) { this.action = action; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setDetails(String details) { this.details = details; }
} 