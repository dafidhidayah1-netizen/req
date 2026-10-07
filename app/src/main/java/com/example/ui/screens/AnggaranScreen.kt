package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnggaranItem
import com.example.data.model.KasTransaction
import com.example.data.model.RincianAnggaranItem
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.NavySecondary
import com.example.util.FormatUtils
import com.example.util.WhatsAppHelper

@Composable
fun AnggaranScreen(
    anggaranList: List<AnggaranItem>,
    rincianAnggaranList: List<RincianAnggaranItem>,
    transactions: List<KasTransaction>,
    onSaveRincianAnggaran: (RincianAnggaranItem) -> Unit,
    onDeleteRincianAnggaran: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Rincian Anggaran (A, B, C), 1: Alokasi Kategori Kas
    var showAddDialog by remember { mutableStateOf(false) }

    val totalPlafon = rincianAnggaranList.sumOf { it.plafonBudget }
    val totalRealisasi = rincianAnggaranList.sumOf { it.realisasiTerpakai }
    val totalDefisit = rincianAnggaranList.sumOf { it.defisitKekurangan }
    val totalSurplus = rincianAnggaranList.sumOf { it.sisaSurplus }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Rincian A, B, C & Selisih", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Alokasi Kas Keluar", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        if (selectedTab == 0) {
            // RINGKASAN VARIANSI ANGGARAN (PLAFON VS REALISASI & DEFISIT)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Rincian Anggaran & Variansi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_add_rincian_anggaran")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Plafon", fontSize = 11.sp, color = Color.Gray)
                            Text(text = FormatUtils.formatRupiah(totalPlafon), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text(text = "Total Realisasi", fontSize = 11.sp, color = Color.Gray)
                            Text(text = FormatUtils.formatRupiah(totalRealisasi), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DangerRed)
                        }
                        Column {
                            Text(text = "Sisa / Defisit", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = if (totalDefisit > 0) "- " + FormatUtils.formatRupiah(totalDefisit) else FormatUtils.formatRupiah(totalSurplus),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (totalDefisit > 0) DangerRed else EmeraldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            WhatsAppHelper.shareRincianAnggaranViaWhatsApp(context, rincianAnggaranList, "Oktober 2026")
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kirim Laporan Selisih Anggaran ke WA", color = EmeraldPrimary, fontSize = 12.sp)
                    }
                }
            }

            // DAFTAR RINCIAN ANGGARAN (A, B, C...)
            if (rincianAnggaranList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Belum ada rincian anggaran yang ditambahkan.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(rincianAnggaranList) { itm ->
                        val isOver = itm.defisitKekurangan > 0
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "${itm.kode}: ${itm.namaAlokasi}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = "Periode ${itm.periode}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            isOver -> DangerRedLight
                                            itm.persenTerpakai >= 85.0 -> GoldLight
                                            else -> EmeraldLight
                                        }
                                    ) {
                                        Text(
                                            text = itm.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isOver -> DangerRed
                                                itm.persenTerpakai >= 85.0 -> GoldAccent
                                                else -> EmeraldPrimary
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LinearProgressIndicator(
                                    progress = { (Math.min(100.0, itm.persenTerpakai) / 100.0).toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = if (isOver) DangerRed else if (itm.persenTerpakai >= 85.0) GoldAccent else EmeraldPrimary,
                                    trackColor = Color.LightGray.copy(alpha = 0.4f),
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Plafon Budget", fontSize = 11.sp, color = Color.Gray)
                                        Text(text = FormatUtils.formatRupiah(itm.plafonBudget), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Column {
                                        Text(text = "Realisasi Terpakai", fontSize = 11.sp, color = Color.Gray)
                                        Text(text = FormatUtils.formatRupiah(itm.realisasiTerpakai), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                                    }
                                    Column {
                                        Text(
                                            text = if (isOver) "Kekurangan Defisit" else "Sisa Surplus",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = if (isOver) "- " + FormatUtils.formatRupiah(itm.defisitKekurangan) else FormatUtils.formatRupiah(itm.sisaSurplus),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isOver) DangerRed else EmeraldPrimary
                                        )
                                    }
                                }

                                if (itm.catatan.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "Catatan: ${itm.catatan}", fontSize = 11.sp, color = Color.DarkGray)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    IconButton(onClick = { onDeleteRincianAnggaran(itm.id) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB ALOKASI KATEGORI KAS
            val outTransactions = transactions.filter { it.type == "KELUAR" }
            val liveAnggaran = anggaranList.map { item ->
                val actualRealized = outTransactions.filter { it.allocation == item.allocation }.sumOf { it.amount }
                val remaining = Math.max(0.0, item.budget - actualRealized)
                val pct = if (item.budget > 0) (actualRealized / item.budget) * 100.0 else 0.0
                val status = if (actualRealized > item.budget) "OVER" else "OK"
                item.copy(realized = actualRealized, remaining = remaining, percentageUsed = pct, status = status)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(liveAnggaran) { item ->
                    val isOver = item.status == "OVER"
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = item.allocation, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isOver) DangerRedLight else EmeraldLight
                                ) {
                                    Text(
                                        text = if (isOver) "⚠️ OVER BUDGET" else "OK (${String.format(java.util.Locale.US, "%.1f", item.percentageUsed)}%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOver) DangerRed else EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { (Math.min(100.0, item.percentageUsed) / 100.0).toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = if (isOver) DangerRed else EmeraldPrimary,
                                trackColor = Color.LightGray.copy(alpha = 0.4f),
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Budget Plafon", fontSize = 11.sp, color = Color.Gray)
                                    Text(text = FormatUtils.formatRupiah(item.budget), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column {
                                    Text(text = "Realisasi Terpakai", fontSize = 11.sp, color = Color.Gray)
                                    Text(text = FormatUtils.formatRupiah(item.realized), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                                }
                                Column {
                                    Text(text = "Sisa Anggaran", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = FormatUtils.formatRupiah(item.remaining),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOver) DangerRed else EmeraldPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var kode by remember { mutableStateOf("Anggaran D") }
        var namaAlokasi by remember { mutableStateOf("") }
        var periode by remember { mutableStateOf("Oktober 2026") }
        var plafonText by remember { mutableStateOf("5000000") }
        var realisasiText by remember { mutableStateOf("0") }
        var catatan by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Rincian Anggaran", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kode,
                        onValueChange = { kode = it },
                        label = { Text("Kode (e.g. Anggaran A, B, C...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = namaAlokasi,
                        onValueChange = { namaAlokasi = it },
                        label = { Text("Nama Alokasi / Proyek") },
                        placeholder = { Text("Pengadaan Bahan, Logistik, dll.") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = plafonText,
                            onValueChange = { plafonText = it },
                            label = { Text("Plafon Budget (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = realisasiText,
                            onValueChange = { realisasiText = it },
                            label = { Text("Realisasi (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = catatan,
                        onValueChange = { catatan = it },
                        label = { Text("Catatan Rincian") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val plafon = plafonText.toDoubleOrNull() ?: 0.0
                        val real = realisasiText.toDoubleOrNull() ?: 0.0
                        if (kode.isNotBlank() && namaAlokasi.isNotBlank()) {
                            onSaveRincianAnggaran(
                                RincianAnggaranItem(
                                    id = "ANG-" + System.currentTimeMillis().toString().takeLast(5),
                                    kode = kode,
                                    namaAlokasi = namaAlokasi,
                                    periode = periode,
                                    plafonBudget = plafon,
                                    realisasiTerpakai = real,
                                    catatan = catatan
                                )
                            )
                            showAddDialog = false
                            Toast.makeText(context, "Rincian anggaran tersimpan", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }
}
