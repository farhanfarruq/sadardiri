package com.example.sadardiri.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "sadardiri.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");
        db.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, amount REAL, type TEXT, category_id INTEGER, note TEXT, date TEXT)");
        db.execSQL("CREATE TABLE savings_targets (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, target_amount REAL, current_amount REAL, target_date TEXT)");
        db.execSQL("CREATE TABLE habits (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, frequency TEXT)");
        db.execSQL("CREATE TABLE habit_logs (id INTEGER PRIMARY KEY AUTOINCREMENT, habit_id INTEGER, date TEXT)");

        // Kategori Default
        db.execSQL("INSERT INTO categories (name) VALUES ('Gaji'), ('Makan'), ('Transport'), ('Hiburan'), ('Belanja'), ('Tagihan')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS transactions");
        db.execSQL("DROP TABLE IF EXISTS categories");
        db.execSQL("DROP TABLE IF EXISTS savings_targets");
        db.execSQL("DROP TABLE IF EXISTS habits");
        db.execSQL("DROP TABLE IF EXISTS habit_logs");
        onCreate(db);
    }

    // === TRANSAKSI (CRUD LENGKAP) ===
    public void addTransaction(double amount, String type, int categoryId, String note, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("amount", amount);
        cv.put("type", type);
        cv.put("category_id", categoryId);
        cv.put("note", note);
        cv.put("date", date);
        db.insert("transactions", null, cv);
        db.close();
    }

    public void updateTransaction(int id, double amount, String type, int categoryId, String note, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("amount", amount);
        cv.put("type", type);
        cv.put("category_id", categoryId);
        cv.put("note", note);
        cv.put("date", date);
        db.update("transactions", cv, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteTransaction(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("transactions", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Cursor getAllTransactions() {
        return getReadableDatabase().rawQuery("SELECT t.*, c.name as category_name FROM transactions t LEFT JOIN categories c ON t.category_id = c.id ORDER BY date DESC", null);
    }

    // === KATEGORI ===
    public Cursor getAllCategories() {
        return getReadableDatabase().rawQuery("SELECT * FROM categories", null);
    }
    public void deleteCategory(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("categories", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void addCategory(String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        db.insert("categories", null, cv);
        db.close();
    }

    // === TABUNGAN (CRUD LENGKAP) ===
    public void addSavingsTarget(String name, double target, double current, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("target_amount", target);
        cv.put("current_amount", current);
        cv.put("target_date", date);
        db.insert("savings_targets", null, cv);
        db.close();
    }

    public void updateSavingsTargetDetails(int id, String name, double targetAmount) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("target_amount", targetAmount);
        db.update("savings_targets", cv, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateSavingsAmount(int id, double newAmount) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("current_amount", newAmount);
        db.update("savings_targets", cv, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteSavingsTarget(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("savings_targets", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Cursor getAllSavingsTargets() {
        return getReadableDatabase().rawQuery("SELECT * FROM savings_targets ORDER BY id DESC", null);
    }

    // === KEBIASAAN (CRUD LENGKAP) ===
    public void addHabit(String name, String frequency) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("frequency", frequency);
        db.insert("habits", null, cv);
        db.close();
    }

    public void updateHabitName(int id, String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        db.update("habits", cv, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteHabit(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("habits", "id=?", new String[]{String.valueOf(id)});
        db.delete("habit_logs", "habit_id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Cursor getAllHabits() {
        return getReadableDatabase().rawQuery("SELECT * FROM habits", null);
    }

    public void logHabit(int habitId, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("habit_id", habitId);
        cv.put("date", date);
        db.insert("habit_logs", null, cv);
        db.close();
    }

    public void removeHabitLog(int habitId, String date) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("habit_logs", "habit_id = ? AND date = ?", new String[]{String.valueOf(habitId), date});
        db.close();
    }

    public boolean isHabitDoneToday(int habitId, String date) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM habit_logs WHERE habit_id = ? AND date = ?", new String[]{String.valueOf(habitId), date});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public int getCompletedHabitsToday(String date) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT habit_id) FROM habit_logs WHERE date = ?", new String[]{date});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getTotalHabits() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM habits", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // === DASHBOARD & REPORTS ===
    public double getTotalIncomeThisMonth() {
        String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT SUM(amount) FROM transactions WHERE type='income' AND substr(date,1,7)=?",
                new String[]{month});
        double total = c.moveToFirst() ? c.getDouble(0) : 0;
        c.close();
        return total;
    }

    public double getTotalExpenseThisMonth() {
        String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT SUM(amount) FROM transactions WHERE type='expense' AND substr(date,1,7)=?",
                new String[]{month});
        double total = c.moveToFirst() ? c.getDouble(0) : 0;
        c.close();
        return total;
    }

    public double predictMonthlyExpense() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(amount) FROM transactions WHERE type = 'expense' AND date >= date('now', '-3 months')", null);
        double totalLast3Months = cursor.moveToFirst() ? cursor.getDouble(0) : 0;
        cursor.close();
        return totalLast3Months / 3;
    }

    public Cursor getExpenseByCategory(String month) {
        return getReadableDatabase().rawQuery(
                "SELECT c.name, SUM(t.amount) FROM transactions t " +
                        "JOIN categories c ON t.category_id = c.id " +
                        "WHERE t.type='expense' AND substr(t.date,1,7)=? " +
                        "GROUP BY c.name", new String[]{month});
    }

    public Cursor getIncomeByCategory(String month) {
        return getReadableDatabase().rawQuery(
                "SELECT c.name, SUM(t.amount) FROM transactions t " +
                        "JOIN categories c ON t.category_id = c.id " +
                        "WHERE t.type='income' AND substr(t.date,1,7)=? " +
                        "GROUP BY c.name", new String[]{month});
    }
}