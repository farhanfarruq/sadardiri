package com.example.sadardiri.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sadardiri.R;
import com.example.sadardiri.data.FirestoreHabitRepository;
import com.example.sadardiri.model.Habit;
import com.google.android.material.textfield.TextInputEditText;

public class AddHabitActivity extends AppCompatActivity {

    private TextInputEditText editHabitName;
    private Button btnSaveHabit;
    private FirestoreHabitRepository habitRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        editHabitName = findViewById(R.id.editTextHabitName);
        btnSaveHabit = findViewById(R.id.buttonSave);

        habitRepo = new FirestoreHabitRepository();

        btnSaveHabit.setOnClickListener(v -> saveHabit());
    }

    private void saveHabit() {
        String name = editHabitName.getText() != null
                ? editHabitName.getText().toString().trim()
                : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Nama kebiasaan tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        // userId null, nanti diisi otomatis di repository
        Habit habit = new Habit(null, null, name, false);

        habitRepo.add(habit)
                .addOnSuccessListener(id -> {
                    Toast.makeText(this, "Kebiasaan ditambahkan", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("REFRESH_HABITS");
                    sendBroadcast(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal menyimpan: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}