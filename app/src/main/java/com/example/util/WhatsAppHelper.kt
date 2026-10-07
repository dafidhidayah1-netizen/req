package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.KasTransaction

object WhatsAppHelper {

    fun shareTransactionViaWhatsApp(
        context: Context,
        transaction: KasTransaction,
        currentAccountBalance: Double? = null,
        phoneNumber: String = "" // Jika kosong, buka pemilih kontak WhatsApp
    ) {
        val typeLabel = when (transaction.type) {
            "MASUK" -> "🟢 KAS MASUK"
            "KELUAR" -> "🔴 KAS KELUAR"
            "TRANSFER" -> "🔄 TRANSFER ANTAR AKUN"
            else -> "📄 TRANSAKSI KAS"
        }

        val textBuilder = StringBuilder()
        textBuilder.append("🧾 *BUKTI TRANSAKSI KAS TERINTEGRASI*\n")
        textBuilder.append("==============================\n")
        textBuilder.append("📌 *Jenis:* $typeLabel\n")
        textBuilder.append("🆔 *No ID:* `${transaction.id}`\n")
        textBuilder.append("📅 *Tanggal:* ${transaction.date} (${transaction.time})\n")
        textBuilder.append("🏦 *Akun Sumber:* ${transaction.account}\n")
        if (transaction.type == "TRANSFER" && transaction.toAccount.isNotEmpty()) {
            textBuilder.append("🎯 *Ke Akun:* ${transaction.toAccount}\n")
        }
        textBuilder.append("🏷️ *Kategori:* ${transaction.category}\n")
        textBuilder.append("📝 *Keterangan:* ${transaction.description}\n")
        textBuilder.append("💰 *NOMINAL:* *${FormatUtils.formatRupiah(transaction.amount)}*\n")
        textBuilder.append("📊 *Alokasi:* ${transaction.allocation}\n")
        textBuilder.append("👤 *PIC:* ${transaction.pic}\n")
        if (transaction.proofNumber.isNotEmpty()) {
            textBuilder.append("🔢 *No. Bukti:* ${transaction.proofNumber}\n")
        }
        if (transaction.proofUrl.isNotEmpty()) {
            textBuilder.append("🔗 *Bukti Google Drive:* ${transaction.proofUrl}\n")
        }
        if (transaction.note.isNotEmpty()) {
            textBuilder.append("💬 *Catatan:* ${transaction.note}\n")
        }
        if (currentAccountBalance != null) {
            textBuilder.append("💳 *Saldo Berjalan Akun:* ${FormatUtils.formatRupiah(currentAccountBalance)}\n")
        }
        textBuilder.append("✅ *Status:* ${transaction.status} (Sinkron Google Sheets)\n")
        textBuilder.append("==============================\n")
        textBuilder.append("_Pesan otomatis Sistem Kas Terintegrasi Cloud_")

        sendWhatsAppText(context, textBuilder.toString(), phoneNumber)
    }

    fun shareDailySummaryViaWhatsApp(
        context: Context,
        date: String,
        totalIn: Double,
        totalOut: Double,
        net: Double,
        balance: Double,
        phoneNumber: String = ""
    ) {
        val message = """
            📊 *LAPORAN HARIAN KAS - $date*
            ==============================
            🟢 Total Kas Masuk : *${FormatUtils.formatRupiah(totalIn)}*
            🔴 Total Kas Keluar: *${FormatUtils.formatRupiah(totalOut)}*
            ⚖️ Net Arus Kas   : *${FormatUtils.formatRupiah(net)}*
            💰 Saldo Akhir     : *${FormatUtils.formatRupiah(balance)}*
            ==============================
            _Terverifikasi Sinkron Real-Time ke Google Sheets_
        """.trimIndent()

        sendWhatsAppText(context, message, phoneNumber)
    }

