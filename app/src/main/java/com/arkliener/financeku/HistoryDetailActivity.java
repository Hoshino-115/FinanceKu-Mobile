package com.arkliener.financeku;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryDetailActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ListView listView;
    private TextView tvJudul;
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

        historyId = getIntent().getIntExtra("history_id", -1);
        historyNama = getIntent().getStringExtra("history_nama");

        if (historyNama != null) {
            tvJudul.setText("Detail: " + historyNama);
        }

        adapter = new TransaksiAdapter();
        listView.setAdapter(adapter);

        refreshData();
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
                String tanggal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TANGGAL));
                listTransaksi.add(new Transaksi(id, judul, nominal, jenis, tanggal));
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
            tvKeterangan.setText(t.jenis + " - " + t.tanggal);

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
