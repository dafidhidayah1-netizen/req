package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AnggaranItem
import com.example.data.model.BukuCatatanItem
import com.example.data.model.CalculatorLogItem
import com.example.data.model.CalendarEventItem
import com.example.data.model.DeletedKasTransaction
import com.example.data.model.KasTransaction
import com.example.data.model.PiutangItem
import kotlinx.coroutines.flow.Flow

@Dao
interface KasDao {
    // TRANSAKSI KAS
    @Query("SELECT * FROM kas_transactions ORDER BY date DESC, time DESC")
    fun getAllTransactions(): Flow<List<KasTransaction>>

    @Query("SELECT * FROM kas_transactions WHERE type = :type ORDER BY date DESC, time DESC")
    fun getTransactionsByType(type: String): Flow<List<KasTransaction>>

    @Query("SELECT * FROM kas_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): KasTransaction?

    @Query("SELECT * FROM kas_transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<KasTransaction>

    @Query("SELECT COUNT(*) FROM kas_transactions WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: KasTransaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transactions: List<KasTransaction>)

    @Update
    suspend fun updateTransaction(transaction: KasTransaction)

    @Query("UPDATE kas_transactions SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM kas_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Query("DELETE FROM kas_transactions")
    suspend fun clearAllTransactions()

    // TRANSAKSI DIHAPUS & ARSIP (PULIHKAN)
    @Query("SELECT * FROM deleted_transactions ORDER BY deletedAt DESC")
    fun getAllDeletedTransactions(): Flow<List<DeletedKasTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedTransaction(item: DeletedKasTransaction)

    @Query("DELETE FROM deleted_transactions WHERE id = :id")
    suspend fun removeDeletedTransaction(id: String)

    // KELOLA PIUTANG
    @Query("SELECT * FROM piutang_items ORDER BY dueDate ASC")
    fun getAllPiutang(): Flow<List<PiutangItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPiutang(item: PiutangItem)

    @Update
    suspend fun updatePiutang(item: PiutangItem)

    @Query("DELETE FROM piutang_items WHERE id = :id")
    suspend fun deletePiutang(id: String)

    // ANGGARAN & ALOKASI
    @Query("SELECT * FROM anggaran_items ORDER BY allocation ASC")
    fun getAllAnggaran(): Flow<List<AnggaranItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnggaran(item: AnggaranItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAnggaran(items: List<AnggaranItem>)

    @Update
    suspend fun updateAnggaran(item: AnggaranItem)

    // BUKU CATATAN
    @Query("SELECT * FROM buku_catatan ORDER BY date DESC")
    fun getAllNotes(): Flow<List<BukuCatatanItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(item: BukuCatatanItem)

    @Update
    suspend fun updateNote(item: BukuCatatanItem)

    @Query("DELETE FROM buku_catatan WHERE id = :id")
    suspend fun deleteNote(id: String)

    // LOG KALKULATOR KE GOOGLE SHEETS
    @Query("SELECT * FROM calculator_logs ORDER BY timestamp DESC")
    fun getAllCalculatorLogs(): Flow<List<CalculatorLogItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculatorLog(log: CalculatorLogItem)

    @Query("DELETE FROM calculator_logs WHERE id = :id")
    suspend fun deleteCalculatorLog(id: String)

    // EVENT KALENDER KAS
    @Query("SELECT * FROM calendar_events ORDER BY date ASC, time ASC")
    fun getAllCalendarEvents(): Flow<List<CalendarEventItem>>

    @Query("SELECT * FROM calendar_events WHERE date = :date ORDER BY time ASC")
    fun getEventsByDate(date: String): Flow<List<CalendarEventItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvent(event: CalendarEventItem)

    @Update
    suspend fun updateCalendarEvent(event: CalendarEventItem)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteCalendarEvent(id: String)

    // KARYAWAN & GAJI
    @Query("SELECT * FROM karyawan ORDER BY nama ASC")
    fun getAllKaryawan(): Flow<List<com.example.data.model.KaryawanItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKaryawan(item: com.example.data.model.KaryawanItem)

    @Query("DELETE FROM karyawan WHERE id = :id")
    suspend fun deleteKaryawan(id: String)

    // ABSENSI KARYAWAN
    @Query("SELECT * FROM absensi_karyawan ORDER BY tanggal DESC")
    fun getAllAbsensi(): Flow<List<com.example.data.model.AbsensiItem>>

    @Query("SELECT * FROM absensi_karyawan WHERE tanggal = :date")
    fun getAbsensiByDate(date: String): Flow<List<com.example.data.model.AbsensiItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbsensi(item: com.example.data.model.AbsensiItem)

    @Query("DELETE FROM absensi_karyawan WHERE id = :id")
    suspend fun deleteAbsensi(id: String)

    // SLIP GAJI
    @Query("SELECT * FROM slip_gaji ORDER BY tanggalBayar DESC")
    fun getAllSlipGaji(): Flow<List<com.example.data.model.SlipGajiItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlipGaji(item: com.example.data.model.SlipGajiItem)

    @Query("DELETE FROM slip_gaji WHERE id = :id")
    suspend fun deleteSlipGaji(id: String)

    // RINCIAN ANGGARAN (A, B, C, DLL.)
    @Query("SELECT * FROM rincian_anggaran ORDER BY kode ASC")
    fun getAllRincianAnggaran(): Flow<List<com.example.data.model.RincianAnggaranItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRincianAnggaran(item: com.example.data.model.RincianAnggaranItem)

    @Query("DELETE FROM rincian_anggaran WHERE id = :id")
    suspend fun deleteRincianAnggaran(id: String)

    // REKAP AUDIT KAS
    @Query("SELECT * FROM audit_kas ORDER BY tanggal DESC, jam DESC")
    fun getAllAudit(): Flow<List<com.example.data.model.AuditKasItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(item: com.example.data.model.AuditKasItem)

    @Query("DELETE FROM audit_kas WHERE id = :id")
    suspend fun deleteAudit(id: String)

    // HAPUS SEMUA DATA PALSU / RESET DATABASE KE DATA REAL
    @Query("DELETE FROM deleted_transactions")
    suspend fun clearDeletedTransactions()

    @Query("DELETE FROM piutang_items")
    suspend fun clearPiutang()

    @Query("DELETE FROM anggaran_items")
    suspend fun clearAnggaran()

    @Query("DELETE FROM rincian_anggaran")
    suspend fun clearRincianAnggaran()

    @Query("DELETE FROM karyawan")
    suspend fun clearKaryawan()

    @Query("DELETE FROM absensi_karyawan")
    suspend fun clearAbsensi()

    @Query("DELETE FROM slip_gaji")
    suspend fun clearSlipGaji()

    @Query("DELETE FROM buku_catatan")
    suspend fun clearNotes()

    @Query("DELETE FROM calculator_logs")
    suspend fun clearCalculatorLogs()

    @Query("DELETE FROM calendar_events")
    suspend fun clearCalendarEvents()
}
