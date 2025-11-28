package com.example.sadardiri.ui;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.adapter.CategoryAdapter;
import com.example.sadardiri.database.DatabaseHelper;
import com.example.sadardiri.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class CategoryManagerActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerCategories;
    private CategoryAdapter adapter;
    private FloatingActionButton btnAddCategory; // Ganti jadi FAB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager);

        dbHelper = new DatabaseHelper(this);
        recyclerCategories = findViewById(R.id.recyclerCategories);
        btnAddCategory = findViewById(R.id.btnAddCategory); // Hubungkan ke FAB

        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));

        // Setup Adapter
        adapter = new CategoryAdapter(this, dbHelper);
        recyclerCategories.setAdapter(adapter);

        loadCategories();

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadCategories() {
        Cursor cursor = dbHelper.getAllCategories();
        adapter.updateData(cursor);
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        TextInputEditText editName = view.findViewById(R.id.editCategoryName);

        builder.setView(view)
                .setTitle("Tambah Kategori Baru")
                .setPositiveButton("Simpan", (d, w) -> {
                    String name = editName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        dbHelper.addCategory(name);
                        loadCategories();
                        Toast.makeText(this, "Kategori ditambahkan", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}