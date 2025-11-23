package com.example.sadardiri.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.model.SavingsTarget;
import com.example.sadardiri.adapter.SavingsAdapter;
import com.example.sadardiri.database.DatabaseHelper;
import com.example.sadardiri.ui.AddSavingsTargetActivity;

import java.util.ArrayList;
import java.util.List;

public class SavingsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerSavings;
    private TextView textEmpty;
    private List<SavingsTarget> savingsList;
    private SavingsAdapter adapter;
    private BroadcastReceiver refreshReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_savings, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerSavings = view.findViewById(R.id.recyclerSavings);
        textEmpty = view.findViewById(R.id.textEmptySavings);
        Button btnAdd = view.findViewById(R.id.btnAddSavings);

        recyclerSavings.setLayoutManager(new LinearLayoutManager(requireContext()));
        savingsList = new ArrayList<>();
        adapter = new SavingsAdapter(savingsList);
        recyclerSavings.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddSavingsTargetActivity.class)));

        loadSavings();

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadSavings();
            }
        };
        requireActivity().registerReceiver(refreshReceiver, new IntentFilter("REFRESH_SAVINGS"));

        return view;
    }

    private void loadSavings() {
        savingsList.clear();
        Cursor cursor = dbHelper.getAllSavingsTargets();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                double target = cursor.getDouble(cursor.getColumnIndexOrThrow("target_amount"));
                double current = cursor.getDouble(cursor.getColumnIndexOrThrow("current_amount"));
                savingsList.add(new SavingsTarget(id, name, target, current));
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (savingsList.isEmpty()) {
            textEmpty.setVisibility(View.VISIBLE);
            recyclerSavings.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerSavings.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (refreshReceiver != null) {
            try {
                requireActivity().unregisterReceiver(refreshReceiver);
            } catch (Exception ignored) {}
        }
    }
}