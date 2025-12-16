package com.example.sadardiri.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.data.FirestoreSavingsRepository;
import com.example.sadardiri.model.SavingsTarget;

import java.util.List;

public class SavingsAdapter extends RecyclerView.Adapter<SavingsAdapter.SavingsViewHolder> {

    private final List<SavingsTarget> savingsList;
    private final FirestoreSavingsRepository savingsRepo = new FirestoreSavingsRepository();

    public SavingsAdapter(List<SavingsTarget> savingsList) {
        this.savingsList = savingsList;
    }

    public void setData(List<SavingsTarget> newList) {
        savingsList.clear();
        savingsList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SavingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_savings, parent, false);
        return new SavingsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SavingsViewHolder holder, int position) {
        SavingsTarget s = savingsList.get(position);

        holder.textName.setText(s.getName());
        holder.textProgress.setText(s.getProgressText());
        holder.progressBar.setMax(100);
        holder.progressBar.setProgress(s.getProgress());

        // Tambah Tabungan
        holder.btnAddAmount.setOnClickListener(v -> showAddAmountDialog(holder.itemView.getContext(), s));

        // FITUR HOLD (Edit Target)
        holder.itemView.setOnLongClickListener(v -> {
            showEditDialog(holder.itemView.getContext(), s);
            return true;
        });
    }

    private void showAddAmountDialog(Context context, SavingsTarget s) {
        EditText input = new EditText(context);
        input.setHint("Nominal (Rp)");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(context)
                .setTitle("Nabung")
                .setMessage("Tambah saldo untuk " + s.getName())
                .setView(input)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String val = input.getText().toString().trim();
                    if (!val.isEmpty()) {
                        double add = Double.parseDouble(val);
                        savingsRepo.updateCurrentAmount(s.getId(), s.getCurrentAmount() + add)
                                .addOnSuccessListener(u -> sendRefreshBroadcast(context));
                    }
                })
                .show();
    }

    private void showEditDialog(Context context, SavingsTarget s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_savings, null);

        EditText editName = view.findViewById(R.id.editName);
        EditText editTarget = view.findViewById(R.id.editTargetAmount);

        editName.setText(s.getName());
        editTarget.setText(String.valueOf((long)s.getTargetAmount()));

        builder.setView(view)
                .setTitle("Edit Target")
                .setPositiveButton("Update", (dialog, which) -> {
                    String newName = editName.getText().toString().trim();
                    String newTargetStr = editTarget.getText().toString().trim();
                    if(!newName.isEmpty() && !newTargetStr.isEmpty()){
                        double newTarget = Double.parseDouble(newTargetStr);
                        savingsRepo.updateTarget(s.getId(), newName, newTarget)
                                .addOnSuccessListener(u -> {
                                    Toast.makeText(context, "Target Diupdate", Toast.LENGTH_SHORT).show();
                                    sendRefreshBroadcast(context);
                                });
                    }
                })
                .setNeutralButton("Hapus", (dialog, which) -> {
                    new AlertDialog.Builder(context).setMessage("Hapus target ini?")
                            .setPositiveButton("Ya", (d, w) -> {
                                savingsRepo.delete(s.getId()).addOnSuccessListener(u -> sendRefreshBroadcast(context));
                            }).show();
                })
                .show();
    }

    private void sendRefreshBroadcast(Context context) {
        Intent intent = new Intent("REFRESH_SAVINGS");
        context.sendBroadcast(intent);
        context.sendBroadcast(new Intent("REFRESH_DASHBOARD"));
    }

    @Override
    public int getItemCount() {
        return savingsList.size();
    }

    static class SavingsViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textProgress;
        ProgressBar progressBar;
        ImageButton btnAddAmount;

        SavingsViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textTargetName);
            textProgress = itemView.findViewById(R.id.textProgress);
            progressBar = itemView.findViewById(R.id.progressBarTarget);
            btnAddAmount = itemView.findViewById(R.id.btnAddAmount);
        }
    }
}