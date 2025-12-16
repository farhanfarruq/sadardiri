package com.example.sadardiri.model;

import com.google.firebase.firestore.DocumentId;

public class Transaction {
    @DocumentId
    private String id;
    private String userId;
    private double amount;
    private String type;
    private String note;
    private String date;
    private String category;

    public Transaction() {
    }

    public Transaction(String id, String userId, double amount, String type, String note, String date, String category) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.note = note;
        this.date = date;
        this.category = category;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}