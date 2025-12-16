package com.example.sadardiri.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

public class SavingsTarget {
    @DocumentId
    private String id;
    private String userId;
    private String name;
    private double targetAmount;
    private double currentAmount;
    private String targetDate;

    public SavingsTarget() {}

    public SavingsTarget(String id, String userId, String name, double targetAmount, double currentAmount, String targetDate) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
    }

    // Getter Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }

    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }

    @Exclude
    public int getProgress() {
        if (targetAmount <= 0) return 0;
        return (int) ((currentAmount / targetAmount) * 100);
    }

    @Exclude
    public String getProgressText() {
        return String.format("Rp %,.0f / Rp %,.0f", currentAmount, targetAmount);
    }
}