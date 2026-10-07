package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.KasTransaction
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NavySecondary
import com.example.util.FormatUtils
import com.example.util.WhatsAppHelper

@Composable
fun TransaksiScreen(
    transactions: List<KasTransaction>,
    selectedTransactionForDetail: KasTransaction? = null,
    onCloseDetail: () -> Unit,
    onSelectTransaction: (KasTransaction) -> Unit,
    onDeleteTransaction: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterType by remember { mutableStateOf("SEMUA") }

    val filteredList = transactions.filter { tx ->
        val matchesType = when (selectedFilterType) {
            "MASUK" -> tx.type == "MASUK"
            "KELUAR" -> tx.type == "KELUAR"
            "TRANSFER" -> tx.type == "TRANSFER"
            else -> true
        }

        val q = searchQuery.trim().lowercase()
        val matchesSearch = q.isEmpty() ||
                tx.id.lowercase().contains(q) ||
                tx.transactionName.lowercase().contains(q) ||
                tx.description.lowercase().contains(q) ||
                tx.account.lowercase().contains(q) ||
                tx.category.lowercase().contains(q) ||
                tx.proofNumber.lowercase().contains(q)

        matchesType && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari ID, nama, keterangan, akun...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Hapus")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_bar_transactions")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val chips = listOf(
                "SEMUA" to "Semua (${transactions.size})",
                "MASUK" to "Kas Masuk",
                "KELUAR" to "Kas Keluar",
                "TRANSFER" to "Transfer"
            )

            items(chips) { (key, label) ->
                FilterChip(
                    selected = selectedFilterType == key,
                    onClick = { selectedFilterType = key },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldLight,
                        selectedLabelColor = EmeraldPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Total Ditemukan: ${filteredList.size} Transaksi",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Tidak ada transaksi yang cocok", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
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
        }
    }

    // Modal Detail Transaksi
    if (selectedTransactionForDetail != null) {
        TransactionDetailDialog(
            transaction = selectedTransactionForDetail,
            onDismiss = onCloseDetail,
            onShareWhatsApp = {
                WhatsAppHelper.shareTransactionViaWhatsApp(context, selectedTransactionForDetail)
            },
            onDelete = {
                onDeleteTransaction(selectedTransactionForDetail.id)
                onCloseDetail()
            }
        )
    }
}

@Composable
fun TransactionDetailDialog(
    transaction: KasTransaction,
    onDismiss: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Detail Transaksi Kas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = transaction.id,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Nominal", fontSize = 11.sp, color = EmeraldPrimary)
                        Text(
                            text = FormatUtils.formatRupiah(transaction.amount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = EmeraldPrimary
                        )
                    }
                }

                DetailRow(label = "Jenis Transaksi", value = transaction.type)
                DetailRow(label = "Tanggal & Jam", value = "${transaction.date} ${transaction.time}")
                DetailRow(label = "Akun", value = transaction.account + if (transaction.toAccount.isNotEmpty()) " ➔ ${transaction.toAccount}" else "")
                DetailRow(label = "Nama Transaksi", value = transaction.transactionName)
                DetailRow(label = "Kategori", value = transaction.category)
                if (transaction.description.isNotEmpty()) DetailRow(label = "Keterangan", value = transaction.description)
                DetailRow(label = "Alokasi", value = transaction.allocation)
                DetailRow(label = "PIC", value = transaction.pic)
                if (transaction.proofNumber.isNotEmpty()) DetailRow(label = "No. Bukti", value = transaction.proofNumber)
                DetailRow(label = "Status", value = transaction.status)

                // Bukti Foto / Link Drive
                if (transaction.proofUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Bukti Foto / Dokumen:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable {
                                try {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(transaction.proofUrl))
                                    context.startActivity(browserIntent)
                                } catch (e: Exception) {
                                    // fallback
                                }
                            }
                    ) {
                        AsyncImage(
                            model = transaction.proofUrl,
                            contentDescription = "Bukti Drive",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(transaction.proofUrl))
                                context.startActivity(browserIntent)
                            } catch (e: Exception) {
                                // fallback
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka di Google Drive", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShareWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kirim WA")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                ) {
                    Text("Hapus")
                }
                TextButton(onClick = onDismiss) {
                    Text("Tutup")
                }
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Arsipkan & Hapus Transaksi?") },
            text = { Text("Data transaksi ini akan dihapus dari buku kas lokal dan diarsipkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
