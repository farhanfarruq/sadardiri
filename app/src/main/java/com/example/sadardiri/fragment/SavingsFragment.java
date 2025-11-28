package com.example.sadardiri.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View; // Pastikan import ini ada
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SavingsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerSavings;

    // PERBAIKAN 2: Ubah dari TextView menjadi View
    private View textEmpty;

    private List<SavingsTarget> savingsList;
    private SavingsAdapter adapter;
    private BroadcastReceiver refreshReceiver;
    private FloatingActionButton btnAddSavings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_savings, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerSavings = view.findViewById(R.id.recyclerSavings);

        // Inisialisasi View Empty State (LinearLayout di XML)
        textEmpty = view.findViewById(R.id.textEmptySavings);

        btnAddSavings = view.findViewById(R.id.btnAddSavings);

        btnAddSavings.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddSavingsTargetActivity.class)));
        recyclerSavings.setLayoutManager(new LinearLayoutManager(requireContext()));
        savingsList = new ArrayList<>();
        adapter = new SavingsAdapter(savingsList);
        recyclerSavings.setAdapter(adapter);


        loadSavings();

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadSavings();
            }
        };
        // Perbaikan: Menambahkan flag untuk registerReceiver (untuk Android terbaru)
        androidx.core.content.ContextCompat.registerReceiver(
                requireActivity(),
                refreshReceiver,
                new IntentFilter("REFRESH_SAVINGS"),
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        );

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

        // Logika Empty State
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