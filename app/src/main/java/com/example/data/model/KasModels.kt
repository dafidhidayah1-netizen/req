package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas Transaksi Kas terintegrasi langsung dengan struktur header Google Sheets:
 * ID, Tanggal, Jam, Akun, Nama Transaksi, Kategori, Keterangan, Nominal,
 * Alokasi Anggaran, PIC, Bukti Foto/URL, No. Bukti, Proyek/Unit, Catatan,
 * Status, Waktu Input, Input Oleh
 */
@Entity(tableName = "kas_transactions")
data class KasTransaction(
    @PrimaryKey
    val id: String,
    val type: String, // "MASUK", "KELUAR", "TRANSFER"
    val date: String, // dd/MM/yyyy
    val time: String, // HH:mm:ss
    val account: String, // e.g. "Kas Tunai", "Bank BCA"
    val toAccount: String = "", // Khusus transfer
    val transactionName: String,
    val category: String,
    val description: String,
    val amount: Double,
    val allocation: String = "Operasional",
    val pic: String = "Admin",
    val proofUrl: String = "",
    val proofNumber: String = "",
    val project: String = "",
    val note: String = "",
    val status: String = "Selesai", // Selesai, Draft, Pending, Batal
    val inputTime: String = "",
    val inputBy: String = "Mobile User",
    val isSynced: Boolean = true
)

/**
 * Entitas Arsip Transaksi Dihapus (Sesuai Sheet Transaksi_Dihapus)
 * Mendukung tombol PULIHKAN (Restore)
 */
@Entity(tableName = "deleted_transactions")
data class DeletedKasTransaction(
    @PrimaryKey
    val id: String,
    val deletedAt: String,
    val deletedBy: String = "Admin",
    val source: String, // Kas_Masuk, Kas_Keluar, Transfer
    val type: String,
    val date: String,
    val time: String,
    val account: String,
    val toAccount: String = "",
    val transactionName: String,
    val category: String,
    val description: String,
    val amount: Double,
    val allocation: String = "Operasional",
    val pic: String = "Admin",
    val proofUrl: String = "",
    val proofNumber: String = "",
    val project: String = "",
    val note: String = "",
    val status: String = "Dihapus"
)

/**
 * Entitas Kelola Piutang (Sesuai Sheet Piutang)
 */
@Entity(tableName = "piutang_items")
data class PiutangItem(
    @PrimaryKey
    val id: String,
    val date: String,
    val customerName: String,
    val description: String,
    val amount: Double,
    val dueDate: String,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = amount,
    val status: String = "Belum Jatuh Tempo", // Lunas, Jatuh Tempo, Belum Jatuh Tempo
    val targetAccount: String = "Bank BCA",
    val note: String = "",
    val isSynced: Boolean = true
)

/**
 * Entitas Anggaran & Realisasi Dasar
 */
@Entity(tableName = "anggaran_items")
data class AnggaranItem(
    @PrimaryKey
    val id: String,
    val period: String, // YYYY-MM
    val allocation: String, // Operasional, Gaji, Marketing, Proyek, Lainnya
    val budget: Double,
    val realized: Double = 0.0,
    val remaining: Double = budget,
    val percentageUsed: Double = 0.0,
    val status: String = "OK", // OK, OVER
    val note: String = ""
)

/**
 * Entitas Rincian Anggaran Multi-Level (Anggaran A, B, C, dll.)
 * Menghitung selisih perbedaan, sisa anggaran, dan kekurangan/defisit secara matematis akurat.
 */
