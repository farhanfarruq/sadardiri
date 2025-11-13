package com.example.sadardiri;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class AddSavingsTargetActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextInputEditText editTargetName, editTargetAmount, editCurrentAmount;
    private Button btnPickTargetDate, btnSaveTarget;
    private String targetDate = "2025-12-31";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_savings_target);

        dbHelper = new DatabaseHelper(this);

        editTargetName = findViewById(R.id.editTargetName);
        editTargetAmount = findViewById(R.id.editTargetAmount);
        editCurrentAmount = findViewById(R.id.editCurrentAmount);
        btnPickTargetDate = findViewById(R.id.btnPickTargetDate);
        btnSaveTarget = findViewById(R.id.btnSaveTarget);

        btnPickTargetDate.setText(targetDate);

        btnPickTargetDate.setOnClickListener(v -> {
            android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        targetDate = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", dayOfMonth);
                        btnPickTargetDate.setText(targetDate);
                    },
                    2025, 11, 31);
            dialog.show();
        });

        btnSaveTarget.setOnClickListener(v -> saveTarget());
    }

    private void saveTarget() {
        String name = editTargetName.getText().toString().trim();
        String targetStr = editTargetAmount.getText().toString().trim();
        String currentStr = editCurrentAmount.getText().toString().trim();

        if (name.isEmpty() || targetStr.isEmpty()) {
            Toast.makeText(this, "Isi nama dan target!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double target = Double.parseDouble(targetStr);
            double current = currentStr.isEmpty() ? 0.0 : Double.parseDouble(currentStr);

            dbHelper.addSavingsTarget(name, target, current, targetDate);
            Toast.makeText(this, "Target tabungan disimpan!", Toast.LENGTH_SHORT).show();

            // KIRIM BROADCAST KE DASHBOARD
            Intent intent = new Intent("REFRESH_SAVINGS");
            sendBroadcast(intent);

            setResult(RESULT_OK);
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Masukkan angka yang valid!", Toast.LENGTH_SHORT).show();
        }
    }
}