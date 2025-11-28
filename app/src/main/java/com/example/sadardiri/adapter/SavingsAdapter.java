package com.example.sadardiri.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.database.DatabaseHelper;
import com.example.sadardiri.model.SavingsTarget;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class SavingsAdapter extends RecyclerView.Adapter<SavingsAdapter.ViewHolder> {

    private List<SavingsTarget> savingsList;
    private Context context;
    private DatabaseHelper dbHelper;

    public SavingsAdapter(List<SavingsTarget> savingsList) {
        this.savingsList = savingsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        dbHelper = new DatabaseHelper(context);
        View view = LayoutInflater.from(context).inflate(R.layout.item_savings, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SavingsTarget target = savingsList.get(position);
        holder.textName.setText(target.getName());
        holder.textProgress.setText(target.getProgressText());
        holder.progressBar.setProgress(target.getProgress());

        holder.btnAddAmount.setOnClickListener(v -> showAddDepositDialog(target));

        holder.itemView.setOnLongClickListener(v -> {
            String[] options = {"Edit Target", "Hapus Target"};
            new AlertDialog.Builder(context)
                    .setTitle("Opsi: " + target.getName())
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) showEditDialog(target);
                        else showDeleteConfirm(target);
                    })
                    .show();
            return true;
        });
    }

    private void showAddDepositDialog(SavingsTarget target) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Nabung: " + target.getName());
        final EditText input = new EditText(context);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Masukkan jumlah (Rp)");
        builder.setView(input);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (!amountStr.isEmpty()) {
                double add = Double.parseDouble(amountStr);
                dbHelper.updateSavingsAmount(target.getId(), target.getCurrentAmount() + add);
                refreshData();
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void showEditDialog(SavingsTarget target) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_savings, null);
        TextInputEditText editName = view.findViewById(R.id.editName);
        TextInputEditText editTarget = view.findViewById(R.id.editTargetAmount);

        editName.setText(target.getName());
        editTarget.setText(String.valueOf(target.getTargetAmount()).replace(".0", ""));

        builder.setView(view);
        builder.setPositiveButton("Update", (d, w) -> {
            String name = editName.getText().toString();
            String amountStr = editTarget.getText().toString();
            if (!name.isEmpty() && !amountStr.isEmpty()) {
                dbHelper.updateSavingsTargetDetails(target.getId(), name, Double.parseDouble(amountStr));
                refreshData();
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void showDeleteConfirm(SavingsTarget target) {
        new AlertDialog.Builder(context)
                .setMessage("Hapus target tabungan ini?")
                .setPositiveButton("Hapus", (d, w) -> {
                    dbHelper.deleteSavingsTarget(target.getId());
                    refreshData();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void refreshData() {
        Intent intent = new Intent("REFRESH_SAVINGS");
        context.sendBroadcast(intent);
    }

    @Override
    public int getItemCount() {
        return savingsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textProgress;
        ProgressBar progressBar;
        ImageButton btnAddAmount;

        public ViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textTargetName);
            textProgress = itemView.findViewById(R.id.textProgress);
            progressBar = itemView.findViewById(R.id.progressBarTarget);
            btnAddAmount = itemView.findViewById(R.id.btnAddAmount);
        }
    }
}