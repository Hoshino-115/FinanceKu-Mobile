package com.arkliener.financeku;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryDetailActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ListView listView;
    private TextView tvJudul;
    private Button btnRestore;
    private List<Transaksi> listTransaksi = new ArrayList<>();
    private TransaksiAdapter adapter;
    private int historyId;
    private String historyNama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        dbHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.listViewHistoryDetail);
        tvJudul = findViewById(R.id.tvDetailJudul);
        btnRestore = findViewById(R.id.btnRestore);

        historyId = getIntent().getIntExtra("history_id", -1);
        historyNama = getIntent().getStringExtra("history_nama");

        if (historyNama != null) {
            tvJudul.setText("Detail: " + historyNama);
        }

        adapter = new TransaksiAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Transaksi t = listTransaksi.get(position);
            showDeleteDetailDialog(t);
            return true;
        });

        btnRestore.setOnClickListener(v -> {
            prosesRestore();
        });

        refreshData();
    }

    private void showDeleteDetailDialog(Transaksi t) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus transaksi '" + t.judul + "'?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    dbHelper.deleteTransaksi(t.id);
                    Toast.makeText(this, "Transaksi dihapus", Toast.LENGTH_SHORT).show();
                    refreshData();
                    // Update main activity if possible
                    if (MainActivity.da != null) MainActivity.da.refreshData();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void prosesRestore() {
        if (dbHelper.hasActiveTransactions()) {
            // Cek apakah di MainActivity sudah ada nama history aktif
            String namaAktif = null;
            if (MainActivity.da != null) {
                namaAktif = MainActivity.da.getCurrentHistoryName();
            }

            if (namaAktif != null && !namaAktif.isEmpty()) {
                // Jika sudah ada nama, langsung arsipkan tanpa tanya
                int pem = hitungTotalAktif(DatabaseHelper.JENIS_PEMASUKAN);
                int peng = hitungTotalAktif(DatabaseHelper.JENIS_PENGELUARAN);
                String tgl = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());

                long newHid = dbHelper.tambahHistory(namaAktif, pem, peng, tgl);
                dbHelper.archiveCurrentTransactions((int) newHid);

                // Lanjutkan restore history yang dipilih
                dbHelper.restoreHistoryToMain(historyId);
                if (MainActivity.da != null) {
                    MainActivity.da.setCurrentHistoryName(historyNama);
                    MainActivity.da.refreshData();
                }
                Toast.makeText(this, "Berhasil ditukar (Arsip otomatis: " + namaAktif + ")", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Jika belum ada nama, minta input (seperti sebelumnya)
                final EditText input = new EditText(this);
                input.setHint("Nama History Baru (Arsip Saat Ini)");

                new AlertDialog.Builder(this)
                        .setTitle("Arsipkan Transaksi Saat Ini")
                        .setMessage("Halaman utama memiliki transaksi aktif. Beri nama untuk mengarsipkannya sebelum memindahkan history ini ke halaman utama.")
                        .setView(input)
                        .setPositiveButton("Arsipkan & Pindahkan", (dialog, which) -> {
                            String namaBaru = input.getText().toString().trim();
                            if (namaBaru.isEmpty()) {
                                namaBaru = "Tanpa Judul Transaksi";
                            }

                            int pem = hitungTotalAktif(DatabaseHelper.JENIS_PEMASUKAN);
                            int peng = hitungTotalAktif(DatabaseHelper.JENIS_PENGELUARAN);
                            String tgl = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());

                            long newHid = dbHelper.tambahHistory(namaBaru, pem, peng, tgl);
                            dbHelper.archiveCurrentTransactions((int) newHid);

                            dbHelper.restoreHistoryToMain(historyId);
                            if (MainActivity.da != null) {
                                MainActivity.da.setCurrentHistoryName(historyNama);
                                MainActivity.da.refreshData();
                            }
                            Toast.makeText(this, "Berhasil ditukar", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }
        } else {
            // Langsung restore jika tidak ada transaksi aktif
            dbHelper.restoreHistoryToMain(historyId);
            if (MainActivity.da != null) {
                MainActivity.da.setCurrentHistoryName(historyNama);
                MainActivity.da.refreshData();
            }
            Toast.makeText(this, "History dipindahkan ke halaman utama", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private int hitungTotalAktif(String jenis) {
        int total = 0;
        Cursor cursor = dbHelper.getAllTransaksi();
        if (cursor.moveToFirst()) {
            do {
                String j = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_JENIS));
                if (j != null && j.equals(jenis)) {
                    total += cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOMINAL));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return total;
    }

    private void refreshData() {
        Cursor cursor = dbHelper.getTransaksiByHistoryId(historyId);
        listTransaksi.clear();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String judul = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_JUDUL));
                int nominal = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOMINAL));
                String jenis = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_JENIS));
                String metode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_METODE));
                String tanggal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TANGGAL));
                listTransaksi.add(new Transaksi(id, judul, nominal, jenis, metode, tanggal));
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private String formatRupiah(int nilai) {
        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
        return "Rp " + format.format(nilai);
    }

    private class TransaksiAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return listTransaksi.size();
        }

        @Override
        public Object getItem(int position) {
            return listTransaksi.get(position);
        }

        @Override
        public long getItemId(int position) {
            return listTransaksi.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(HistoryDetailActivity.this).inflate(R.layout.activity_transaksi, parent, false);
            }

            Transaksi t = listTransaksi.get(position);

            TextView tvJudul = convertView.findViewById(R.id.tvJudul);
            TextView tvKeterangan = convertView.findViewById(R.id.tvKeterangan);
            TextView tvNominal = convertView.findViewById(R.id.tvNominal);
            View indikator = convertView.findViewById(R.id.viewIndikator);

            tvJudul.setText(t.judul);
            tvKeterangan.setText(t.jenis + " (" + t.metode + ") - " + t.tanggal);

            if (t.jenis != null && t.jenis.equals(DatabaseHelper.JENIS_PEMASUKAN)) {
                tvNominal.setText("+ " + formatRupiah(t.nominal));
                tvNominal.setTextColor(0xFF2E7D32);
                indikator.setBackgroundColor(0xFF2E7D32);
            } else {
                tvNominal.setText("- " + formatRupiah(t.nominal));
                tvNominal.setTextColor(0xFFC62828);
                indikator.setBackgroundColor(0xFFC62828);
            }

            return convertView;
        }
    }
}
