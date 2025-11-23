package com.example.sadardiri.model;

public class SavingsTarget {
    private int id;
    private String name;
    private double targetAmount;
    private double currentAmount;

    public SavingsTarget(int id, String name, double targetAmount, double currentAmount) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getTargetAmount() { return targetAmount; }
    public double getCurrentAmount() { return currentAmount; }

    public int getProgress() {
        return targetAmount > 0 ? (int) ((currentAmount / targetAmount) * 100) : 0;
    }

    public String getProgressText() {
        return String.format("Rp %,.0f / Rp %,.0f", currentAmount, targetAmount);
    }
}