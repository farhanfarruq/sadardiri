package com.example.sadardiri.model; // Perhatikan package ini sekarang menggunakan .model

public class Transaction {
    private int id;
    private double amount;
    private String type, note, date, category;

    public Transaction(int id, double amount, String type, String note, String date, String category) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.note = note;
        this.date = date;
        this.category = category;
    }

    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getNote() { return note; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
}