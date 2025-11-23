package com.example.sadardiri.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.example.sadardiri.R;
import com.example.sadardiri.database.DatabaseHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.Calendar;

public class ReportsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private PieChart pieChart;
    private TextView textPrediction;
    private Button btnPickMonth;
    private String currentMonth = "2025-11";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        pieChart = view.findViewById(R.id.pieChartReports);
        textPrediction = view.findViewById(R.id.textPrediction);
        btnPickMonth = view.findViewById(R.id.btnPickMonth);

        Calendar c = Calendar.getInstance();
        currentMonth = String.format("%d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
        btnPickMonth.setText(currentMonth);

        btnPickMonth.setOnClickListener(v -> showMonthPicker());
        loadReport();

        return view;
    }

    private void showMonthPicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            currentMonth = year + "-" + String.format("%02d", month + 1);
            btnPickMonth.setText(currentMonth);
            loadReport();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1).show();
    }

    private void loadReport() {
        double expense = dbHelper.getTotalExpenseByMonth(currentMonth);
        double prediction = dbHelper.predictMonthlyExpense();
        textPrediction.setText("Prediksi bulan depan: Rp " + String.format("%,.0f", prediction));

        ArrayList<PieEntry> entries = new ArrayList<>();
        android.database.Cursor cursor = dbHelper.getExpenseByCategory(currentMonth);
        if (cursor.moveToFirst()) {
            do {
                String cat = cursor.getString(0);
                float amount = cursor.getFloat(1);
                entries.add(new PieEntry(amount, cat));
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (entries.isEmpty()) {
            pieChart.setNoDataText("Tidak ada pengeluaran di bulan ini");
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Pengeluaran");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Pengeluaran " + currentMonth);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}