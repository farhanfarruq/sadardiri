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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

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
        textEmpty = view.findViewById(R.id.textEmptySavings);
        btnAddSavings = view.findViewById(R.id.btnAddSavings);

        Animation scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.item_fall_down);
        btnAddSavings.startAnimation(scaleUp);

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
        androidx.core.content.ContextCompat.registerReceiver(
                requireActivity(),
                refreshReceiver,
                new IntentFilter("REFRESH_SAVINGS"),
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        );

        return view;
    }

    // --- PERBAIKAN ANIMASI ---
    private void runLayoutAnimation(RecyclerView recyclerView) {
        if (recyclerView == null || recyclerView.getAdapter() == null) return;

        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
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
        runLayoutAnimation(recyclerSavings);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavings();
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