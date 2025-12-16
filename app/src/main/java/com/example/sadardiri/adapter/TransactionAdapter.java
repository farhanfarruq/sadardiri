package com.example.sadardiri.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.data.FirestoreTransactionRepository;
import com.example.sadardiri.model.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final Context context;
    private final List<Transaction> transactions;
    private final FirestoreTransactionRepository transactionRepo = new FirestoreTransactionRepository();

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    public void setData(List<Transaction> newList) {
        transactions.clear();
        transactions.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction t = transactions.get(position);

        holder.textNote.setText(t.getNote().isEmpty() ? "(Tanpa catatan)" : t.getNote());
        holder.textDate.setText(t.getDate());
        holder.textCategory.setText(t.getCategory());

        String prefix = t.getType().equalsIgnoreCase("income") ? "+ " : "- ";
        holder.textAmount.setText(prefix + String.format("Rp %,.0f", t.getAmount()));

        if (t.getType().equalsIgnoreCase("income")) {
            holder.textAmount.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.textAmount.setTextColor(Color.parseColor("#C62828"));
        }

        // FITUR HOLD (Tekan Lama)
        holder.itemView.setOnLongClickListener(v -> {
            showEditDialog(t);
            return true;
        });
    }

    private void showEditDialog(Transaction t) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_transaction, null);

        EditText editAmount = view.findViewById(R.id.editAmount);
        EditText editNote = view.findViewById(R.id.editNote);
        Spinner spinnerType = view.findViewById(R.id.spinnerType);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Button btnPickDate = view.findViewById(R.id.btnPickDate);

        // Isi data lama
        editAmount.setText(String.valueOf((long) t.getAmount()));
        editNote.setText(t.getNote());
        btnPickDate.setText(t.getDate());

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                context, R.array.transaction_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setSelection(t.getType().equalsIgnoreCase("income") ? 0 : 1);

        String[] categories = {"Makan", "Transport", "Belanja", "Tagihan", "Hiburan", "Kesehatan", "Pendidikan", "Gaji", "Bonus", "Lainnya"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categories);
        spinnerCategory.setAdapter(catAdapter);
        for(int i=0; i<categories.length; i++) {
            if(categories[i].equalsIgnoreCase(t.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        final String[] selectedDate = {t.getDate()};
        btnPickDate.setOnClickListener(v -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(context, (view1, year, month, dayOfMonth) -> {
                selectedDate[0] = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                btnPickDate.setText(selectedDate[0]);
            }, c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(view)
                .setTitle("Edit Transaksi")
                .setPositiveButton("Update", (dialog, which) -> {
                    String amountStr = editAmount.getText().toString();
                    if(amountStr.isEmpty()) return;

                    double newAmount = Double.parseDouble(amountStr);
                    String newNote = editNote.getText().toString();
                    String newType = spinnerType.getSelectedItem().toString().equalsIgnoreCase("Pemasukan") ? "income" : "expense";
                    String newCat = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "Lainnya";

                    Transaction newT = new Transaction(t.getId(), t.getUserId(), newAmount, newType, newNote, selectedDate[0], newCat);

                    transactionRepo.update(t.getId(), newT).addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Update Berhasil", Toast.LENGTH_SHORT).show();
                        sendRefreshBroadcast();
                    });
                })
                .setNeutralButton("Hapus", (dialog, which) -> {
                    new AlertDialog.Builder(context)
                            .setMessage("Hapus transaksi ini?")
                            .setPositiveButton("Ya", (d, w) -> {
                                transactionRepo.delete(t.getId()).addOnSuccessListener(unused -> {
                                    Toast.makeText(context, "Terhapus", Toast.LENGTH_SHORT).show();
                                    sendRefreshBroadcast();
                                });
                            }).show();
                })
                .show();
    }

    private void sendRefreshBroadcast() {
        Intent intent = new Intent("REFRESH_FINANCE");
        context.sendBroadcast(intent);
        context.sendBroadcast(new Intent("REFRESH_DASHBOARD"));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView textAmount, textNote, textDate, textCategory;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textAmount = itemView.findViewById(R.id.textAmount);
            textNote = itemView.findViewById(R.id.textNote);
            textDate = itemView.findViewById(R.id.textDate);
            textCategory = itemView.findViewById(R.id.textCategory);
        }
    }
}