package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.CalculatorDatabase
import com.example.data.CalculatorRepository
import com.example.ui.screens.CalculatorDetailScreen
import com.example.ui.screens.MainDashboard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.ui.viewmodel.CalculatorViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize database components
    val database = CalculatorDatabase.getDatabase(applicationContext)
    val repository = CalculatorRepository(database.calculatorDao())
    val viewModel = ViewModelProvider(
      this,
      CalculatorViewModelFactory(application, repository)
    )[CalculatorViewModel::class.java]

    setContent {
      MyApplicationTheme {
        val currentCalculatorId by viewModel.currentCalculatorId.collectAsState()

        // Handle Android system hardware/gesture back presses
        BackHandler(enabled = currentCalculatorId != null) {
          viewModel.navigateBack()
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Crossfade(
            targetState = currentCalculatorId,
            label = "screen_routing",
            modifier = Modifier.padding(innerPadding)
          ) { calcId ->
            if (calcId == null) {
              MainDashboard(viewModel = viewModel)
            } else {
              CalculatorDetailScreen(
                viewModel = viewModel,
                calculatorId = calcId,
                onBackClick = { viewModel.navigateBack() }
              )
            }
          }
        }
      }
    }
  }
}

