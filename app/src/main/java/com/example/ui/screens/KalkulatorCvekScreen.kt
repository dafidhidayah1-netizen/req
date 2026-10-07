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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalculatorLogItem
import com.example.data.model.CvekCalculation
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavySecondary
import com.example.util.FormatUtils
import com.example.util.WhatsAppHelper

@Composable
fun KalkulatorCvekScreen(
    currentCalculation: CvekCalculation,
    calculatorLogs: List<CalculatorLogItem>,
    isSyncing: Boolean,
    onCalculate: (Double, Double, Double, Double, Double) -> Unit,
    onLogToSheets: (CalculatorLogItem, (Boolean, String) -> Unit) -> Unit,
    onApplyToTransaction: (type: String, amount: Double, name: String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: CVEK & Margin, 1: Hitung Persentase & Selisih

    var initialBalanceText by remember { mutableStateOf(currentCalculation.initialBalance.toInt().toString()) }
    var incomeText by remember { mutableStateOf(currentCalculation.totalIncome.toInt().toString()) }
    var fixedCostText by remember { mutableStateOf(currentCalculation.fixedCosts.toInt().toString()) }
    var variableCostText by remember { mutableStateOf(currentCalculation.variableCosts.toInt().toString()) }
    var targetProfitText by remember { mutableStateOf(currentCalculation.targetProfit.toInt().toString()) }

    // State untuk Tab Hitung Persentase & Selisih
    var baseAmountText by remember { mutableStateOf("1000000") }
    var percentText by remember { mutableStateOf("11") } // PPN 11% / Diskon
    var customNotes by remember { mutableStateOf("Estimasi Arus Kas") }

    fun recompute() {
        val init = initialBalanceText.toDoubleOrNull() ?: 0.0
        val inc = incomeText.toDoubleOrNull() ?: 0.0
        val fix = fixedCostText.toDoubleOrNull() ?: 0.0
        val varC = variableCostText.toDoubleOrNull() ?: 0.0
        val tgt = targetProfitText.toDoubleOrNull() ?: 0.0
        onCalculate(init, inc, fix, varC, tgt)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Mode Kalkulator
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("CVEK & Margin", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Persentase & Selisih", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            // HASIL METRIK UTAMA (SUMMARY CARD)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentCalculation.netMargin >= 0) EmeraldLight else Color(0xFFFFEBEE)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MARGIN / NET LABA OPERASIONAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentCalculation.netMargin >= 0) EmeraldPrimary else DangerRed,
                            letterSpacing = 0.5.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", currentCalculation.expensePercentage)}% Rasio Beban",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentCalculation.expensePercentage > 85) DangerRed else EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = FormatUtils.formatRupiah(currentCalculation.netMargin),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (currentCalculation.netMargin >= 0) EmeraldPrimary else DangerRed
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Pengeluaran", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = FormatUtils.formatRupiah(currentCalculation.totalExpense),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed
                            )
                        }
                        Column {
                            Text(text = "Proyeksi Saldo Akhir", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = FormatUtils.formatRupiah(currentCalculation.finalBalance),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavySecondary
                            )
                        }
                        Column {
                            Text(text = "Titik Impas (BEP)", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = FormatUtils.formatRupiah(currentCalculation.breakEvenPointUnits),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC67D00)
                            )
                        }
                    }
                }
            }

            // INPUT PARAMETER
            Text(text = "Parameter Analisis Keuangan", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            OutlinedTextField(
                value = initialBalanceText,
                onValueChange = {
                    initialBalanceText = it
                    recompute()
                },
                label = { Text("Nilai Kas Awal") },
                placeholder = { Text("10000000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cvek_input_initial"),
                singleLine = true
            )

            OutlinedTextField(
                value = incomeText,
                onValueChange = {
                    incomeText = it
                    recompute()
                },
                label = { Text("Pemasukan / Proyeksi Omzet") },
                placeholder = { Text("15000000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cvek_input_income"),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fixedCostText,
                    onValueChange = {
                        fixedCostText = it
                        recompute()
                    },
                    label = { Text("Biaya Tetap (Fixed Cost)") },
                    placeholder = { Text("Sewa, Gaji, dll") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cvek_input_fixed"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = variableCostText,
                    onValueChange = {
                        variableCostText = it
                        recompute()
                    },
                    label = { Text("Biaya Variabel (Variable)") },
                    placeholder = { Text("HPP, Bahan baku") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cvek_input_variable"),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = customNotes,
                onValueChange = { customNotes = it },
                label = { Text("Judul / Catatan Perhitungan") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // TOMBOL LOG KE GOOGLE SHEETS & BAGIKAN
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val logItem = CalculatorLogItem(
                            id = "LOG-" + System.currentTimeMillis(),
                            timestamp = FormatUtils.getCurrentTimestamp(),
                            title = customNotes.ifBlank { "Analisis Kas & Margin CVEK" },
                            initialBalance = currentCalculation.initialBalance,
                            income = currentCalculation.totalIncome,
                            fixedCosts = currentCalculation.fixedCosts,
                            variableCosts = currentCalculation.variableCosts,
                            totalExpense = currentCalculation.totalExpense,
                            netMargin = currentCalculation.netMargin,
                            expensePercentage = currentCalculation.expensePercentage,
                            finalBalance = currentCalculation.finalBalance,
                            formulaOrNote = "BEP: ${FormatUtils.formatRupiah(currentCalculation.breakEvenPointUnits)}"
                        )
                        onLogToSheets(logItem) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_log_calculator_to_sheets")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mencatat...", fontSize = 12.sp)
                    } else {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log ke Google Sheets", fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = {
                        val details = """
                            • Kas Awal: ${FormatUtils.formatRupiah(currentCalculation.initialBalance)}
                            • Omzet/In: ${FormatUtils.formatRupiah(currentCalculation.totalIncome)}
                            • Fixed Cost: ${FormatUtils.formatRupiah(currentCalculation.fixedCosts)}
                            • Variable: ${FormatUtils.formatRupiah(currentCalculation.variableCosts)}
                            • Beban: ${FormatUtils.formatRupiah(currentCalculation.totalExpense)}
                            • Net Margin: *${FormatUtils.formatRupiah(currentCalculation.netMargin)}*
                            • Saldo Akhir: *${FormatUtils.formatRupiah(currentCalculation.finalBalance)}*
                        """.trimIndent()
                        WhatsAppHelper.shareCalculationViaWhatsApp(context, customNotes, details)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kirim WA", color = EmeraldPrimary, fontSize = 12.sp)
                }
            }

            // SHORTCUT KE BUKU KAS
            Text(text = "Terapkan Hasil Langsung ke Buku Kas", fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val incVal = incomeText.toDoubleOrNull() ?: 0.0
                        if (incVal > 0) onApplyToTransaction("MASUK", incVal, "Pemasukan Omzet $customNotes")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ Kas Masuk", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        val expVal = currentCalculation.totalExpense
                        if (expVal > 0) onApplyToTransaction("KELUAR", expVal, "Beban Operasional $customNotes")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("- Kas Keluar", fontSize = 12.sp)
                }
            }
        } else {
            // TAB HITUNG PERSENTASE & PAJAK/DISKON
            val baseVal = baseAmountText.toDoubleOrNull() ?: 0.0
            val pctVal = percentText.toDoubleOrNull() ?: 0.0
            val calculatedPercentVal = (baseVal * pctVal) / 100.0
            val totalPlus = baseVal + calculatedPercentVal
            val totalMinus = baseVal - calculatedPercentVal

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "HASIL PERHITUNGAN PERSENTASE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavySecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nilai $pctVal%: ${FormatUtils.formatRupiah(calculatedPercentVal)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = NavySecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "+ Pajak/Markup: ${FormatUtils.formatRupiah(totalPlus)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        Text(text = "- Diskon: ${FormatUtils.formatRupiah(totalMinus)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                    }
                }
            }

            OutlinedTextField(
                value = baseAmountText,
                onValueChange = { baseAmountText = it },
                label = { Text("Nominal Dasar (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = percentText,
                onValueChange = { percentText = it },
                label = { Text("Persentase (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // RIWAYAT LOG KALKULATOR
        if (calculatorLogs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Riwayat Log Kalkulasi (Tersinkron Sheet Kalkulator)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            calculatorLogs.take(5).forEach { log ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = log.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = log.timestamp.take(10), fontSize = 11.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Net Margin: ${FormatUtils.formatRupiah(log.netMargin)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            Text(text = "Beban: ${FormatUtils.formatRupiah(log.totalExpense)}", fontSize = 12.sp, color = DangerRed)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
