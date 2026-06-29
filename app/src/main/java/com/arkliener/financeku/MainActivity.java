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
    private TextView tvTotalSaldo, tvRingkasan;
    private ListView listView;
    private FloatingActionButton fabTambah;
    private final List<Transaksi> daftarTransaksi = new ArrayList<>();
    private TransaksiAdapter adapter;
    public static MainActivity da;


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
        listView = (ListView) findViewById(R.id.listViewTransaksi);
        fabTambah = (FloatingActionButton) findViewById(R.id.fabTambah);

        adapter = new TransaksiAdapter();
        listView.setAdapter(adapter);

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
        // Ambil seluruh transaksi dari database (modul: rawQuery).
        Cursor cursor = dbHelper.getAllTransaksi();

        daftarTransaksi.clear();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String judul = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_JUDUL));
                int nominal = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOMINAL));
                String jenis = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_JENIS));
                String tanggal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TANGGAL));
                daftarTransaksi.add(new Transaksi(id, judul, nominal, jenis, tanggal));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Beritahu adapter supaya ListView tergambar ulang (modul: setAdapter ulang).
        adapter.notifyDataSetChanged();

        // Update TextView Total Saldo via hitungTotalSaldo().
        int saldo = dbHelper.hitungTotalSaldo();
        tvTotalSaldo.setText(formatRupiah(saldo));
        tvRingkasan.setText("Pemasukan: " + formatRupiah(hitungTotal(DatabaseHelper.JENIS_PEMASUKAN)) + "  |  Pengeluaran: " + formatRupiah(hitungTotal(DatabaseHelper.JENIS_PENGELUARAN)));
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
        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY); // titik sebagai pemilih ribuan
        return "Rp " + format.format(nilai);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Pastikan data selalu segar saat kembali dari Tambah/Ubah Activity.
        refreshData();
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
                convertView = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.activity_transaksi, parent, false);
            }

            Transaksi t = daftarTransaksi.get(position);

            TextView tvJudul = convertView.findViewById(R.id.tvJudul);
            TextView tvKeterangan = convertView.findViewById(R.id.tvKeterangan);
            TextView tvNominal = convertView.findViewById(R.id.tvNominal);
            View indikator = convertView.findViewById(R.id.viewIndikator);

            tvJudul.setText(t.judul);
            tvKeterangan.setText(t.jenis + " - " + t.tanggal);

            // Pemasukan tanda + (hijau), pengeluaran tanda - (merah).
            if (t.jenis != null && t.jenis.equals(DatabaseHelper.JENIS_PEMASUKAN)) {
                tvNominal.setText("+ " + formatRupiah(t.nominal));
                tvNominal.setTextColor(0xFF2E7D32); // hijau
                indikator.setBackgroundColor(0xFF2E7D32);
            } else {
                tvNominal.setText("- " + formatRupiah(t.nominal));
                tvNominal.setTextColor(0xFFC62828); // merah
                indikator.setBackgroundColor(0xFFC62828);
            }

            return convertView;
        }
    }
}