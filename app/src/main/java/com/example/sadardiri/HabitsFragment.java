package com.example.sadardiri;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String frequency = cursor.getString(cursor.getColumnIndexOrThrow("frequency"));
                habitList.add(new Habit(id, name, frequency));
            } while (cursor.moveToNext());
        }
        cursor.close();
        habitAdapter.notifyDataSetChanged();
    }

    private void updateHabitScore() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        int completed = dbHelper.getCompletedHabitsToday(today);
        int total = habitList.size();
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