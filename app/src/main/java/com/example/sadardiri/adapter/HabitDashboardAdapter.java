package com.example.sadardiri.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sadardiri.R;
import com.example.sadardiri.model.Habit;

import java.util.List;

public class HabitDashboardAdapter extends RecyclerView.Adapter<HabitDashboardAdapter.HabitDashViewHolder> {

    private final List<Habit> habits;

    public HabitDashboardAdapter(List<Habit> habits) {
        this.habits = habits;
    }

    public void setData(List<Habit> newList) {
        habits.clear();
        habits.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitDashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nama layout disesuaikan dengan file yang ada
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit_dashboard, parent, false);
        return new HabitDashViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitDashViewHolder holder, int position) {
        Habit h = habits.get(position);

        // XML hanya berisi CheckBox, jadi kita set text dan status di checkbox tersebut
        holder.checkBox.setText(h.getName());
        holder.checkBox.setChecked(h.isDone());
        holder.checkBox.setEnabled(false); // Read-only untuk dashboard
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class HabitDashViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        HabitDashViewHolder(@NonNull View itemView) {
            super(itemView);
            // ID disesuaikan dengan isi item_habit_dashboard.xml
            checkBox = itemView.findViewById(R.id.checkHabit);
        }
    }
}