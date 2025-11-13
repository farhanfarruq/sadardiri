package com.example.sadardiri;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextInputEditText editAmount, editNote;
    private Spinner spinnerType, spinnerCategory;
    private Button btnPickDate, btnSaveTransaction;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        dbHelper = new DatabaseHelper(this);
        editAmount = findViewById(R.id.editAmount);
        editNote = findViewById(R.id.editNote);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.transaction_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        loadCategories();

        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        btnPickDate.setText(selectedDate);

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSaveTransaction.setOnClickListener(v -> saveTransaction());
    }

    private void loadCategories() {
        ArrayList<String> categories = new ArrayList<>();
        Cursor cursor = dbHelper.getAllCategories();
        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (categories.isEmpty()) categories.add("Tidak ada kategori");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new android.app.DatePickerDialog(this, (view, y, m, d) -> {
            c.set(y, m, d);
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.getTime());
            btnPickDate.setText(selectedDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTransaction() {
        String amountStr = editAmount.getText().toString().trim();
        String note = editNote.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        String categoryName = spinnerCategory.getSelectedItem().toString();

        if (amountStr.isEmpty() || categoryName.isEmpty() || categoryName.equals("Tidak ada kategori")) {
            Toast.makeText(this, "Isi semua field!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        int categoryId = getCategoryId(categoryName);
        if (categoryId == -1) {
            Toast.makeText(this, "Kategori tidak valid!", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.addTransaction(amount, type.equals("Pemasukan") ? "income" : "expense", categoryId, note, selectedDate);
        Toast.makeText(this, "Transaksi disimpan!", Toast.LENGTH_SHORT).show();

        Intent refresh = new Intent("REFRESH_FINANCE");
        sendBroadcast(refresh);
        finish();
    }

    private int getCategoryId(String name) {
        Cursor cursor = dbHelper.getAllCategories();
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndexOrThrow("name")).equals(name)) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    cursor.close();
                    return id;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return -1;
    }
}