package com.example.sadardiri;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SavingsAdapter extends RecyclerView.Adapter<SavingsAdapter.ViewHolder> {

    private List<SavingsTarget> savingsList;

    public SavingsAdapter(List<SavingsTarget> savingsList) {
        this.savingsList = savingsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_savings, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SavingsTarget target = savingsList.get(position);
        holder.textName.setText(target.getName());
        holder.textProgress.setText(target.getProgressText());
        holder.progressBar.setProgress(target.getProgress());
    }

    @Override
    public int getItemCount() {
        return savingsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textProgress;
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textTargetName);
            textProgress = itemView.findViewById(R.id.textProgress);
            progressBar = itemView.findViewById(R.id.progressBarTarget);
        }
    }
}