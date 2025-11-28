package com.example.sadardiri.adapter;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.model.Habit;
import com.example.sadardiri.R;
import com.example.sadardiri.database.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit> habits;
    private DatabaseHelper dbHelper;
    private String today;

    public HabitAdapter(List<Habit> habits, DatabaseHelper dbHelper) {
        this.habits = habits;
        this.dbHelper = dbHelper;
        this.today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    @Override
    public HabitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.textHabitName.setText(habit.getName());
        holder.checkBox.setOnCheckedChangeListener(null);
        boolean isDone = dbHelper.isHabitDoneToday(habit.getId(), today);
        holder.checkBox.setChecked(isDone);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) dbHelper.logHabit(habit.getId(), today);
            else dbHelper.removeHabitLog(habit.getId(), today);

            Intent intent = new Intent("REFRESH_HABITS");
            holder.itemView.getContext().sendBroadcast(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            String[] options = {"Edit Kebiasaan", "Hapus Kebiasaan"};
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) showEditDialog(holder.itemView.getContext(), habit);
                        else showDeleteConfirm(holder.itemView.getContext(), habit);
                    })
                    .show();
            return true;
        });
    }

    private void showEditDialog(android.content.Context context, Habit habit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_habit, null);
        TextInputEditText editName = view.findViewById(R.id.editHabitName);
        editName.setText(habit.getName());

        builder.setView(view);
        builder.setPositiveButton("Simpan", (d, w) -> {
            String name = editName.getText().toString();
            if (!name.isEmpty()) {
                dbHelper.updateHabitName(habit.getId(), name);
                context.sendBroadcast(new Intent("REFRESH_HABITS"));
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void showDeleteConfirm(android.content.Context context, Habit habit) {
        new AlertDialog.Builder(context)
                .setMessage("Hapus kebiasaan ini?")
                .setPositiveButton("Hapus", (d, w) -> {
                    dbHelper.deleteHabit(habit.getId());
                    context.sendBroadcast(new Intent("REFRESH_HABITS"));
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public int getItemCount() { return habits.size(); }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView textHabitName;
        CheckBox checkBox;
        public HabitViewHolder(View itemView) {
            super(itemView);
            textHabitName = itemView.findViewById(R.id.textHabitName);
            checkBox = itemView.findViewById(R.id.checkBoxHabit);
        }
    }
}