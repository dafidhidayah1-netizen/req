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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.AbsensiItem
import com.example.data.model.KaryawanItem
import com.example.data.model.SlipGajiItem
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavySecondary
import com.example.util.FormatUtils
import com.example.util.WhatsAppHelper

@Composable
fun GajiKaryawanScreen(
    karyawanList: List<KaryawanItem>,
    absensiList: List<AbsensiItem>,
    slipGajiList: List<SlipGajiItem>,
    onSaveKaryawan: (KaryawanItem) -> Unit,
    onDeleteKaryawan: (String) -> Unit,
    onSaveAbsensi: (AbsensiItem) -> Unit,
    onDeleteAbsensi: (String) -> Unit,
    onSaveSlipGaji: (SlipGajiItem) -> Unit,
    onDeleteSlipGaji: (String) -> Unit,
    onBayarGaji: (SlipGajiItem, (Boolean, String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Slip Gaji, 1: Absensi, 2: Karyawan

    var showAddSlipDialog by remember { mutableStateOf(false) }
    var showAddAbsensiDialog by remember { mutableStateOf(false) }
    var showAddKaryawanDialog by remember { mutableStateOf(false) }

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
                text = { Text("Slip Gaji", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Absensi", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(imageVector = Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Karyawan", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (selectedTab) {
            0 -> {
                // TAB SLIP GAJI
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Slip Gaji Karyawan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showAddSlipDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_add_slip_gaji")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buat Slip Gaji", fontSize = 12.sp)
                    }
                }

                if (slipGajiList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Belum ada slip gaji yang dibuat.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(slipGajiList) { slip ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = slip.karyawanNama, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text(text = "${slip.jabatan} • Periode ${slip.periode}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (slip.statusBayar == "Lunas") EmeraldLight else DangerRedLight
                                        ) {
                                            Text(
                                                text = slip.statusBayar,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (slip.statusBayar == "Lunas") EmeraldPrimary else DangerRed,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Rincian Komponen Gaji
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(text = "Gaji Pokok", fontSize = 11.sp, color = Color.Gray)
                                                Text(text = FormatUtils.formatRupiah(slip.gajiPokok), fontSize = 12.sp)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(text = "Tunjangan", fontSize = 11.sp, color = Color.Gray)
                                                Text(text = "+ " + FormatUtils.formatRupiah(slip.tunjangan), fontSize = 12.sp, color = EmeraldPrimary)
                                            }
                                            if (slip.bonusLembur > 0) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(text = "Bonus / Lembur", fontSize = 11.sp, color = Color.Gray)
                                                    Text(text = "+ " + FormatUtils.formatRupiah(slip.bonusLembur), fontSize = 12.sp, color = EmeraldPrimary)
                                                }
                                            }
                                            if (slip.potongan > 0) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(text = "Potongan", fontSize = 11.sp, color = Color.Gray)
                                                    Text(text = "- " + FormatUtils.formatRupiah(slip.potongan), fontSize = 12.sp, color = DangerRed)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(text = "TOTAL DITERIMA (THP)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(
                                                    text = FormatUtils.formatRupiah(slip.totalGajiBersih),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 15.sp,
                                                    color = EmeraldPrimary
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "Tanggal Bayar: ${slip.tanggalBayar} • Akun: ${slip.akunKasPembayar}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // TOMBOL AKSI: KIRIM VIA WA & BAYAR KAS KELUAR
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Kirim Slip ke WhatsApp
                                        Button(
                                            onClick = {
                                                WhatsAppHelper.shareSlipGajiViaWhatsApp(context, slip)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_share_slip_wa_${slip.id}")
                                        ) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Kirim ke WA", fontSize = 12.sp)
                                        }

                                        // Bayar & Catat ke Kas Keluar
                                        if (slip.statusBayar != "Lunas") {
                                            OutlinedButton(
                                                onClick = {
                                                    onBayarGaji(slip) { success, msg ->
                                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Bayar ke Kas", fontSize = 12.sp)
                                            }
                                        }

                                        IconButton(onClick = { onDeleteSlipGaji(slip.id) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // TAB ABSENSI KARYAWAN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Kehadiran & Absensi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showAddAbsensiDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Catat Absen", fontSize = 12.sp)
                    }
                }

                if (absensiList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Belum ada catatan absensi.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(absensiList) { abs ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = abs.karyawanNama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            text = "${abs.tanggal} • Masuk: ${abs.jamMasuk} - Pulang: ${abs.jamPulang}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        if (abs.catatan.isNotEmpty()) {
                                            Text(text = abs.catatan, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (abs.statusKehadiran) {
                                            "Hadir" -> EmeraldLight
                                            "Sakit", "Izin" -> NavyLight
                                            else -> DangerRedLight
                                        }
                                    ) {
                                        Text(
                                            text = abs.statusKehadiran,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (abs.statusKehadiran) {
                                                "Hadir" -> EmeraldPrimary
                                                "Sakit", "Izin" -> NavySecondary
                                                else -> DangerRed
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    IconButton(onClick = { onDeleteAbsensi(abs.id) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB DATA KARYAWAN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Data Karyawan & Staf",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showAddKaryawanDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Karyawan", fontSize = 12.sp)
                    }
                }

                if (karyawanList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Belum ada data karyawan.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(karyawanList) { k ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
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
                                        Column {
                                            Text(text = k.nama, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text(text = "${k.jabatan} • WA: ${k.noWa}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        IconButton(onClick = { onDeleteKaryawan(k.id) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Gaji Pokok: ${FormatUtils.formatRupiah(k.gajiPokok)} • Tunjangan: ${FormatUtils.formatRupiah(k.tunjangan)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldPrimary
                                    )
                                    Text(
                                        text = "Rekening: ${k.rekeningBank}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG BUAT SLIP GAJI
    if (showAddSlipDialog) {
        AddSlipGajiDialog(
            karyawanList = karyawanList,
            onDismiss = { showAddSlipDialog = false },
            onSave = { slip ->
                onSaveSlipGaji(slip)
                showAddSlipDialog = false
                Toast.makeText(context, "Slip gaji berhasil dibuat", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // DIALOG CATAT ABSENSI
    if (showAddAbsensiDialog) {
        AddAbsensiDialog(
            karyawanList = karyawanList,
            onDismiss = { showAddAbsensiDialog = false },
            onSave = { abs ->
                onSaveAbsensi(abs)
                showAddAbsensiDialog = false
                Toast.makeText(context, "Absensi berhasil dicatat", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // DIALOG TAMBAH KARYAWAN
    if (showAddKaryawanDialog) {
        AddKaryawanDialog(
            onDismiss = { showAddKaryawanDialog = false },
            onSave = { k ->
                onSaveKaryawan(k)
                showAddKaryawanDialog = false
                Toast.makeText(context, "Karyawan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun AddSlipGajiDialog(
    karyawanList: List<KaryawanItem>,
    onDismiss: () -> Unit,
    onSave: (SlipGajiItem) -> Unit
) {
    var selectedKaryawan by remember { mutableStateOf(karyawanList.firstOrNull()) }
    var periode by remember { mutableStateOf("Oktober 2026") }
    var gajiPokokText by remember(selectedKaryawan) {
        mutableStateOf(selectedKaryawan?.gajiPokok?.toInt()?.toString() ?: "4000000")
    }
    var tunjanganText by remember(selectedKaryawan) {
        mutableStateOf(selectedKaryawan?.tunjangan?.toInt()?.toString() ?: "500000")
    }
    var bonusText by remember { mutableStateOf("0") }
    var potonganText by remember { mutableStateOf("0") }
    var akunKas by remember { mutableStateOf("Bank BCA") }
    var catatan by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Slip Gaji Karyawan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (karyawanList.isNotEmpty()) {
                    Text(text = "Pilih Karyawan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    DropdownField(
                        label = "Karyawan",
                        selected = selectedKaryawan?.nama ?: "Pilih",
                        options = karyawanList.map { it.nama },
                        modifier = Modifier.fillMaxWidth(),
                        onSelect = { name ->
                            selectedKaryawan = karyawanList.find { it.nama == name }
                        }
                    )
                }

                OutlinedTextField(
                    value = periode,
                    onValueChange = { periode = it },
                    label = { Text("Periode Gaji") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gajiPokokText,
                        onValueChange = { gajiPokokText = it },
                        label = { Text("Gaji Pokok") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tunjanganText,
                        onValueChange = { tunjanganText = it },
                        label = { Text("Tunjangan") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bonusText,
                        onValueChange = { bonusText = it },
                        label = { Text("Bonus/Lembur") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = potonganText,
                        onValueChange = { potonganText = it },
                        label = { Text("Potongan") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                val gp = gajiPokokText.toDoubleOrNull() ?: 0.0
                val tj = tunjanganText.toDoubleOrNull() ?: 0.0
                val bn = bonusText.toDoubleOrNull() ?: 0.0
                val pt = potonganText.toDoubleOrNull() ?: 0.0
                val totalThp = (gp + tj + bn) - pt

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Total Diterima (THP): ${FormatUtils.formatRupiah(totalThp)}",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val k = selectedKaryawan
                    if (k != null) {
                        val gp = gajiPokokText.toDoubleOrNull() ?: 0.0
                        val tj = tunjanganText.toDoubleOrNull() ?: 0.0
                        val bn = bonusText.toDoubleOrNull() ?: 0.0
                        val pt = potonganText.toDoubleOrNull() ?: 0.0
                        val slip = SlipGajiItem(
                            id = "SLIP-" + System.currentTimeMillis(),
                            periode = periode,
                            tanggalBayar = FormatUtils.getCurrentDate(),
                            karyawanId = k.id,
                            karyawanNama = k.nama,
                            jabatan = k.jabatan,
                            noWa = k.noWa,
                            gajiPokok = gp,
                            tunjangan = tj,
                            bonusLembur = bn,
                            potongan = pt,
                            akunKasPembayar = akunKas,
                            statusBayar = "Lunas",
                            catatan = catatan
                        )
                        onSave(slip)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Simpan Slip Gaji")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AddAbsensiDialog(
    karyawanList: List<KaryawanItem>,
    onDismiss: () -> Unit,
    onSave: (AbsensiItem) -> Unit
) {
    var selectedKaryawan by remember { mutableStateOf(karyawanList.firstOrNull()) }
    var statusKehadiran by remember { mutableStateOf("Hadir") }
    var jamMasuk by remember { mutableStateOf("08:00") }
    var jamPulang by remember { mutableStateOf("17:00") }
    var catatan by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Kehadiran Karyawan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (karyawanList.isNotEmpty()) {
                    DropdownField(
                        label = "Pilih Karyawan",
                        selected = selectedKaryawan?.nama ?: "Pilih",
                        options = karyawanList.map { it.nama },
                        modifier = Modifier.fillMaxWidth(),
                        onSelect = { name ->
                            selectedKaryawan = karyawanList.find { it.nama == name }
                        }
                    )
                }

                DropdownField(
                    label = "Status Kehadiran",
                    selected = statusKehadiran,
                    options = listOf("Hadir", "Sakit", "Izin", "Alpa"),
                    modifier = Modifier.fillMaxWidth(),
                    onSelect = { statusKehadiran = it }
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = jamMasuk,
                        onValueChange = { jamMasuk = it },
                        label = { Text("Jam Masuk") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = jamPulang,
                        onValueChange = { jamPulang = it },
                        label = { Text("Jam Pulang") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan / Keterangan (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val k = selectedKaryawan
                    if (k != null) {
                        onSave(
                            AbsensiItem(
                                id = "ABS-" + System.currentTimeMillis(),
                                karyawanId = k.id,
                                karyawanNama = k.nama,
                                tanggal = FormatUtils.getCurrentDate(),
                                statusKehadiran = statusKehadiran,
                                jamMasuk = jamMasuk,
                                jamPulang = jamPulang,
                                catatan = catatan
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavySecondary)
            ) {
                Text("Simpan Absensi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AddKaryawanDialog(
    onDismiss: () -> Unit,
    onSave: (KaryawanItem) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var jabatan by remember { mutableStateOf("") }
    var noWa by remember { mutableStateOf("") }
    var rekening by remember { mutableStateOf("Bank BCA ") }
    var gajiPokokText by remember { mutableStateOf("4500000") }
    var tunjanganText by remember { mutableStateOf("500000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Karyawan Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = jabatan,
                    onValueChange = { jabatan = it },
                    label = { Text("Jabatan / Posisi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = noWa,
                    onValueChange = { noWa = it },
                    label = { Text("Nomor WhatsApp (Kirim Slip)") },
                    placeholder = { Text("081234567890") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rekening,
                    onValueChange = { rekening = it },
                    label = { Text("Rekening Bank") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gajiPokokText,
                        onValueChange = { gajiPokokText = it },
                        label = { Text("Gaji Pokok") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tunjanganText,
                        onValueChange = { tunjanganText = it },
                        label = { Text("Tunjangan") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nama.isNotBlank() && noWa.isNotBlank()) {
                        onSave(
                            KaryawanItem(
                                id = "KRY-" + System.currentTimeMillis().toString().takeLast(5),
                                nama = nama,
                                jabatan = jabatan,
                                noWa = noWa,
                                rekeningBank = rekening,
                                gajiPokok = gajiPokokText.toDoubleOrNull() ?: 0.0,
                                tunjangan = tunjanganText.toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Simpan Karyawan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
