package com.arkliener.financeku;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.BaseAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView tvTotalSaldo, tvRingkasan, tvStatusHistory;
    private ListView listView;
    private FloatingActionButton fabTambah;
    private android.widget.Button btnReset, btnSimpanHistory, btnLihatHistory;
    private final List<Transaksi> daftarTransaksi = new ArrayList<>();
    private TransaksiAdapter adapter;
    public static MainActivity da;
    private String currentHistoryName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        da = this;
        dbHelper = new DatabaseHelper(this);
        tvTotalSaldo = (TextView) findViewById(R.id.tvTotalSaldo);
        tvRingkasan = (TextView) findViewById(R.id.tvRingkasan);
        tvStatusHistory = (TextView) findViewById(R.id.tvStatusHistory);
        listView = (ListView) findViewById(R.id.listViewTransaksi);
        fabTambah = (FloatingActionButton) findViewById(R.id.fabTambah);
        btnReset = (android.widget.Button) findViewById(R.id.btnReset);
        btnSimpanHistory = (android.widget.Button) findViewById(R.id.btnSimpanHistory);
        btnLihatHistory = (android.widget.Button) findViewById(R.id.btnLihatHistory);

        adapter = new TransaksiAdapter();
        listView.setAdapter(adapter);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Hapus Semua")
                        .setMessage("Apakah Anda yakin ingin menghapus semua data transaksi?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            dbHelper.deleteAllTransaksi();
                            currentHistoryName = null;
                            refreshData();
                        })
                        .setNegativeButton("Tidak", null)
                        .show();
            }
        });

        btnSimpanHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tampilkanDialogSimpanHistory();
            }
        });

        btnLihatHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        fabTambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TambahActivity.class);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Transaksi t = daftarTransaksi.get(position);
                Intent intent = new Intent(MainActivity.this, UbahActivity.class);
                intent.putExtra("id", t.id);
                startActivity(intent);
            }
        });

        refreshData();
    }

    public void refreshData() {
        Cursor cursor = dbHelper.getAllTransaksi();
        daftarTransaksi.clear();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String judul = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_JUDUL));
                int nominal = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOMINAL));
                String jenis = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_JENIS));
                String metode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_METODE));
                String tanggal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TANGGAL));
                daftarTransaksi.add(new Transaksi(id, judul, nominal, jenis, metode, tanggal));
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();

        int saldo = dbHelper.hitungTotalSaldo();
        tvTotalSaldo.setText(formatRupiah(saldo));
        tvRingkasan.setText("Pemasukan: " + formatRupiah(hitungTotal(DatabaseHelper.JENIS_PEMASUKAN)) + "  |  Pengeluaran: " + formatRupiah(hitungTotal(DatabaseHelper.JENIS_PENGELUARAN)));
        
        if (currentHistoryName != null && !currentHistoryName.isEmpty()) {
            tvStatusHistory.setText("Transaksi Aktif: " + currentHistoryName);
        } else {
            tvStatusHistory.setText("Transaksi Aktif: (Belum Bernama)");
        }
    }

    private int hitungTotal(String jenis) {
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

    private String formatRupiah(int nilai) {
        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
        return "Rp " + format.format(nilai);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void tampilkanDialogSimpanHistory() {
        if (currentHistoryName != null && !currentHistoryName.isEmpty()) {
            int pem = hitungTotal(DatabaseHelper.JENIS_PEMASUKAN);
            int peng = hitungTotal(DatabaseHelper.JENIS_PENGELUARAN);
            String tgl = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());

            long historyId = dbHelper.tambahHistory(currentHistoryName, pem, peng, tgl);
            dbHelper.archiveCurrentTransactions((int) historyId);
            currentHistoryName = null;
            refreshData();
            android.widget.Toast.makeText(MainActivity.this, "Berhasil diarsipkan", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nama History (misal: Januari 2024)");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Simpan ke History")
                .setMessage("Data transaksi saat ini akan disimpan sebagai history dan daftar transaksi akan dikosongkan.")
                .setView(input)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String nama = input.getText().toString().trim();
                    if (nama.isEmpty()) {
                        nama = "Tanpa Judul Transaksi";
                    }
                    
                    int pem = hitungTotal(DatabaseHelper.JENIS_PEMASUKAN);
                    int peng = hitungTotal(DatabaseHelper.JENIS_PENGELUARAN);
                    String tgl = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());

                    long historyId = dbHelper.tambahHistory(nama, pem, peng, tgl);
                    dbHelper.archiveCurrentTransactions((int) historyId);
                    refreshData();
                    android.widget.Toast.makeText(MainActivity.this, "Berhasil disimpan ke history", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    public String getCurrentHistoryName() {
        return this.currentHistoryName;
    }

    public void setCurrentHistoryName(String name) {
        this.currentHistoryName = name;
    }

    private class TransaksiAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return daftarTransaksi.size();
        }
        @Override
        public Object getItem(int position) {
            return daftarTransaksi.get(position);
        }
        @Override
        public long getItemId(int position) {
            return daftarTransaksi.get(position).id;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_transaksi, parent, false);
            }
            Transaksi t = daftarTransaksi.get(position);
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
