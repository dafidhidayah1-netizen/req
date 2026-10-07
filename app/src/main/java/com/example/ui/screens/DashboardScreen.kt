package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountBalance
import com.example.data.model.DashboardSummary
import com.example.data.model.KasTransaction
import com.example.ui.components.AccountBalanceCard
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavySecondary
import com.example.util.FormatUtils
import com.example.util.WhatsAppHelper

@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    accounts: List<AccountBalance>,
    recentTransactions: List<KasTransaction>,
    syncMessage: String,
    unsyncedCount: Int,
    onNavigateToForm: (String) -> Unit, // "MASUK", "KELUAR", "TRANSFER"
    onNavigateToScreen: (String) -> Unit, // "TRANSAKSI", "LAPORAN", "KALENDER", "KALKULATOR", "PIUTANG", "ANGGARAN", "CATATAN", "ARSIP_DIHAPUS", "PENGATURAN", "BANTUAN"
    onSelectTransaction: (KasTransaction) -> Unit,
    onSyncNow: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Hero Card: Saldo Kas Saat Ini
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(EmeraldPrimary, EmeraldDark)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL SALDO KAS BERJALAN",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (unsyncedCount == 0) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = if (unsyncedCount == 0) EmeraldLight else Color(0xFFFFD54F),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (unsyncedCount == 0) "Tersinkron" else "$unsyncedCount Belum Sync",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = FormatUtils.formatRupiah(summary.currentBalance),
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Arus Kas Hari Ini
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.Black.copy(alpha = 0.15f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Masuk Hari Ini",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(summary.todayIn),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )
                            }
                            Column {
                                Text(
                                    text = "Keluar Hari Ini",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(summary.todayOut),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFCDD2)
                                )
                            }
                            Column {
                                Text(
                                    text = "Net Hari Ini",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(summary.todayNet),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.todayNet >= 0) EmeraldLight else Color(0xFFFF8A80)
                                )
                            }
                        }
                    }
                }
            }
        }

        // TIGA TOMBOL BESAR UTAMA (SESUAI REQUEST)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // TOMBOL BESAR + KAS MASUK
                Button(
                    onClick = { onNavigateToForm("MASUK") },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("btn_big_kas_masuk")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "KAS MASUK", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }

                // TOMBOL BESAR - KAS KELUAR
                Button(
                    onClick = { onNavigateToForm("KELUAR") },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("btn_big_kas_keluar")
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "KAS KELUAR", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }

                // TOMBOL BESAR TRANSFER
                Button(
                    onClick = { onNavigateToForm("TRANSFER") },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("btn_big_transfer")
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "TRANSFER", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
        }

        // SALDO SELURUH AKUN CAROUSEL
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saldo Akun & Kas Bank",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${accounts.size} Akun Aktif",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    accounts.forEach { account ->
                        AccountBalanceCard(
                            account = account,
                            onClick = { onNavigateToScreen("TRANSAKSI") }
                        )
                    }
                }
            }
        }

        // GRID MODUL LENGKAP SISTEM KAS
        item {
            Text(
                text = "Modul Lengkap Sistem Kas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModuleItemCard(
                        icon = Icons.Default.Assessment,
                        title = "Laporan",
                        subtitle = "Semua Akun",
                        color = NavySecondary,
                        bgColor = NavyLight,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("LAPORAN") }
                    )
                    ModuleItemCard(
                        icon = Icons.Default.CalendarMonth,
                        title = "Kalender",
                        subtitle = "Arus Kas & Event",
                        color = Color(0xFF0D47A1),
                        bgColor = Color(0xFFE3F2FD),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("KALENDER") }
                    )
                    ModuleItemCard(
                        icon = Icons.Default.Calculate,
                        title = "Kalkulator",
                        subtitle = "CVEK & Sheets Log",
                        color = Color(0xFF00695C),
                        bgColor = Color(0xFFE0F2F1),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("KALKULATOR") }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModuleItemCard(
                        icon = Icons.Default.AttachMoney,
                        title = "Piutang",
                        subtitle = "Kelola & Jatuh Tempo",
                        color = Color(0xFF6A1B9A),
                        bgColor = Color(0xFFF3E5F5),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("PIUTANG") }
                    )
                    ModuleItemCard(
                        icon = Icons.Default.PieChart,
                        title = "Anggaran",
                        subtitle = "Realisasi & Alokasi",
                        color = Color(0xFFE65100),
                        bgColor = Color(0xFFFFF3E0),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("ANGGARAN") }
                    )
                    ModuleItemCard(
                        icon = Icons.Default.MenuBook,
                        title = "Catatan",
                        subtitle = "Memo & Pengingat",
                        color = Color(0xFF455A64),
                        bgColor = Color(0xFFECEFF1),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("CATATAN") }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModuleItemCard(
                        icon = Icons.Default.Payments,
                        title = "Gaji & Absensi",
                        subtitle = "Slip Gaji WA",
                        color = Color(0xFF2E7D32),
                        bgColor = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("GAJI_KARYAWAN") }
                    )
                    ModuleItemCard(
                        icon = Icons.Default.Restore,
                        title = "Arsip Dihapus",
                        subtitle = "Pulihkan Transaksi",
                        color = DangerRed,
                        bgColor = Color(0xFFFFEBEE),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("ARSIP_DIHAPUS") }
                    )
                    ModuleItemCard(
                        icon = Icons.Default.Settings,
                        title = "Pengaturan",
                        subtitle = "Sheets & Backup",
                        color = EmeraldPrimary,
                        bgColor = EmeraldLight,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("PENGATURAN") }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModuleItemCard(
                        icon = Icons.Default.FactCheck,
                        title = "Rekap Audit",
                        subtitle = "Opname Kas Riil",
                        color = Color(0xFF6A1B9A),
                        bgColor = Color(0xFFF3E5F5),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("AUDIT") }
                    )
                    ModuleItemCard(
                        icon = Icons.Default.HelpCenter,
                        title = "Bantuan",
                        subtitle = "Panduan Alur",
                        color = NavySecondary,
                        bgColor = NavyLight,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("BANTUAN") }
                    )
                }
            }
        }

        // Transaksi Terbaru
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaksi Terakhir",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateToScreen("TRANSAKSI") }) {
                    Text(text = "Lihat Semua")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum ada transaksi tersimpan",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            items(recentTransactions.take(6)) { tx ->
                TransactionItemCard(
                    transaction = tx,
                    onItemClick = { onSelectTransaction(tx) },
                    onShareWhatsApp = {
                        val accBal = accounts.find { it.name == tx.account }?.currentBalance
                        WhatsAppHelper.shareTransactionViaWhatsApp(context, tx, accBal)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ModuleItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
            Text(text = subtitle, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
        }
    }
}
