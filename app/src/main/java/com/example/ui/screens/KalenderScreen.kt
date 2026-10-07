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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarCashflow
import com.example.data.model.CalendarEventItem
import com.example.data.model.KasTransaction
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NavySecondary
import com.example.util.CalendarHelper
import com.example.util.FormatUtils
import com.example.util.WhatsAppHelper

@Composable
fun KalenderScreen(
    cashflows: List<CalendarCashflow>,
    allTransactions: List<KasTransaction>,
    calendarEvents: List<CalendarEventItem>,
    onSaveCalendarEvent: (CalendarEventItem, (Boolean, String) -> Unit) -> Unit,
    onDeleteCalendarEvent: (String) -> Unit,
    onSelectTransaction: (KasTransaction) -> Unit
) {
    val context = LocalContext.current
    var selectedDate by remember {
        mutableStateOf(cashflows.firstOrNull()?.date ?: FormatUtils.getCurrentDate())
    }

    var showAddEventDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<CalendarEventItem?>(null) }

    val selectedDayCashflow = cashflows.find { it.date == selectedDate }
    val dayTransactions = allTransactions.filter { it.date == selectedDate }
    val dayEvents = calendarEvents.filter { it.date == selectedDate }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header Info Kalender Kas
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Kalender Arus Kas & Event",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Sinkronisasi Real-Time Google Sheets",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { showAddEventDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_add_calendar_event")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Event", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Detail Hari Terpilih
                        if (selectedDayCashflow != null) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${selectedDayCashflow.dayOfWeek}, $selectedDate",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (selectedDayCashflow.status == "Positif") EmeraldLight else DangerRedLight
                                        ) {
                                            Text(
                                                text = selectedDayCashflow.status,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedDayCashflow.status == "Positif") EmeraldPrimary else DangerRed,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(text = "Masuk (${selectedDayCashflow.countIn} tx)", fontSize = 11.sp, color = Color.Gray)
                                            Text(
                                                text = FormatUtils.formatRupiah(selectedDayCashflow.totalIn),
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldPrimary,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Column {
                                            Text(text = "Keluar (${selectedDayCashflow.countOut} tx)", fontSize = 11.sp, color = Color.Gray)
                                            Text(
                                                text = FormatUtils.formatRupiah(selectedDayCashflow.totalOut),
                                                fontWeight = FontWeight.Bold,
                                                color = DangerRed,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Column {
                                            Text(text = "Net Arus Kas", fontSize = 11.sp, color = Color.Gray)
                                            Text(
                                                text = FormatUtils.formatRupiah(selectedDayCashflow.net),
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedDayCashflow.net >= 0) EmeraldPrimary else DangerRed,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Tombol Google Kalender & WhatsApp
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val title = "Review Kas: ${FormatUtils.formatRupiah(selectedDayCashflow.net)}"
                                                val desc = "Total Masuk: ${FormatUtils.formatRupiah(selectedDayCashflow.totalIn)}\nTotal Keluar: ${FormatUtils.formatRupiah(selectedDayCashflow.totalOut)}"
                                                CalendarHelper.addToGoogleCalendar(context, title, desc, selectedDate)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_add_google_calendar")
                                        ) {
                                            Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Google Kalender", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                WhatsAppHelper.shareDailySummaryViaWhatsApp(
                                                    context,
                                                    selectedDate,
                                                    selectedDayCashflow.totalIn,
                                                    selectedDayCashflow.totalOut,
                                                    selectedDayCashflow.net,
                                                    selectedDayCashflow.totalIn - selectedDayCashflow.totalOut
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_share_daily_wa")
                                        ) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Kirim WA", color = EmeraldPrimary, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // DAFTAR EVENT / JADWAL PADA TANGGAL INI
            if (dayEvents.isNotEmpty()) {
                item {
                    Text(
                        text = "Jadwal & Pengingat Kas ($selectedDate)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(dayEvents) { ev ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val updated = ev.copy(isCompleted = !ev.isCompleted)
                                    onSaveCalendarEvent(updated) { _, _ -> }
                                }
                            ) {
                                Icon(
                                    imageVector = if (ev.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (ev.isCompleted) EmeraldPrimary else Color.Gray
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ev.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${ev.time} • ${ev.type}" + if (ev.amount > 0) " • ${FormatUtils.formatRupiah(ev.amount)}" else "",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (ev.description.isNotEmpty()) {
                                    Text(
                                        text = ev.description,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            IconButton(onClick = { onDeleteCalendarEvent(ev.id) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // PILIH HARI LAIN
            item {
                Text(
                    text = "Riwayat Arus Kas per Tanggal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (cashflows.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada transaksi tersimpan untuk kalender.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                items(cashflows) { item ->
                    val isSelected = item.date == selectedDate
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDate = item.date }
                            .testTag("calendar_day_${item.date}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${item.dayOfWeek}, ${item.date}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${item.countIn} Masuk • ${item.countOut} Keluar",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Net: " + FormatUtils.formatRupiah(item.net),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (item.net >= 0) EmeraldPrimary else DangerRed
                                )
                                Text(
                                    text = "In: " + FormatUtils.formatRupiah(item.totalIn),
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // TRANSAKSI PADA HARI TERPILIH
            if (dayTransactions.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Transaksi Kas pada $selectedDate (${dayTransactions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(dayTransactions) { tx ->
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
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // DIALOG TAMBAH EVENT KALENDER
    if (showAddEventDialog) {
        AddCalendarEventDialog(
            initialDate = selectedDate,
            onDismiss = { showAddEventDialog = false },
            onSave = { event ->
                onSaveCalendarEvent(event) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) showAddEventDialog = false
                }
            }
        )
    }
}

@Composable
fun AddCalendarEventDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onSave: (CalendarEventItem) -> Unit
) {
    var date by remember { mutableStateOf(initialDate) }
    var time by remember { mutableStateOf("09:00") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PENGINGAT") }
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Event Kalender Kas", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Event / Jadwal") },
                    placeholder = { Text("Setor Kas BCA, Tagihan Supplier") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Tanggal") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Jam") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Nominal Terkait (Opsional)") },
                    placeholder = { Text("1500000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan Tambahan") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val ev = CalendarEventItem(
                        id = "EV-" + System.currentTimeMillis(),
                        date = date,
                        time = time,
                        title = title,
                        description = description,
                        type = type,
                        amount = amountText.toDoubleOrNull() ?: 0.0
                    )
                    onSave(ev)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Simpan Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
