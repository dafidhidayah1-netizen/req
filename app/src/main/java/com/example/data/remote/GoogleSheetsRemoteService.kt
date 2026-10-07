package com.example.data.remote

import com.example.data.model.AccountBalance
import com.example.data.model.DashboardSummary
import com.example.data.model.KasTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class RemoteResult<out T> {
    data class Success<T>(val data: T) : RemoteResult<T>()
    data class Error(val message: String) : RemoteResult<Nothing>()
}

class GoogleSheetsRemoteService {

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testConnection(webAppUrl: String): RemoteResult<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext RemoteResult.Error("URL Web App Google Sheets belum diisi.")
        }

        try {
            val url = if (webAppUrl.contains("?")) "$webAppUrl&action=ping" else "$webAppUrl?action=ping"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext RemoteResult.Error("HTTP ${response.code}: $body")
                }

                val json = JSONObject(body)
                val status = json.optString("status", "")
                if (status == "success" || status == "ok") {
                    val msg = json.optString("message", "Terhubung ke Google Sheets")
                    val sheet = json.optString("sheetName", "")
                    RemoteResult.Success(if (sheet.isNotEmpty()) "$msg ($sheet)" else msg)
                } else {
                    RemoteResult.Error(json.optString("message", "Format respons tidak valid."))
                }
            }
        } catch (e: Exception) {
            RemoteResult.Error("Koneksi gagal: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun fetchDashboardSummary(webAppUrl: String): RemoteResult<Pair<DashboardSummary, List<AccountBalance>>> =
        withContext(Dispatchers.IO) {
            if (webAppUrl.isBlank()) {
                return@withContext RemoteResult.Error("URL Web App belum dikonfigurasi.")
            }

            try {
                val url = if (webAppUrl.contains("?")) "$webAppUrl&action=get_summary" else "$webAppUrl?action=get_summary"
                val request = Request.Builder().url(url).get().build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        return@withContext RemoteResult.Error("Gagal mengambil data: HTTP ${response.code}")
                    }

                    val json = JSONObject(body)
                    if (json.optString("status") != "success") {
                        return@withContext RemoteResult.Error(json.optString("message", "Gagal memproses respons"))
                    }

                    val summaryObj = json.optJSONObject("summary")
                    val summary = if (summaryObj != null) {
                        DashboardSummary(
                            initialBalance = summaryObj.optDouble("saldoAwal", 0.0),
                            todayIn = summaryObj.optDouble("masukHariIni", 0.0),
                            todayOut = summaryObj.optDouble("keluarHariIni", 0.0),
                            todayNet = summaryObj.optDouble("netHariIni", 0.0),
                            monthIn = summaryObj.optDouble("masukBulanIni", 0.0),
                            monthOut = summaryObj.optDouble("keluarBulanIni", 0.0),
                            currentBalance = summaryObj.optDouble("saldoSaatIni", 0.0),
                            lastSyncTime = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                            isConnected = true
                        )
                    } else {
                        DashboardSummary(isConnected = true)
                    }

                    val accountsList = mutableListOf<AccountBalance>()
                    val accountsArr = json.optJSONArray("accounts")
                    if (accountsArr != null) {
                        for (i in 0 until accountsArr.length()) {
                            val acc = accountsArr.getJSONObject(i)
                            accountsList.add(
                                AccountBalance(
                                    name = acc.optString("name"),
                                    type = acc.optString("type", "Kas"),
                                    initialBalance = acc.optDouble("initial", 0.0),
                                    totalIn = acc.optDouble("totalIn", 0.0),
                                    totalOut = acc.optDouble("totalOut", 0.0),
                                    currentBalance = acc.optDouble("current", 0.0)
                                )
                            )
                        }
                    }

                    RemoteResult.Success(Pair(summary, accountsList))
                }
            } catch (e: Exception) {
                RemoteResult.Error("Error koneksi Google Sheets: ${e.message}")
            }
        }

    suspend fun postTransaction(webAppUrl: String, transaction: KasTransaction): RemoteResult<String> =
        withContext(Dispatchers.IO) {
            if (webAppUrl.isBlank()) {
                return@withContext RemoteResult.Error("URL Web App belum dikonfigurasi.")
            }

            try {
                val payload = JSONObject().apply {
                    put("action", "ADD_TRANSACTION")
                    val tObj = JSONObject().apply {
                        put("id", transaction.id)
                        put("type", transaction.type)
                        put("date", transaction.date)
                        put("time", transaction.time)
                        put("account", transaction.account)
                        put("toAccount", transaction.toAccount)
                        put("transactionName", transaction.transactionName)
                        put("category", transaction.category)
                        put("description", transaction.description)
                        put("amount", transaction.amount)
                        put("allocation", transaction.allocation)
                        put("pic", transaction.pic)
                        put("proofUrl", transaction.proofUrl)
                        put("proofNumber", transaction.proofNumber)
                        put("project", transaction.project)
                        put("note", transaction.note)
                        put("status", transaction.status)
                    }
                    put("transaction", tObj)
                }

                val body = payload.toString().toRequestBody(jsonMediaType)
                val request = Request.Builder().url(webAppUrl).post(body).build()

                client.newCall(request).execute().use { response ->
                    val respText = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        return@withContext RemoteResult.Error("HTTP ${response.code}: $respText")
                    }

                    val json = JSONObject(respText)
                    if (json.optString("status") == "success") {
                        RemoteResult.Success(json.optString("id", transaction.id))
                    } else {
                        RemoteResult.Error(json.optString("message", "Gagal menyimpan ke Google Sheets."))
                    }
                }
            } catch (e: Exception) {
                RemoteResult.Error("Koneksi gagal saat kirim data: ${e.message}")
            }
        }

    suspend fun uploadProofToDrive(
        webAppUrl: String,
        base64Data: String,
        fileName: String,
        mimeType: String = "image/jpeg"
    ): RemoteResult<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext RemoteResult.Error("URL Web App belum diisi.")
        }

        try {
            val payload = JSONObject().apply {
                put("action", "UPLOAD_BUKTI")
                put("base64", base64Data)
                put("fileName", fileName)
                put("mimeType", mimeType)
            }

            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(webAppUrl).post(body).build()

            client.newCall(request).execute().use { response ->
                val respText = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext RemoteResult.Error("Upload gagal: HTTP ${response.code}")
                }

                val json = JSONObject(respText)
                if (json.optString("status") == "success") {
                    val fileUrl = json.optString("fileUrl")
                    RemoteResult.Success(fileUrl)
                } else {
                    RemoteResult.Error(json.optString("message", "Upload bukti gagal di Apps Script"))
                }
            }
        } catch (e: Exception) {
            RemoteResult.Error("Gagal upload bukti: ${e.message}")
        }
    }

    suspend fun syncSpreadsheet(webAppUrl: String): RemoteResult<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext RemoteResult.Error("URL Web App belum diisi.")
        }

        try {
            val payload = JSONObject().apply {
                put("action", "SYNC")
            }
            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(webAppUrl).post(body).build()

            client.newCall(request).execute().use { response ->
                val respText = response.body?.string() ?: ""
                val json = JSONObject(respText)
                if (json.optString("status") == "success") {
                    RemoteResult.Success("Spreadsheet Google Sheets berhasil di-flush dan dihitung ulang!")
                } else {
                    RemoteResult.Error(json.optString("message", "Gagal sync."))
                }
            }
        } catch (e: Exception) {
            RemoteResult.Error("Sync gagal: ${e.message}")
        }
    }

    suspend fun triggerDriveBackup(webAppUrl: String): RemoteResult<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext RemoteResult.Error("URL Web App belum diisi.")
        }

        try {
            val payload = JSONObject().apply {
                put("action", "BACKUP")
            }
            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(webAppUrl).post(body).build()

            client.newCall(request).execute().use { response ->
                val respText = response.body?.string() ?: ""
                val json = JSONObject(respText)
                if (json.optString("status") == "success") {
                    RemoteResult.Success(json.optString("message", "Backup berhasil dibuat di Google Drive."))
                } else {
                    RemoteResult.Error(json.optString("message", "Backup gagal."))
                }
            }
        } catch (e: Exception) {
            RemoteResult.Error("Gagal trigger backup: ${e.message}")
        }
    }

    suspend fun logCalculatorToSheets(
        webAppUrl: String,
        calc: com.example.data.model.CalculatorLogItem
    ): RemoteResult<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) return@withContext RemoteResult.Error("URL Web App belum diisi")
        try {
            val payload = JSONObject().apply {
                put("action", "LOG_CALCULATOR")
                val cObj = JSONObject().apply {
                    put("id", calc.id)
                    put("title", calc.title)
                    put("initialBalance", calc.initialBalance)
                    put("income", calc.income)
                    put("fixedCosts", calc.fixedCosts)
                    put("variableCosts", calc.variableCosts)
                    put("totalExpense", calc.totalExpense)
                    put("netMargin", calc.netMargin)
                    put("expensePercentage", calc.expensePercentage)
                    put("finalBalance", calc.finalBalance)
                    put("formulaOrNote", calc.formulaOrNote)
                }
                put("calc", cObj)
            }
            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(webAppUrl).post(body).build()
            client.newCall(request).execute().use { response ->
                val respText = response.body?.string() ?: ""
                val json = JSONObject(respText)
                if (json.optString("status") == "success") {
                    RemoteResult.Success("Kalkulasi berhasil dicatat di sheet Kalkulator!")
                } else {
                    RemoteResult.Error(json.optString("message", "Gagal log kalkulasi"))
                }
            }
        } catch (e: Exception) {
            RemoteResult.Error("Gagal log kalkulator ke Google Sheets: ${e.message}")
        }
    }

    suspend fun postCalendarEventToSheets(
        webAppUrl: String,
        event: com.example.data.model.CalendarEventItem
    ): RemoteResult<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) return@withContext RemoteResult.Error("URL Web App belum diisi")
        try {
            val payload = JSONObject().apply {
                put("action", "ADD_CALENDAR_EVENT")
                val eObj = JSONObject().apply {
                    put("id", event.id)
                    put("date", event.date)
                    put("time", event.time)
                    put("title", event.title)
                    put("description", event.description)
                    put("type", event.type)
                    put("amount", event.amount)
                    put("isCompleted", event.isCompleted)
                }
                put("event", eObj)
            }
            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(webAppUrl).post(body).build()
            client.newCall(request).execute().use { response ->
                val respText = response.body?.string() ?: ""
                val json = JSONObject(respText)
                if (json.optString("status") == "success") {
                    RemoteResult.Success("Event kalender berhasil dicatat di sheet Kalender!")
                } else {
                    RemoteResult.Error(json.optString("message", "Gagal sync event kalender"))
                }
            }
        } catch (e: Exception) {
            RemoteResult.Error("Gagal sync event kalender: ${e.message}")
        }
    }

    suspend fun postPiutangToSheets(
        webAppUrl: String,
        piutang: com.example.data.model.PiutangItem
    ): RemoteResult<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) return@withContext RemoteResult.Error("URL Web App belum diisi")
        try {
            val payload = JSONObject().apply {
                put("action", "ADD_PIUTANG")
                val pObj = JSONObject().apply {
                    put("id", piutang.id)
                    put("date", piutang.date)
                    put("customerName", piutang.customerName)
                    put("description", piutang.description)
                    put("amount", piutang.amount)
                    put("dueDate", piutang.dueDate)
                    put("paidAmount", piutang.paidAmount)
                    put("remainingAmount", piutang.remainingAmount)
                    put("status", piutang.status)
                    put("targetAccount", piutang.targetAccount)
                    put("note", piutang.note)
                }
                put("piutang", pObj)
            }
            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(webAppUrl).post(body).build()
            client.newCall(request).execute().use { response ->
                val respText = response.body?.string() ?: ""
                val json = JSONObject(respText)
                if (json.optString("status") == "success") {
                    RemoteResult.Success("Piutang berhasil dicatat di sheet Piutang!")
                } else {
                    RemoteResult.Error(json.optString("message", "Gagal sync piutang"))
                }
            }
        } catch (e: Exception) {
            RemoteResult.Error("Gagal sync piutang: ${e.message}")
        }
    }

    suspend fun postAuditToSheets(
        webAppUrl: String,
        audit: com.example.data.model.AuditKasItem
    ): RemoteResult<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) return@withContext RemoteResult.Error("URL Web App belum diisi")
        try {
            val payload = JSONObject().apply {
                put("action", "RECORD_AUDIT")
                val aObj = JSONObject().apply {
                    put("id", audit.id)
                    put("tanggal", audit.tanggal)
                    put("jam", audit.jam)
                    put("auditor", audit.auditor)
                    put("akun", audit.akun)
                    put("saldoSistemBuku", audit.saldoSistemBuku)
                    put("saldoFisikRiil", audit.saldoFisikRiil)
                    put("selisih", audit.selisih)
                    put("statusHasil", audit.statusHasil)
                    put("catatanPenyebab", audit.catatanPenyebab)
                    put("tindakLanjut", audit.tindakLanjut)
                }
                put("audit", aObj)
            }
            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(webAppUrl).post(body).build()
            client.newCall(request).execute().use { response ->
                val respText = response.body?.string() ?: ""
                val json = JSONObject(respText)
                if (json.optString("status") == "success") {
                    RemoteResult.Success("Rekap audit berhasil dicatat ke Google Sheets!")
                } else {
                    RemoteResult.Error(json.optString("message", "Gagal mencatat audit ke Google Sheets"))
                }
            }
        } catch (e: Exception) {
            RemoteResult.Error("Gagal sync audit ke Google Sheets: ${e.message}")
        }
    }
}
