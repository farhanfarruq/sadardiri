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
// import android.widget.TextView; // HAPUS INI: Tidak dipakai lagi karena diganti View/LinearLayout

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.adapter.TransactionAdapter;
import com.example.sadardiri.data.FirestoreTransactionRepository;
import com.example.sadardiri.model.Transaction;
import com.example.sadardiri.ui.AddTransactionActivity;
import com.example.sadardiri.ui.CategoryManagerActivity;
// PENTING: Import FloatingActionButton agar tidak error ClassCastException
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class FinanceFragment extends Fragment {

    private RecyclerView recyclerTransactions;

    // PERBAIKAN 1: Ubah TextView menjadi View (karena di XML ini adalah LinearLayout)
    private View layoutEmpty;

    // PERBAIKAN 2: Ubah Button menjadi FloatingActionButton (Penyebab Crash Utama)
    private FloatingActionButton btnAddTransaction;

    private Button btnManageCategory; // Tombol baru untuk Kelola Kategori

    private TransactionAdapter transactionAdapter;
    private final List<Transaction> transactionList = new ArrayList<>();

    private FirestoreTransactionRepository firestoreRepo;
    private BroadcastReceiver refreshReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_finance, container, false);

        recyclerTransactions = view.findViewById(R.id.recyclerTransactions);

        // PERBAIKAN 1: ID disesuaikan dengan XML (textEmptyFinance)
        layoutEmpty = view.findViewById(R.id.textEmptyFinance);

        // PERBAIKAN 2: Casting ke tipe yang benar (FloatingActionButton)
        btnAddTransaction = view.findViewById(R.id.btnAddTransaction);

        // Inisialisasi tombol Kelola Kategori
        btnManageCategory = view.findViewById(R.id.btnManageCategory);

        recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        transactionAdapter = new TransactionAdapter(requireContext(), transactionList);
        recyclerTransactions.setAdapter(transactionAdapter);

        firestoreRepo = new FirestoreTransactionRepository();

        Animation fallDown = AnimationUtils.loadAnimation(requireContext(), R.anim.item_fall_down);
        LayoutAnimationController controller = new LayoutAnimationController(fallDown);
        recyclerTransactions.setLayoutAnimation(controller);

        // Listener untuk tombol tambah transaksi
        btnAddTransaction.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddTransactionActivity.class))
        );

        // Listener untuk tombol kelola kategori
        btnManageCategory.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CategoryManagerActivity.class))
        );

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("REFRESH_FINANCE".equals(intent.getAction())) {
                    loadTransactions();
                }
            }
        };
        IntentFilter filter = new IntentFilter("REFRESH_FINANCE");
        requireActivity().registerReceiver(refreshReceiver, filter);

        loadTransactions();
        return view;
    }

    private void loadTransactions() {
        // Gunakan layoutEmpty (View), bukan textEmpty (TextView)
        layoutEmpty.setVisibility(View.GONE);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        firestoreRepo.getAll()
                .addOnSuccessListener(list -> {
                    transactionAdapter.setData(list);
                    if (list.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                    }
                    recyclerTransactions.scheduleLayoutAnimation();
                })
                .addOnFailureListener(e -> {
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactions();
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