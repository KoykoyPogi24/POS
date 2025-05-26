package com.example.pos.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String userType; // "admin", "cashier", or "inventory_manager"

    public User() {}

    public User(int id, String username, String passwordHash, String userType) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.userType = userType;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
} 