package com.example.sadardiri.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.data.FirestoreCategoryRepository;
import com.example.sadardiri.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<Category> categories;
    private final FirestoreCategoryRepository categoryRepo = new FirestoreCategoryRepository();

    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
    }

    public void setData(List<Category> newList) {
        categories.clear();
        categories.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category c = categories.get(position);

        holder.textName.setText(c.getName());

        holder.btnDelete.setOnClickListener(v -> deleteCategory(holder.itemView.getContext(), c, position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    private void deleteCategory(Context context, Category c, int position) {
        if (c.getId() == null) return;

        new AlertDialog.Builder(context)
                .setTitle("Hapus Kategori")
                .setMessage("Yakin ingin menghapus \"" + c.getName() + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    categoryRepo.delete(c.getId())
                            .addOnSuccessListener(unused -> {
                                categories.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "Kategori dihapus", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context,
                                            "Gagal menghapus: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        ImageView btnDelete; // Menggunakan ImageView sesuai XML

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textCategoryName);
            // btnEdit dihapus
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}