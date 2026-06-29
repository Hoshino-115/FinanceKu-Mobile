package com.arkliener.financeku;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "financeku.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_TRANSAKSI = "transaksi";
    public static final String COL_ID = "id";
    public static final String COL_JUDUL = "judul";
    public static final String COL_NOMINAL = "nominal";
    public static final String COL_JENIS = "jenis";
    public static final String COL_TANGGAL = "tanggal";
    public static final String JENIS_PEMASUKAN = "Pemasukan";
    public static final String JENIS_PENGELUARAN = "Pengeluaran";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + TABLE_TRANSAKSI + "("
                + COL_ID + " integer primary key autoincrement, "
                + COL_JUDUL + " text null, "
                + COL_NOMINAL + " integer, "
                + COL_JENIS + " text null, "
                + COL_TANGGAL + " text null);";
        Log.d("FinanceKu", "onCreate: " + sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSAKSI);
        onCreate(db);
    }

    // Tambah transaksi baru
    public void tambahTransaksi(String judul, int nominal, String jenis, String tanggal) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into " + TABLE_TRANSAKSI + "(" + COL_JUDUL + ", " + COL_NOMINAL + ", " + COL_JENIS + ", " + COL_TANGGAL + ") values('" + judul + "','" + nominal + "','" + jenis + "','" + tanggal + "')";
        Log.d("FinanceKu", "tambahtTransaksi: " + sql);
        db.execSQL(sql);
        db.close();
    }

    // Ambil seluruh transaksi dari yang terbaru
    public Cursor getAllTransaksi() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_TRANSAKSI + " ORDER BY " + COL_ID + " DESC";
        Log.d("FinanceKu", "getAllTransaksi: " + sql);
        return db.rawQuery(sql, null);
    }

    // Update transaksi berdasarkan ID
    public void updateTransaksi(int id, String judul, int nominal, String jenis) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "update " + TABLE_TRANSAKSI + " set " + COL_JUDUL + "='" + judul + "', " + COL_NOMINAL + "=" + nominal + ", " + COL_JENIS + "='" + jenis + "' " + "where " + COL_ID + "=" + id;
        Log.d("FinanceKu", "updateTransaksi: " + sql);
        db.execSQL(sql);
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

    // Hitung total saldo
    public int hitungTotalSaldo() {
        int saldo = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        // Pemasukan
        Cursor cursorPemasukan = db.rawQuery("SELECT SUM(" + COL_NOMINAL + ") FROM " + TABLE_TRANSAKSI + " WHERE " + COL_JENIS + " = '" + JENIS_PEMASUKAN + "'", null);
        int totalPemasukan = 0;
        if (cursorPemasukan.moveToFirst()) {
            totalPemasukan = cursorPemasukan.getInt(0);
        }
        cursorPemasukan.close();

        // Pengeluaran
        Cursor cursorPengeluaran = db.rawQuery("SELECT SUM(" + COL_NOMINAL + ") FROM " + TABLE_TRANSAKSI + " WHERE " + COL_JENIS + " = '" + JENIS_PENGELUARAN + "'", null);
        int totalPengeluaran = 0;
        if (cursorPengeluaran.moveToFirst()) {
            totalPengeluaran = cursorPengeluaran.getInt(0);
        }
        cursorPengeluaran.close();

        saldo = totalPemasukan - totalPengeluaran;
        db.close();
        return saldo;
    }
}
