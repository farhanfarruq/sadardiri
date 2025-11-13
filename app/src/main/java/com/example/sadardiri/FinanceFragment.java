package com.example.sadardiri;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FinanceFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerTransactions;
    private TransactionAdapter transactionAdapter;
    private BroadcastReceiver refreshReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finance, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerTransactions = view.findViewById(R.id.recyclerTransactions);

        if (recyclerTransactions == null) return view;

        recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadTransactions();

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadTransactions();
            }
        };
        requireActivity().registerReceiver(refreshReceiver, new IntentFilter("REFRESH_FINANCE"));

        return view;
    }

    private void loadTransactions() {
        Cursor cursor = dbHelper.getAllTransactions();
        if (cursor != null) {
            transactionAdapter = new TransactionAdapter(requireContext(), cursor);
            recyclerTransactions.setAdapter(transactionAdapter);
        }
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