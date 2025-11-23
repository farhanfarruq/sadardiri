package com.example.sadardiri.model; // Perhatikan package ini sekarang menggunakan .model

public class Habit {
    private int id;
    private String name;
    private boolean done;

    public Habit(int id, String name, boolean done) {
        this.id = id;
        this.name = name;
        this.done = done;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}