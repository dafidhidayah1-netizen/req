package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary

@Composable
fun BantuanScreen() {
    val panduanList = listOf(
        "1. Alur Kas Masuk" to "Isi tanggal, akun penerima, kategori, nominal, dan foto bukti kwitansi. Tekan [Simpan Keluar] untuk selesai, atau [Simpan & Lanjutkan] untuk mencatat transaksi berikutnya seketika.",
        "2. Alur Kas Keluar" to "Isi akun sumber pengeluaran, nama barang/operasional, alokasi anggaran, nominal, dan upload nota/kwitansi. Saldo kas akun langsung berkurang secara otomatis saat disimpan.",
        "3. Tombol Simpan & Lanjutkan" to "Fitur khusus agar Anda tidak perlu bolak-balik ke dashboard saat menginput banyak struk/nota sekaligus. Form langsung bersih dan siap digunakan lagi.",
        "4. Upload Bukti ke Google Drive" to "Pilih foto dari kamera atau galeri. Foto otomatis dikonversi dan disimpan ke folder 'Bukti_Kas' di Google Drive Anda, lalu link dibagikan.",
        "5. Kirim via WhatsApp" to "Setiap transaksi, ringkasan harian, atau hasil kalkulator dapat dibagikan langsung ke WhatsApp dengan format resmi kwitansi dan link bukti.",
        "6. Kalender & Jadwal Kas" to "Melihat arus kas per tanggal, mencatat event pengingat setor kas, dan menambahkan jadwal langsung ke Google Kalender ponsel Anda.",
        "7. Kalkulator CVEK & Margin" to "Menghitung omzet, biaya tetap, biaya variabel, margin laba operasional, titik impas (BEP), dan mencatat hasil ke sheet Kalkulator.",
        "8. Kelola Piutang Usaha" to "Mencatat piutang pelanggan, menghitung jatuh tempo otomatis, mencatat cicilan/pelunasan, dan mengirim tagihan WhatsApp.",
        "9. Transaksi Dihapus & Pulihkan" to "Transaksi yang dihapus tidak lenyap seketika, melainkan masuk ke arsip Transaksi Dihapus dan dapat dipulihkan (Restore) kapan pun.",
        "10. Auto-Sync Google Sheets" to "Data bekerja offline-first di ponsel dan otomatis tersinkronisasi ke Google Sheets secara real-time saat terhubung internet tanpa biaya."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.HelpCenter, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Panduan Sistem Kas Terintegrasi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Cara Kerja & Alur Operasional Profesional", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        items(panduanList.size) { i ->
            val (judul, isi) = panduanList[i]
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = judul, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = isi, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}
