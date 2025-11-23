package com.example.sadardiri.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sadardiri.database.DatabaseHelper;
import com.example.sadardiri.R;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddSavingsTargetActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextInputEditText editName, editTarget, editCurrent; // Ditambah editCurrent
    private Button btnPickDate, btnSave;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_savings_target);

        dbHelper = new DatabaseHelper(this);

        // Inisialisasi View
        editName = findViewById(R.id.editTargetName);
        editTarget = findViewById(R.id.editTargetAmount);
        editCurrent = findViewById(R.id.editCurrentAmount); // Inisialisasi Jumlah Saat Ini
        btnPickDate = findViewById(R.id.btnPickTargetDate); // ID diperbaiki (sebelumnya R.id.btnPickDate)
        btnSave = findViewById(R.id.btnSaveTarget);

        // Default tanggal: hari ini
        Calendar calendar = Calendar.getInstance();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        btnPickDate.setText(selectedDate);

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveTarget());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year1, month1, dayOfMonth);
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selected.getTime());
            btnPickDate.setText(selectedDate);
        }, year, month, day).show();
    }

    private void saveTarget() {
        String name = editName.getText().toString().trim();
        String targetAmountStr = editTarget.getText().toString().trim();
        String currentAmountStr = editCurrent.getText().toString().trim(); // Ambil Jumlah Saat Ini

        if (name.isEmpty() || targetAmountStr.isEmpty()) {
            Toast.makeText(this, "Isi semua field yang wajib (Nama Target dan Jumlah Target)!", Toast.LENGTH_SHORT).show();
            return;
        }

        double targetAmount;
        double currentAmount = 0.0; // Default 0.0 jika opsional

        // 1. Validasi Target Amount
        try {
            targetAmount = Double.parseDouble(targetAmountStr.replace(",", ""));
            if (targetAmount <= 0) throw new Exception();
        } catch (Exception e) {
            Toast.makeText(this, "Masukkan jumlah target yang valid (> 0)!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validasi Current Amount (Opsional)
        if (!currentAmountStr.isEmpty()) {
            try {
                currentAmount = Double.parseDouble(currentAmountStr.replace(",", ""));
                if (currentAmount < 0) throw new Exception();
            } catch (Exception e) {
                Toast.makeText(this, "Masukkan jumlah saat ini yang valid!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 3. Validasi Current Amount vs Target Amount
        if (currentAmount > targetAmount) {
            Toast.makeText(this, "Jumlah saat ini tidak boleh melebihi jumlah target!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simpan ke database (currentAmount sudah terisi atau 0.0)
        dbHelper.addSavingsTarget(name, targetAmount, currentAmount, selectedDate);

        Intent intent = new Intent("REFRESH_SAVINGS");
        sendBroadcast(intent);

        Toast.makeText(this, "Target disimpan!", Toast.LENGTH_SHORT).show();
        finish();
    }
}