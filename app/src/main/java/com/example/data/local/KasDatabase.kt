package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AbsensiItem
import com.example.data.model.AnggaranItem
import com.example.data.model.AuditKasItem
import com.example.data.model.BukuCatatanItem
import com.example.data.model.CalculatorLogItem
import com.example.data.model.CalendarEventItem
import com.example.data.model.DeletedKasTransaction
import com.example.data.model.KaryawanItem
import com.example.data.model.KasTransaction
import com.example.data.model.PiutangItem
import com.example.data.model.RincianAnggaranItem
import com.example.data.model.SlipGajiItem

@Database(
    entities = [
        KasTransaction::class,
        DeletedKasTransaction::class,
        PiutangItem::class,
        AnggaranItem::class,
        RincianAnggaranItem::class,
        KaryawanItem::class,
        AbsensiItem::class,
        SlipGajiItem::class,
        BukuCatatanItem::class,
        CalculatorLogItem::class,
        CalendarEventItem::class,
        AuditKasItem::class
    ],
    version = 4,
    exportSchema = false
)
abstract class KasDatabase : RoomDatabase() {
    abstract fun kasDao(): KasDao

    companion object {
        @Volatile
        private var INSTANCE: KasDatabase? = null

        fun getDatabase(context: Context): KasDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KasDatabase::class.java,
                    "kas_terintegrasi_v3.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
