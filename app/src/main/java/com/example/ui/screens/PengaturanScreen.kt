package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NavySecondary
import com.example.util.AppsScriptTemplate

@Composable
fun PengaturanScreen(
    currentUrl: String,
    autoSync: Boolean,
    isSyncing: Boolean,
    syncMessage: String,
    onSaveSettings: (String, Boolean) -> Unit,
    onTestConnection: (String, (Boolean, String) -> Unit) -> Unit,
    onTriggerBackup: ((Boolean, String) -> Unit) -> Unit,
    onSyncNow: () -> Unit,
    onHapusSemuaDataPalsu: (() -> Unit) -> Unit = {}
) {
    val context = LocalContext.current
    var urlText by remember { mutableStateOf(currentUrl) }
    var autoSyncState by remember { mutableStateOf(autoSync) }
    var testResult by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Pengaturan
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(EmeraldLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Integrasi Google Sheets Cloud",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "100% Gratis Tanpa Billing API",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // KONFIGURASI WEB APP URL
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Endpoint Web App Google Apps Script",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Salin URL Web App dari deployment Google Sheets Anda (https://script.google.com/macros/s/.../exec)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = urlText,
                    onValueChange = {
                        urlText = it
                        onSaveSettings(it, autoSyncState)
                    },
                    placeholder = { Text("https://script.google.com/macros/s/AKfycb.../exec") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_web_app_url"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (urlText.isBlank()) {
                                Toast.makeText(context, "Masukkan URL Web App terlebih dahulu", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onTestConnection(urlText) { success, msg ->
                                isSuccess = success
                                testResult = msg
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isSyncing,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_test_connection")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Menguji...", fontSize = 12.sp)
                        } else {
                            Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Uji Koneksi Live", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onSyncNow,
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isSyncing,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_sync_now_settings")
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Sekarang", fontSize = 12.sp)
                    }
                }

                if (testResult.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSuccess) EmeraldLight else Color(0xFFFFEBEE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = testResult,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSuccess) EmeraldPrimary else Color.Red,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // AUTO-SYNC TOGGLE & BACKUP CLOUD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Auto-Sync Real-Time", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "Otomatis dorong setiap transaksi ke Google Sheets saat disimpan",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoSyncState,
                        onCheckedChange = {
                            autoSyncState = it
                            onSaveSettings(urlText, it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider()
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Backup ke Google Drive", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "Memicu backupKeDrive() dan menduplikasi spreadsheet di Drive",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            onTriggerBackup { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_trigger_backup")
                    ) {
                        Text("Backup", fontSize = 12.sp)
                    }
                }
            }
        }

        // PANDUAN CEPAT & SALIN KODE APPS SCRIPT
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Kode Script Web App Google Sheets", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("AppsScript_Web_App", AppsScriptTemplate.COMPANION_SCRIPT)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Kode Apps Script berhasil disalin ke Clipboard!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_copy_apps_script")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin Kode", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Langkah Pemasangan 1 Menit:\n" +
                            "1. Buka file Google Sheets Anda.\n" +
                            "2. Klik menu Extensions > Apps Script.\n" +
                            "3. Tempel kode ini di bagian bawah Code.gs.\n" +
                            "4. Klik tombol Deploy (Terapkan) > New deployment (Penerapan baru).\n" +
                            "5. Pilih 'Web app', Execute as: 'Me', Who has access: 'Anyone'.\n" +
                            "6. Klik Deploy, salin Web app URL dan tempel ke kolom di atas.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "function doGet(e) { ... }\nfunction doPost(e) { ... }\n// Mendukung ADD_TRANSACTION, UPLOAD_BUKTI, SYNC, BACKUP, RECORD_AUDIT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // PEMBERSIHAN DATA PALSU / RESET DATABASE MURNI REAL
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DangerRedLight.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Hapus Semua Data Palsu / Reset", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DangerRed)
                        Text(
                            text = "Mengosongkan semua transaksi uji coba/dummy agar pembukuan kas 100% data real audit.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_purge_fake_settings")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bersihkan", fontSize = 11.sp)
                    }
                }
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                icon = { Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = DangerRed) },
                title = { Text("Hapus Semua Data Palsu?", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Tindakan ini akan menghapus seluruh data contoh/uji coba dari database lokal secara permanen. Sistem akan bersih 100% untuk pencatatan riil.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetDialog = false
                            onHapusSemuaDataPalsu {
                                Toast.makeText(context, "Seluruh data palsu berhasil dibersihkan! Database siap untuk data real.", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Ya, Bersihkan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
