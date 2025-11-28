package com.example.sadardiri.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.database.DatabaseHelper;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private ArrayList<String> categoryNames;
    private ArrayList<Integer> categoryIds; // Butuh ID untuk menghapus
    private DatabaseHelper dbHelper;
    private Context context;

    public CategoryAdapter(Context context, DatabaseHelper dbHelper) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.categoryNames = new ArrayList<>();
        this.categoryIds = new ArrayList<>();
    }

    public void updateData(Cursor cursor) {
        categoryNames.clear();
        categoryIds.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                categoryNames.add(name);
                categoryIds.add(id);
            } while (cursor.moveToNext());
            cursor.close();
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // GANTI layout ke item_category
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name = categoryNames.get(position);
        int id = categoryIds.get(position);

        holder.textName.setText(name);

        // LOGIKA HAPUS KATEGORI
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Kategori?")
                    .setMessage("Kategori '" + name + "' akan dihapus permanen.")
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        dbHelper.deleteCategory(id);

                        // Refresh data manual dari adapter
                        categoryNames.remove(position);
                        categoryIds.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, categoryNames.size());

                        Toast.makeText(context, "Kategori dihapus", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return categoryNames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        ImageView btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textCategoryName);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}