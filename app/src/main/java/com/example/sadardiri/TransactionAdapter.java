package com.example.sadardiri;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private Cursor cursor;
    private Context context;

    public TransactionAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.textCategory.setText(cursor.getString(cursor.getColumnIndexOrThrow("category_name")));
        holder.textNote.setText(cursor.getString(cursor.getColumnIndexOrThrow("note")));
        double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
        String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
        holder.textAmount.setText(type.equals("income") ? "+ Rp " + amount : "- Rp " + amount);
        holder.textDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("date")));
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textCategory, textNote, textAmount, textDate;

        public ViewHolder(View itemView) {
            super(itemView);
            textCategory = itemView.findViewById(R.id.textCategory);
            textNote = itemView.findViewById(R.id.textNote);
            textAmount = itemView.findViewById(R.id.textAmount);
            textDate = itemView.findViewById(R.id.textDate);
        }
    }
}