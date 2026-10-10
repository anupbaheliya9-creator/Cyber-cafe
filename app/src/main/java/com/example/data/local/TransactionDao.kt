package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TransactionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startOfDayTimestamp ORDER BY timestamp DESC")
    fun getTodayTransactions(startOfDayTimestamp: Long): Flow<List<TransactionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionRecord): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionRecord)

    @Query("UPDATE transactions SET customerPhone = 'XXXXXX' || SUBSTR(customerPhone, -4), isPiiMasked = 1")
    suspend fun maskAllPii()

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}
