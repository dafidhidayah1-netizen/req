package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KasTransaction
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.util.FormatUtils
import com.example.util.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun LaporanScreen(
    transactions: List<KasTransaction>,
    onSelectTransaction: (KasTransaction) -> Unit
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf("BULAN_INI") } // HARI_INI, BULAN_INI, TAHUN_INI, SEMUA

    val todayStr = FormatUtils.getCurrentDate()
    val cal = Calendar.getInstance()
    val currentMonth = cal.get(Calendar.MONTH)
    val currentYear = cal.get(Calendar.YEAR)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val filteredList = transactions.filter { tx ->
        when (selectedPeriod) {
            "HARI_INI" -> tx.date == todayStr
            "BULAN_INI" -> {
                try {
                    val d = sdf.parse(tx.date)
                    if (d != null) {
                        val c = Calendar.getInstance().apply { time = d }
                        c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
                    } else false
                } catch (e: Exception) {
                    false
                }
            }
            "TAHUN_INI" -> {
                try {
                    val d = sdf.parse(tx.date)
                    if (d != null) {
                        val c = Calendar.getInstance().apply { time = d }
                        c.get(Calendar.YEAR) == currentYear
                    } else false
                } catch (e: Exception) {
                    false
                }
            }
            else -> true
        }
    }

    val totalIn = filteredList.filter { it.type == "MASUK" }.sumOf { it.amount }
    val totalOut = filteredList.filter { it.type == "KELUAR" }.sumOf { it.amount }
    val netCash = totalIn - totalOut

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Periode Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "HARI_INI" to "Hari Ini",
                    "BULAN_INI" to "Bulan Ini",
                    "TAHUN_INI" to "Tahun Ini",
                    "SEMUA" to "Semua"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedPeriod == key,
                        onClick = { selectedPeriod = key },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldLight,
                            selectedLabelColor = EmeraldPrimary
                        )
                    )
                }
            }
        }

        item {
            // KPI Summary Card Laporan
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "RINGKASAN ARUS KAS LAPORAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Net: " + FormatUtils.formatRupiah(netCash),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (netCash >= 0) EmeraldPrimary else DangerRed
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Masuk", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = FormatUtils.formatRupiah(totalIn),
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                fontSize = 14.sp
                            )
                        }
                        Column {
                            Text(text = "Total Keluar", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = FormatUtils.formatRupiah(totalOut),
                                fontWeight = FontWeight.Bold,
                                color = DangerRed,
                                fontSize = 14.sp
                            )
                        }
                        Column {
                            Text(text = "Total Transaksi", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = "${filteredList.size} tx",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val periodLabel = when (selectedPeriod) {
                                "HARI_INI" -> "Hari Ini ($todayStr)"
                                "BULAN_INI" -> "Bulan Ini"
                                "TAHUN_INI" -> "Tahun Ini ($currentYear)"
                                else -> "Semua Periode"
                            }
                            WhatsAppHelper.shareDailySummaryViaWhatsApp(
                                context,
                                periodLabel,
                                totalIn,
                                totalOut,
                                netCash,
                                netCash
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bagikan Laporan ke WhatsApp")
                    }
                }
            }
        }

        item {
            Text(
                text = "Daftar Transaksi Periode (${filteredList.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (filteredList.isEmpty()) {
            item {
                Text(
                    text = "Tidak ada transaksi pada periode yang dipilih.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            items(filteredList) { tx ->
                TransactionItemCard(
                    transaction = tx,
                    onItemClick = { onSelectTransaction(tx) },
                    onShareWhatsApp = {
                        WhatsAppHelper.shareTransactionViaWhatsApp(context, tx)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
