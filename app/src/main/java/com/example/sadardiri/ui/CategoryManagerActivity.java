package com.example.sadardiri.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.adapter.CategoryAdapter;
import com.example.sadardiri.data.FirestoreCategoryRepository;
import com.example.sadardiri.model.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryManagerActivity extends AppCompatActivity {

    private RecyclerView recyclerCategories;
    private FloatingActionButton btnAddCategory;

    private CategoryAdapter categoryAdapter;
    private final List<Category> categoryList = new ArrayList<>();
    private FirestoreCategoryRepository categoryRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager);

        recyclerCategories = findViewById(R.id.recyclerCategories);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        categoryRepo = new FirestoreCategoryRepository();

        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(categoryList);
        recyclerCategories.setAdapter(categoryAdapter);

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        loadCategories();
    }

    private void showAddCategoryDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText editCategoryName = view.findViewById(R.id.editCategoryName);

        new AlertDialog.Builder(this)
                .setTitle("Tambah Kategori")
                .setView(view)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String name = editCategoryName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        addCategory(name);
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void loadCategories() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        categoryRepo.getAll()
                .addOnSuccessListener(list -> {
                    // FITUR BARU: Smart Seeding (Isi otomatis tanpa duplikat)
                    seedDefaultCategoriesIfMissing(list);

                    categoryList.clear();
                    categoryList.addAll(list);
                    categoryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // Logika Cerdas: Cek dulu apakah nama sudah ada sebelum nambah
    private void seedDefaultCategoriesIfMissing(List<Category> currentList) {
        List<String> defaults = Arrays.asList(
                "Gaji", "Bonus", "Makan", "Transport", "Belanja",
                "Tagihan", "Hiburan", "Kesehatan", "Pendidikan", "Lainnya"
        );

        // Ambil semua nama kategori yang sudah ada (ubah ke huruf kecil biar akurat)
        List<String> existingNames = new ArrayList<>();
        for (Category c : currentList) {
            if (c.getName() != null) {
                existingNames.add(c.getName().toLowerCase());
            }
        }

        boolean addedNew = false;
        for (String def : defaults) {
            // Hanya tambahkan jika BELUM ada di list
            if (!existingNames.contains(def.toLowerCase())) {
                categoryRepo.add(new Category(null, null, def));
                addedNew = true;
            }
        }

        // Jika ada yang baru ditambahkan, refresh layar otomatis setelah 1 detik
        if (addedNew) {
            recyclerCategories.postDelayed(this::reloadListOnly, 1000);
        }
    }

    private void reloadListOnly() {
        categoryRepo.getAll().addOnSuccessListener(list -> {
            categoryList.clear();
            categoryList.addAll(list);
            categoryAdapter.notifyDataSetChanged();
        });
    }

    private void addCategory(String name) {
        Category c = new Category(null, null, name);
        categoryRepo.add(c)
                .addOnSuccessListener(id -> {
                    Toast.makeText(this, "Berhasil", Toast.LENGTH_SHORT).show();
                    loadCategories(); // Reload setelah tambah manual
                });
    }
}