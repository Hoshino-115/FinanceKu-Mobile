package com.arkliener.financeku;

public class Transaksi {
    public int id;
    public String judul;
    public int nominal;
    public String jenis;
    public String metode;
    public String tanggal;

    public Transaksi(int id, String judul, int nominal, String jenis, String metode, String tanggal) {
        this.id = id;
        this.judul = judul;
        this.nominal = nominal;
        this.jenis = jenis;
        this.metode = metode;
        this.tanggal = tanggal;
    }
}