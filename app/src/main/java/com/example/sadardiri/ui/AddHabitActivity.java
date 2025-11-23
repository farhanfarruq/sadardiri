package com.example.sadardiri.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sadardiri.database.DatabaseHelper;
import com.example.sadardiri.R;
import com.google.android.material.textfield.TextInputEditText;

public class AddHabitActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextInputEditText editTextHabitName;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        dbHelper = new DatabaseHelper(this);
        editTextHabitName = findViewById(R.id.editTextHabitName);
        buttonSave = findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(v -> {
            String name = editTextHabitName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Masukkan nama kebiasaan!", Toast.LENGTH_SHORT).show();
                return;
            }
            dbHelper.addHabit(name, "daily");
            Toast.makeText(this, "Kebiasaan disimpan!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}