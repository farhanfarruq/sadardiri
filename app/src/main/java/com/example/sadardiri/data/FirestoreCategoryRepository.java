package com.example.sadardiri.data;

import com.example.sadardiri.model.Category;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;

public class FirestoreCategoryRepository {

    private final CollectionReference categoryRef;
    private final FirebaseAuth auth;

    public FirestoreCategoryRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        categoryRef = db.collection("categories");
        auth = FirebaseAuth.getInstance();
    }

    public Task<String> add(Category c) {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new Exception("User belum login"));
        }
        String uid = auth.getCurrentUser().getUid();
        c.setUserId(uid);

        return categoryRef.add(c)
                .continueWith(task -> task.getResult().getId());
    }

    public Task<List<Category>> getAll() {
        if (auth.getCurrentUser() == null) {
            return Tasks.forResult(Collections.emptyList());
        }
        String uid = auth.getCurrentUser().getUid();

        return categoryRef
                .whereEqualTo("userId", uid) // Filter
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(Category.class);
                    } else {
                        throw task.getException();
                    }
                });
    }

    public Task<Void> rename(String id, String newName) {
        return categoryRef.document(id).update("name", newName);
    }

    public Task<Void> delete(String id) {
        return categoryRef.document(id).delete();
    }
}