    fun shareCalculationViaWhatsApp(
        context: Context,
        title: String,
        details: String,
        phoneNumber: String = ""
    ) {
        val message = """
            🧮 *KALKULATOR KEUANGAN & MARGIN CVEK*
            ==============================
            *Analisis:* $title
            $details
            ==============================
            _Sistem Kas Terintegrasi - Cloud Financial Ledger_
        """.trimIndent()

        sendWhatsAppText(context, message, phoneNumber)
    }

    fun shareSlipGajiViaWhatsApp(
        context: Context,
        slip: com.example.data.model.SlipGajiItem,
        companyName: String = "Sistem Kas Terintegrasi"
    ) {
        val message = """
            🏢 *SLIP GAJI KARYAWAN RESMI*
            *${companyName.uppercase()}*
            ==============================
            📄 *No. Slip:* `${slip.id}`
            🗓️ *Periode:* ${slip.periode}
            📅 *Tanggal Bayar:* ${slip.tanggalBayar}
            ------------------------------
            👤 *Nama:* *${slip.karyawanNama}*
            💼 *Jabatan:* ${slip.jabatan}
            🏦 *Akun Pembayar:* ${slip.akunKasPembayar}
            ==============================
            *RINCIAN PENDAPATAN:*
            (+) Gaji Pokok   : ${FormatUtils.formatRupiah(slip.gajiPokok)}
            (+) Tunjangan    : ${FormatUtils.formatRupiah(slip.tunjangan)}
            (+) Bonus/Lembur : ${FormatUtils.formatRupiah(slip.bonusLembur)}
            ------------------------------
            *POTONGAN:*
            (-) Potongan     : ${FormatUtils.formatRupiah(slip.potongan)}
            ==============================
            💰 *TOTAL GAJI BERSIH (TAKE HOME PAY):*
            👉 *${FormatUtils.formatRupiah(slip.totalGajiBersih)}*
            ==============================
            ✅ *Status:* ${slip.statusBayar}
            ${if (slip.catatan.isNotEmpty()) "💬 *Catatan:* ${slip.catatan}\n" else ""}_Dokumen slip gaji digital otomatis - Mohon simpan sebagai bukti pembayaran sah._
        """.trimIndent()

        sendWhatsAppText(context, message, slip.noWa)
    }

    fun shareRincianAnggaranViaWhatsApp(
        context: Context,
        items: List<com.example.data.model.RincianAnggaranItem>,
        period: String,
        phoneNumber: String = ""
    ) {
        val totalPlafon = items.sumOf { it.plafonBudget }
        val totalRealisasi = items.sumOf { it.realisasiTerpakai }
        val totalDefisit = items.sumOf { it.defisitKekurangan }
        val totalSisa = items.sumOf { it.sisaSurplus }

        val b = StringBuilder()
        b.append("📊 *LAPORAN RINCIAN ANGGARAN & SELISIH REALISASI*\n")
        b.append("Periode: $period\n")
        b.append("==============================\n")
        items.forEach { itm ->
            b.append("📌 *${itm.kode} - ${itm.namaAlokasi}*\n")
            b.append("   • Plafon Budget : ${FormatUtils.formatRupiah(itm.plafonBudget)}\n")
            b.append("   • Realisasi     : ${FormatUtils.formatRupiah(itm.realisasiTerpakai)} (${String.format(java.util.Locale.US, "%.1f", itm.persenTerpakai)}%)\n")
            if (itm.defisitKekurangan > 0) {
                b.append("   • ⚠️ *DEFISIT/KURANG: -${FormatUtils.formatRupiah(itm.defisitKekurangan)}*\n")
            } else {
                b.append("   • Sisa Surplus  : ${FormatUtils.formatRupiah(itm.sisaSurplus)}\n")
            }
            b.append("   • Status        : ${itm.status}\n\n")
        }
        b.append("==============================\n")
        b.append("📈 *TOTAL PLAFON:* ${FormatUtils.formatRupiah(totalPlafon)}\n")
        b.append("📉 *TOTAL REALISASI:* ${FormatUtils.formatRupiah(totalRealisasi)}\n")
        if (totalDefisit > 0) {
            b.append("⚠️ *TOTAL KEKURANGAN/DEFISIT:* -${FormatUtils.formatRupiah(totalDefisit)}\n")
        }
        b.append("💰 *TOTAL SISA SURPLUS:* ${FormatUtils.formatRupiah(totalSisa)}\n")
        b.append("==============================\n")
        b.append("_Analisis Selisih & Variansi Arus Kas_")

        sendWhatsAppText(context, b.toString(), phoneNumber)
    }

