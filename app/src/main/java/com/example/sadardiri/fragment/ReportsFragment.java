package com.example.sadardiri.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.sadardiri.R;
import com.example.sadardiri.database.DatabaseHelper;
import com.example.sadardiri.ui.MainActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Calendar;

public class ReportsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private PieChart pieChart;
    private View layoutEmptyReport;
    private TextView textPrediction;
    private Button btnPickMonth;
    private ImageView btnSettings;
    private MaterialButtonToggleGroup toggleChartType;

    private String currentMonth;
    private boolean isExpenseMode = true; // Default Pengeluaran

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        pieChart = view.findViewById(R.id.pieChartReports);
        layoutEmptyReport = view.findViewById(R.id.layoutEmptyReport);
        textPrediction = view.findViewById(R.id.textPrediction);
        btnPickMonth = view.findViewById(R.id.btnPickMonth);
        btnSettings = view.findViewById(R.id.btnSettings);
        toggleChartType = view.findViewById(R.id.toggleChartType);

        // Set Bulan Default ke Bulan Ini
        Calendar c = Calendar.getInstance();
        currentMonth = String.format("%d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
        btnPickMonth.setText(currentMonth);

        // Listener Tombol
        btnPickMonth.setOnClickListener(v -> showMonthPicker());
        btnSettings.setOnClickListener(v -> showSettingsDialog());

        // Listener Toggle (Pemasukan / Pengeluaran)
        toggleChartType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnChartExpense) isExpenseMode = true;
                else if (checkedId == R.id.btnChartIncome) isExpenseMode = false;
                loadReport();
            }
        });

        loadReport();
        return view;
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_settings, null);

        View optionTheme = view.findViewById(R.id.optionTheme);
        SwitchMaterial switchTheme = view.findViewById(R.id.switchTheme);
        View optionInfo = view.findViewById(R.id.optionInfo);

        // 1. Baca Settingan Tema
        SharedPreferences prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        boolean isNightMode = prefs.getBoolean("night_mode", false);
        switchTheme.setChecked(isNightMode);

        // Logic Switch
        optionTheme.setOnClickListener(v -> switchTheme.toggle());
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 2. Simpan Pilihan User
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("night_mode", isChecked);
            editor.apply();

            // 3. Terapkan Tema & Restart
            if (isChecked) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            startActivity(new Intent(requireContext(), MainActivity.class));
            requireActivity().finish();
        });

        // Logic Info
        optionInfo.setOnClickListener(v -> {
            try {
                PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Tentang SadarDiri")
                        .setMessage("Versi: " + pInfo.versionName + "\n\nBuild with love.")
                        .setPositiveButton("Tutup", null)
                        .show();
            } catch (Exception e) {}
        });

        builder.setView(view);
        builder.show();
    }

    private void showMonthPicker() {
        Calendar c = Calendar.getInstance();
        // Menggunakan tanggal 1 sebagai default tanggal picker
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            currentMonth = year + "-" + String.format("%02d", month + 1);
            btnPickMonth.setText(currentMonth);
            loadReport();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1).show();
    }

    private void loadReport() {
        // Prediksi Pengeluaran
        double prediction = dbHelper.predictMonthlyExpense();
        textPrediction.setText("Prediksi Pengeluaran: Rp " + String.format("%,.0f", prediction));

        // Setup Data Chart
        ArrayList<PieEntry> entries = new ArrayList<>();
        android.database.Cursor cursor;

        if (isExpenseMode) {
            cursor = dbHelper.getExpenseByCategory(currentMonth);
            pieChart.setCenterText("Pengeluaran\n" + currentMonth);
        } else {
            cursor = dbHelper.getIncomeByCategory(currentMonth);
            pieChart.setCenterText("Pemasukan\n" + currentMonth);
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String cat = cursor.getString(0);
                float amount = cursor.getFloat(1);
                entries.add(new PieEntry(amount, cat));
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Cek Kosong
        if (entries.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            layoutEmptyReport.setVisibility(View.VISIBLE);
            return;
        }

        pieChart.setVisibility(View.VISIBLE);
        layoutEmptyReport.setVisibility(View.GONE);

        // Styling Chart
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE); // Agar kontras

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterTextSize(14f);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);

        // ANIMASI: Durasi 1000ms (Cukup cepat & Smooth)
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReport();
    }
}