@Entity(tableName = "rincian_anggaran")
data class RincianAnggaranItem(
    @PrimaryKey
    val id: String,
    val kode: String, // "Anggaran A", "Anggaran B", "Anggaran C", dll.
    val namaAlokasi: String, // Nama proyek / pengeluaran
    val periode: String, // e.g. "Oktober 2026"
    val plafonBudget: Double, // Plafon yang disetujui
    val realisasiTerpakai: Double, // Realisasi aktual dari buku kas
    val selisih: Double = plafonBudget - realisasiTerpakai, // Plafon - Realisasi
    val defisitKekurangan: Double = if (realisasiTerpakai > plafonBudget) (realisasiTerpakai - plafonBudget) else 0.0,
    val sisaSurplus: Double = if (plafonBudget > realisasiTerpakai) (plafonBudget - realisasiTerpakai) else 0.0,
    val persenTerpakai: Double = if (plafonBudget > 0) (realisasiTerpakai / plafonBudget) * 100.0 else 0.0,
    val status: String = when {
        realisasiTerpakai > plafonBudget -> "OVER BUDGET / DEFISIT"
        persenTerpakai >= 85.0 -> "WASPADA (>85%)"
        else -> "AMAN"
    },
    val catatan: String = "",
    val isSynced: Boolean = true
)

/**
 * Entitas Data Karyawan
 */
@Entity(tableName = "karyawan")
data class KaryawanItem(
    @PrimaryKey
    val id: String,
    val nama: String,
    val jabatan: String,
    val noWa: String, // Nomor WhatsApp untuk kirim slip gaji
    val rekeningBank: String = "Bank BCA",
    val gajiPokok: Double,
    val tunjangan: Double = 0.0,
    val status: String = "Aktif"
)

/**
 * Entitas Absensi Karyawan
 */
@Entity(tableName = "absensi_karyawan")
data class AbsensiItem(
    @PrimaryKey
    val id: String,
    val karyawanId: String,
    val karyawanNama: String,
    val tanggal: String, // dd/MM/yyyy
    val statusKehadiran: String, // Hadir, Sakit, Izin, Alpa
    val jamMasuk: String = "08:00",
    val jamPulang: String = "17:00",
    val catatan: String = "",
    val isSynced: Boolean = true
)

/**
 * Entitas Slip Gaji Karyawan (Kirim via WhatsApp & Terhubung ke Kas Keluar)
 */
@Entity(tableName = "slip_gaji")
data class SlipGajiItem(
    @PrimaryKey
    val id: String, // e.g. SLIP-202610-001
    val periode: String, // e.g. Oktober 2026
    val tanggalBayar: String, // dd/MM/yyyy
    val karyawanId: String,
    val karyawanNama: String,
    val jabatan: String,
    val noWa: String,
    val gajiPokok: Double,
    val tunjangan: Double,
    val bonusLembur: Double,
    val potongan: Double,
    val totalGajiBersih: Double = (gajiPokok + tunjangan + bonusLembur) - potongan,
    val akunKasPembayar: String = "Bank BCA",
    val statusBayar: String = "Lunas", // Lunas, Menunggu
    val catatan: String = "",
    val kasTransactionId: String = "", // Terhubung ke ID Transaksi Kas Keluar
    val isSynced: Boolean = true
)

/**
 * Entitas Buku Catatan & Pengingat Kas
 */
@Entity(tableName = "buku_catatan")
data class BukuCatatanItem(
    @PrimaryKey
    val id: String,
    val date: String,
    val title: String,
    val note: String,
    val pic: String = "Admin",
    val priority: String = "Sedang", // Rendah, Sedang, Tinggi
    val status: String = "Open", // Open, Done, Follow Up
    val isSynced: Boolean = true
)

/**
 * Log Kalkulator Keuangan (Sesuai Sheet Kalkulator)
 */
@Entity(tableName = "calculator_logs")
data class CalculatorLogItem(
    @PrimaryKey
    val id: String,
    val timestamp: String,
    val title: String,
    val initialBalance: Double,
    val income: Double,
    val fixedCosts: Double,
    val variableCosts: Double,
    val totalExpense: Double,
    val netMargin: Double,
    val expensePercentage: Double,
    val finalBalance: Double,
    val formulaOrNote: String,
    val isSynced: Boolean = true
)

/**
 * Event / Jadwal Kalender Kas
 */