    fun shareRekapAuditViaWhatsApp(
        context: Context,
        audit: com.example.data.model.AuditKasItem,
        companyName: String = "Sistem Kas Terintegrasi",
        phoneNumber: String = ""
    ) {
        val message = """
            📋 *BERITA ACARA REKAP AUDIT & OPNAME KAS RIIL*
            *${companyName.uppercase()}*
            ==============================
            🆔 *No. Audit:* `${audit.id}`
            📅 *Tanggal & Jam:* ${audit.tanggal} (${audit.jam})
            👤 *Auditor / PIC:* ${audit.auditor}
            🏦 *Akun Kas:* ${audit.akun}
            ==============================
            *HASIL REKONSILIASI OPNAME FISIK:*
            📊 Saldo Sistem (Buku) : ${FormatUtils.formatRupiah(audit.saldoSistemBuku)}
            💵 Saldo Fisik Riil     : ${FormatUtils.formatRupiah(audit.saldoFisikRiil)}
            ------------------------------
            ⚖️ *SELISIH AUDIT:* *${if (audit.selisih > 0) "+" else ""}${FormatUtils.formatRupiah(audit.selisih)}*
            🏷️ *STATUS HASIL:* *${audit.statusHasil}*
            ==============================
            *RINCIAN PECAHAN FISIK:*
            • Rp 100.000 : ${audit.lembar100k} lembar (${FormatUtils.formatRupiah(audit.lembar100k * 100000.0)})
            • Rp 50.000  : ${audit.lembar50k} lembar (${FormatUtils.formatRupiah(audit.lembar50k * 50000.0)})
            • Rp 20.000  : ${audit.lembar20k} lembar (${FormatUtils.formatRupiah(audit.lembar20k * 20000.0)})
            • Rp 10.000  : ${audit.lembar10k} lembar (${FormatUtils.formatRupiah(audit.lembar10k * 10000.0)})
            • Rp 5.000   : ${audit.lembar5k} lembar (${FormatUtils.formatRupiah(audit.lembar5k * 5000.0)})
            • Rp 2.000   : ${audit.lembar2k} lembar (${FormatUtils.formatRupiah(audit.lembar2k * 2000.0)})
            • Rp 1.000   : ${audit.lembar1k} lembar (${FormatUtils.formatRupiah(audit.lembar1k * 1000.0)})
            • Uang Koin  : ${FormatUtils.formatRupiah(audit.koinTotal)}
            ==============================
            ${if (audit.catatanPenyebab.isNotEmpty()) "📝 *Catatan Penyebab:* ${audit.catatanPenyebab}\n" else ""}${if (audit.tindakLanjut.isNotEmpty()) "🔧 *Tindak Lanjut:* ${audit.tindakLanjut}\n" else ""}_Dokumen Berita Acara Rekap Audit Kas Resmi - Bebas Manipulasi & Sah._
        """.trimIndent()

        sendWhatsAppText(context, message, phoneNumber)
    }

    private fun sendWhatsAppText(context: Context, message: String, phoneNumber: String) {
        try {
            val cleanPhone = phoneNumber.replace("+", "").replace("-", "").replace(" ", "")
            val intent = if (cleanPhone.isNotEmpty()) {
                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    setPackage("com.whatsapp")
                }
            } else {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                }
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Jika WhatsApp belum terinstall atau setPackage gagal, buka Chooser umum
            try {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                context.startActivity(Intent.createChooser(fallbackIntent, "Kirim via WhatsApp / Aplikasi Lain").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (err: Exception) {
                Toast.makeText(context, "Tidak dapat membuka aplikasi pengirim: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
