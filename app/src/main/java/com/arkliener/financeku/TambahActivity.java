package com.arkliener.financeku;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TambahActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private EditText etJudul, etNominal;
    private RadioGroup rgJenis;
    private Button btnSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tambah);
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

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanData();
            }
        });
    }

    private void simpanData() {
        String judul = etJudul.getText().toString().trim();
        String nominalText = etNominal.getText().toString().trim();

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

        String tanggal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        dbHelper.tambahTransaksi(judul, nominal, jenis, tanggal);

        Toast.makeText(getApplicationContext(), "Berhasil disimpan",
                Toast.LENGTH_LONG).show();

        if (MainActivity.da != null) {
            MainActivity.da.refreshData();
        }

        finish();
    }
}