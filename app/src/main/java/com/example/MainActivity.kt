package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KasTransaction
import com.example.ui.components.KasTopAppBar
import com.example.ui.screens.AnggaranScreen
import com.example.ui.screens.ArsipDihapusScreen
import com.example.ui.screens.AuditScreen
import com.example.ui.screens.BantuanScreen
import com.example.ui.screens.BukuCatatanScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GajiKaryawanScreen
import com.example.ui.screens.KalenderScreen
import com.example.ui.screens.KalkulatorCvekScreen
import com.example.ui.screens.KasFormScreen
import com.example.ui.screens.LaporanScreen
import com.example.ui.screens.PengaturanScreen
import com.example.ui.screens.PiutangScreen
import com.example.ui.screens.TransaksiScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.KasViewModel

enum class KasNavScreen(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Beranda", Icons.Default.Dashboard),
    TRANSAKSI("Transaksi", Icons.Default.ReceiptLong),
    LAPORAN("Laporan", Icons.Default.Assessment),
    KALENDER("Kalender", Icons.Default.CalendarMonth),
    KALKULATOR("Kalkulator", Icons.Default.Calculate),
    PENGATURAN("Pengaturan", Icons.Default.Settings),

    // Sub-layar operasional lengkap
    FORM("Input Kas", Icons.Default.Add),
    PIUTANG("Kelola Piutang", Icons.Default.Assessment),
    ANGGARAN("Anggaran", Icons.Default.Assessment),
    GAJI_KARYAWAN("Gaji & Absensi", Icons.Default.Assessment),
    AUDIT("Rekap Audit", Icons.Default.FactCheck),
    CATATAN("Buku Catatan", Icons.Default.Assessment),
    ARSIP_DIHAPUS("Arsip Dihapus", Icons.Default.Assessment),
    BANTUAN("Bantuan", Icons.Default.Assessment)
}

class MainActivity : ComponentActivity() {

