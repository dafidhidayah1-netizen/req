package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object FormatUtils {
    private val localeId = Locale("id", "ID")
    private val rupiahFormat = NumberFormat.getCurrencyInstance(localeId).apply {
        maximumFractionDigits = 0
    }

    fun formatRupiah(amount: Double): String {
        return try {
            val formatted = rupiahFormat.format(amount)
            formatted.replace("Rp", "Rp ")
        } catch (e: Exception) {
            "Rp " + String.format(Locale.US, "%,.0f", amount).replace(",", ".")
        }
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", localeId)
        return sdf.format(Date())
    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", localeId)
        return sdf.format(Date())
    }

    fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", localeId)
        return sdf.format(Date())
    }

    /**
     * Menghasilkan ID unik sesuai standar Apps Script:
     * Prefix-yyyyMMdd-HHmmss-XXXXXX (contoh: KM-20261007-142010-A9F3C1)
     */
    fun generateTransactionId(prefix: String): String {
        val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
        val timeStamp = sdf.format(Date())
        val shortUuid = UUID.randomUUID().toString().replace("-", "").take(6).uppercase()
        return "$prefix-$timeStamp-$shortUuid"
    }

    fun parseDate(dateStr: String): Date? {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", localeId)
            sdf.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }
}
