package com.example.sadardiri.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.data.FirestoreHabitRepository;
import com.example.sadardiri.model.Habit;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private final List<Habit> habits;
    private final FirestoreHabitRepository habitRepo = new FirestoreHabitRepository();

    public HabitAdapter(List<Habit> habits) {
        this.habits = habits;
    }

    public void setData(List<Habit> newHabits) {
        habits.clear();
        habits.addAll(newHabits);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);

        holder.textHabitName.setText(habit.getName());
        holder.checkHabit.setOnCheckedChangeListener(null);
        holder.checkHabit.setChecked(habit.isDone());

        holder.checkHabit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (habit.getId() == null) return;
            habitRepo.setDone(habit.getId(), isChecked)
                    .addOnSuccessListener(unused -> {
                        habit.setDone(isChecked);
                        Intent intent = new Intent("REFRESH_HABITS");
                        holder.itemView.getContext().sendBroadcast(intent);
                        holder.itemView.getContext().sendBroadcast(new Intent("REFRESH_DASHBOARD"));
                    });
        });

        // FITUR HOLD
        holder.itemView.setOnLongClickListener(v -> {
            showEditDialog(holder.itemView.getContext(), habit);
            return true;
        });
    }

    private void showEditDialog(Context context, Habit h) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_habit, null);
        EditText editName = view.findViewById(R.id.editHabitName);
        editName.setText(h.getName());

        builder.setView(view)
                .setTitle("Edit Kebiasaan")
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String newName = editName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        habitRepo.updateName(h.getId(), newName).addOnSuccessListener(u -> {
                            Toast.makeText(context, "Diperbarui", Toast.LENGTH_SHORT).show();
                            context.sendBroadcast(new Intent("REFRESH_HABITS"));
                            context.sendBroadcast(new Intent("REFRESH_DASHBOARD"));
                        });
                    }
                })
                .setNeutralButton("Hapus", (dialog, which) -> {
                    new AlertDialog.Builder(context)
                            .setMessage("Hapus kebiasaan ini?")
                            .setPositiveButton("Ya", (d, w) -> {
                                habitRepo.delete(h.getId()).addOnSuccessListener(u -> {
                                    Toast.makeText(context, "Terhapus", Toast.LENGTH_SHORT).show();
                                    context.sendBroadcast(new Intent("REFRESH_HABITS"));
                                    context.sendBroadcast(new Intent("REFRESH_DASHBOARD"));
                                });
                            }).show();
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView textHabitName;
        CheckBox checkHabit;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            textHabitName = itemView.findViewById(R.id.textHabitName);
            checkHabit = itemView.findViewById(R.id.checkBoxHabit);
        }
    }
}