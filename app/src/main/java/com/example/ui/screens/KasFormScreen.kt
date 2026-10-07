package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.KasTransaction
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

@Composable
fun KasFormScreen(
    initialType: String = "MASUK",
    master: MasterCategories,
    isSyncing: Boolean,
    isUploadingProof: Boolean,
    uploadProofMessage: String,
    onSaveTransaction: (KasTransaction, (Boolean, String) -> Unit) -> Unit,
    onUploadProof: (Uri, (String) -> Unit, (String) -> Unit) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(initialType) }

    fun generateNewId(type: String): String {
        val p = when (type) {
            "MASUK" -> "KM"
            "KELUAR" -> "KK"
            else -> "TR"
        }
        return FormatUtils.generateTransactionId(p)
    }

    var txId by remember(selectedType) { mutableStateOf(generateNewId(selectedType)) }
    var txDate by remember { mutableStateOf(FormatUtils.getCurrentDate()) }
    var txTime by remember { mutableStateOf(FormatUtils.getCurrentTime()) }

    var account by remember { mutableStateOf(master.accounts.firstOrNull() ?: "Kas Tunai") }
    var toAccount by remember { mutableStateOf(master.accounts.getOrNull(1) ?: "Bank BCA") }
    var transactionName by remember { mutableStateOf("") }
    var category by remember(selectedType) {
        mutableStateOf(if (selectedType == "MASUK") master.inCategories.first() else master.outCategories.first())
    }
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var allocation by remember { mutableStateOf(master.allocations.first()) }
    var pic by remember { mutableStateOf(master.pics.first()) }
    var proofUrl by remember { mutableStateOf("") }
    var proofNumber by remember { mutableStateOf("") }
    var project by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Selesai") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    fun resetForm() {
        txId = generateNewId(selectedType)
        txDate = FormatUtils.getCurrentDate()
        txTime = FormatUtils.getCurrentTime()
        transactionName = ""
        description = ""
        amountText = ""
        proofUrl = ""
        proofNumber = ""
        project = ""
        note = ""
        selectedImageUri = null
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            onUploadProof(
                uri,
                { uploadedUrl ->
                    proofUrl = uploadedUrl
                    Toast.makeText(context, "Bukti berhasil diunggah ke Google Drive!", Toast.LENGTH_SHORT).show()
                },
                { error ->
                    Toast.makeText(context, "Gagal unggah bukti: $error", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    val typeColor = when (selectedType) {
        "MASUK" -> EmeraldPrimary
        "KELUAR" -> DangerRed
        else -> GoldAccent
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tab Switcher Jenis Transaksi
        TabRow(
            selectedTabIndex = when (selectedType) {
                "MASUK" -> 0
                "KELUAR" -> 1
                else -> 2
            },
            containerColor = MaterialTheme.colorScheme.surface,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[when (selectedType) {
                        "MASUK" -> 0
                        "KELUAR" -> 1
                        else -> 2
                    }]),
                    color = typeColor
                )
            }
        ) {
            Tab(
                selected = selectedType == "MASUK",
                onClick = {
                    selectedType = "MASUK"
                    txId = generateNewId("MASUK")
                    category = master.inCategories.first()
                },
                text = { Text("Kas Masuk", fontWeight = FontWeight.Bold, color = if (selectedType == "MASUK") EmeraldPrimary else MaterialTheme.colorScheme.onSurface) }
            )
            Tab(
                selected = selectedType == "KELUAR",
                onClick = {
                    selectedType = "KELUAR"
                    txId = generateNewId("KELUAR")
                    category = master.outCategories.first()
                },
                text = { Text("Kas Keluar", fontWeight = FontWeight.Bold, color = if (selectedType == "KELUAR") DangerRed else MaterialTheme.colorScheme.onSurface) }
            )
            Tab(
                selected = selectedType == "TRANSFER",
                onClick = {
                    selectedType = "TRANSFER"
                    txId = generateNewId("TRANSFER")
                },
                text = { Text("Transfer", fontWeight = FontWeight.Bold, color = if (selectedType == "TRANSFER") GoldAccent else MaterialTheme.colorScheme.onSurface) }
            )
        }

        // Header Card dengan ID dan Waktu Otomatis Permanen
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "ID TRANSAKSI (AUTO-GENERATED)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = txId, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = typeColor)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Tanggal & Jam Otomatis", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "$txDate $txTime", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Nominal Input dengan Format Rupiah
        val amountValue = amountText.toDoubleOrNull() ?: 0.0
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (selectedType) {
                    "MASUK" -> EmeraldLight
                    "KELUAR" -> DangerRedLight
                    else -> GoldLight
                }
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "NOMINAL TRANSAKSI (RUPIAH)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) amountText = input
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_input_amount"),
                    placeholder = { Text("Contoh: 2500000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = typeColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Terbilang: ${FormatUtils.formatRupiah(amountValue)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = typeColor
                )
            }
        }

        // Akun & Transfer
        if (selectedType == "TRANSFER") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DropdownField(
                    label = "Dari Akun",
                    selected = account,
                    options = master.accounts,
                    modifier = Modifier.weight(1f),
                    onSelect = { account = it }
                )
                DropdownField(
                    label = "Ke Akun",
                    selected = toAccount,
                    options = master.accounts.filter { it != account },
                    modifier = Modifier.weight(1f),
                    onSelect = { toAccount = it }
                )
            }
        } else {
            DropdownField(
                label = "Akun Kas / Bank",
                selected = account,
                options = master.accounts,
                modifier = Modifier.fillMaxWidth(),
                onSelect = { account = it }
            )

            // Nama Barang / Transaksi & Kategori
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = transactionName,
                    onValueChange = { transactionName = it },
                    label = { Text(if (selectedType == "MASUK") "Nama Transaksi" else "Nama Barang / Transaksi") },
                    placeholder = { Text(if (selectedType == "MASUK") "Penjualan Barang / Jasa" else "Beli Bahan, Ayam, Operasional") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_input_tx_name"),
                    singleLine = true
                )
                val catList = if (selectedType == "MASUK") master.inCategories else master.outCategories
                DropdownField(
                    label = "Kategori",
                    selected = category,
                    options = catList,
                    modifier = Modifier.weight(1f),
                    onSelect = { category = it }
                )
            }
        }

        // Keterangan
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Keterangan") },
            placeholder = { Text("Keterangan transaksi...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_input_desc"),
            minLines = 2
        )

        // Metadata: Alokasi Anggaran, PIC
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DropdownField(
                label = "Alokasi Anggaran",
                selected = allocation,
                options = master.allocations,
                modifier = Modifier.weight(1f),
                onSelect = { allocation = it }
            )
            DropdownField(
                label = "PIC (Penanggung Jawab)",
                selected = pic,
                options = master.pics,
                modifier = Modifier.weight(1f),
                onSelect = { pic = it }
            )
        }

        // Nomor Bukti / Nota & Proyek/Unit
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = proofNumber,
                onValueChange = { proofNumber = it },
                label = { Text("No. Nota / Kwitansi") },
                placeholder = { Text("NOTA-0012") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = project,
                onValueChange = { project = it },
                label = { Text("Proyek / Unit") },
                placeholder = { Text("Unit Operasional") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        // UPLOAD BUKTI FOTO / KWITANSI KE GOOGLE DRIVE REAL-TIME
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Upload Bukti Kwitansi / Nota",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Otomatis tersimpan ke Google Drive Bukti_Kas",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("btn_pick_proof_photo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Foto / Galeri", fontSize = 12.sp)
                    }
                }

                if (isUploadingProof) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = uploadProofMessage, fontSize = 12.sp, color = EmeraldPrimary)
                    }
                }

                if (selectedImageUri != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Preview Bukti",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Foto Nota / Kwitansi Terpilih", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(
                                text = if (proofUrl.isNotEmpty()) "Tersinkron ke Google Drive" else "Mengunggah...",
                                fontSize = 11.sp,
                                color = if (proofUrl.isNotEmpty()) EmeraldPrimary else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = proofUrl,
                    onValueChange = { proofUrl = it },
                    label = { Text("URL Link Bukti Google Drive") },
                    placeholder = { Text("https://drive.google.com/...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Catatan
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Catatan Internal (Opsional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        // DUA TOMBOL AKSI UTAMA:
        // 1. [SIMPAN KELUAR] -> Simpan & kembali ke dashboard
        // 2. [SIMPAN & LANJUTKAN] -> Simpan & form langsung kosong kembali untuk transaksi berikutnya!
        fun buildTransactionObject(): KasTransaction? {
            if (amountValue <= 0) {
                Toast.makeText(context, "Nominal harus lebih besar dari 0!", Toast.LENGTH_SHORT).show()
                return null
            }
            return KasTransaction(
                id = txId,
                type = selectedType,
                date = txDate,
                time = txTime,
                account = account,
                toAccount = if (selectedType == "TRANSFER") toAccount else "",
                transactionName = transactionName.ifEmpty { if (selectedType == "TRANSFER") "Transfer Antar Akun" else category },
                category = category,
                description = description.ifEmpty { transactionName },
                amount = amountValue,
                allocation = allocation,
                pic = pic,
                proofUrl = proofUrl,
                proofNumber = proofNumber,
                project = project,
                note = note,
                status = status,
                inputTime = FormatUtils.getCurrentTimestamp()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // TOMBOL 1: SIMPAN KELUAR
            Button(
                onClick = {
                    val tx = buildTransactionObject() ?: return@Button
                    onSaveTransaction(tx) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) onNavigateBack()
                    }
                },
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = typeColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_simpan_keluar")
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Menyimpan...", fontSize = 12.sp)
                } else {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan Keluar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // TOMBOL 2: SIMPAN & LANJUTKAN
            Button(
                onClick = {
                    val tx = buildTransactionObject() ?: return@Button
                    onSaveTransaction(tx) { success, msg ->
                        Toast.makeText(context, "✅ Berhasil Disimpan! Saldo langsung terupdate.", Toast.LENGTH_SHORT).show()
                        if (success) {
                            resetForm()
                        }
                    }
                },
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_simpan_lanjutkan")
            ) {
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simpan & Lanjutkan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // BAGIKAN VIA WHATSAPP
        OutlinedButton(
            onClick = {
                val draftTx = buildTransactionObject() ?: return@OutlinedButton
                WhatsAppHelper.shareTransactionViaWhatsApp(context, draftTx)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_share_form_wa")
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bagikan Bukti Kwitansi via WhatsApp", color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
