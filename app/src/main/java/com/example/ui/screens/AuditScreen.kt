package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.AccountBalance
import com.example.data.model.AuditKasItem
import com.example.data.model.MasterCategories
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.NavySecondary
import com.example.util.FormatUtils
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditScreen(
    auditList: List<AuditKasItem>,
    accounts: List<AccountBalance>,
    master: MasterCategories,
    isSyncing: Boolean,
    onSaveAudit: (AuditKasItem, (String) -> Unit) -> Unit,
    onDeleteAudit: (String) -> Unit,
    onHapusSemuaDataPalsu: (() -> Unit) -> Unit,
    onSyncNow: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Form Audit Baru, 1: Riwayat Audit
    var showConfirmResetDialog by remember { mutableStateOf(false) }

    // State Input Form Audit
    var selectedAccount by remember { mutableStateOf(master.accounts.firstOrNull() ?: "Kas Tunai") }
    var accountMenuExpanded by remember { mutableStateOf(false) }

    val currentAccountBal = accounts.find { it.name == selectedAccount }?.currentBalance ?: 0.0

    // Pecahan uang kertas & logam
    var lembar100k by remember { mutableIntStateOf(0) }
    var lembar50k by remember { mutableIntStateOf(0) }
    var lembar20k by remember { mutableIntStateOf(0) }
    var lembar10k by remember { mutableIntStateOf(0) }
    var lembar5k by remember { mutableIntStateOf(0) }
    var lembar2k by remember { mutableIntStateOf(0) }
    var lembar1k by remember { mutableIntStateOf(0) }
    var koinTotal by remember { mutableDoubleStateOf(0.0) }
    var manualPhysicalBalanceText by remember { mutableStateOf("") }
    var useManualPhysicalInput by remember { mutableStateOf(false) }

    val calculatedDenomTotal = (lembar100k * 100000.0) +
            (lembar50k * 50000.0) +
            (lembar20k * 20000.0) +
            (lembar10k * 10000.0) +
            (lembar5k * 5000.0) +
            (lembar2k * 2000.0) +
            (lembar1k * 1000.0) +
            koinTotal

    val finalPhysicalBalance = if (useManualPhysicalInput) {
        manualPhysicalBalanceText.toDoubleOrNull() ?: 0.0
    } else {
        calculatedDenomTotal
    }

    val variance = finalPhysicalBalance - currentAccountBal
    val auditStatus = when {
        finalPhysicalBalance == currentAccountBal -> "MATCH / COCOK"
        finalPhysicalBalance > currentAccountBal -> "SELISIH LEBIH"
        else -> "SELISIH KURANG"
    }

    var auditorName by remember { mutableStateOf("Auditor Independen") }
    var auditReasonNote by remember { mutableStateOf("") }
    var actionPlanNote by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner Aksi Bersihkan Data Palsu & Tab
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DangerRedLight.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Data Real & Rekap Audit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DangerRed
                    )
                    Text(
                        text = "Pastikan pembukuan bebas dari data palsu / uji coba.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { showConfirmResetDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_purge_fake_data")
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus Data Palsu", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Audit & Rekonsiliasi Baru", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Riwayat Berita Acara (${auditList.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // KARTU HASIL REKONSILIASI REAL-TIME
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (auditStatus) {
                                "MATCH / COCOK" -> EmeraldLight
                                "SELISIH LEBIH" -> GoldLight
                                else -> DangerRedLight
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hasil Audit: $auditStatus",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = when (auditStatus) {
                                        "MATCH / COCOK" -> EmeraldPrimary
                                        "SELISIH LEBIH" -> GoldAccent
                                        else -> DangerRed
                                    }
                                )
                                Icon(
                                    imageVector = when (auditStatus) {
                                        "MATCH / COCOK" -> Icons.Default.CheckCircle
                                        else -> Icons.Default.Warning
                                    },
                                    contentDescription = null,
                                    tint = when (auditStatus) {
                                        "MATCH / COCOK" -> EmeraldPrimary
                                        "SELISIH LEBIH" -> GoldAccent
                                        else -> DangerRed
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Saldo Sistem (Buku)", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = FormatUtils.formatRupiah(currentAccountBal),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Column {
                                    Text(text = "Saldo Fisik Riil", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = FormatUtils.formatRupiah(finalPhysicalBalance),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Column {
                                    Text(text = "Selisih Audit", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = (if (variance > 0) "+" else "") + FormatUtils.formatRupiah(variance),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = if (variance < 0) DangerRed else if (variance > 0) GoldAccent else EmeraldPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // PILIH AKUN & AUDITOR
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(text = "1. Parameter Akun & Auditor", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            ExposedDropdownMenuBox(
                                expanded = accountMenuExpanded,
                                onExpandedChange = { accountMenuExpanded = !accountMenuExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedAccount,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Pilih Akun yang Diaudit") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountMenuExpanded) },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = accountMenuExpanded,
                                    onDismissRequest = { accountMenuExpanded = false }
                                ) {
                                    master.accounts.forEach { acc ->
                                        DropdownMenuItem(
                                            text = { Text(acc) },
                                            onClick = {
                                                selectedAccount = acc
                                                accountMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = auditorName,
                                onValueChange = { auditorName = it },
                                label = { Text("Nama Pemeriksa / Auditor") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_auditor_name")
                            )
                        }
                    }
                }

                // OPNAME PECAHAN UANG FISIK (CASH DENOMINATIONS)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "2. Hitung Uang Fisik Kas", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                TextButton(
                                    onClick = { useManualPhysicalInput = !useManualPhysicalInput }
                                ) {
                                    Text(if (useManualPhysicalInput) "Gunakan Kalkulator Pecahan" else "Input Manual Total")
                                }
                            }

                            if (!useManualPhysicalInput) {
                                Text(
                                    text = "Hitung lembar per pecahan (otomatis dijumlahkan):",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )

                                DenomInputRow("Rp 100.000", lembar100k, 100000.0) { lembar100k = it }
                                DenomInputRow("Rp 50.000", lembar50k, 50000.0) { lembar50k = it }
                                DenomInputRow("Rp 20.000", lembar20k, 20000.0) { lembar20k = it }
                                DenomInputRow("Rp 10.000", lembar10k, 10000.0) { lembar10k = it }
                                DenomInputRow("Rp 5.000", lembar5k, 5000.0) { lembar5k = it }
                                DenomInputRow("Rp 2.000", lembar2k, 2000.0) { lembar2k = it }
                                DenomInputRow("Rp 1.000", lembar1k, 1000.0) { lembar1k = it }

                                OutlinedTextField(
                                    value = if (koinTotal > 0) koinTotal.toLong().toString() else "",
                                    onValueChange = { koinTotal = it.toDoubleOrNull() ?: 0.0 },
                                    label = { Text("Total Uang Logam / Koin (Rp)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total Fisik Dihitung:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(FormatUtils.formatRupiah(calculatedDenomTotal), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = manualPhysicalBalanceText,
                                    onValueChange = { manualPhysicalBalanceText = it },
                                    label = { Text("Total Saldo Fisik / Rekening Riil (Rp)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_manual_physical")
                                )
                            }
                        }
                    }
                }

                // CATATAN BERITA ACARA AUDIT
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(text = "3. Berita Acara & Tindak Lanjut", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            OutlinedTextField(
                                value = auditReasonNote,
                                onValueChange = { auditReasonNote = it },
                                label = { Text("Catatan Penyebab Selisih (Jika Ada)") },
                                placeholder = { Text("Misal: Nota BBM belum diserahkan, selisih uang kembalian Rp 2.000...") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = actionPlanNote,
                                onValueChange = { actionPlanNote = it },
                                label = { Text("Rencana Tindak Lanjut / Rekonsiliasi") },
                                placeholder = { Text("Misal: Disesuaikan di kas keluar petty cash tanggal...") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // TOMBOL SIMPAN & SHARE WA
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val nowId = "AUD-" + System.currentTimeMillis()
                                val auditItem = AuditKasItem(
                                    id = nowId,
                                    tanggal = FormatUtils.getCurrentDate(),
                                    jam = FormatUtils.getCurrentTime(),
                                    auditor = auditorName.ifBlank { "Auditor Kas" },
                                    akun = selectedAccount,
                                    saldoSistemBuku = currentAccountBal,
                                    saldoFisikRiil = finalPhysicalBalance,
                                    selisih = variance,
                                    statusHasil = auditStatus,
                                    lembar100k = lembar100k,
                                    lembar50k = lembar50k,
                                    lembar20k = lembar20k,
                                    lembar10k = lembar10k,
                                    lembar5k = lembar5k,
                                    lembar2k = lembar2k,
                                    lembar1k = lembar1k,
                                    koinTotal = koinTotal,
                                    catatanPenyebab = auditReasonNote,
                                    tindakLanjut = actionPlanNote,
                                    isSynced = true
                                )
                                onSaveAudit(auditItem) { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    selectedTab = 1
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("btn_save_audit")
                        ) {
                            Icon(imageVector = Icons.Default.FactCheck, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan Berita Acara", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val tempAudit = AuditKasItem(
                                    id = "PREVIEW-AUDIT",
                                    tanggal = FormatUtils.getCurrentDate(),
                                    jam = FormatUtils.getCurrentTime(),
                                    auditor = auditorName.ifBlank { "Auditor Kas" },
                                    akun = selectedAccount,
                                    saldoSistemBuku = currentAccountBal,
                                    saldoFisikRiil = finalPhysicalBalance,
                                    selisih = variance,
                                    statusHasil = auditStatus,
                                    lembar100k = lembar100k,
                                    lembar50k = lembar50k,
                                    lembar20k = lembar20k,
                                    lembar10k = lembar10k,
                                    lembar5k = lembar5k,
                                    lembar2k = lembar2k,
                                    lembar1k = lembar1k,
                                    koinTotal = koinTotal,
                                    catatanPenyebab = auditReasonNote,
                                    tindakLanjut = actionPlanNote,
                                    isSynced = true
                                )
                                WhatsAppHelper.shareRekapAuditViaWhatsApp(context, tempAudit)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("btn_share_audit_wa")
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kirim ke WA", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
            // TAB 1: RIWAYAT BERITA ACARA AUDIT
            if (auditList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada riwayat rekap audit.", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("Lakukan opname kas fisik riil untuk membuat berita acara.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(auditList, key = { it.id }) { item ->
                        AuditHistoryCard(
                            audit = item,
                            onShareWa = { WhatsAppHelper.shareRekapAuditViaWhatsApp(context, item) },
                            onDelete = { onDeleteAudit(item.id) }
                        )
                    }
                }
            }
        }
    }

    // DIALOG KONFIRMASI HAPUS SEMUA DATA PALSU
    if (showConfirmResetDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmResetDialog = false },
            icon = { Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = DangerRed) },
            title = { Text("Hapus Semua Data Palsu?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Tindakan ini akan menghapus permanen seluruh transaksi uji coba/dummy, karyawan contoh, dan rincian simulasi dari database. " +
                            "Sistem akan bersih 100% dan siap diisi data real atau disinkronkan langsung dari Google Sheets riil Anda."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmResetDialog = false
                        onHapusSemuaDataPalsu {
                            Toast.makeText(context, "Seluruh data palsu berhasil dibersihkan! Sistem siap untuk data real.", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Ya, Bersihkan Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmResetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun DenomInputRow(
    label: String,
    count: Int,
    nominalPerPiece: Double,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.weight(1.2f))
        OutlinedTextField(
            value = if (count > 0) count.toString() else "",
            onValueChange = { onValueChange(it.toIntOrNull() ?: 0) },
            placeholder = { Text("0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .width(80.dp)
                .height(52.dp)
        )
        Text(
            text = FormatUtils.formatRupiah(count * nominalPerPiece),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(1.3f)
                .padding(start = 10.dp)
        )
    }
}

@Composable
fun AuditHistoryCard(
    audit: AuditKasItem,
    onShareWa: () -> Unit,
    onDelete: () -> Unit
) {
    val isMatch = audit.saldoFisikRiil == audit.saldoSistemBuku
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = audit.id, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "${audit.tanggal} • ${audit.jam} • ${audit.akun}", fontSize = 11.sp, color = Color.Gray)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isMatch) EmeraldLight else if (audit.selisih > 0) GoldLight else DangerRedLight
                ) {
                    Text(
                        text = audit.statusHasil,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMatch) EmeraldPrimary else if (audit.selisih > 0) GoldAccent else DangerRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Sistem", fontSize = 10.sp, color = Color.Gray)
                    Text(text = FormatUtils.formatRupiah(audit.saldoSistemBuku), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text(text = "Fisik Riil", fontSize = 10.sp, color = Color.Gray)
                    Text(text = FormatUtils.formatRupiah(audit.saldoFisikRiil), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text(text = "Selisih", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = (if (audit.selisih > 0) "+" else "") + FormatUtils.formatRupiah(audit.selisih),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (audit.selisih < 0) DangerRed else if (audit.selisih > 0) GoldAccent else EmeraldPrimary
                    )
                }
            }

            if (audit.catatanPenyebab.isNotBlank()) {
                Text(text = "Penyebab: ${audit.catatanPenyebab}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (audit.tindakLanjut.isNotBlank()) {
                Text(text = "Tindak Lanjut: ${audit.tindakLanjut}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShareWa, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Kirim WA", tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
