package com.arkliener.financeku;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.app.DatePickerDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TambahActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private EditText etJudul, etNominal, etTanggal;
    private RadioGroup rgJenis, rgMetode;
    private Button btnSimpan;
    private Calendar calendar;

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
        etTanggal = (EditText) findViewById(R.id.etTanggal);
        rgJenis = (RadioGroup) findViewById(R.id.rgJenis);
        rgMetode = (RadioGroup) findViewById(R.id.rgMetode);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);

        calendar = Calendar.getInstance();
        updateLabel();

        etTanggal.setOnClickListener(v -> showDatePicker());

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

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanData();
            }
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(TambahActivity.this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel() {
        String myFormat = "EEEE, dd MMMM yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("id", "ID"));
        etTanggal.setText(sdf.format(calendar.getTime()));
    }

    private void simpanData() {
        String judul = etJudul.getText().toString().trim();
        String nominalText = etNominal.getText().toString().trim().replaceAll("[.,]", "");
        String tanggal = etTanggal.getText().toString().trim();

        if (judul.isEmpty() || nominalText.isEmpty() || tanggal.isEmpty()) {
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

        String metode;
        int selectedMetodeId = rgMetode.getCheckedRadioButtonId();
        if (selectedMetodeId == R.id.rbEWallet) {
            metode = "E-Wallet";
        } else if (selectedMetodeId == R.id.rbLainnya) {
            metode = "Lainnya";
        } else {
            metode = "Cash";
        }

        dbHelper.tambahTransaksi(judul, nominal, jenis, metode, tanggal);

        Toast.makeText(getApplicationContext(), "Berhasil disimpan",
                Toast.LENGTH_LONG).show();

        if (MainActivity.da != null) {
            MainActivity.da.refreshData();
        }

        finish();
    }
}