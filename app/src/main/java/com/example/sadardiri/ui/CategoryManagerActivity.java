package com.example.sadardiri.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.adapter.CategoryAdapter;
import com.example.sadardiri.database.DatabaseHelper;
import com.example.sadardiri.R;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

public class CategoryManagerActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerCategories;
    private ArrayList<String> categoryList;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager);

        dbHelper = new DatabaseHelper(this);
        recyclerCategories = findViewById(R.id.recyclerCategories);
        Button btnAddCategory = findViewById(R.id.btnAddCategory);

        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(categoryList, dbHelper);
        recyclerCategories.setAdapter(adapter);

        loadCategories();

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadCategories() {
        categoryList.clear();
        android.database.Cursor cursor = dbHelper.getAllCategories();
        if (cursor.moveToFirst()) {
            do {
                categoryList.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        TextInputEditText editName = view.findViewById(R.id.editCategoryName);
        builder.setView(view)
                .setPositiveButton("Simpan", (d, w) -> {
                    String name = editName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        dbHelper.addCategory(name);
                        loadCategories();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}