@Entity(tableName = "calendar_events")
data class CalendarEventItem(
    @PrimaryKey
    val id: String,
    val date: String, // dd/MM/yyyy
    val time: String = "09:00",
    val title: String,
    val description: String,
    val type: String = "PENGINGAT", // MASUK, KELUAR, PENGINGAT, JATUH_TEMPO
    val amount: Double = 0.0,
    val isCompleted: Boolean = false,
    val isSynced: Boolean = true
)

/**
 * Entitas Rekap Audit & Rekonsiliasi Kas Riil (Opname Kas Fisik vs Saldo Sistem)
 */
@Entity(tableName = "audit_kas")
data class AuditKasItem(
    @PrimaryKey
    val id: String, // e.g. AUD-202610-001
    val tanggal: String, // dd/MM/yyyy
    val jam: String, // HH:mm
    val auditor: String, // Nama auditor / PIC
    val akun: String, // e.g. "Kas Tunai", "Bank BCA"
    val saldoSistemBuku: Double, // Saldo menurut catatan sistem buku
    val saldoFisikRiil: Double, // Saldo uang fisik riil hasil opname
    val selisih: Double = saldoFisikRiil - saldoSistemBuku, // Fisik - Sistem
    val statusHasil: String = when {
        saldoFisikRiil == saldoSistemBuku -> "MATCH / COCOK"
        saldoFisikRiil > saldoSistemBuku -> "SELISIH LEBIH"
        else -> "SELISIH KURANG"
    },
    val lembar100k: Int = 0,
    val lembar50k: Int = 0,
    val lembar20k: Int = 0,
    val lembar10k: Int = 0,
    val lembar5k: Int = 0,
    val lembar2k: Int = 0,
    val lembar1k: Int = 0,
    val koinTotal: Double = 0.0,
    val catatanPenyebab: String = "",
    val tindakLanjut: String = "",
    val isSynced: Boolean = true
)

data class AccountBalance(
    val name: String,
    val type: String, // "Kas", "Bank"
    val initialBalance: Double,
    val totalIn: Double,
    val totalOut: Double,
    val currentBalance: Double
)

data class DashboardSummary(
    val initialBalance: Double = 0.0,
    val todayIn: Double = 0.0,
    val todayOut: Double = 0.0,
    val todayNet: Double = 0.0,
    val monthIn: Double = 0.0,
    val monthOut: Double = 0.0,
    val currentBalance: Double = 0.0,
    val pendingSyncCount: Int = 0,
    val lastSyncTime: String = "",
    val isConnected: Boolean = true
)

data class CalendarCashflow(
    val date: String, // dd/MM/yyyy
    val dayOfWeek: String,
    val totalIn: Double,
    val totalOut: Double,
    val net: Double,
    val countIn: Int,
    val countOut: Int,
    val status: String // "Positif", "Minus", "Nol"
)

data class CvekCalculation(
    val initialBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val fixedCosts: Double = 0.0,
    val variableCosts: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netMargin: Double = 0.0,
    val expensePercentage: Double = 0.0,
    val finalBalance: Double = 0.0,
    val breakEvenPointUnits: Double = 0.0,
    val targetProfit: Double = 0.0
)

data class MasterCategories(
    val accounts: List<String> = listOf("Kas Tunai", "Bank BCA", "Bank BRI", "Bank Mandiri", "Kas Besar"),
    val inCategories: List<String> = listOf("Penjualan", "Piutang Masuk", "Modal", "Pendapatan Lain", "Transfer Masuk"),
    val outCategories: List<String> = listOf("Belanja Barang", "Gaji", "Transportasi", "Listrik/Internet", "Sewa", "Marketing", "Operasional"),
    val allocations: List<String> = listOf("Operasional", "Gaji", "Marketing", "Proyek", "Lainnya"),
    val pics: List<String> = listOf("Admin", "Bendahara", "Keuangan", "Manager", "PIC Operasional"),
    val statuses: List<String> = listOf("Selesai", "Draft", "Pending", "Batal")
)
