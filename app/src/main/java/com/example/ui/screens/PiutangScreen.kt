package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PiutangItem
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.util.FormatUtils
import com.example.util.WhatsAppHelper

@Composable
fun PiutangScreen(
    piutangList: List<PiutangItem>,
    onSavePiutang: (PiutangItem, (Boolean, String) -> Unit) -> Unit,
    onUpdatePiutang: (PiutangItem) -> Unit,
    onDeletePiutang: (String) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToPay by remember { mutableStateOf<PiutangItem?>(null) }

    val totalPiutang = piutangList.sumOf { it.amount }
    val totalTerbayar = piutangList.sumOf { it.paidAmount }
    val totalSisa = piutangList.sumOf { it.remainingAmount }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // Header & Ringkasan Piutang
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
                            Text(text = "Kelola Piutang Pelanggan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp)
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
                                Text(text = "Total Piutang", fontSize = 11.sp, color = Color.Gray)
                                Text(text = FormatUtils.formatRupiah(totalPiutang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text(text = "Sudah Dibayar", fontSize = 11.sp, color = Color.Gray)
                                Text(text = FormatUtils.formatRupiah(totalTerbayar), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldPrimary)
                            }
                            Column {
                                Text(text = "Sisa Piutang", fontSize = 11.sp, color = Color.Gray)
                                Text(text = FormatUtils.formatRupiah(totalSisa), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DangerRed)
                            }
                        }
                    }
                }
            }

            if (piutangList.isEmpty()) {
                item {
                    Text(text = "Belum ada catatan piutang.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                items(piutangList) { item ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.customerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = item.description, fontSize = 12.sp, color = Color.Gray)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (item.remainingAmount == 0.0) EmeraldLight else GoldLight
                                ) {
                                    Text(
                                        text = if (item.remainingAmount == 0.0) "Lunas" else "Sisa: ${FormatUtils.formatRupiah(item.remainingAmount)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.remainingAmount == 0.0) EmeraldPrimary else GoldAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Total: ${FormatUtils.formatRupiah(item.amount)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Jatuh Tempo: ${item.dueDate}", fontSize = 11.sp, color = DangerRed)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (item.remainingAmount > 0.0) {
                                    OutlinedButton(
                                        onClick = { itemToPay = item },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Bayar / Cicil", fontSize = 11.sp)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        val waMsg = """
                                            📌 *TAGIHAN PIUTANG USAHA*
                                            Kepada Yth: *${item.customerName}*
                                            Keterangan: ${item.description}
                                            Total Tagihan: ${FormatUtils.formatRupiah(item.amount)}
                                            Sudah Dibayar: ${FormatUtils.formatRupiah(item.paidAmount)}
                                            *Sisa Belum Lunas: ${FormatUtils.formatRupiah(item.remainingAmount)}*
                                            Jatuh Tempo: *${item.dueDate}*
                                            Rekening Tujuan: ${item.targetAccount}
                                            Mohon konfirmasi pembayaran terima kasih.
                                        """.trimIndent()
                                        WhatsAppHelper.shareCalculationViaWhatsApp(context, "Tagihan Piutang", waMsg)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kirim WA", color = EmeraldPrimary, fontSize = 11.sp)
                                }

                                IconButton(onClick = { onDeletePiutang(item.id) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPiutangDialog(
            onDismiss = { showAddDialog = false },
            onSave = { p ->
                onSavePiutang(p) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) showAddDialog = false
                }
            }
        )
    }

    if (itemToPay != null) {
        PayPiutangDialog(
            item = itemToPay!!,
            onDismiss = { itemToPay = null },
            onPay = { paid ->
                val newPaid = itemToPay!!.paidAmount + paid
                val newRemaining = Math.max(0.0, itemToPay!!.amount - newPaid)
                val newStatus = if (newRemaining == 0.0) "Lunas" else "Belum Jatuh Tempo"
                onUpdatePiutang(itemToPay!!.copy(paidAmount = newPaid, remainingAmount = newRemaining, status = newStatus))
                itemToPay = null
                Toast.makeText(context, "Pembayaran berhasil dicatat", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun AddPiutangDialog(
    onDismiss: () -> Unit,
    onSave: (PiutangItem) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(FormatUtils.getCurrentDate()) }
    var account by remember { mutableStateOf("Bank BCA") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Catatan Piutang", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Nama Pelanggan / Pihak") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan Tagihan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Nominal Piutang (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Tanggal Jatuh Tempo (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (customerName.isNotBlank() && amt > 0) {
                        onSave(
                            PiutangItem(
                                id = "PT-" + System.currentTimeMillis(),
                                date = FormatUtils.getCurrentDate(),
                                customerName = customerName,
                                description = description,
                                amount = amt,
                                dueDate = dueDate,
                                remainingAmount = amt,
                                targetAccount = account
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Simpan Piutang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun PayPiutangDialog(
    item: PiutangItem,
    onDismiss: () -> Unit,
    onPay: (Double) -> Unit
) {
    var payAmountText by remember { mutableStateOf(item.remainingAmount.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Pembayaran Piutang", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Pelanggan: ${item.customerName}", fontWeight = FontWeight.SemiBold)
                Text(text = "Sisa Tagihan: ${FormatUtils.formatRupiah(item.remainingAmount)}", color = DangerRed)
                OutlinedTextField(
                    value = payAmountText,
                    onValueChange = { payAmountText = it },
                    label = { Text("Nominal Dibayar (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paid = payAmountText.toDoubleOrNull() ?: 0.0
                    if (paid > 0) onPay(paid)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Konfirmasi Bayar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
