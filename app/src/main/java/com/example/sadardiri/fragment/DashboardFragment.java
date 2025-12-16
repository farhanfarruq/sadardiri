package com.example.sadardiri.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.adapter.HabitDashboardAdapter;
import com.example.sadardiri.adapter.TransactionAdapter;
import com.example.sadardiri.data.FirestoreCategoryRepository;
import com.example.sadardiri.data.FirestoreHabitRepository;
import com.example.sadardiri.data.FirestoreSavingsRepository;
import com.example.sadardiri.data.FirestoreTransactionRepository;
import com.example.sadardiri.model.Habit;
import com.example.sadardiri.model.SavingsTarget;
import com.example.sadardiri.model.Transaction;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private TextView textBalance, textIncome, textExpense;
    private TextView textSavingsSummary, textHabitScore;

    private PieChart pieChart;
    private View layoutEmptyChart;

    private RecyclerView recyclerRecent, recyclerHabits;
    private TransactionAdapter recentAdapter;
    private HabitDashboardAdapter habitDashboardAdapter;

    private final List<Transaction> recentList = new ArrayList<>();
    private final List<Habit> habitList = new ArrayList<>();

    private FirestoreTransactionRepository transactionRepo;
    private FirestoreHabitRepository habitRepo;
    private FirestoreSavingsRepository savingsRepo;

    private BroadcastReceiver refreshReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        textIncome = view.findViewById(R.id.textIncome);
        textExpense = view.findViewById(R.id.textExpense);
        textSavingsSummary = view.findViewById(R.id.textSavingsSummary);
        textHabitScore = view.findViewById(R.id.textHabitScore);

        pieChart = view.findViewById(R.id.pieChart);
        layoutEmptyChart = view.findViewById(R.id.layoutEmptyChart);

        recyclerRecent = view.findViewById(R.id.recyclerTransactions);
        recyclerHabits = view.findViewById(R.id.recyclerHabits);

        transactionRepo = new FirestoreTransactionRepository();
        habitRepo = new FirestoreHabitRepository();
        savingsRepo = new FirestoreSavingsRepository();

        recyclerRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        recentAdapter = new TransactionAdapter(requireContext(), recentList);
        recyclerRecent.setAdapter(recentAdapter);

        recyclerHabits.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        habitDashboardAdapter = new HabitDashboardAdapter(habitList);
        recyclerHabits.setAdapter(habitDashboardAdapter);

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadAll();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("REFRESH_DASHBOARD");
        filter.addAction("REFRESH_FINANCE");
        filter.addAction("REFRESH_HABITS");
        filter.addAction("REFRESH_SAVINGS");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(refreshReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireActivity().registerReceiver(refreshReceiver, filter);
        }

        loadAll();
        return view;
    }

    private void loadAll() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            textBalance.setText("Rp 0");
            textIncome.setText("Rp 0");
            textExpense.setText("Rp 0");
            textSavingsSummary.setText("Silakan login");
            textHabitScore.setText("-");
            return;
        }

        loadTransactionsSummary();
        loadHabits();
        loadSavingsSummary();
    }

    private void loadTransactionsSummary() {
        transactionRepo.getAll().addOnSuccessListener(list -> {
            double income = 0;
            double expense = 0;

            for (Transaction t : list) {
                if ("income".equalsIgnoreCase(t.getType())) {
                    income += t.getAmount();
                } else {
                    expense += t.getAmount();
                }
            }

            double balance = income - expense;

            textIncome.setText(String.format("Rp %,.0f", income));
            textExpense.setText(String.format("Rp %,.0f", expense));
            if(textBalance != null) textBalance.setText(String.format("Rp %,.0f", balance));

            updateIncomeVsExpenseChart(income, expense);

            List<Transaction> sorted = new ArrayList<>(list);
            Collections.sort(sorted, (a, b) -> b.getDate().compareTo(a.getDate()));

            recentList.clear();
            int count = Math.min(3, sorted.size());
            for (int i = 0; i < count; i++) {
                recentList.add(sorted.get(i));
            }
            recentAdapter.notifyDataSetChanged();
        });
    }

    private void updateIncomeVsExpenseChart(double income, double expense) {
        if (income == 0 && expense == 0) {
            pieChart.setVisibility(View.GONE);
            layoutEmptyChart.setVisibility(View.VISIBLE);
            return;
        }

        pieChart.setVisibility(View.VISIBLE);
        layoutEmptyChart.setVisibility(View.GONE);

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (income > 0) entries.add(new PieEntry((float) income, "Pemasukan"));
        if (expense > 0) entries.add(new PieEntry((float) expense, "Pengeluaran"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#F44336")); // Hijau & Merah

        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Cash Flow");
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);

        pieChart.setEntryLabelTextSize(10f);
        pieChart.setEntryLabelColor(Color.WHITE);

        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void loadHabits() {
        habitRepo.getAll().addOnSuccessListener(list -> {
            habitList.clear();
            habitList.addAll(list);
            habitDashboardAdapter.notifyDataSetChanged();

            if (list.isEmpty()) {
                textHabitScore.setText("0%");
            } else {
                int total = list.size();
                int done = 0;
                for (Habit h : list) if (h.isDone()) done++;
                int score = done * 100 / total;
                textHabitScore.setText(score + "%");
            }
        });
    }

    private void loadSavingsSummary() {
        savingsRepo.getAll().addOnSuccessListener(list -> {
            double totalTarget = 0;
            double totalCurrent = 0;
            for (SavingsTarget s : list) {
                totalTarget += s.getTargetAmount();
                totalCurrent += s.getCurrentAmount();
            }
            textSavingsSummary.setText(String.format("Rp %,.0f / Rp %,.0f", totalCurrent, totalTarget));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAll();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (refreshReceiver != null) {
            try { requireActivity().unregisterReceiver(refreshReceiver); } catch (Exception ignored) {}
        }
    }
}