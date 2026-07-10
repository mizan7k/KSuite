package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculatorDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: HistoryEntry)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM calculation_history")
    suspend fun clearHistory()

    @Query("SELECT * FROM favorite_calculators ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteCalculator>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(fav: FavoriteCalculator)

    @Query("DELETE FROM favorite_calculators WHERE calculatorId = :calculatorId")
    suspend fun deleteFavorite(calculatorId: String)
}
