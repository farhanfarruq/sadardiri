package com.example.sadardiri.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreUserScope {

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public FirestoreUserScope() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public String getUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public CollectionReference userCollection(String subCollection) {
        String uid = getUid();
        if (uid == null) return null;
        return firestore
                .collection("users")
                .document(uid)
                .collection(subCollection);
    }
}
