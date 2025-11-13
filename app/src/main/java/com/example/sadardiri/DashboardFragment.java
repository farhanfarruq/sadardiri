package com.example.sadardiri;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class DashboardFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private PieChart pieChart;
    private TextView textIncome, textExpense, textHabitScore;
    private Button btnLogHabit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        pieChart = view.findViewById(R.id.pieChart);
        textIncome = view.findViewById(R.id.textIncome);
        textExpense = view.findViewById(R.id.textExpense);
        textHabitScore = view.findViewById(R.id.textHabitScore);
        btnLogHabit = view.findViewById(R.id.btnLogHabit);

        btnLogHabit.setOnClickListener(v -> startActivity(new Intent(requireContext(), HabitsFragment.class)));

        loadSummary();
        loadChart();
        updateHabitScore();

        requireActivity().registerReceiver(refreshReceiver, new IntentFilter("REFRESH_FINANCE"));

        return view;
    }

    private void loadSummary() {
        double income = dbHelper.getTotalIncomeThisMonth();
        double expense = dbHelper.getTotalExpenseThisMonth();
        textIncome.setText("Pemasukan\nRp " + String.format("%,.0f", income));
        textExpense.setText("Pengeluaran\nRp " + String.format("%,.0f", expense));
    }

    private void loadChart() {
        double income = dbHelper.getTotalIncomeThisMonth();
        double expense = dbHelper.getTotalExpenseThisMonth();

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (income > 0) entries.add(new PieEntry((float) income, "Pemasukan"));
        if (expense > 0) entries.add(new PieEntry((float) expense, "Pengeluaran"));

        if (entries.isEmpty()) {
            pieChart.setNoDataText("Belum ada transaksi bulan ini");
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.parseColor("#66BB6A"), Color.parseColor("#42A5F5"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Keuangan Bulan Ini");
        pieChart.setCenterTextSize(14f);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void updateHabitScore() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int completed = dbHelper.getCompletedHabitsToday(today);
        int total = dbHelper.getTotalHabits();
        int score = total > 0 ? (completed * 100 / total) : 0;
        textHabitScore.setText("Skor Kebiasaan: " + score + "%");
    }

    private final BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadSummary();
            loadChart();
            updateHabitScore();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            requireActivity().unregisterReceiver(refreshReceiver);
        } catch (Exception ignored) {}
    }
}