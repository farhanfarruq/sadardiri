package com.example.sadardiri.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.model.Habit;
import com.example.sadardiri.R;
import com.example.sadardiri.model.Transaction;
import com.example.sadardiri.adapter.HabitDashboardAdapter;
import com.example.sadardiri.adapter.TransactionAdapter;
import com.example.sadardiri.database.DatabaseHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private PieChart pieChart;
    private View layoutEmptyChart; // View Empty State
    private TextView textIncome, textExpense, textHabitScore;
    private RecyclerView recyclerTransactions, recyclerHabits;
    private TextView textSavingsSummary;
    private TransactionAdapter transactionAdapter;
    private HabitDashboardAdapter habitAdapter;
    private ArrayList<Habit> habitList;
    private BroadcastReceiver refreshReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        pieChart = view.findViewById(R.id.pieChart);
        layoutEmptyChart = view.findViewById(R.id.layoutEmptyChart); // Init View

        textIncome = view.findViewById(R.id.textIncome);
        textExpense = view.findViewById(R.id.textExpense);
        textHabitScore = view.findViewById(R.id.textHabitScore);
        recyclerTransactions = view.findViewById(R.id.recyclerTransactions);
        recyclerHabits = view.findViewById(R.id.recyclerHabits);
        textSavingsSummary = view.findViewById(R.id.textSavingsSummary);

        recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerHabits.setLayoutManager(new LinearLayoutManager(requireContext()));

        transactionAdapter = new TransactionAdapter(requireContext(), new ArrayList<>());
        recyclerTransactions.setAdapter(transactionAdapter);

        habitList = new ArrayList<>();
        habitAdapter = new HabitDashboardAdapter(habitList, dbHelper, this::updateHabitScore);
        recyclerHabits.setAdapter(habitAdapter);

        loadSummary();
        loadChart();
        loadRecentTransactions();
        loadHabits();
        updateSavingsSummary();

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadSummary();
                loadChart();
                loadRecentTransactions();
                loadHabits();
                updateSavingsSummary();
                updateHabitScore();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("REFRESH_FINANCE");
        filter.addAction("REFRESH_SAVINGS");
        filter.addAction("REFRESH_HABITS");
        ContextCompat.registerReceiver(requireActivity(), refreshReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        return view;
    }

    private void loadSummary() {
        double income = dbHelper.getTotalIncomeThisMonth();
        double expense = dbHelper.getTotalExpenseThisMonth();
        textIncome.setText("Rp " + String.format("%,.0f", income));
        textExpense.setText("Rp " + String.format("%,.0f", expense));
    }

    private void loadChart() {
        double income = dbHelper.getTotalIncomeThisMonth();
        double expense = dbHelper.getTotalExpenseThisMonth();

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (income > 0) entries.add(new PieEntry((float) income, "Pemasukan"));
        if (expense > 0) entries.add(new PieEntry((float) expense, "Pengeluaran"));

        if (entries.isEmpty()) {
            // Tampilkan Empty State
            pieChart.setVisibility(View.GONE);
            layoutEmptyChart.setVisibility(View.VISIBLE);
            return;
        }

        // Tampilkan Chart
        pieChart.setVisibility(View.VISIBLE);
        layoutEmptyChart.setVisibility(View.GONE);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.parseColor("#2E7D32"), Color.parseColor("#BA1A1A")); // Gunakan warna tema baru
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Bulan Ini");
        pieChart.setCenterTextSize(12f);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void loadRecentTransactions() {
        Cursor cursor = dbHelper.getAllTransactions();
        List<Transaction> list = new ArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int limit = Math.min(3, cursor.getCount());
            for (int i = 0; i < limit; i++) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category_name"));
                list.add(new Transaction(id, amount, type, note, date, category));
                cursor.moveToNext();
            }
        }
        if (cursor != null) cursor.close();
        if (transactionAdapter != null) {
            transactionAdapter.setData(list);
        }
    }

    private void loadHabits() {
        habitList.clear();
        Cursor cursor = dbHelper.getAllHabits();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                boolean done = dbHelper.isHabitDoneToday(id, today);
                habitList.add(new Habit(id, name, done));
            } while (cursor.moveToNext());
        }
        cursor.close();
        habitAdapter.notifyDataSetChanged();
        updateHabitScore();
    }

    private void updateHabitScore() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int completed = dbHelper.getCompletedHabitsToday(today);
        int total = dbHelper.getTotalHabits();
        int score = total > 0 ? (completed * 100 / total) : 0;
        textHabitScore.setText("Skor: " + score + "% (" + completed + "/" + total + ")");
    }

    private void updateSavingsSummary() {
        Cursor cursor = dbHelper.getAllSavingsTargets();
        double totalCurrent = 0, totalTarget = 0;
        if (cursor.moveToFirst()) {
            do {
                totalCurrent += cursor.getDouble(cursor.getColumnIndexOrThrow("current_amount"));
                totalTarget += cursor.getDouble(cursor.getColumnIndexOrThrow("target_amount"));
            } while (cursor.moveToNext());
        }
        cursor.close();
        textSavingsSummary.setText(
                "Tabungan: Rp " + String.format("%,.0f", totalCurrent) +
                        " / Rp " + String.format("%,.0f", totalTarget)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            requireActivity().unregisterReceiver(refreshReceiver);
        } catch (Exception ignored) {}
    }
}