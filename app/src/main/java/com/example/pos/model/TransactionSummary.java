package com.example.pos.model;

public class TransactionSummary {
    private int id;
    private String date;
    private double total;
    private String cashierName;

    public TransactionSummary(int id, String date, double total, String cashierName) {
        this.id = id;
        this.date = date;
        this.total = total;
        this.cashierName = cashierName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }
} 