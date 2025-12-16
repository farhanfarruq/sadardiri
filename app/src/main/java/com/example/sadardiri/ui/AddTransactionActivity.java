package com.example.sadardiri.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sadardiri.R;
import com.example.sadardiri.data.FirestoreCategoryRepository;
import com.example.sadardiri.data.FirestoreTransactionRepository;
import com.example.sadardiri.model.Category;
import com.example.sadardiri.model.Transaction;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    // Hapus DatabaseHelper, ganti dengan FirestoreCategoryRepository
    private FirestoreTransactionRepository transactionRepo;
    private FirestoreCategoryRepository categoryRepo;

    private TextInputEditText editAmount, editNote;
    private Spinner spinnerType, spinnerCategory;
    private Button btnPickDate, btnSave;

    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        transactionRepo = new FirestoreTransactionRepository();
        categoryRepo = new FirestoreCategoryRepository(); // Inisialisasi repo kategori

        editAmount = findViewById(R.id.editAmount);
        editNote = findViewById(R.id.editNote);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnSave = findViewById(R.id.btnSaveTransaction);

        // Setup Spinner Tipe (Pemasukan/Pengeluaran)
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.transaction_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // LOAD KATEGORI DARI FIRESTORE (Bukan SQLite lagi)
        loadCategoriesToSpinner();

        // Set Default Date Hari Ini
        Calendar c = Calendar.getInstance();
        updateDateButton(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void loadCategoriesToSpinner() {
        // Ambil kategori dari Firestore milik user yang sedang login
        categoryRepo.getAll()
                .addOnSuccessListener(categoryList -> {
                    ArrayList<String> categories = new ArrayList<>();

                    for (Category c : categoryList) {
                        categories.add(c.getName());
                    }

                    // Kalau kosong, kasih default
                    if (categories.isEmpty()) {
                        categories.add("Umum");
                    }

                    ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, categories);
                    catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(catAdapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat kategori", Toast.LENGTH_SHORT).show();
                    // Fallback kalau gagal loading, isi default aja
                    ArrayList<String> fallback = new ArrayList<>();
                    fallback.add("Umum");
                    spinnerCategory.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, fallback));
                });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> updateDateButton(year, month, dayOfMonth),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateButton(int year, int month, int dayOfMonth) {
        selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
        btnPickDate.setText(selectedDate);
    }

    private void saveTransaction() {
        String amountStr = editAmount.getText() != null ? editAmount.getText().toString().trim() : "";
        String note = editNote.getText() != null ? editNote.getText().toString().trim() : "";

        // Ambil nilai dari spinner
        String type = spinnerType.getSelectedItem().toString().equalsIgnoreCase("Pemasukan") ? "income" : "expense";

        // Cek null safety kalau spinner belum siap
        String category = "Umum";
        if (spinnerCategory.getSelectedItem() != null) {
            category = spinnerCategory.getSelectedItem().toString();
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Nominal tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Nominal tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // userId = null (akan diisi otomatis oleh repository)
        Transaction t = new Transaction(null, null, amount, type, note, selectedDate, category);

        transactionRepo.add(t)
                .addOnSuccessListener(id -> {
                    Toast.makeText(this, "Transaksi tersimpan", Toast.LENGTH_SHORT).show();
                    // Kirim broadcast agar fragment refresh
                    android.content.Intent intent = new android.content.Intent("REFRESH_FINANCE");
                    sendBroadcast(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}