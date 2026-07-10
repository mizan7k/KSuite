package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CalculatorDatabase
import com.example.data.CalculatorRepository
import com.example.data.FavoriteCalculator
import com.example.data.HistoryEntry
import com.example.ui.screens.CalculatorCategory
import com.example.ui.screens.CalculatorMeta
import com.example.ui.screens.CalculatorRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalculatorViewModel(
    application: Application,
    private val repository: CalculatorRepository
) : AndroidViewModel(application) {

    // Navigation Stack
    val navigationStack = mutableStateListOf<String?>(null) // null represents the home dashboard

    // Active screen selection
    private val _currentCalculatorId = MutableStateFlow<String?>(null)
    val currentCalculatorId: StateFlow<String?> = _currentCalculatorId.asStateFlow()

    // Search and Filtering State
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<CalculatorCategory?>(null)

    // Favorites and History from Room Repository
    val favorites: StateFlow<List<FavoriteCalculator>> = repository.allFavorites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val history: StateFlow<List<HistoryEntry>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Computed filtered calculators list
    val filteredCalculators: StateFlow<List<CalculatorMeta>> = combine(
        searchQuery,
        selectedCategory,
        favorites
    ) { query, category, favs ->
        CalculatorRegistry.list.filter { calc ->
            val matchesSearch = calc.name.contains(query, ignoreCase = true) ||
                    calc.description.contains(query, ignoreCase = true) ||
                    calc.category.displayName.contains(query, ignoreCase = true)
            
            val matchesCategory = category == null || calc.category == category
            
            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalculatorRegistry.list
    )

    // Get Active Calculator Meta
    val activeCalculator: StateFlow<CalculatorMeta?> = currentCalculatorId
        .map { id ->
            CalculatorRegistry.list.find { it.id == id }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Navigation triggers
    fun navigateTo(calculatorId: String?) {
        if (navigationStack.lastOrNull() != calculatorId) {
            navigationStack.add(calculatorId)
            _currentCalculatorId.value = calculatorId
        }
    }

    fun navigateBack(): Boolean {
        return if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.size - 1)
            _currentCalculatorId.value = navigationStack.lastOrNull()
            true
        } else {
            false
        }
    }

    // Database Actions
    fun toggleFavorite(calculatorId: String) {
        viewModelScope.launch {
            val isFav = favorites.value.any { it.calculatorId == calculatorId }
            if (isFav) {
                repository.deleteFavorite(calculatorId)
            } else {
                repository.insertFavorite(FavoriteCalculator(calculatorId))
            }
        }
    }

    fun addHistory(calculatorId: String, calculatorName: String, inputs: String, result: String) {
        viewModelScope.launch {
            repository.insertHistory(
                HistoryEntry(
                    calculatorId = calculatorId,
                    calculatorName = calculatorName,
                    inputs = inputs,
                    result = result
                )
            )
        }
    }

    fun deleteHistory(id: Int) {
        viewModelScope.launch {
            repository.deleteHistoryById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}

class CalculatorViewModelFactory(
    private val application: Application,
    private val repository: CalculatorRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