    private val viewModel: KasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: KasViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(KasNavScreen.DASHBOARD) }
    var formInitialType by remember { mutableStateOf("MASUK") }
    var selectedTxForDetail by remember { mutableStateOf<KasTransaction?>(null) }

    val transactions by viewModel.transactions.collectAsState()
    val summary by viewModel.dashboardSummary.collectAsState()
    val accounts by viewModel.accountBalances.collectAsState()
    val cashflows by viewModel.calendarCashflows.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val cvekCalc by viewModel.cvekCalculation.collectAsState()
    val calculatorLogs by viewModel.calculatorLogs.collectAsState()
    val piutangList by viewModel.piutangList.collectAsState()
    val anggaranList by viewModel.anggaranList.collectAsState()
    val notesList by viewModel.notesList.collectAsState()
    val deletedList by viewModel.deletedTransactions.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    val karyawanList by viewModel.karyawanList.collectAsState()
    val absensiList by viewModel.absensiList.collectAsState()
    val slipGajiList by viewModel.slipGajiList.collectAsState()
    val rincianAnggaranList by viewModel.rincianAnggaranList.collectAsState()
    val auditList by viewModel.auditList.collectAsState()

    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val isUploadingProof by viewModel.isUploadingProof.collectAsState()
    val uploadProofMsg by viewModel.uploadProofMessage.collectAsState()
    val webAppUrl by viewModel.webAppUrl.collectAsState()
    val autoSync by viewModel.autoSync.collectAsState()

    BackHandler(enabled = currentScreen != KasNavScreen.DASHBOARD) {
        currentScreen = KasNavScreen.DASHBOARD
    }

    Scaffold(
        topBar = {
            KasTopAppBar(
                title = when (currentScreen) {
                    KasNavScreen.DASHBOARD -> "Sistem Kas Terintegrasi"
                    KasNavScreen.TRANSAKSI -> "Buku Transaksi Semua Akun"
                    KasNavScreen.LAPORAN -> "Laporan Arus Kas Semua Akun"
                    KasNavScreen.FORM -> if (formInitialType == "MASUK") "Input Kas Masuk" else if (formInitialType == "KELUAR") "Input Kas Keluar" else "Transfer Antar Akun"
                    KasNavScreen.KALENDER -> "Kalender Kas & Google Calendar"
                    KasNavScreen.KALKULATOR -> "Kalkulator CVEK & Google Sheets"
                    KasNavScreen.PIUTANG -> "Kelola Piutang & Jatuh Tempo"
                    KasNavScreen.ANGGARAN -> "Anggaran & Realisasi Alokasi"
                    KasNavScreen.GAJI_KARYAWAN -> "Gaji & Absensi Karyawan"
                    KasNavScreen.AUDIT -> "Rekap Audit & Opname Kas Riil"
                    KasNavScreen.CATATAN -> "Buku Catatan & Memo Kas"
                    KasNavScreen.ARSIP_DIHAPUS -> "Arsip Transaksi Dihapus"
                    KasNavScreen.PENGATURAN -> "Integrasi Google Sheets & Drive"
                    KasNavScreen.BANTUAN -> "Bantuan & Alur Sistem Kas"
                },
                subtitle = if (webAppUrl.isNotEmpty()) "Sheets Live Sync" else "Mode Offline Mandiri",
                isConnected = webAppUrl.isNotEmpty(),
                isSyncing = isSyncing,
                onSyncClick = {
                    viewModel.syncNow { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        },
        bottomBar = {
            val bottomNavItems = listOf(
                KasNavScreen.DASHBOARD,
                KasNavScreen.TRANSAKSI,
                KasNavScreen.LAPORAN,
                KasNavScreen.KALENDER,
                KasNavScreen.KALKULATOR,
                KasNavScreen.PENGATURAN
            )

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = currentScreen == KasNavScreen.DASHBOARD || currentScreen == KasNavScreen.TRANSAKSI || currentScreen == KasNavScreen.LAPORAN
            ) {
                FloatingActionButton(
                    onClick = {
                        formInitialType = "MASUK"
                        currentScreen = KasNavScreen.FORM
                    },
                    containerColor = EmeraldPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_transaction")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Transaksi")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                KasNavScreen.DASHBOARD -> {
                    DashboardScreen(
                        summary = summary,
                        accounts = accounts,
                        recentTransactions = transactions,
                        syncMessage = syncMessage,
                        unsyncedCount = unsyncedCount,
                        onNavigateToForm = { type ->
                            formInitialType = type
                            currentScreen = KasNavScreen.FORM
                        },
                        onNavigateToScreen = { screenKey ->
                            currentScreen = when (screenKey) {
                                "TRANSAKSI" -> KasNavScreen.TRANSAKSI
                                "LAPORAN" -> KasNavScreen.LAPORAN
                                "KALENDER" -> KasNavScreen.KALENDER
                                "KALKULATOR" -> KasNavScreen.KALKULATOR
                                "PIUTANG" -> KasNavScreen.PIUTANG
                                "ANGGARAN" -> KasNavScreen.ANGGARAN
                                "GAJI_KARYAWAN" -> KasNavScreen.GAJI_KARYAWAN
                                "AUDIT" -> KasNavScreen.AUDIT
                                "CATATAN" -> KasNavScreen.CATATAN
                                "ARSIP_DIHAPUS" -> KasNavScreen.ARSIP_DIHAPUS
                                "PENGATURAN" -> KasNavScreen.PENGATURAN
                                "BANTUAN" -> KasNavScreen.BANTUAN
                                else -> KasNavScreen.DASHBOARD
                            }
                        },
                        onSelectTransaction = { tx ->
                            selectedTxForDetail = tx
                            currentScreen = KasNavScreen.TRANSAKSI
                        },
                        onSyncNow = {
                            viewModel.syncNow { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                KasNavScreen.TRANSAKSI -> {
                    TransaksiScreen(
                        transactions = transactions,
                        selectedTransactionForDetail = selectedTxForDetail,
                        onCloseDetail = { selectedTxForDetail = null },
                        onSelectTransaction = { tx -> selectedTxForDetail = tx },
                        onDeleteTransaction = { id ->
                            val tx = transactions.find { it.id == id }
                            if (tx != null) {
                                viewModel.archiveAndDeleteTransaction(tx)
                                Toast.makeText(context, "Transaksi dipindahkan ke Arsip Dihapus (Dapat dipulihkan)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                KasNavScreen.FORM -> {
                    KasFormScreen(
                        initialType = formInitialType,
                        master = viewModel.master,
                        isSyncing = isSyncing,
                        isUploadingProof = isUploadingProof,
                        uploadProofMessage = uploadProofMsg,
                        onSaveTransaction = { tx, cb ->
                            viewModel.saveTransaction(tx, cb)
                        },
                        onUploadProof = { uri, onSuccess, onError ->
                            viewModel.uploadProofPhoto(uri, onSuccess, onError)
                        },
                        onNavigateBack = {
                            currentScreen = KasNavScreen.DASHBOARD
                        }
                    )
                }

                KasNavScreen.LAPORAN -> {
                    LaporanScreen(
                        transactions = transactions,
                        onSelectTransaction = { tx ->
                            selectedTxForDetail = tx
                            currentScreen = KasNavScreen.TRANSAKSI
                        }
                    )
                }

                KasNavScreen.KALENDER -> {
                    KalenderScreen(
                        cashflows = cashflows,
                        allTransactions = transactions,
                        calendarEvents = calendarEvents,
                        onSaveCalendarEvent = { ev, cb ->
                            viewModel.saveCalendarEvent(ev, cb)
                        },
                        onDeleteCalendarEvent = { id ->
                            viewModel.deleteCalendarEvent(id)
                            Toast.makeText(context, "Event kalender dihapus", Toast.LENGTH_SHORT).show()
                        },
                        onSelectTransaction = { tx ->
                            selectedTxForDetail = tx
                            currentScreen = KasNavScreen.TRANSAKSI
                        }
                    )
                }

                KasNavScreen.KALKULATOR -> {
                    KalkulatorCvekScreen(
                        currentCalculation = cvekCalc,
                        calculatorLogs = calculatorLogs,
                        isSyncing = isSyncing,
                        onCalculate = { init, inc, fix, varC, tgt ->
                            viewModel.calculateCvek(init, inc, fix, varC, tgt)
                        },
                        onLogToSheets = { logItem, cb ->
                            viewModel.logCalculator(logItem, cb)
                        },
                        onApplyToTransaction = { type, amount, name ->
                            formInitialType = type
                            currentScreen = KasNavScreen.FORM
                        }
                    )
                }

                KasNavScreen.PIUTANG -> {
                    PiutangScreen(
                        piutangList = piutangList,
                        onSavePiutang = { p, cb ->
                            viewModel.savePiutang(p, cb)
                        },
                        onUpdatePiutang = { p ->
                            viewModel.updatePiutang(p)
                        },
                        onDeletePiutang = { id ->
                            viewModel.deletePiutang(id)
                            Toast.makeText(context, "Catatan piutang dihapus", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                KasNavScreen.ANGGARAN -> {
                    AnggaranScreen(
                        anggaranList = anggaranList,
                        rincianAnggaranList = rincianAnggaranList,
                        transactions = transactions,
                        onSaveRincianAnggaran = { r ->
                            viewModel.saveRincianAnggaran(r)
                        },
                        onDeleteRincianAnggaran = { id ->
                            viewModel.deleteRincianAnggaran(id)
                            Toast.makeText(context, "Rincian anggaran dihapus", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                KasNavScreen.GAJI_KARYAWAN -> {
                    GajiKaryawanScreen(
                        karyawanList = karyawanList,
                        absensiList = absensiList,
                        slipGajiList = slipGajiList,
                        onSaveKaryawan = { k ->
                            viewModel.saveKaryawan(k)
                        },
                        onDeleteKaryawan = { id ->
                            viewModel.deleteKaryawan(id)
                            Toast.makeText(context, "Data karyawan dihapus", Toast.LENGTH_SHORT).show()
                        },
                        onSaveAbsensi = { a ->
                            viewModel.saveAbsensi(a)
                        },
                        onDeleteAbsensi = { id ->
                            viewModel.deleteAbsensi(id)
                            Toast.makeText(context, "Data absensi dihapus", Toast.LENGTH_SHORT).show()
                        },
                        onSaveSlipGaji = { s ->
                            viewModel.saveSlipGaji(s)
                        },
                        onDeleteSlipGaji = { id ->
                            viewModel.deleteSlipGaji(id)
                            Toast.makeText(context, "Slip gaji dihapus", Toast.LENGTH_SHORT).show()
                        },
                        onBayarGaji = { slip, cb ->
                            viewModel.bayarGaji(slip) { success, msg ->
                                cb(success, msg)
                            }
                        }
                    )
                }

                KasNavScreen.AUDIT -> {
                    AuditScreen(
                        auditList = auditList,
                        accounts = accounts,
                        master = viewModel.master,
                        isSyncing = isSyncing,
                        onSaveAudit = { item, cb ->
                            viewModel.saveAudit(item, cb)
                        },
                        onDeleteAudit = { id ->
                            viewModel.deleteAudit(id)
                            Toast.makeText(context, "Berita acara audit dihapus", Toast.LENGTH_SHORT).show()
                        },
                        onHapusSemuaDataPalsu = { cb ->
                            viewModel.hapusSemuaDataPalsu(cb)
                        },
                        onSyncNow = {
                            viewModel.syncNow { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                KasNavScreen.CATATAN -> {
                    BukuCatatanScreen(
                        notes = notesList,
                        onSaveNote = { note ->
                            viewModel.saveNote(note)
                        },
                        onDeleteNote = { id ->
                            viewModel.deleteNote(id)
                            Toast.makeText(context, "Catatan memo dihapus", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                KasNavScreen.ARSIP_DIHAPUS -> {
                    ArsipDihapusScreen(
                        deletedList = deletedList,
                        onRestore = { deletedItem, cb ->
                            viewModel.restoreDeletedTransaction(deletedItem) {
                                cb()
                            }
                        }
                    )
                }

                KasNavScreen.PENGATURAN -> {
                    PengaturanScreen(
                        currentUrl = webAppUrl,
                        autoSync = autoSync,
                        isSyncing = isSyncing,
                        syncMessage = syncMessage,
                        onSaveSettings = { url, auto ->
                            viewModel.updateSettings(url, auto, viewModel.businessName.value)
                        },
                        onTestConnection = { url, cb ->
                            viewModel.testConnection(url, cb)
                        },
                        onTriggerBackup = { cb ->
                            viewModel.triggerBackup(cb)
                        },
                        onSyncNow = {
                            viewModel.syncNow { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onHapusSemuaDataPalsu = { cb ->
                            viewModel.hapusSemuaDataPalsu(cb)
                        }
                    )
                }

                KasNavScreen.BANTUAN -> {
                    BantuanScreen()
                }
            }
        }
    }
}
