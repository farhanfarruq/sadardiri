package com.example.sadardiri.data;

import com.example.sadardiri.model.Habit;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;

public class FirestoreHabitRepository {

    private final CollectionReference habitsRef;
    private final FirebaseAuth auth;

    public FirestoreHabitRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        habitsRef = db.collection("habits");
        auth = FirebaseAuth.getInstance();
    }

    public Task<String> add(Habit h) {
        if (auth.getCurrentUser() == null) return Tasks.forException(new Exception("Belum login"));

        String uid = auth.getCurrentUser().getUid();
        h.setUserId(uid);

        return habitsRef.add(h).continueWith(task -> task.getResult().getId());
    }

    public Task<List<Habit>> getAll() {
        if (auth.getCurrentUser() == null) return Tasks.forResult(Collections.emptyList());

        String uid = auth.getCurrentUser().getUid();
        return habitsRef.whereEqualTo("userId", uid).get()
                .continueWith(task -> {
                    if (task.isSuccessful()) return task.getResult().toObjects(Habit.class);
                    else throw task.getException();
                });
    }

    public Task<Void> setDone(String habitId, boolean done) {
        return habitsRef.document(habitId).update("done", done);
    }

    // FITUR BARU: Update Nama Habit
    public Task<Void> updateName(String id, String newName) {
        return habitsRef.document(id).update("name", newName);
    }

    public Task<Void> delete(String habitId) {
        return habitsRef.document(habitId).delete();
    }
}