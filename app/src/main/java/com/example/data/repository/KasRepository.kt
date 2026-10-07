package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.KasDao
import com.example.data.model.AccountBalance
import com.example.data.model.AnggaranItem
import com.example.data.model.BukuCatatanItem
import com.example.data.model.CalculatorLogItem
import com.example.data.model.CalendarCashflow
import com.example.data.model.CalendarEventItem
import com.example.data.model.DashboardSummary
import com.example.data.model.DeletedKasTransaction
import com.example.data.model.KasTransaction
import com.example.data.model.PiutangItem
import com.example.data.remote.GoogleSheetsRemoteService
import com.example.data.remote.RemoteResult
import com.example.util.FormatUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class KasRepository(
    private val context: Context,
    private val kasDao: KasDao,
    private val remoteService: GoogleSheetsRemoteService = GoogleSheetsRemoteService()
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kas_settings_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WEB_APP_URL = "key_web_app_url"
        private const val KEY_AUTO_SYNC = "key_auto_sync"
        private const val KEY_BUSINESS_NAME = "key_business_name"
    }

    fun getWebAppUrl(): String = prefs.getString(KEY_WEB_APP_URL, "") ?: ""
    fun setWebAppUrl(url: String) = prefs.edit().putString(KEY_WEB_APP_URL, url.trim()).apply()

    fun isAutoSyncEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_SYNC, true)
    fun setAutoSyncEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()

    fun getBusinessName(): String = prefs.getString(KEY_BUSINESS_NAME, "Sistem Kas Mandiri") ?: "Sistem Kas Mandiri"
    fun setBusinessName(name: String) = prefs.edit().putString(KEY_BUSINESS_NAME, name.trim()).apply()

    // 1. TRANSAKSI KAS
    fun getAllTransactions(): Flow<List<KasTransaction>> = kasDao.getAllTransactions()
    fun getUnsyncedCount(): Flow<Int> = kasDao.getUnsyncedCount()

    suspend fun saveTransaction(transaction: KasTransaction): Result<String> = withContext(Dispatchers.IO) {
        kasDao.insertTransaction(transaction.copy(isSynced = false))

        val webAppUrl = getWebAppUrl()
        if (webAppUrl.isNotBlank() && isAutoSyncEnabled()) {
            when (val remoteRes = remoteService.postTransaction(webAppUrl, transaction)) {
                is RemoteResult.Success -> {
                    kasDao.markSynced(transaction.id)
                    Result.success("Tersimpan & tersinkronisasi ke Google Sheets (${remoteRes.data})")
                }
                is RemoteResult.Error -> {
                    Result.success("Tersimpan lokal (Siap sync saat online: ${remoteRes.message})")
                }
            }
        } else {
            Result.success("Tersimpan lokal secara offline-first")
        }
    }

    // 2. ARSIP TRANSAKSI DIHAPUS (PULIHKAN / RESTORE)
    fun getAllDeletedTransactions(): Flow<List<DeletedKasTransaction>> = kasDao.getAllDeletedTransactions()

    suspend fun archiveAndDeleteTransaction(tx: KasTransaction, deletedBy: String = "Admin") = withContext(Dispatchers.IO) {
        val deleted = DeletedKasTransaction(
            id = tx.id,
            deletedAt = FormatUtils.getCurrentTimestamp(),
            deletedBy = deletedBy,
            source = if (tx.type == "MASUK") "Kas_Masuk" else if (tx.type == "KELUAR") "Kas_Keluar" else "Transfer",
            type = tx.type,
            date = tx.date,
            time = tx.time,
            account = tx.account,
            toAccount = tx.toAccount,
            transactionName = tx.transactionName,
            category = tx.category,
            description = tx.description,
            amount = tx.amount,
            allocation = tx.allocation,
            pic = tx.pic,
            proofUrl = tx.proofUrl,
            proofNumber = tx.proofNumber,
            project = tx.project,
            note = tx.note,
            status = "Dihapus"
        )
        kasDao.insertDeletedTransaction(deleted)
        kasDao.deleteTransaction(tx.id)
    }

    suspend fun restoreDeletedTransaction(deleted: DeletedKasTransaction) = withContext(Dispatchers.IO) {
        val restored = KasTransaction(
            id = deleted.id,
            type = deleted.type,
            date = deleted.date,
            time = deleted.time,
            account = deleted.account,
            toAccount = deleted.toAccount,
            transactionName = deleted.transactionName,
            category = deleted.category,
            description = deleted.description,
            amount = deleted.amount,
            allocation = deleted.allocation,
            pic = deleted.pic,
            proofUrl = deleted.proofUrl,
            proofNumber = deleted.proofNumber,
            project = deleted.project,
            note = deleted.note,
            status = "Selesai",
            inputTime = FormatUtils.getCurrentTimestamp(),
            isSynced = false
        )
        kasDao.insertTransaction(restored)
        kasDao.removeDeletedTransaction(deleted.id)
        saveTransaction(restored)
    }

    // 3. PIUTANG
    fun getAllPiutang(): Flow<List<PiutangItem>> = kasDao.getAllPiutang()

    suspend fun savePiutang(item: PiutangItem): Result<String> = withContext(Dispatchers.IO) {
        kasDao.insertPiutang(item)
        val webAppUrl = getWebAppUrl()
        if (webAppUrl.isNotBlank() && isAutoSyncEnabled()) {
            remoteService.postPiutangToSheets(webAppUrl, item)
        }
        Result.success("Data piutang berhasil disimpan")
    }

    suspend fun updatePiutang(item: PiutangItem) = withContext(Dispatchers.IO) {
        kasDao.updatePiutang(item)
    }

    suspend fun deletePiutang(id: String) = withContext(Dispatchers.IO) {
        kasDao.deletePiutang(id)
    }

    // 4. ANGGARAN & ALOKASI
    fun getAllAnggaran(): Flow<List<AnggaranItem>> = kasDao.getAllAnggaran()

    suspend fun saveAnggaran(item: AnggaranItem) = withContext(Dispatchers.IO) {
        kasDao.insertAnggaran(item)
    }

    // 4B. RINCIAN ANGGARAN MULTI-LEVEL (A, B, C, DLL.) DENGAN SELISIH & DEFISIT
    fun getAllRincianAnggaran(): Flow<List<com.example.data.model.RincianAnggaranItem>> = kasDao.getAllRincianAnggaran()

    suspend fun saveRincianAnggaran(item: com.example.data.model.RincianAnggaranItem) = withContext(Dispatchers.IO) {
        kasDao.insertRincianAnggaran(item)
    }

    suspend fun deleteRincianAnggaran(id: String) = withContext(Dispatchers.IO) {
        kasDao.deleteRincianAnggaran(id)
    }

    // 4C. KARYAWAN & ABSENSI
    fun getAllKaryawan(): Flow<List<com.example.data.model.KaryawanItem>> = kasDao.getAllKaryawan()

    suspend fun saveKaryawan(item: com.example.data.model.KaryawanItem) = withContext(Dispatchers.IO) {
        kasDao.insertKaryawan(item)
    }

    suspend fun deleteKaryawan(id: String) = withContext(Dispatchers.IO) {
        kasDao.deleteKaryawan(id)
    }

    fun getAllAbsensi(): Flow<List<com.example.data.model.AbsensiItem>> = kasDao.getAllAbsensi()

    suspend fun saveAbsensi(item: com.example.data.model.AbsensiItem) = withContext(Dispatchers.IO) {
        kasDao.insertAbsensi(item)
    }

    suspend fun deleteAbsensi(id: String) = withContext(Dispatchers.IO) {
        kasDao.deleteAbsensi(id)
    }

    // 4D. SLIP GAJI & PEMBAYARAN TERHUBUNG LANGSUNG KE KAS KELUAR
    fun getAllSlipGaji(): Flow<List<com.example.data.model.SlipGajiItem>> = kasDao.getAllSlipGaji()

    suspend fun saveSlipGaji(item: com.example.data.model.SlipGajiItem) = withContext(Dispatchers.IO) {
        kasDao.insertSlipGaji(item)
    }

    suspend fun deleteSlipGaji(id: String) = withContext(Dispatchers.IO) {
        kasDao.deleteSlipGaji(id)
    }

    suspend fun bayarGajiDanCatatKasKeluar(slip: com.example.data.model.SlipGajiItem): Result<String> = withContext(Dispatchers.IO) {
        val txId = FormatUtils.generateTransactionId("KK")
        val kasTx = KasTransaction(
            id = txId,
            type = "KELUAR",
            date = slip.tanggalBayar,
            time = FormatUtils.getCurrentTime(),
            account = slip.akunKasPembayar,
            transactionName = "Gaji Karyawan - ${slip.karyawanNama}",
            category = "Gaji",
            description = "Pembayaran Gaji ${slip.karyawanNama} (${slip.jabatan}) Periode ${slip.periode}",
            amount = slip.totalGajiBersih,
            allocation = "Gaji",
            pic = "Bendahara",
            proofNumber = slip.id,
            status = "Selesai",
            inputTime = FormatUtils.getCurrentTimestamp()
        )
        // Simpan transaksi kas keluar
        saveTransaction(kasTx)

        // Update slip gaji dengan status Lunas dan tautkan kasTransactionId
        val updatedSlip = slip.copy(
            statusBayar = "Lunas",
            kasTransactionId = txId
        )
        kasDao.insertSlipGaji(updatedSlip)

        Result.success("Gaji ${slip.karyawanNama} lunas dan tercatat di Kas Keluar ($txId)")
    }

    // 5. BUKU CATATAN
    fun getAllNotes(): Flow<List<BukuCatatanItem>> = kasDao.getAllNotes()

    suspend fun saveNote(item: BukuCatatanItem) = withContext(Dispatchers.IO) {
        kasDao.insertNote(item)
    }

    suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        kasDao.deleteNote(id)
    }

    // 6. LOG KALKULATOR KE GOOGLE SHEETS
    fun getAllCalculatorLogs(): Flow<List<CalculatorLogItem>> = kasDao.getAllCalculatorLogs()

    suspend fun logCalculator(item: CalculatorLogItem): Result<String> = withContext(Dispatchers.IO) {
        kasDao.insertCalculatorLog(item)
        val webAppUrl = getWebAppUrl()
        if (webAppUrl.isNotBlank()) {
            when (val res = remoteService.logCalculatorToSheets(webAppUrl, item)) {
                is RemoteResult.Success -> Result.success(res.data)
                is RemoteResult.Error -> Result.success("Tersimpan lokal (${res.message})")
            }
        } else {
            Result.success("Tersimpan di riwayat kalkulator lokal")
        }
    }

    // 7. EVENT KALENDER KAS
    fun getAllCalendarEvents(): Flow<List<CalendarEventItem>> = kasDao.getAllCalendarEvents()

    suspend fun saveCalendarEvent(event: CalendarEventItem): Result<String> = withContext(Dispatchers.IO) {
        kasDao.insertCalendarEvent(event)
        val webAppUrl = getWebAppUrl()
        if (webAppUrl.isNotBlank() && isAutoSyncEnabled()) {
            remoteService.postCalendarEventToSheets(webAppUrl, event)
        }
        Result.success("Event kalender tersimpan")
    }

    suspend fun updateCalendarEvent(event: CalendarEventItem) = withContext(Dispatchers.IO) {
        kasDao.updateCalendarEvent(event)
    }

    suspend fun deleteCalendarEvent(id: String) = withContext(Dispatchers.IO) {
        kasDao.deleteCalendarEvent(id)
    }

    // 8. BUKTI FOTO UPLOAD REAL-TIME
    suspend fun uploadProofToDrive(base64Data: String, fileName: String): RemoteResult<String> {
        val webAppUrl = getWebAppUrl()
        return if (webAppUrl.isNotBlank()) {
            remoteService.uploadProofToDrive(webAppUrl, base64Data, fileName)
        } else {
            RemoteResult.Error("URL Web App Google Sheets belum diatur di Pengaturan.")
        }
    }

    // 9. SINKRONISASI PENDING TRANSACTION
    suspend fun syncPendingTransactions(): Pair<Int, String> = withContext(Dispatchers.IO) {
        val webAppUrl = getWebAppUrl()
        if (webAppUrl.isBlank()) {
            return@withContext Pair(0, "URL Web App belum diatur")
        }

        val pending = kasDao.getUnsyncedTransactions()
        if (pending.isEmpty()) {
            return@withContext Pair(0, "Semua data sudah tersinkronisasi")
        }

        var syncedCount = 0
        var lastError = ""
        for (item in pending) {
            when (val res = remoteService.postTransaction(webAppUrl, item)) {
                is RemoteResult.Success -> {
                    kasDao.markSynced(item.id)
                    syncedCount++
                }
                is RemoteResult.Error -> {
                    lastError = res.message
                }
            }
        }

        remoteService.syncSpreadsheet(webAppUrl)
        Pair(syncedCount, if (lastError.isNotEmpty()) "Sebagian sync: $lastError" else "Berhasil sinkronisasi $syncedCount data")
    }

    // 10. REKAP SALDO LOKAL
    suspend fun calculateLocalSummary(): Pair<DashboardSummary, List<AccountBalance>> = withContext(Dispatchers.IO) {
        val transactions = kasDao.getAllTransactions().first()
        val todayStr = FormatUtils.getCurrentDate()

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        var todayIn = 0.0
        var todayOut = 0.0
        var monthIn = 0.0
        var monthOut = 0.0

        val accountMap = mutableMapOf<String, Double>()
        val defaultAccounts = listOf("Kas Tunai", "Bank BCA", "Bank BRI", "Bank Mandiri", "Kas Besar")
        defaultAccounts.forEach { accountMap[it] = 0.0 }

        for (t in transactions) {
            val amount = t.amount
            val isToday = t.date == todayStr
            val isThisMonth = try {
                val d = sdf.parse(t.date)
                if (d != null) {
                    val c = Calendar.getInstance().apply { time = d }
                    c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
                } else false
            } catch (e: Exception) {
                false
            }

            when (t.type) {
                "MASUK" -> {
                    if (isToday) todayIn += amount
                    if (isThisMonth) monthIn += amount
                    val cur = accountMap.getOrDefault(t.account, 0.0)
                    accountMap[t.account] = cur + amount
                }
                "KELUAR" -> {
                    if (isToday) todayOut += amount
                    if (isThisMonth) monthOut += amount
                    val cur = accountMap.getOrDefault(t.account, 0.0)
                    accountMap[t.account] = cur - amount
                }
                "TRANSFER" -> {
                    val fromCur = accountMap.getOrDefault(t.account, 0.0)
                    accountMap[t.account] = fromCur - amount
                    if (t.toAccount.isNotEmpty()) {
                        val toCur = accountMap.getOrDefault(t.toAccount, 0.0)
                        accountMap[t.toAccount] = toCur + amount
                    }
                }
            }
        }

        val totalCurrentBalance = accountMap.values.sum()
        val pendingCount = kasDao.getUnsyncedTransactions().size

        val summary = DashboardSummary(
            initialBalance = 0.0,
            todayIn = todayIn,
            todayOut = todayOut,
            todayNet = todayIn - todayOut,
            monthIn = monthIn,
            monthOut = monthOut,
            currentBalance = totalCurrentBalance,
            pendingSyncCount = pendingCount,
            lastSyncTime = FormatUtils.getCurrentTimestamp(),
            isConnected = getWebAppUrl().isNotBlank()
        )

        val accountsList = defaultAccounts.map { name ->
            AccountBalance(
                name = name,
                type = if (name.startsWith("Kas")) "Kas" else "Bank",
                initialBalance = 0.0,
                totalIn = transactions.filter { it.account == name && it.type == "MASUK" }.sumOf { it.amount },
                totalOut = transactions.filter { it.account == name && it.type == "KELUAR" }.sumOf { it.amount },
                currentBalance = accountMap.getOrDefault(name, 0.0)
            )
        }

        Pair(summary, accountsList)
    }

    suspend fun getDailyCashflows(): List<CalendarCashflow> = withContext(Dispatchers.IO) {
        val transactions = kasDao.getAllTransactions().first()
        val grouped = transactions.groupBy { it.date }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        val dayFormat = SimpleDateFormat("EEEE", Locale("id", "ID"))

        grouped.map { (dateStr, items) ->
            val totalIn = items.filter { it.type == "MASUK" }.sumOf { it.amount }
            val totalOut = items.filter { it.type == "KELUAR" }.sumOf { it.amount }
            val net = totalIn - totalOut
            val countIn = items.count { it.type == "MASUK" }
            val countOut = items.count { it.type == "KELUAR" }

            val dayOfWeek = try {
                val d = sdf.parse(dateStr)
                if (d != null) dayFormat.format(d) else "Hari"
            } catch (e: Exception) {
                "Hari"
            }

            val status = when {
                net > 0 -> "Positif"
                net < 0 -> "Minus"
                else -> "Nol"
            }

            CalendarCashflow(
                date = dateStr,
                dayOfWeek = dayOfWeek,
                totalIn = totalIn,
                totalOut = totalOut,
                net = net,
                countIn = countIn,
                countOut = countOut,
                status = status
            )
        }.sortedByDescending { it.date }
    }

    suspend fun testSheetsConnection(url: String): RemoteResult<String> {
        return remoteService.testConnection(url)
    }

    suspend fun fetchRemoteSummary(): RemoteResult<Pair<DashboardSummary, List<AccountBalance>>> {
        val webAppUrl = getWebAppUrl()
        return if (webAppUrl.isNotBlank()) {
            remoteService.fetchDashboardSummary(webAppUrl)
        } else {
            RemoteResult.Error("URL Web App belum diatur")
        }
    }

    suspend fun triggerFlush(): RemoteResult<String> {
        val webAppUrl = getWebAppUrl()
        return if (webAppUrl.isNotBlank()) {
            remoteService.syncSpreadsheet(webAppUrl)
        } else {
            RemoteResult.Error("URL Web App belum diatur")
        }
    }

    suspend fun triggerBackup(): RemoteResult<String> {
        val webAppUrl = getWebAppUrl()
        return if (webAppUrl.isNotBlank()) {
            remoteService.triggerDriveBackup(webAppUrl)
        } else {
            RemoteResult.Error("URL Web App belum diatur")
        }
    }

    // 11. REKAP AUDIT KAS & REKONSILIASI OPNAME FISIK
    fun getAllAudit(): Flow<List<com.example.data.model.AuditKasItem>> = kasDao.getAllAudit()

    suspend fun saveAudit(item: com.example.data.model.AuditKasItem): Result<String> = withContext(Dispatchers.IO) {
        kasDao.insertAudit(item)
        val webAppUrl = getWebAppUrl()
        if (webAppUrl.isNotBlank() && isAutoSyncEnabled()) {
            remoteService.postAuditToSheets(webAppUrl, item)
        }
        Result.success("Berita Acara Rekap Audit ${item.id} berhasil disimpan")
    }

    suspend fun deleteAudit(id: String) = withContext(Dispatchers.IO) {
        kasDao.deleteAudit(id)
    }

    // 12. PEMBERSIHAN TOTAL DATA PALSU (RESET KE MURNI DATA REAL AUDIT)
    suspend fun hapusSemuaDataPalsu() = withContext(Dispatchers.IO) {
        kasDao.clearAllTransactions()
        kasDao.clearDeletedTransactions()
        kasDao.clearPiutang()
        kasDao.clearAnggaran()
        kasDao.clearRincianAnggaran()
        kasDao.clearKaryawan()
        kasDao.clearAbsensi()
        kasDao.clearSlipGaji()
        kasDao.clearNotes()
        kasDao.clearCalculatorLogs()
        kasDao.clearCalendarEvents()
    }
}
