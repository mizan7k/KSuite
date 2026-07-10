package com.example.data

import kotlinx.coroutines.flow.Flow

class CalculatorRepository(private val dao: CalculatorDao) {
    val allHistory: Flow<List<HistoryEntry>> = dao.getAllHistory()
    val allFavorites: Flow<List<FavoriteCalculator>> = dao.getAllFavorites()

    suspend fun insertHistory(entry: HistoryEntry) {
        dao.insertHistory(entry)
    }

    suspend fun deleteHistoryById(id: Int) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }

    suspend fun insertFavorite(fav: FavoriteCalculator) {
        dao.insertFavorite(fav)
    }

    suspend fun deleteFavorite(calculatorId: String) {
        dao.deleteFavorite(calculatorId)
    }
}
