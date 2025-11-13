// CategoryManagerActivity.java
package com.example.sadardiri;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryManagerActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager); // Create similar layout

        dbHelper = new DatabaseHelper(this);

        recyclerCategories = findViewById(R.id.recyclerCategories);
        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        // Set adapter for categories with CRUD

        // Add FAB for add category
    }
}