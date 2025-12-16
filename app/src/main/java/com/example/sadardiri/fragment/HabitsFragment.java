package com.example.sadardiri.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.adapter.HabitAdapter;
import com.example.sadardiri.data.FirestoreHabitRepository;
import com.example.sadardiri.model.Habit;
import com.example.sadardiri.ui.AddHabitActivity;
// PENTING: Import ini harus ada!
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HabitsFragment extends Fragment {

    private RecyclerView recyclerHabits;
    private TextView textHabitScore;
    private View layoutEmptyHabits; // Ubah ke View/LinearLayout
    private FloatingActionButton btnAddHabit; // PERBAIKAN: Ubah ke FloatingActionButton

    private HabitAdapter habitAdapter;
    private final List<Habit> habitList = new ArrayList<>();
    private FirestoreHabitRepository habitRepo;

    private BroadcastReceiver refreshReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_habits, container, false);

        recyclerHabits = view.findViewById(R.id.recyclerHabits);
        textHabitScore = view.findViewById(R.id.textHabitScore);

        // PERBAIKAN: Casting ke tipe yang benar
        btnAddHabit = view.findViewById(R.id.btnAddHabit);

        // Sesuaikan ID dengan XML
        layoutEmptyHabits = view.findViewById(R.id.layoutEmptyHabits);

        habitRepo = new FirestoreHabitRepository();

        recyclerHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        habitAdapter = new HabitAdapter(habitList);
        recyclerHabits.setAdapter(habitAdapter);

        Animation fallDown = AnimationUtils.loadAnimation(requireContext(), R.anim.item_fall_down);
        LayoutAnimationController controller = new LayoutAnimationController(fallDown);
        recyclerHabits.setLayoutAnimation(controller);

        btnAddHabit.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddHabitActivity.class))
        );

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("REFRESH_HABITS".equals(intent.getAction())) {
                    loadHabits();
                }
            }
        };
        // Nanti kita perbaiki warning receiver di sini (lihat poin 2 di bawah)
        IntentFilter filter = new IntentFilter("REFRESH_HABITS");
        requireActivity().registerReceiver(refreshReceiver, filter);

        loadHabits();

        return view;
    }

    private void loadHabits() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            layoutEmptyHabits.setVisibility(View.VISIBLE);
            textHabitScore.setText("Skor Hari Ini: -");
            return;
        }

        habitRepo.getAll()
                .addOnSuccessListener(list -> {
                    habitList.clear();
                    habitList.addAll(list);
                    habitAdapter.notifyDataSetChanged();

                    if (habitList.isEmpty()) {
                        layoutEmptyHabits.setVisibility(View.VISIBLE);
                        textHabitScore.setText("Skor Hari Ini: -");
                    } else {
                        layoutEmptyHabits.setVisibility(View.GONE);
                        updateHabitScore();
                    }

                    recyclerHabits.scheduleLayoutAnimation();
                })
                .addOnFailureListener(e -> {
                    layoutEmptyHabits.setVisibility(View.VISIBLE);
                    textHabitScore.setText("Gagal memuat");
                });
    }

    private void updateHabitScore() {
        int total = habitList.size();
        int completed = 0;
        for (Habit h : habitList) {
            if (h.isDone()) completed++;
        }
        int score = total > 0 ? (completed * 100 / total) : 0;
        textHabitScore.setText("Skor Hari Ini: " + score + "%");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHabits();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            requireActivity().unregisterReceiver(refreshReceiver);
        } catch (Exception ignored) {}
    }
}