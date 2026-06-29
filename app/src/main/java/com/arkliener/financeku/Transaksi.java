package com.arkliener.financeku;

public class Transaksi {
    public int id;
    public String judul;
    public int nominal;
    public String jenis;
    public String tanggal;

    public Transaksi(int id, String judul, int nominal, String jenis, String tanggal) {
        this.id = id;
        this.judul = judul;
        this.nominal = nominal;
        this.jenis = jenis;
        this.tanggal = tanggal;
    }
}