package com.example.sadardiri.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.sadardiri.R;
import com.example.sadardiri.data.FirestoreTransactionRepository;
import com.example.sadardiri.model.Transaction;
import com.example.sadardiri.ui.LoginActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsFragment extends Fragment {

    private PieChart pieChart;
    private View layoutEmptyReport;
    private TextView textPrediction;
    private Button btnPickMonth;
    private ImageView btnSettings;
    private MaterialButtonToggleGroup toggleChartType;

    private String currentMonth;
    private boolean isExpenseMode = true;

    private FirestoreTransactionRepository firestoreRepo;
    private BroadcastReceiver refreshReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        firestoreRepo = new FirestoreTransactionRepository();

        pieChart = view.findViewById(R.id.pieChartReports);
        layoutEmptyReport = view.findViewById(R.id.layoutEmptyReport);
        textPrediction = view.findViewById(R.id.textPrediction);
        btnPickMonth = view.findViewById(R.id.btnPickMonth);
        btnSettings = view.findViewById(R.id.btnSettings);
        toggleChartType = view.findViewById(R.id.toggleChartType);

        Calendar c = Calendar.getInstance();
        currentMonth = String.format(Locale.getDefault(), "%04d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
        btnPickMonth.setText(currentMonth);

        btnPickMonth.setOnClickListener(v -> showMonthPicker());

        // Tombol Pengaturan
        btnSettings.setOnClickListener(v -> showSettingsDialog());

        toggleChartType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isExpenseMode = (checkedId == R.id.btnChartExpense);
                loadReport();
            }
        });

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("REFRESH_FINANCE".equals(intent.getAction())) loadReport();
            }
        };

        IntentFilter filter = new IntentFilter("REFRESH_FINANCE");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(refreshReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireActivity().registerReceiver(refreshReceiver, filter);
        }

        loadReport();
        return view;
    }

    private void showMonthPicker() {
        Calendar c = Calendar.getInstance();
        try {
            String[] parts = currentMonth.split("-");
            c.set(Calendar.YEAR, Integer.parseInt(parts[0]));
            c.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
        } catch (Exception ignored) {}

        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            currentMonth = String.format(Locale.getDefault(), "%04d-%02d", year, month + 1);
            btnPickMonth.setText(currentMonth);
            loadReport();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // === PERBAIKAN UTAMA DI SINI (SETTINGS DIALOG) ===
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_settings, null);

        View optionTheme = view.findViewById(R.id.optionTheme);
        SwitchMaterial switchTheme = view.findViewById(R.id.switchTheme);
        View optionInfo = view.findViewById(R.id.optionInfo);
        View optionLogout = view.findViewById(R.id.optionLogout);

        SharedPreferences prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        boolean isNightMode = prefs.getBoolean("night_mode", false);
        switchTheme.setChecked(isNightMode);

        // Buat dialog object dulu agar bisa di-dismiss di dalam listener
        AlertDialog dialog = builder.setView(view).create();

        // 1. LOGIKA GANTI TEMA YANG MULUS (TANPA LAG)
        View.OnClickListener themeListener = v -> {
            boolean newState = !switchTheme.isChecked();
            if (v == switchTheme) newState = switchTheme.isChecked(); // Kalau klik switch langsung
            else switchTheme.setChecked(newState); // Kalau klik barisnya

            // Simpan preferensi
            prefs.edit().putBoolean("night_mode", newState).apply();

            // Set Mode Malam
            int mode = newState ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            AppCompatDelegate.setDefaultNightMode(mode);

            // PENTING: Tutup dialog dulu biar animasi switch tidak patah-patah
            dialog.dismiss();

            // Beri jeda 200ms agar dialog tertutup sempurna baru refresh halaman
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (getActivity() != null) {
                    getActivity().recreate();
                }
            }, 200);
        };

        optionTheme.setOnClickListener(themeListener);
        switchTheme.setOnClickListener(themeListener);

        // 2. TENTANG APLIKASI
        optionInfo.setOnClickListener(v -> {
            try {
                PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Tentang Sadar Diri")
                        .setMessage("Aplikasi Manajemen Keuangan\nVersi: " + pInfo.versionName + "\n\nDibuat oleh: Farhan Miftakhul Farruq")
                        .setPositiveButton("Tutup", null)
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 3. LOGOUT
        optionLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Keluar Akun")
                    .setMessage("Apakah Anda yakin ingin keluar?")
                    .setPositiveButton("Ya", (d, w) -> {
                        performLogout();
                        dialog.dismiss();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        dialog.show();
    }

    private void performLogout() {
        Context context = requireContext();
        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignIn.getClient(context, gso).signOut().addOnCompleteListener(task -> {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadReport() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        firestoreRepo.getAll().addOnSuccessListener(this::processChartData)
                .addOnFailureListener(e -> {
                    layoutEmptyReport.setVisibility(View.VISIBLE);
                    pieChart.setVisibility(View.GONE);
                });
    }

    private void processChartData(List<Transaction> list) {
        Map<String, Double> categoryTotals = new HashMap<>();
        double totalCurrentMonth = 0;

        for (Transaction t : list) {
            if (t.getDate().startsWith(currentMonth)) {
                boolean isExpense = "expense".equalsIgnoreCase(t.getType());
                if ((isExpenseMode && isExpense) || (!isExpenseMode && !isExpense)) {
                    String cat = t.getCategory();
                    if (cat == null || cat.isEmpty()) cat = "Lainnya";
                    categoryTotals.put(cat, categoryTotals.getOrDefault(cat, 0.0) + t.getAmount());
                    totalCurrentMonth += t.getAmount();
                }
            }
        }

        if (categoryTotals.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            layoutEmptyReport.setVisibility(View.VISIBLE);
            textPrediction.setText(isExpenseMode ? "Prediksi Pengeluaran: Rp 0" : "Prediksi Pemasukan: Rp 0");
            return;
        }

        pieChart.setVisibility(View.VISIBLE);
        layoutEmptyReport.setVisibility(View.GONE);

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

        // Ukuran font chart diperkecil
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText((isExpenseMode ? "Pengeluaran\n" : "Pemasukan\n") + currentMonth);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);

        // Label kategori diperkecil
        pieChart.setEntryLabelTextSize(10f);
        pieChart.setEntryLabelColor(Color.WHITE);

        pieChart.animateY(800);
        pieChart.invalidate();

        Calendar c = Calendar.getInstance();
        String thisMonth = String.format(Locale.getDefault(), "%04d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
        double prediction = totalCurrentMonth;

        if (currentMonth.equals(thisMonth)) {
            int maxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            int currentDay = c.get(Calendar.DAY_OF_MONTH);
            if (currentDay > 0) {
                prediction = (totalCurrentMonth / currentDay) * maxDays;
            }
        }

        textPrediction.setText((isExpenseMode ? "Prediksi Pengeluaran: Rp " : "Prediksi Pemasukan: Rp ") +
                String.format(Locale.getDefault(), "%,.0f", prediction));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (refreshReceiver != null) {
            try { requireActivity().unregisterReceiver(refreshReceiver); } catch (Exception ignored) {}
        }
    }
}