package com.example.sadardiri.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sadardiri.R;
import com.example.sadardiri.data.FirestoreSavingsRepository;
import com.example.sadardiri.model.SavingsTarget;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class AddSavingsTargetActivity extends AppCompatActivity {

    private TextInputEditText editSavingName, editTargetAmount, editCurrentAmount;
    private Button btnSaveSaving, btnPickTargetDate;
    private FirestoreSavingsRepository savingsRepo;
    private String selectedDate = ""; // Variabel untuk menyimpan tanggal

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_savings_target);

        editSavingName = findViewById(R.id.editTargetName);
        editTargetAmount = findViewById(R.id.editTargetAmount);
        editCurrentAmount = findViewById(R.id.editCurrentAmount);
        btnSaveSaving = findViewById(R.id.btnSaveTarget);
        btnPickTargetDate = findViewById(R.id.btnPickTargetDate); // Inisialisasi tombol tanggal

        savingsRepo = new FirestoreSavingsRepository();

        // Set default tanggal (Hari ini)
        Calendar c = Calendar.getInstance();
        selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
        btnPickTargetDate.setText("Target: " + selectedDate);

        // Listener tombol tanggal
        btnPickTargetDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                btnPickTargetDate.setText("Target: " + selectedDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSaveSaving.setOnClickListener(v -> saveSavingTarget());
    }

    private void saveSavingTarget() {
        String name = editSavingName.getText() != null ? editSavingName.getText().toString().trim() : "";
        String targetStr = editTargetAmount.getText() != null ? editTargetAmount.getText().toString().trim() : "";
        String currentStr = editCurrentAmount.getText() != null ? editCurrentAmount.getText().toString().trim() : "";

        if (name.isEmpty() || targetStr.isEmpty()) {
            Toast.makeText(this, "Nama dan Target harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        double target, current = 0;
        try {
            target = Double.parseDouble(targetStr);
            if (!currentStr.isEmpty()) {
                current = Double.parseDouble(currentStr);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Angka tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simpan dengan tanggal
        SavingsTarget s = new SavingsTarget(null, null, name, target, current, selectedDate);

        savingsRepo.add(s)
                .addOnSuccessListener(id -> {
                    Toast.makeText(this, "Target tersimpan", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("REFRESH_SAVINGS");
                    sendBroadcast(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}