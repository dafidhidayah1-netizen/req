package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KasDatabase
import com.example.data.model.AccountBalance
import com.example.data.model.AnggaranItem
import com.example.data.model.BukuCatatanItem
import com.example.data.model.CalculatorLogItem
import com.example.data.model.CalendarCashflow
import com.example.data.model.CalendarEventItem
import com.example.data.model.CvekCalculation
import com.example.data.model.DashboardSummary
import com.example.data.model.DeletedKasTransaction
import com.example.data.model.KasTransaction
import com.example.data.model.MasterCategories
import com.example.data.model.PiutangItem
import com.example.data.remote.RemoteResult
import com.example.data.repository.KasRepository
import com.example.util.FormatUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

class KasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KasRepository

    private val _transactions = MutableStateFlow<List<KasTransaction>>(emptyList())
    val transactions: StateFlow<List<KasTransaction>> = _transactions.asStateFlow()

    private val _dashboardSummary = MutableStateFlow(DashboardSummary())
    val dashboardSummary: StateFlow<DashboardSummary> = _dashboardSummary.asStateFlow()

    private val _accountBalances = MutableStateFlow<List<AccountBalance>>(emptyList())
    val accountBalances: StateFlow<List<AccountBalance>> = _accountBalances.asStateFlow()

    private val _calendarCashflows = MutableStateFlow<List<CalendarCashflow>>(emptyList())
    val calendarCashflows: StateFlow<List<CalendarCashflow>> = _calendarCashflows.asStateFlow()

    private val _deletedTransactions = MutableStateFlow<List<DeletedKasTransaction>>(emptyList())
    val deletedTransactions: StateFlow<List<DeletedKasTransaction>> = _deletedTransactions.asStateFlow()

    private val _piutangList = MutableStateFlow<List<PiutangItem>>(emptyList())
    val piutangList: StateFlow<List<PiutangItem>> = _piutangList.asStateFlow()

    private val _anggaranList = MutableStateFlow<List<AnggaranItem>>(emptyList())
    val anggaranList: StateFlow<List<AnggaranItem>> = _anggaranList.asStateFlow()

    private val _notesList = MutableStateFlow<List<BukuCatatanItem>>(emptyList())
    val notesList: StateFlow<List<BukuCatatanItem>> = _notesList.asStateFlow()

    private val _calculatorLogs = MutableStateFlow<List<CalculatorLogItem>>(emptyList())
    val calculatorLogs: StateFlow<List<CalculatorLogItem>> = _calculatorLogs.asStateFlow()

    private val _calendarEvents = MutableStateFlow<List<CalendarEventItem>>(emptyList())
    val calendarEvents: StateFlow<List<CalendarEventItem>> = _calendarEvents.asStateFlow()

    private val _karyawanList = MutableStateFlow<List<com.example.data.model.KaryawanItem>>(emptyList())
    val karyawanList: StateFlow<List<com.example.data.model.KaryawanItem>> = _karyawanList.asStateFlow()

    private val _absensiList = MutableStateFlow<List<com.example.data.model.AbsensiItem>>(emptyList())
    val absensiList: StateFlow<List<com.example.data.model.AbsensiItem>> = _absensiList.asStateFlow()

    private val _slipGajiList = MutableStateFlow<List<com.example.data.model.SlipGajiItem>>(emptyList())
    val slipGajiList: StateFlow<List<com.example.data.model.SlipGajiItem>> = _slipGajiList.asStateFlow()

    private val _rincianAnggaranList = MutableStateFlow<List<com.example.data.model.RincianAnggaranItem>>(emptyList())
    val rincianAnggaranList: StateFlow<List<com.example.data.model.RincianAnggaranItem>> = _rincianAnggaranList.asStateFlow()

    private val _auditList = MutableStateFlow<List<com.example.data.model.AuditKasItem>>(emptyList())
    val auditList: StateFlow<List<com.example.data.model.AuditKasItem>> = _auditList.asStateFlow()

    private val _cvekCalculation = MutableStateFlow(CvekCalculation())
    val cvekCalculation: StateFlow<CvekCalculation> = _cvekCalculation.asStateFlow()

    val unsyncedCount: StateFlow<Int>

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow("")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    private val _isUploadingProof = MutableStateFlow(false)
    val isUploadingProof: StateFlow<Boolean> = _isUploadingProof.asStateFlow()

    private val _uploadProofMessage = MutableStateFlow("")
    val uploadProofMessage: StateFlow<String> = _uploadProofMessage.asStateFlow()

    private val _webAppUrl = MutableStateFlow("")
    val webAppUrl: StateFlow<String> = _webAppUrl.asStateFlow()

    private val _autoSync = MutableStateFlow(true)
    val autoSync: StateFlow<Boolean> = _autoSync.asStateFlow()

    private val _businessName = MutableStateFlow("")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    val master = MasterCategories()

    init {
        val database = KasDatabase.getDatabase(application)
        repository = KasRepository(application, database.kasDao())
        _webAppUrl.value = repository.getWebAppUrl()
        _autoSync.value = repository.isAutoSyncEnabled()
        _businessName.value = repository.getBusinessName()

        unsyncedCount = repository.getUnsyncedCount().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

        viewModelScope.launch {
            repository.getAllTransactions().collectLatest { list ->
                _transactions.value = list
                refreshSummary()
                refreshCalendar()
            }
        }

        viewModelScope.launch {
            repository.getAllDeletedTransactions().collectLatest {
                _deletedTransactions.value = it
            }
        }

        viewModelScope.launch {
            repository.getAllPiutang().collectLatest {
                _piutangList.value = it
            }
        }

        viewModelScope.launch {
            repository.getAllAnggaran().collectLatest {
                _anggaranList.value = it
            }
        }

        viewModelScope.launch {
            repository.getAllNotes().collectLatest {
                _notesList.value = it
            }
        }

        viewModelScope.launch {
            repository.getAllCalculatorLogs().collectLatest {
                _calculatorLogs.value = it
            }
        }

        viewModelScope.launch {
            repository.getAllCalendarEvents().collectLatest {
                _calendarEvents.value = it
            }
        }

        viewModelScope.launch {
            repository.getAllKaryawan().collectLatest {
                _karyawanList.value = it
            }
        }

        viewModelScope.launch {
            repository.getAllAbsensi().collectLatest {
                _absensiList.value = it
            }
        }

        viewModelScope.launch {
            repository.getAllSlipGaji().collectLatest {
                _slipGajiList.value = it
            }
        }

        viewModelScope.launch {
            repository.getAllRincianAnggaran().collectLatest {
                _rincianAnggaranList.value = it
            }
        }

        viewModelScope.launch {
            repository.getAllAudit().collectLatest {
                _auditList.value = it
            }
        }

        // Nilai awal murni 0 tanpa data palsu / dummy
        calculateCvek(initial = 0.0, income = 0.0, fixed = 0.0, variable = 0.0, targetProfit = 0.0)
    }

    fun refreshSummary() {
        viewModelScope.launch {
            val (summary, accounts) = repository.calculateLocalSummary()
            _dashboardSummary.value = summary
            _accountBalances.value = accounts
        }
    }

    fun refreshCalendar() {
        viewModelScope.launch {
            val cashflows = repository.getDailyCashflows()
            _calendarCashflows.value = cashflows
        }
    }

    fun saveTransaction(
        transaction: KasTransaction,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val res = repository.saveTransaction(transaction)
                refreshSummary()
                refreshCalendar()
                onComplete(true, res.getOrDefault("Tersimpan"))
            } catch (e: Exception) {
                onComplete(false, "Gagal simpan: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun archiveAndDeleteTransaction(tx: KasTransaction) {
        viewModelScope.launch {
            repository.archiveAndDeleteTransaction(tx)
            refreshSummary()
            refreshCalendar()
        }
    }

    fun restoreDeletedTransaction(deleted: DeletedKasTransaction, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.restoreDeletedTransaction(deleted)
            refreshSummary()
            refreshCalendar()
            onComplete()
        }
    }

    fun savePiutang(item: PiutangItem, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.savePiutang(item)
            onComplete(true, res.getOrDefault("Piutang tersimpan"))
        }
    }

    fun updatePiutang(item: PiutangItem) {
        viewModelScope.launch { repository.updatePiutang(item) }
    }

    fun deletePiutang(id: String) {
        viewModelScope.launch { repository.deletePiutang(id) }
    }

    fun saveNote(item: BukuCatatanItem) {
        viewModelScope.launch { repository.saveNote(item) }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch { repository.deleteNote(id) }
    }

    fun saveCalendarEvent(event: CalendarEventItem, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.saveCalendarEvent(event)
            onComplete(true, res.getOrDefault("Event kalender tersimpan"))
        }
    }

    fun deleteCalendarEvent(id: String) {
        viewModelScope.launch { repository.deleteCalendarEvent(id) }
    }

    fun logCalculator(item: CalculatorLogItem, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.logCalculator(item)
            _isSyncing.value = false
            onComplete(true, res.getOrDefault("Kalkulasi dicatat"))
        }
    }

    fun uploadProofPhoto(
        uri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isUploadingProof.value = true
            _uploadProofMessage.value = "Mengompres & mengunggah bukti ke Google Drive..."
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap == null) {
                    withContext(Dispatchers.Main) {
                        _isUploadingProof.value = false
                        onError("Gagal membaca file gambar")
                    }
                    return@launch
                }

                val outputStream = ByteArrayOutputStream()
                val scaled = if (bitmap.width > 1280 || bitmap.height > 1280) {
                    val ratio = 1280f / Math.max(bitmap.width, bitmap.height)
                    Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
                } else bitmap

                scaled.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                val imageBytes = outputStream.toByteArray()
                val base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                val fileName = "Bukti_Kas_${System.currentTimeMillis()}.jpg"

                val result = repository.uploadProofToDrive(base64String, fileName)
                withContext(Dispatchers.Main) {
                    _isUploadingProof.value = false
                    when (result) {
                        is RemoteResult.Success -> {
                            _uploadProofMessage.value = "Upload bukti berhasil!"
                            onSuccess(result.data)
                        }
                        is RemoteResult.Error -> {
                            _uploadProofMessage.value = result.message
                            onError(result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isUploadingProof.value = false
                    _uploadProofMessage.value = "Error: ${e.message}"
                    onError(e.message ?: "Gagal upload bukti")
                }
            }
        }
    }

    fun syncNow(onResult: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Menghubungkan ke Google Sheets..."
            try {
                val (count, msg) = repository.syncPendingTransactions()
                val remoteSumRes = repository.fetchRemoteSummary()
                if (remoteSumRes is RemoteResult.Success) {
                    _dashboardSummary.value = remoteSumRes.data.first
                    if (remoteSumRes.data.second.isNotEmpty()) {
                        _accountBalances.value = remoteSumRes.data.second
                    }
                } else {
                    refreshSummary()
                }
                refreshCalendar()
                val finalMsg = if (count > 0) "Berhasil sync $count transaksi ke Google Sheets!" else msg
                _syncMessage.value = finalMsg
                onResult?.invoke(finalMsg)
            } catch (e: Exception) {
                val err = "Gagal sinkronisasi: ${e.message}"
                _syncMessage.value = err
                onResult?.invoke(err)
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun testConnection(url: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            when (val res = repository.testSheetsConnection(url)) {
                is RemoteResult.Success -> {
                    repository.setWebAppUrl(url)
                    _webAppUrl.value = url
                    _syncMessage.value = res.data
                    onResult(true, res.data)
                }
                is RemoteResult.Error -> {
                    onResult(false, res.message)
                }
            }
            _isSyncing.value = false
        }
    }

    fun updateSettings(webAppUrl: String, autoSync: Boolean, businessName: String) {
        repository.setWebAppUrl(webAppUrl)
        repository.setAutoSyncEnabled(autoSync)
        repository.setBusinessName(businessName)
        _webAppUrl.value = webAppUrl
        _autoSync.value = autoSync
        _businessName.value = businessName
    }

    fun calculateCvek(
        initial: Double,
        income: Double,
        fixed: Double,
        variable: Double,
        targetProfit: Double = 0.0
    ) {
        val totalExpense = fixed + variable
        val margin = income - totalExpense
        val pctExpense = if (income > 0) (totalExpense / income) * 100 else 0.0
        val finalBal = initial + margin
        val bep = if (income > variable) {
            val contributionMarginRatio = (income - variable) / income
            if (contributionMarginRatio > 0) fixed / contributionMarginRatio else 0.0
        } else 0.0

        _cvekCalculation.value = CvekCalculation(
            initialBalance = initial,
            totalIncome = income,
            fixedCosts = fixed,
            variableCosts = variable,
            totalExpense = totalExpense,
            netMargin = margin,
            expensePercentage = pctExpense,
            finalBalance = finalBal,
            breakEvenPointUnits = bep,
            targetProfit = targetProfit
        )
    }

    fun triggerBackup(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            when (val res = repository.triggerBackup()) {
                is RemoteResult.Success -> onResult(true, res.data)
                is RemoteResult.Error -> onResult(false, res.message)
            }
            _isSyncing.value = false
        }
    }

    // KARYAWAN & GAJI
    fun saveKaryawan(item: com.example.data.model.KaryawanItem) {
        viewModelScope.launch { repository.saveKaryawan(item) }
    }

    fun deleteKaryawan(id: String) {
        viewModelScope.launch { repository.deleteKaryawan(id) }
    }

    // ABSENSI KARYAWAN
    fun saveAbsensi(item: com.example.data.model.AbsensiItem) {
        viewModelScope.launch { repository.saveAbsensi(item) }
    }

    fun deleteAbsensi(id: String) {
        viewModelScope.launch { repository.deleteAbsensi(id) }
    }

    // SLIP GAJI & PEMBAYARAN KAS KELUAR
    fun saveSlipGaji(item: com.example.data.model.SlipGajiItem) {
        viewModelScope.launch { repository.saveSlipGaji(item) }
    }

    fun deleteSlipGaji(id: String) {
        viewModelScope.launch { repository.deleteSlipGaji(id) }
    }

    fun bayarGaji(slip: com.example.data.model.SlipGajiItem, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.bayarGajiDanCatatKasKeluar(slip)
            refreshSummary()
            refreshCalendar()
            _isSyncing.value = false
            onComplete(true, res.getOrDefault("Gaji berhasil dibayar & tercatat di Buku Kas"))
        }
    }

    // RINCIAN ANGGARAN (A, B, C, DLL.)
    fun saveRincianAnggaran(item: com.example.data.model.RincianAnggaranItem) {
        viewModelScope.launch { repository.saveRincianAnggaran(item) }
    }

    fun deleteRincianAnggaran(id: String) {
        viewModelScope.launch { repository.deleteRincianAnggaran(id) }
    }

    // REKAP AUDIT KAS & REKONSILIASI OPNAME FISIK
    fun saveAudit(item: com.example.data.model.AuditKasItem, onComplete: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.saveAudit(item)
            _isSyncing.value = false
            onComplete?.invoke(res.getOrDefault("Rekap audit berhasil disimpan & disinkronkan"))
        }
    }

    fun deleteAudit(id: String) {
        viewModelScope.launch { repository.deleteAudit(id) }
    }

    // BERSIHKAN / RESET SEMUA DATA PALSU UJI COBA KE MURNI DATA REAL AUDIT
    fun hapusSemuaDataPalsu(onDone: () -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.hapusSemuaDataPalsu()
            refreshSummary()
            refreshCalendar()
            _isSyncing.value = false
            onDone()
        }
    }
}
