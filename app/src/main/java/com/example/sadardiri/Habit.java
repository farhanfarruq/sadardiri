package com.example.sadardiri;

public class Habit {
    private int id;
    private String name;
    private String frequency;

    public Habit(int id, String name, String frequency) {
        this.id = id;
        this.name = name;
        this.frequency = frequency;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getFrequency() { return frequency; }
}