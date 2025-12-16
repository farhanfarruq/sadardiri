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
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.adapter.SavingsAdapter;
import com.example.sadardiri.data.FirestoreSavingsRepository;
import com.example.sadardiri.model.SavingsTarget;
import com.example.sadardiri.ui.AddSavingsTargetActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class SavingsFragment extends Fragment {

    private RecyclerView recyclerSavings;
    private View layoutEmptySavings;
    private View btnAddSaving; // FloatingActionButton di XML, bisa pakai View/FloatingActionButton

    // textSummarySavings tidak ada di XML, logika dihapus

    private SavingsAdapter savingsAdapter;
    private final List<SavingsTarget> savingsList = new ArrayList<>();
    private FirestoreSavingsRepository savingsRepo;

    private BroadcastReceiver refreshReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_savings, container, false);

        recyclerSavings = view.findViewById(R.id.recyclerSavings);
        // ID di XML adalah textEmptySavings (LinearLayout)
        layoutEmptySavings = view.findViewById(R.id.textEmptySavings);
        // ID di XML adalah btnAddSavings
        btnAddSaving = view.findViewById(R.id.btnAddSavings);

        savingsRepo = new FirestoreSavingsRepository();

        recyclerSavings.setLayoutManager(new LinearLayoutManager(requireContext()));
        savingsAdapter = new SavingsAdapter(savingsList);
        recyclerSavings.setAdapter(savingsAdapter);

        Animation fallDown = AnimationUtils.loadAnimation(requireContext(), R.anim.item_fall_down);
        LayoutAnimationController controller = new LayoutAnimationController(fallDown);
        recyclerSavings.setLayoutAnimation(controller);

        btnAddSaving.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddSavingsTargetActivity.class))
        );

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("REFRESH_SAVINGS".equals(intent.getAction())) {
                    loadSavings();
                }
            }
        };
        IntentFilter filter = new IntentFilter("REFRESH_SAVINGS");
        requireActivity().registerReceiver(refreshReceiver, filter);

        loadSavings();

        return view;
    }

    private void loadSavings() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            layoutEmptySavings.setVisibility(View.VISIBLE);
            return;
        }

        savingsRepo.getAll()
                .addOnSuccessListener(list -> {
                    savingsList.clear();
                    savingsList.addAll(list);
                    savingsAdapter.notifyDataSetChanged();

                    if (savingsList.isEmpty()) {
                        layoutEmptySavings.setVisibility(View.VISIBLE);
                    } else {
                        layoutEmptySavings.setVisibility(View.GONE);
                    }

                    recyclerSavings.scheduleLayoutAnimation();
                })
                .addOnFailureListener(e -> {
                    layoutEmptySavings.setVisibility(View.VISIBLE);
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavings();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            requireActivity().unregisterReceiver(refreshReceiver);
        } catch (Exception ignored) {}
    }
}