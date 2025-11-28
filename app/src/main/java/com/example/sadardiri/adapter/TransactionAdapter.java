package com.example.sadardiri.adapter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.database.DatabaseHelper;
import com.example.sadardiri.model.Transaction;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> transactionList;
    private Context context;
    private NumberFormat currencyFormat;
    private DatabaseHelper dbHelper;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList != null ? transactionList : new ArrayList<>();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        this.currencyFormat.setMaximumFractionDigits(0);
        this.dbHelper = new DatabaseHelper(context);
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
                        ? "+ " + currencyFormat.format(amount)
                        : "- " + currencyFormat.format(amount)
        );

        int colorResId = type.equals("income") ? R.color.button_income : R.color.button_expense;
        holder.textAmount.setTextColor(ContextCompat.getColor(context, colorResId));
        holder.textDate.setText(transaction.getDate());

        // LOGIKA TEKAN LAMA (OPSI)
        holder.itemView.setOnLongClickListener(v -> {
            showOptionDialog(transaction);
            return true;
        });
    }

    private void showOptionDialog(Transaction transaction) {
        String[] options = {"Edit Transaksi", "Hapus Transaksi"};
        new AlertDialog.Builder(context)
                .setTitle("Pilihan")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditDialog(transaction);
                    } else {
                        showDeleteConfirm(transaction);
                    }
                })
                .show();
    }

    private void showDeleteConfirm(Transaction transaction) {
        new AlertDialog.Builder(context)
                .setMessage("Hapus transaksi ini?")
                .setPositiveButton("Hapus", (d, w) -> {
                    dbHelper.deleteTransaction(transaction.getId());
                    refreshData();
                    Toast.makeText(context, "Terhapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showEditDialog(Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_transaction, null);

        TextInputEditText editAmount = view.findViewById(R.id.editAmount);
        TextInputEditText editNote = view.findViewById(R.id.editNote);
        Spinner spinnerType = view.findViewById(R.id.spinnerType);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Button btnPickDate = view.findViewById(R.id.btnPickDate);

        // Isi Data Lama
        editAmount.setText(String.valueOf(transaction.getAmount()).replace(".0", ""));
        editNote.setText(transaction.getNote());
        btnPickDate.setText(transaction.getDate());

        // Setup Spinner Type
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(context,
                R.array.transaction_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        if (transaction.getType().equals("expense")) spinnerType.setSelection(1);

        // Setup Spinner Kategori
        ArrayList<String> categories = new ArrayList<>();
        ArrayList<Integer> categoryIds = new ArrayList<>();
        Cursor cursor = dbHelper.getAllCategories();
        int selectedCatIndex = 0;
        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                categories.add(name);
                categoryIds.add(id);
                if (name.equals(transaction.getCategory())) selectedCatIndex = i;
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);
        spinnerCategory.setSelection(selectedCatIndex);

        // Date Picker Logic
        final String[] selectedDate = {transaction.getDate()};
        btnPickDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(context, (view1, year, month, day) -> {
                c.set(year, month, day);
                selectedDate[0] = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.getTime());
                btnPickDate.setText(selectedDate[0]);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(view);
        builder.setPositiveButton("Simpan", (d, w) -> {
            String amountStr = editAmount.getText().toString();
            if(!amountStr.isEmpty()){
                double amount = Double.parseDouble(amountStr);
                String type = spinnerType.getSelectedItem().toString().equals("Pemasukan") ? "income" : "expense";
                int catId = categoryIds.get(spinnerCategory.getSelectedItemPosition());
                String note = editNote.getText().toString();

                dbHelper.updateTransaction(transaction.getId(), amount, type, catId, note, selectedDate[0]);
                refreshData();
                Toast.makeText(context, "Diupdate!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void refreshData() {
        Intent intent = new Intent("REFRESH_FINANCE");
        context.sendBroadcast(intent);
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