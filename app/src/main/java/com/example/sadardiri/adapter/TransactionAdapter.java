package com.example.sadardiri.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.model.Transaction; // Menggunakan model.Transaction
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> transactionList;
    private Context context;
    private NumberFormat currencyFormat;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList != null ? transactionList : new ArrayList<>();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        this.currencyFormat.setMaximumFractionDigits(0);
    }

    public void setData(List<Transaction> list) {
        this.transactionList.clear();
        this.transactionList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.textCategory.setText(transaction.getCategory());
        holder.textNote.setText(transaction.getNote() != null && !transaction.getNote().isEmpty() ? transaction.getNote() : "-");

        String type = transaction.getType();
        double amount = transaction.getAmount();

        holder.textAmount.setText(
                type.equals("income")
                        ? "+ Rp " + currencyFormat.format(amount)
                        : "- Rp " + currencyFormat.format(amount)
        );

        // Menggunakan R.color.button_income/expense dari colors.xml
        int colorResId = type.equals("income") ? R.color.button_income : R.color.button_expense;
        holder.textAmount.setTextColor(ContextCompat.getColor(context, colorResId));

        holder.textDate.setText(transaction.getDate());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
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