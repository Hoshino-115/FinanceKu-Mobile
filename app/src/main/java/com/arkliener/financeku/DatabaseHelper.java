package com.arkliener.financeku;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "financeku.db";
    private static final int DATABASE_VERSION = 4;
    public static final String TABLE_TRANSAKSI = "transaksi";
    public static final String TABLE_HISTORY = "history";

    public static final String COL_ID = "id";
    public static final String COL_JUDUL = "judul";
    public static final String COL_NOMINAL = "nominal";
    public static final String COL_JENIS = "jenis";
    public static final String COL_METODE = "metode";
    public static final String COL_TANGGAL = "tanggal";
    public static final String COL_HISTORY_ID = "history_id";

    public static final String COL_H_ID = "id";
    public static final String COL_H_NAMA = "nama_history";
    public static final String COL_H_PEMASUKAN = "total_pemasukan";
    public static final String COL_H_PENGELUARAN = "total_pengeluaran";
    public static final String COL_H_TANGGAL = "tanggal";

    public static final String JENIS_PEMASUKAN = "Pemasukan";
    public static final String JENIS_PENGELUARAN = "Pengeluaran";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlTransaksi = "create table " + TABLE_TRANSAKSI + "("
                + COL_ID + " integer primary key autoincrement, "
                + COL_JUDUL + " text, "
                + COL_NOMINAL + " integer, "
                + COL_JENIS + " text, "
                + COL_METODE + " text, "
                + COL_TANGGAL + " text, "
                + COL_HISTORY_ID + " integer);";
        db.execSQL(sqlTransaksi);

        String sqlHistory = "create table " + TABLE_HISTORY + "("
                + COL_H_ID + " integer primary key autoincrement, "
                + COL_H_NAMA + " text, "
                + COL_H_PEMASUKAN + " integer, "
                + COL_H_PENGELUARAN + " integer, "
                + COL_H_TANGGAL + " text);";
        db.execSQL(sqlHistory);

        Log.d("FinanceKu", "onCreate: Tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSAKSI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    // Tambah transaksi baru
    public void tambahTransaksi(String judul, int nominal, String jenis, String metode, String tanggal) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(COL_JUDUL, judul);
        values.put(COL_NOMINAL, nominal);
        values.put(COL_JENIS, jenis);
        values.put(COL_METODE, metode);
        values.put(COL_TANGGAL, tanggal);
        db.insert(TABLE_TRANSAKSI, null, values);
        db.close();
    }

    // Ambil seluruh transaksi dari yang terbaru (yang belum masuk history)
    public Cursor getAllTransaksi() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_TRANSAKSI + " WHERE " + COL_HISTORY_ID + " IS NULL ORDER BY " + COL_ID + " DESC";
        Log.d("FinanceKu", "getAllTransaksi: " + sql);
        return db.rawQuery(sql, null);
    }

    // Ambil detail transaksi berdasarkan history ID
    public Cursor getTransaksiByHistoryId(int historyId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_TRANSAKSI + " WHERE " + COL_HISTORY_ID + " = " + historyId + " ORDER BY " + COL_ID + " ASC";
        return db.rawQuery(sql, null);
    }

    // Update transaksi berdasarkan ID
    public void updateTransaksi(int id, String judul, int nominal, String jenis, String metode, String tanggal) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(COL_JUDUL, judul);
        values.put(COL_NOMINAL, nominal);
        values.put(COL_JENIS, jenis);
        values.put(COL_METODE, metode);
        values.put(COL_TANGGAL, tanggal);
        db.update(TABLE_TRANSAKSI, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Hapus transaksi brdasarkan ID
    public void deleteTransaksi(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "delete from " + TABLE_TRANSAKSI + " where " + COL_ID + " = '" + id + "'";
        Log.d("FinanceKu", "deleteTransaksi: " + sql);
        db.execSQL(sql);
        db.close();
    }

    // Hapus seluruh transaksi
    public void deleteAllTransaksi() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_TRANSAKSI);
        db.close();
    }

    // Arsipkan transaksi saat ini ke history
    public void archiveCurrentTransactions(int historyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(COL_HISTORY_ID, historyId);
        db.update(TABLE_TRANSAKSI, values, COL_HISTORY_ID + " IS NULL", null);
        db.close();
    }

    // Tambah history baru
    public long tambahHistory(String nama, int pemasukan, int pengeluaran, String tanggal) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(COL_H_NAMA, nama);
        values.put(COL_H_PEMASUKAN, pemasukan);
        values.put(COL_H_PENGELUARAN, pengeluaran);
        values.put(COL_H_TANGGAL, tanggal);
        long id = db.insert(TABLE_HISTORY, null, values);
        db.close();
        return id;
    }

    // Ambil seluruh history
    public Cursor getAllHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_HISTORY + " ORDER BY " + COL_H_ID + " DESC";
        return db.rawQuery(sql, null);
    }

    // Hapus history dan transaksi yang terkait
    public void deleteHistory(int historyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Hapus transaksi yang memiliki history_id tersebut
        db.delete(TABLE_TRANSAKSI, COL_HISTORY_ID + "=?", new String[]{String.valueOf(historyId)});
        // Hapus history-nya
        db.delete(TABLE_HISTORY, COL_H_ID + "=?", new String[]{String.valueOf(historyId)});
        db.close();
    }

    public void updateHistoryName(int historyId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(COL_H_NAMA, newName);
        db.update(TABLE_HISTORY, values, COL_H_ID + "=?", new String[]{String.valueOf(historyId)});
        db.close();
    }

    // Hitung total saldo (hanya transaksi aktif)
    public int hitungTotalSaldo() {
        int saldo = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        // Pemasukan
        Cursor cursorPemasukan = db.rawQuery("SELECT SUM(" + COL_NOMINAL + ") FROM " + TABLE_TRANSAKSI + " WHERE " + COL_JENIS + " = '" + JENIS_PEMASUKAN + "' AND " + COL_HISTORY_ID + " IS NULL", null);
        int totalPemasukan = 0;
        if (cursorPemasukan.moveToFirst()) {
            totalPemasukan = cursorPemasukan.getInt(0);
        }
        cursorPemasukan.close();

        // Pengeluaran
        Cursor cursorPengeluaran = db.rawQuery("SELECT SUM(" + COL_NOMINAL + ") FROM " + TABLE_TRANSAKSI + " WHERE " + COL_JENIS + " = '" + JENIS_PENGELUARAN + "' AND " + COL_HISTORY_ID + " IS NULL", null);
        int totalPengeluaran = 0;
        if (cursorPengeluaran.moveToFirst()) {
            totalPengeluaran = cursorPengeluaran.getInt(0);
        }
        cursorPengeluaran.close();

        saldo = totalPemasukan - totalPengeluaran;
        db.close();
        return saldo;
    }

    public boolean hasActiveTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TRANSAKSI + " WHERE " + COL_HISTORY_ID + " IS NULL", null);
        boolean has = false;
        if (cursor.moveToFirst()) {
            has = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return has;
    }

    public void restoreHistoryToMain(int historyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Set history_id ke NULL untuk transaksi yang tadinya milik history ini
        android.content.ContentValues values = new android.content.ContentValues();
        values.putNull(COL_HISTORY_ID);
        db.update(TABLE_TRANSAKSI, values, COL_HISTORY_ID + "=?", new String[]{String.valueOf(historyId)});
        
        // Hapus entry history-nya karena transaksinya sudah keluar
        db.delete(TABLE_HISTORY, COL_H_ID + "=?", new String[]{String.valueOf(historyId)});
        db.close();
    }
}
