package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val calculatorId: String,
    val calculatorName: String,
    val inputs: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_calculators")
data class FavoriteCalculator(
    @PrimaryKey val calculatorId: String,
    val timestamp: Long = System.currentTimeMillis()
)
