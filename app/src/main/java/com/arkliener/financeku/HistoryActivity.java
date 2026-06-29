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

public class HistoryActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ListView listView;
    private List<HistoryModel> listHistory = new ArrayList<>();
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.listViewHistory);

        adapter = new HistoryAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            HistoryModel h = listHistory.get(position);
            android.content.Intent intent = new android.content.Intent(HistoryActivity.this, HistoryDetailActivity.class);
            intent.putExtra("history_id", h.id);
            intent.putExtra("history_nama", h.nama);
            startActivity(intent);
        });

        refreshData();
    }

    private void refreshData() {
        Cursor cursor = dbHelper.getAllHistory();
        listHistory.clear();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_H_ID));
                String nama = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_H_NAMA));
                int pem = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_H_PEMASUKAN));
                int peng = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_H_PENGELUARAN));
                String tgl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_H_TANGGAL));
                listHistory.add(new HistoryModel(id, nama, pem, peng, tgl));
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private String formatRupiah(int nilai) {
        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
        return "Rp " + format.format(nilai);
    }

    private static class HistoryModel {
        int id;
        String nama;
        int pemasukan;
        int pengeluaran;
        String tanggal;

        HistoryModel(int id, String nama, int pemasukan, int pengeluaran, String tanggal) {
            this.id = id;
            this.nama = nama;
            this.pemasukan = pemasukan;
            this.pengeluaran = pengeluaran;
            this.tanggal = tanggal;
        }
    }

    private class HistoryAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return listHistory.size();
        }

        @Override
        public Object getItem(int position) {
            return listHistory.get(position);
        }

        @Override
        public long getItemId(int position) {
            return listHistory.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.item_history, parent, false);
            }

            HistoryModel h = listHistory.get(position);

            TextView tvNama = convertView.findViewById(R.id.tvNamaHistory);
            TextView tvTanggal = convertView.findViewById(R.id.tvTanggalHistory);
            TextView tvPem = convertView.findViewById(R.id.tvTotalPemasukan);
            TextView tvPeng = convertView.findViewById(R.id.tvTotalPengeluaran);
            TextView tvSisa = convertView.findViewById(R.id.tvTotalSisa);

            tvNama.setText(h.nama);
            tvTanggal.setText(h.tanggal);
            tvPem.setText("Pemasukan: " + formatRupiah(h.pemasukan));
            tvPeng.setText("Pengeluaran: " + formatRupiah(h.pengeluaran));
            tvSisa.setText("Total Sisa: " + formatRupiah(h.pemasukan - h.pengeluaran));

            return convertView;
        }
    }
}
