package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CalendarHelper {

    fun addToGoogleCalendar(
        context: Context,
        title: String,
        description: String,
        dateStr: String // dd/MM/yyyy
    ) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            val parsedDate = sdf.parse(dateStr) ?: java.util.Date()

            val calendar = Calendar.getInstance().apply {
                time = parsedDate
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
            }

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.timeInMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendar.timeInMillis + (60 * 60 * 1000))
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.Events.EVENT_LOCATION, "Sistem Kas Terintegrasi")
                putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuka Kalender: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
