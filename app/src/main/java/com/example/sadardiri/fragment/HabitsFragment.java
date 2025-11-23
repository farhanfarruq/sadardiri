package com.example.sadardiri.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.model.Habit; // Menggunakan model.Habit
import com.example.sadardiri.R;
import com.example.sadardiri.adapter.HabitAdapter;
import com.example.sadardiri.database.DatabaseHelper;
import com.example.sadardiri.ui.AddHabitActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerHabits;
    private TextView textHabitScore;
    private Button btnAddHabit;
    private HabitAdapter habitAdapter;
    private List<Habit> habitList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habits, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerHabits = view.findViewById(R.id.recyclerHabits);
        textHabitScore = view.findViewById(R.id.textHabitScore);
        btnAddHabit = view.findViewById(R.id.btnAddHabit);

        recyclerHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        habitList = new ArrayList<>();
        // ASUMSI: HabitAdapter Anda menerima List<Habit> dan DatabaseHelper
        habitAdapter = new HabitAdapter(habitList, dbHelper);
        recyclerHabits.setAdapter(habitAdapter);

        btnAddHabit.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddHabitActivity.class)));

        loadHabits();
        updateHabitScore();

        return view;
    }

    private void loadHabits() {
        habitList.clear();
        Cursor cursor = dbHelper.getAllHabits();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

                // FIX: Cek status selesai hari ini (boolean)
                boolean done = dbHelper.isHabitDoneToday(id, today);

                // FIX: Gunakan konstruktor Habit yang benar (id, name, done)
                habitList.add(new Habit(id, name, done));
            } while (cursor.moveToNext());
        }
        cursor.close();
        habitAdapter.notifyDataSetChanged();
    }

    private void updateHabitScore() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        int completed = dbHelper.getCompletedHabitsToday(today);
        int total = dbHelper.getTotalHabits();
        int score = total > 0 ? (completed * 100 / total) : 0;
        textHabitScore.setText("Skor Hari Ini: " + score + "%");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHabits();
        updateHabitScore();
    }
}