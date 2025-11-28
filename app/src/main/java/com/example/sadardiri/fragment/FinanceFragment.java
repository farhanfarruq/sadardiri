package com.example.sadardiri.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View; // Pastikan ada import ini
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadardiri.R;
import com.example.sadardiri.model.Transaction;
import com.example.sadardiri.adapter.TransactionAdapter;
import com.example.sadardiri.database.DatabaseHelper;
import com.example.sadardiri.ui.AddTransactionActivity;
import com.example.sadardiri.ui.CategoryManagerActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Tambahkan import ini

import java.util.ArrayList;
import java.util.List;

public class FinanceFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerTransactions;

    // PERBAIKAN 1: Ubah tipe data dari TextView menjadi View atau LinearLayout
    private View textEmpty;

    private TransactionAdapter transactionAdapter;
    private BroadcastReceiver refreshReceiver;
    private FloatingActionButton btnAddTransaction;
    private Button btnManageCategory;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finance, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerTransactions = view.findViewById(R.id.recyclerTransactions);

        // Inisialisasi View Empty State
        textEmpty = view.findViewById(R.id.textEmptyFinance);

        btnAddTransaction = view.findViewById(R.id.btnAddTransaction);
        btnManageCategory = view.findViewById(R.id.btnManageCategory);

        btnAddTransaction.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddTransactionActivity.class));
        });

        btnManageCategory.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CategoryManagerActivity.class));
        });

        recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));

        transactionAdapter = new TransactionAdapter(requireContext(), new ArrayList<>());
        recyclerTransactions.setAdapter(transactionAdapter);

        loadTransactions();

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadTransactions();
            }
        };
        // Gunakan RECEIVER_EXPORTED atau NOT_EXPORTED sesuai target SDK (biasanya NOT_EXPORTED aman)
        ContextCompat.registerReceiver(requireActivity(), refreshReceiver, new IntentFilter("REFRESH_FINANCE"), ContextCompat.RECEIVER_NOT_EXPORTED);

        return view;
    }

    private void loadTransactions() {
        Cursor cursor = dbHelper.getAllTransactions();
        List<Transaction> transactionList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category_name"));

                transactionList.add(new Transaction(id, amount, type, note, date, category));
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        if (transactionAdapter != null) {
            transactionAdapter.setData(transactionList);
        }

        // Logika Empty State
        if (transactionList.isEmpty()) {
            textEmpty.setVisibility(View.VISIBLE);
            recyclerTransactions.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerTransactions.setVisibility(View.VISIBLE);
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