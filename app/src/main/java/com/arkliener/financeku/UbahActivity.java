package com.arkliener.financeku;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UbahActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private EditText etJudul, etNominal;
    private RadioGroup rgJenis;
    private Button btnSimpan, btnHapus;
    private int idTransaksi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ubah);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        etJudul = (EditText) findViewById(R.id.etJudul);
        etNominal = (EditText) findViewById(R.id.etNominal);
        rgJenis = (RadioGroup) findViewById(R.id.rgJenis);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnHapus = (Button) findViewById(R.id.btnHapus);

        etNominal.addTextChangedListener(new android.text.TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    etNominal.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[.,]", "");
                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            java.text.NumberFormat format = java.text.NumberFormat.getInstance(java.util.Locale.GERMANY);
                            String formatted = format.format(parsed);

                            current = formatted;
                            etNominal.setText(formatted);
                            etNominal.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }

                    etNominal.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        idTransaksi = getIntent().getIntExtra("id", -1);
        if (idTransaksi == -1) {
            Toast.makeText(this, "ID transaksi tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tampilkanDataLama();

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanPerubahan();
            }
        });

        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                konfirmasiHapus();
            }
        });
    }

    private void tampilkanDataLama() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_TRANSAKSI
                        + " WHERE " + DatabaseHelper.COL_ID + " = '" + idTransaksi + "' LIMIT 1",
                null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            cursor.moveToPosition(0);
            etJudul.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_JUDUL)));

            int nominal = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOMINAL));
            java.text.NumberFormat format = java.text.NumberFormat.getInstance(java.util.Locale.GERMANY);
            etNominal.setText(format.format(nominal));

            String jenis = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_JENIS));
            if (jenis != null && jenis.equals(DatabaseHelper.JENIS_PENGELUARAN)) {
                rgJenis.check(R.id.rbPengeluaran);
            } else {
                rgJenis.check(R.id.rbPemasukan);
            }
        }
        cursor.close();
        db.close();
    }

    private void simpanPerubahan() {
        String judul = etJudul.getText().toString().trim();
        String nominalText = etNominal.getText().toString().trim().replaceAll("[.,]", "");

        if (judul.isEmpty() || nominalText.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Data tidak boleh kosong!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int nominal = Integer.parseInt(nominalText);

        String jenis;
        int selectedId = rgJenis.getCheckedRadioButtonId();
        if (selectedId == R.id.rbPengeluaran) {
            jenis = DatabaseHelper.JENIS_PENGELUARAN;
        } else {
            jenis = DatabaseHelper.JENIS_PEMASUKAN;
        }

        dbHelper.updateTransaksi(idTransaksi, judul, nominal, jenis);

        Toast.makeText(getApplicationContext(), "Berhasil disimpan", Toast.LENGTH_LONG).show();

        if (MainActivity.da != null) {
            MainActivity.da.refreshData();
        }

        finish();
    }

    private void konfirmasiHapus() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Apakah Anda yakin ingin menghapus?");
        builder.setCancelable(true);

        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.deleteTransaksi(idTransaksi);

                Toast.makeText(getApplicationContext(), "Data terhapus", Toast.LENGTH_SHORT).show();

                if (MainActivity.da != null) {
                    MainActivity.da.refreshData();
                }

                finish();
            }
        });

        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}