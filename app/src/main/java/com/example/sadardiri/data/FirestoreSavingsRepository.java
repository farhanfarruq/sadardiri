package com.example.sadardiri.data;

import com.example.sadardiri.model.SavingsTarget;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FirestoreSavingsRepository {

    private final CollectionReference savingsRef;
    private final FirebaseAuth auth;

    public FirestoreSavingsRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        savingsRef = db.collection("savings_targets");
        auth = FirebaseAuth.getInstance();
    }

    public Task<String> add(SavingsTarget s) {
        if (auth.getCurrentUser() == null) return Tasks.forException(new Exception("Belum login"));

        String uid = auth.getCurrentUser().getUid();
        s.setUserId(uid);

        return savingsRef.add(s).continueWith(task -> task.getResult().getId());
    }

    public Task<List<SavingsTarget>> getAll() {
        if (auth.getCurrentUser() == null) return Tasks.forResult(Collections.emptyList());

        String uid = auth.getCurrentUser().getUid();
        return savingsRef.whereEqualTo("userId", uid).get()
                .continueWith(task -> {
                    if (task.isSuccessful()) return task.getResult().toObjects(SavingsTarget.class);
                    else throw task.getException();
                });
    }

    public Task<Void> updateCurrentAmount(String id, double currentAmount) {
        return savingsRef.document(id).update("currentAmount", currentAmount);
    }

    // FITUR BARU: Update Target Lengkap
    public Task<Void> updateTarget(String id, String name, double targetAmount) {
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("name", name);
        updates.put("targetAmount", targetAmount);
        return savingsRef.document(id).update(updates);
    }

    public Task<Void> delete(String id) {
        return savingsRef.document(id).delete();
    }
}