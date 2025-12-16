package com.example.sadardiri.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {

    private static FirebaseFirestore getDb() {
        return FirebaseFirestore.getInstance();
    }

    private static String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private static CollectionReference userCollection(String name) {
        String uid = getUserId();
        if (uid == null) return null;
        return getDb()
                .collection("users")
                .document(uid)
                .collection(name);
    }

    // ===== TRANSACTIONS =====
    public static void syncAddOrUpdateTransaction(int id, double amount, String type, int categoryId, String note, String date) {
        CollectionReference col = userCollection("transactions");
        if (col == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("amount", amount);
        data.put("type", type);
        data.put("category_id", categoryId);
        data.put("note", note);
        data.put("date", date);

        col.document(String.valueOf(id)).set(data);
    }

    public static void syncDeleteTransaction(int id) {
        CollectionReference col = userCollection("transactions");
        if (col == null) return;
        col.document(String.valueOf(id)).delete();
    }

    // ===== SAVINGS TARGETS =====
    public static void syncAddOrUpdateSavings(int id, String name, double target, double current, String targetDate) {
        CollectionReference col = userCollection("savings_targets");
        if (col == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", name);
        data.put("target_amount", target);
        data.put("current_amount", current);
        data.put("target_date", targetDate);

        col.document(String.valueOf(id)).set(data);
    }

    public static void syncDeleteSavings(int id) {
        CollectionReference col = userCollection("savings_targets");
        if (col == null) return;
        col.document(String.valueOf(id)).delete();
    }

    // ===== HABITS =====
    public static void syncAddOrUpdateHabit(int id, String name, String frequency) {
        CollectionReference col = userCollection("habits");
        if (col == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", name);
        data.put("frequency", frequency);

        col.document(String.valueOf(id)).set(data);
    }

    public static void syncDeleteHabit(int id) {
        CollectionReference col = userCollection("habits");
        if (col == null) return;
        col.document(String.valueOf(id)).delete();
    }

    // ===== HABIT LOGS =====
    public static void syncAddHabitLog(int id, int habitId, String date) {
        CollectionReference col = userCollection("habit_logs");
        if (col == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("habit_id", habitId);
        data.put("date", date);

        col.document(String.valueOf(id)).set(data);
    }

    public static void syncDeleteHabitLog(int id) {
        CollectionReference col = userCollection("habit_logs");
        if (col == null) return;
        col.document(String.valueOf(id)).delete();
    }

    // ===== CATEGORIES (opsional) =====
    public static void syncAddOrUpdateCategory(int id, String name) {
        CollectionReference col = userCollection("categories");
        if (col == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", name);

        col.document(String.valueOf(id)).set(data);
    }

    public static void syncDeleteCategory(int id) {
        CollectionReference col = userCollection("categories");
        if (col == null) return;
        col.document(String.valueOf(id)).delete();
    }
}
