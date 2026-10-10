package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String,
    val customerPhone: String = "",
    val serviceCategory: String, // Sahara, Legal, e-District, Student, Photo, Print
    val serviceName: String,
    val amount: Double,
    val paymentMode: String = "UPI", // UPI, Cash
    val timestamp: Long = System.currentTimeMillis(),
    val referenceNumber: String = "",
    val status: String = "Completed", // Pending, Completed, Refunded
    val isPiiMasked: Boolean = false
)
