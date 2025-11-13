// ReportsFragment.java
package com.example.sadardiri;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class ReportsFragment extends Fragment {

    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        TextView textPrediction = view.findViewById(R.id.textPrediction);

        if (textPrediction != null) {
            double prediction = dbHelper.predictMonthlyExpense();
            textPrediction.setText("Prediksi Bulanan: Rp " + String.format("%,.0f", prediction));
        }
        return view;
    }
}