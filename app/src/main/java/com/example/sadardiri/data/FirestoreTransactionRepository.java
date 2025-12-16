package com.example.sadardiri.data;

import com.example.sadardiri.model.Transaction;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Collections;
import java.util.List;

public class FirestoreTransactionRepository {

    private final CollectionReference transactionsRef;
    private final FirebaseAuth auth;

    public FirestoreTransactionRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        transactionsRef = db.collection("transactions");
        auth = FirebaseAuth.getInstance();
    }

    public Task<String> add(Transaction transaction) {
        if (auth.getCurrentUser() == null) return Tasks.forException(new Exception("Belum login"));

        String uid = auth.getCurrentUser().getUid();
        transaction.setUserId(uid);

        return transactionsRef.add(transaction)
                .continueWith(task -> task.getResult().getId());
    }

    public Task<List<Transaction>> getAll() {
        if (auth.getCurrentUser() == null) return Tasks.forResult(Collections.emptyList());

        String uid = auth.getCurrentUser().getUid();

        return transactionsRef
                .whereEqualTo("userId", uid)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(Transaction.class);
                    } else {
                        throw task.getException();
                    }
                });
    }

    public Task<Void> update(String id, Transaction t) {
        return transactionsRef.document(id).set(t);
    }

    public Task<Void> delete(String id) {
        return transactionsRef.document(id).delete();
    }
}