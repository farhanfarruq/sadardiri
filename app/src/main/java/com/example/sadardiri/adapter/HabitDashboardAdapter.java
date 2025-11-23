package com.example.sadardiri.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.model.Habit; // Menggunakan model.Habit
import com.example.sadardiri.R;
import com.example.sadardiri.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HabitDashboardAdapter extends RecyclerView.Adapter<HabitDashboardAdapter.ViewHolder> {
    private ArrayList<Habit> habits;
    private DatabaseHelper dbHelper;
    private Runnable onUpdate;

    public HabitDashboardAdapter(ArrayList<Habit> habits, DatabaseHelper dbHelper, Runnable onUpdate) {
        this.habits = habits;
        this.dbHelper = dbHelper;
        this.onUpdate = onUpdate;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.checkBox.setText(habit.getName());

        // FIX: isDone() sekarang tersedia
        holder.checkBox.setChecked(habit.isDone());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            if (isChecked) {
                dbHelper.logHabit(habit.getId(), today);
            } else {
                dbHelper.removeHabitLog(habit.getId(), today);
            }
            // FIX: setDone(boolean) sekarang tersedia
            habit.setDone(isChecked);
            if (onUpdate != null) onUpdate.run();
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkHabit);
        }
    }
}