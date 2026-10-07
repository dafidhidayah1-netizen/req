package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeletedKasTransaction
import com.example.ui.theme.EmeraldPrimary
import com.example.util.FormatUtils

@Composable
fun ArsipDihapusScreen(
    deletedList: List<DeletedKasTransaction>,
    onRestore: (DeletedKasTransaction, () -> Unit) -> Unit
) {
    val context = LocalContext.current

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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Arsip Transaksi yang Dihapus", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "Data yang dihapus tidak langsung hilang permanen, Anda dapat memulihkannya kembali kapan saja.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        if (deletedList.isEmpty()) {
            item {
                Text(text = "Tidak ada transaksi dalam arsip terhapus.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            items(deletedList) { item ->
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
                                Text(text = item.transactionName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "${item.id} • ${item.date}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(
                                text = FormatUtils.formatRupiah(item.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Dihapus pada: ${item.deletedAt} oleh ${item.deletedBy} (Sumber: ${item.source})",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                onRestore(item) {
                                    Toast.makeText(context, "Transaksi ${item.id} berhasil dipulihkan!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pulihkan Transaksi Ini")
                        }
                    }
                }
            }
        }
    }
}
