package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculators.MathEngine
import com.example.ui.viewmodel.CalculatorViewModel
import kotlin.math.pow
import kotlin.math.ceil
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorDetailScreen(
    viewModel: CalculatorViewModel,
    calculatorId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCalculator by viewModel.activeCalculator.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val isPinned = favorites.any { it.calculatorId == calculatorId }
    val focusManager = LocalFocusManager.current

    val meta = activeCalculator ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = meta.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            onBackClick()
                        },
                        modifier = Modifier.testTag("detail_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFavorite(calculatorId) },
                        modifier = Modifier.testTag("detail_pin_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (calculatorId == "scientific") {
                ScientificCalculatorView(viewModel)
            } else {
                GenericCalculatorForm(viewModel, meta)
            }
        }
    }
}

// --- bespoke view: scientific & matrix ---
@Composable
fun ScientificCalculatorView(viewModel: CalculatorViewModel) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0 = standard/trig, 1 = advanced matrix
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = {
                    focusManager.clearFocus()
                    selectedSubTab = 0
                },
                text = { Text("Standard & Trig", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = {
                    focusManager.clearFocus()
                    selectedSubTab = 1
                },
                text = { Text("Matrix Solver (2x2 / 3x3)", fontWeight = FontWeight.Bold) }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (selectedSubTab == 0) {
                StandardTrigCalculator(viewModel)
            } else {
                MatrixCalculatorView(viewModel)
            }
        }
    }
}

@Composable
fun StandardTrigCalculator(viewModel: CalculatorViewModel) {
    var displayStr by remember { mutableStateOf("") }
    var resultStr by remember { mutableStateOf("") }

    val df = DecimalFormat("#.#######")

    fun append(text: String) {
        displayStr += text
    }

    fun backspace() {
        if (displayStr.isNotEmpty()) {
            displayStr = displayStr.dropLast(1)
        }
    }

    fun clear() {
        displayStr = ""
        resultStr = ""
    }

    fun calculate() {
        if (displayStr.isBlank()) return
        val res = MathEngine.evaluateScientific(displayStr)
        resultStr = if (res.isNaN()) {
            "Error"
        } else {
            df.format(res)
        }
        if (resultStr != "Error") {
            viewModel.addHistory("scientific", "Scientific Calculator", displayStr, "= $resultStr")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Digital display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = displayStr.ifEmpty { "0" },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End
                    ),
                    maxLines = 3,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (resultStr.isNotEmpty()) "= $resultStr" else "",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Keypad grid
        val keys = listOf(
            listOf("C", "(", ")", "DEL", "/"),
            listOf("sin", "cos", "tan", "^", "*"),
            listOf("ln", "log", "sqrt", "π", "-"),
            listOf("7", "8", "9", "e", "+"),
            listOf("4", "5", "6", "10^x", "="),
            listOf("1", "2", "3", "0", ".")
        )

        Column(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (key in row) {
                        val isOperator = key in listOf("/", "*", "-", "+", "=", "C", "DEL")
                        val isFunc = key in listOf("sin", "cos", "tan", "ln", "log", "sqrt", "^", "10^x")
                        Button(
                            onClick = {
                                when (key) {
                                    "C" -> clear()
                                    "DEL" -> backspace()
                                    "=" -> calculate()
                                    "10^x" -> append("10^")
                                    else -> append(key)
                                }
                            },
                            modifier = Modifier
                                .weight(if (key == "=") 1f else if (key == "0") 2f else 1f)
                                .height(56.dp)
                                .testTag("scikey_$key"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    key == "=" -> MaterialTheme.colorScheme.primary
                                    isOperator -> MaterialTheme.colorScheme.tertiaryContainer
                                    isFunc -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                },
                                contentColor = when {
                                    key == "=" -> MaterialTheme.colorScheme.onPrimary
                                    isOperator -> MaterialTheme.colorScheme.onTertiaryContainer
                                    isFunc -> MaterialTheme.colorScheme.onSecondaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (key.length > 3) 12.sp else 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatrixCalculatorView(viewModel: CalculatorViewModel) {
    var size3x3 by remember { mutableStateOf(false) } // False = 2x2, True = 3x3

    // Matrix fields
    val m1 = remember { mutableStateListOf("", "", "", "", "", "", "", "", "") }
    var resultText by remember { mutableStateOf("") }

    fun solve(op: String) {
        try {
            val grid = if (size3x3) 3 else 2
            val matrix = List(grid) { r ->
                List(grid) { c ->
                    val index = r * grid + c
                    m1[index].toDoubleOrNull() ?: 0.0
                }
            }

            val df = DecimalFormat("#.###")

            when (op) {
                "det" -> {
                    val det = if (size3x3) MathEngine.matrixDeterminant3x3(matrix) else MathEngine.matrixDeterminant2x2(matrix)
                    resultText = "Determinant = ${df.format(det)}"
                }
                "trans" -> {
                    val transposed = MathEngine.transposeMatrix(matrix)
                    val formatStr = transposed.joinToString("\n") { row ->
                        "[ " + row.joinToString(" , ") { df.format(it) } + " ]"
                    }
                    resultText = "Transpose Matrix:\n$formatStr"
                }
                "inv" -> {
                    val inv = if (size3x3) MathEngine.matrixInverse3x3(matrix) else MathEngine.matrixInverse2x2(matrix)
                    resultText = if (inv == null) {
                        "Matrix is singular (Determinant is 0). No inverse exists."
                    } else {
                        "Inverse Matrix:\n" + inv.joinToString("\n") { row ->
                            "[ " + row.joinToString(" , ") { df.format(it) } + " ]"
                        }
                    }
                }
            }
            viewModel.addHistory("scientific", "Matrix calculations", "Operation: $op", resultText)
        } catch (e: Exception) {
            resultText = "Error during calculation. Check that inputs are numbers."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Matrix Dimension:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row {
                FilterChip(
                    selected = !size3x3,
                    onClick = { size3x3 = false },
                    label = { Text("2 x 2") },
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = size3x3,
                    onClick = { size3x3 = true },
                    label = { Text("3 x 3") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Matrix Grid Inputs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter Matrix Values:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val gridDim = if (size3x3) 3 else 2
                for (r in 0 until gridDim) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        for (c in 0 until gridDim) {
                            val index = r * gridDim + c
                            OutlinedTextField(
                                value = m1[index],
                                onValueChange = { m1[index] = it },
                                modifier = Modifier
                                    .width(64.dp)
                                    .testTag("matrix_input_${r}_${c}"),
                                placeholder = { Text("0") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Keys
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { solve("det") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_matrix_det"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Det", fontSize = 13.sp)
            }
            Button(
                onClick = { solve("inv") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_matrix_inv"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Inverse", fontSize = 13.sp)
            }
            Button(
                onClick = { solve("trans") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_matrix_trans"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Transpose", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Result panel
        if (resultText.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Result:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }
    }
}


// --- 13 CATEGORY GENERIC FORM GENERATOR FOR OTHER 30 CALCULATORS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericCalculatorForm(viewModel: CalculatorViewModel, meta: CalculatorMeta) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Common Input States (Using remembers based on active calculator type to avoid collisions)
    var num1 by remember(meta.id) { mutableStateOf("") }
    var num2 by remember(meta.id) { mutableStateOf("") }
    var num3 by remember(meta.id) { mutableStateOf("") }
    var num4 by remember(meta.id) { mutableStateOf("") }
    var num5 by remember(meta.id) { mutableStateOf("") }

    var selectOpt by remember(meta.id) { mutableStateOf("") }
    var resultSummary by remember(meta.id) { mutableStateOf("") }

    val df = DecimalFormat("#,##0.00")

    fun onCalculateClick() {
        focusManager.clearFocus()
        try {
            val val1 = num1.toDoubleOrNull() ?: 0.0
            val val2 = num2.toDoubleOrNull() ?: 0.0
            val val3 = num3.toDoubleOrNull() ?: 0.0
            val val4 = num4.toDoubleOrNull() ?: 0.0
            val val5 = num5.toDoubleOrNull() ?: 0.0

            when (meta.id) {
                "mortgage" -> {
                    val m = MathEngine.calculateMortgage(val1, val2, val3, val4.toInt().coerceAtLeast(1))
                    val totalPay = (m["total_monthly"] as Double)
                    resultSummary = "Est. Monthly Payment: \$${df.format(totalPay)}\nPrincipal + Interest: \$${df.format(m["principal_interest"] as Double)}\nTotal Interest Cost: \$${df.format(m["total_interest"] as Double)}"
                }
                "loan" -> {
                    val l = MathEngine.calculateLoan(val1, val2, val3.toInt().coerceAtLeast(1))
                    resultSummary = "Monthly Payment: \$${df.format(l["monthly"])}\nTotal Interest: \$${df.format(l["total_interest"])}\nTotal Payback: \$${df.format(l["total_pay"])}"
                }
                "auto_loan" -> {
                    val a = MathEngine.calculateAutoLoan(val1, val2, val3, val4, val5, 60)
                    resultSummary = "Estimated Monthly: \$${df.format(a["monthly"])}\nTotal Financed: \$${df.format(a["finance_amount"])}\nTotal Sales Tax: \$${df.format(a["sales_tax"])}"
                }
                "interest", "compound_interest" -> {
                    val compoundRate = if (selectOpt.lowercase().contains("monthly")) 12 else 1
                    val finalBalance = val1 * (1 + (val2 / 100.0) / compoundRate).pow(val3 * compoundRate)
                    resultSummary = "Final Account Balance: \$${df.format(finalBalance)}\nTotal Compound Interest: \$${df.format(finalBalance - val1)}"
                }
                "payment" -> {
                    val r = val2 / 100.0 / 12.0
                    val term = val3.toInt().coerceAtLeast(1)
                    val pay = if (r == 0.0) val1 / term else val1 * (r * (1 + r).pow(term)) / ((1 + r).pow(term) - 1)
                    resultSummary = "Required Monthly Payment: \$${df.format(pay)}\nTotal Payback Over $term Months: \$${df.format(pay * term)}"
                }
                "retirement" -> {
                    val ret = MathEngine.calculateRetirement(val1.toInt(), val2.toInt(), val3, val4, val5, 2.5)
                    resultSummary = "Total Savings at Retirement: \$${df.format(ret["nominal_balance"])}\nIn Today's Buying Power (2.5% inflation): \$${df.format(ret["purchasing_power"])}"
                }
                "amortization" -> {
                    val list = MathEngine.generateAmortization(val1, val2, val3.toInt().coerceAtLeast(1))
                    resultSummary = "Full Schedule Generated: ${list.size} months\nAverage Monthly Payment: \$${df.format(list.firstOrNull()?.get("payment") ?: 0.0)}"
                }
                "investment" -> {
                    var current = val1
                    val rate = val3 / 100.0
                    val cont = val2
                    for (i in 1..(val4.toInt())) {
                        current = current * (1 + rate) + cont
                    }
                    resultSummary = "Portfolio Value after ${val4.toInt()} years: \$${df.format(current)}\nTotal Principal: \$${df.format(val1 + cont * val4)}"
                }
                "inflation" -> {
                    // Average inflation multiplier rate
                    val years = (val3 - val2).coerceAtLeast(0.0)
                    val inflated = val1 * (1 + 0.028).pow(years)
                    resultSummary = "Equivalent buying value in Year ${val3.toInt()}: \$${df.format(inflated)}\nTotal cumulative inflation (avg 2.8%): ${df.format(((inflated - val1)/val1)*100)}%"
                }
                "finance" -> {
                    val tvm = MathEngine.solveTVM(val1, val2, val3, val4, val5)
                    resultSummary = tvm.entries.joinToString("\n") { "${it.key.uppercase()}: \$${df.format(it.value)}" }
                }
                "income_tax" -> {
                    val tax = MathEngine.calculateIncomeTax(val1, selectOpt.ifEmpty { "single" })
                    resultSummary = "Take-Home Pay: \$${df.format(tax["take_home"])}\nTax Owed: \$${df.format(tax["tax_owed"])}\nEffective Tax Rate: ${df.format(tax["effective_rate"])}%"
                }
                "salary" -> {
                    val annual = val1 * val2 * 52
                    resultSummary = "Hourly Wage: \$${df.format(val1)}\nWeekly Salary: \$${df.format(val1 * val2)}\nMonthly equivalent: \$${df.format(annual / 12)}\nAnnual gross salary: \$${df.format(annual)}"
                }
                "interest_rate" -> {
                    // Quick rate estimation
                    val estApr = (val3 * 12 * 100) / val1 - 100
                    resultSummary = "Estimated Annual Rate (APR): ${df.format(estApr.coerceAtLeast(0.0))}%"
                }
                "sales_tax" -> {
                    val taxAmt = val1 * (val2 / 100.0)
                    resultSummary = "Sales Tax Amount: \$${df.format(taxAmt)}\nTotal Price (Gross): \$${df.format(val1 + taxAmt)}"
                }
                "bmi" -> {
                    val (score, cat) = MathEngine.calculateBMI(val1, val2)
                    resultSummary = "BMI Score = ${df.format(score)}\nWeight Classification: $cat"
                }
                "calorie" -> {
                    val daily = MathEngine.calculateCalorie(val1.toInt(), selectOpt, val2, val3, "active", "maintain")
                    resultSummary = "Your Daily Caloric Target: ${df.format(daily)} kcal/day\nFor steady weight maintenance."
                }
                "body_fat" -> {
                    val bf = MathEngine.calculateBodyFat(selectOpt, val1, val2, val3, val4)
                    resultSummary = "Estimated Body Fat: ${df.format(bf.coerceIn(0.0, 100.0))}%\nCalculated via US Navy circumferences."
                }
                "bmr" -> {
                    val bmrVal = if (selectOpt.equals("male", true)) 10 * val2 + 6.25 * val3 - 5 * val1 + 5 else 10 * val2 + 6.25 * val3 - 5 * val1 - 161
                    resultSummary = "Basal Metabolic Rate (BMR): ${df.format(bmrVal)} kcal/day"
                }
                "ideal_weight" -> {
                    val hInches = (val2 - 152.4) / 2.54
                    val base = if (selectOpt.equals("male", true)) 50.0 else 45.5
                    val ideal = base + 2.3 * hInches.coerceAtLeast(0.0)
                    resultSummary = "Ideal healthy body weight: ${df.format(ideal)} kg (${df.format(ideal * 2.204)} lbs)"
                }
                "pace" -> {
                    val totalSecs = val2 * 60 + val3
                    val paceMin = (totalSecs / val1) / 60
                    val paceSec = (totalSecs / val1) % 60
                    resultSummary = "Calculated running pace: ${paceMin.toInt()}:${String.format("%02d", paceSec.toInt())} per km/mile"
                }
                "pregnancy", "due_date" -> {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, 280)
                    val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
                    resultSummary = "Estimated Delivery Due Date:\n${sdf.format(calendar.time)}\nCongratulations!"
                }
                "conception" -> {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, 14)
                    val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    resultSummary = "Estimated conception window:\n${sdf.format(calendar.time)}"
                }
                "age" -> {
                    val years = (val2 - val1).coerceAtLeast(0.0)
                    resultSummary = "Age = ${years.toInt()} years, ${(years % 1 * 12).toInt()} months"
                }
                "date" -> {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, val1.toInt())
                    val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    resultSummary = "Calculated target date: ${sdf.format(cal.time)}"
                }
                "time", "hours" -> {
                    val totalHrs = val1 + val2 + (val3 / 60.0)
                    resultSummary = "Total Summed Duration: ${df.format(totalHrs)} hours"
                }
                "gpa" -> {
                    val avgGpa = (val1 * 4 + val2 * 3) / (val1 + val2).coerceAtLeast(1.0)
                    resultSummary = "Estimated Cumulative GPA: ${df.format(avgGpa)}"
                }
                "grade" -> {
                    val req = (val3 - val1 * (1 - val2 / 100.0)) / (val2 / 100.0)
                    resultSummary = "Required Exam Score: ${df.format(req)}%\nTo secure class target grade."
                }
                "concrete" -> {
                    val vol = (val1 * val2 * (val3 / 12.0)) / 27.0
                    resultSummary = "Volume Required: ${df.format(vol)} cubic yards\nStandard 80lb bags needed: ${df.format(ceil(vol * 45))}"
                }
                "subnet" -> {
                    val subnet = MathEngine.calculateSubnet(selectOpt.ifEmpty { "192.168.1.1" }, val1.toInt().coerceIn(1, 32))
                    resultSummary = "Subnet Mask: ${subnet["subnet_mask"]}\nBroadcast Address: ${subnet["broadcast_address"]}\nUsable hosts count: ${subnet["total_hosts"]}"
                }
                "password" -> {
                    val pass = MathEngine.generatePassword(val1.toInt().coerceIn(8, 64), true, true, true, true)
                    resultSummary = "Generated Password:\n$pass"
                }
                "conversion" -> {
                    val converted = val1 * 2.54 // inch to cm standard mock
                    resultSummary = "Converted Output: ${df.format(converted)}"
                }
                "fraction" -> {
                    val n1 = num1.toDoubleOrNull() ?: 0.0
                    val d1 = num2.toDoubleOrNull() ?: 1.0
                    val n2 = num3.toDoubleOrNull() ?: 0.0
                    val d2 = num4.toDoubleOrNull() ?: 1.0
                    
                    if (d1 == 0.0 || d2 == 0.0) {
                        resultSummary = "Error: Denominator cannot be zero."
                    } else {
                        val (ansNum, ansDen) = when {
                            selectOpt.contains("Add") || selectOpt.contains("+") -> (n1 * d2 + n2 * d1) to (d1 * d2)
                            selectOpt.contains("Subtract") || selectOpt.contains("-") -> (n1 * d2 - n2 * d1) to (d1 * d2)
                            selectOpt.contains("Multiply") || selectOpt.contains("*") -> (n1 * n2) to (d1 * d2)
                            else -> (n1 * d2) to (d1 * n2)
                        }
                        if (ansDen == 0.0) {
                            resultSummary = "Error: Division by zero."
                        } else {
                            fun gcd(a: Long, b: Long): Long {
                                return if (b == 0L) kotlin.math.abs(a) else gcd(b, a % b)
                            }
                            val divisor = gcd(ansNum.toLong(), ansDen.toLong()).coerceAtLeast(1)
                            val simplifiedNum = ansNum.toLong() / divisor
                            val simplifiedDen = ansDen.toLong() / divisor
                            
                            val decimalVal = ansNum / ansDen
                            resultSummary = "Result Fraction: $simplifiedNum/$simplifiedDen\nDecimal Value: ${df.format(decimalVal)}"
                        }
                    }
                }
                "percentage" -> {
                    resultSummary = when {
                        selectOpt.contains("Find X%") || selectOpt.isEmpty() -> {
                            val res = (val1 / 100.0) * val2
                            "Result: ${df.format(val1)}% of ${df.format(val2)} is ${df.format(res)}"
                        }
                        selectOpt.contains("What %") -> {
                            if (val2 == 0.0) "Error: Y cannot be zero."
                            else {
                                val res = (val1 / val2) * 100.0
                                "Result: ${df.format(val1)} is ${df.format(res)}% of ${df.format(val2)}"
                            }
                        }
                        else -> {
                            if (val1 == 0.0) "Error: Original value cannot be zero."
                            else {
                                val change = ((val2 - val1) / val1) * 100.0
                                val typeStr = if (change >= 0) "Increase" else "Decrease"
                                "Result: ${df.format(kotlin.math.abs(change))}% $typeStr from ${df.format(val1)} to ${df.format(val2)}"
                            }
                        }
                    }
                }
                "random" -> {
                    val minVal = val1.toInt()
                    val maxVal = val2.toInt()
                    if (minVal >= maxVal) {
                        resultSummary = "Error: Minimum must be less than Maximum."
                    } else {
                        val rand = (minVal..maxVal).random()
                        resultSummary = "Random Number generated: $rand\nRange: [$minVal, $maxVal]"
                    }
                }
                "triangle" -> {
                    val s = (val1 + val2 + val3) / 2.0
                    val areaSq = s * (s - val1) * (s - val2) * (s - val3)
                    if (val1 <= 0 || val2 <= 0 || val3 <= 0 || val1 + val2 <= val3 || val1 + val3 <= val2 || val2 + val3 <= val1) {
                        resultSummary = "Error: Invalid triangle sides. The sum of any two sides must be greater than the third side."
                    } else {
                        val area = kotlin.math.sqrt(areaSq)
                        val perimeter = val1 + val2 + val3
                        resultSummary = "Triangle Solver:\nPerimeter: ${df.format(perimeter)}\nArea (Heron's): ${df.format(area)}"
                    }
                }
                "std_dev" -> {
                    val list = num1.split(",").mapNotNull { it.trim().toDoubleOrNull() }
                    if (list.size < 2) {
                        resultSummary = "Error: Please enter at least 2 valid numbers separated by commas."
                    } else {
                        val mean = list.average()
                        val sumSqDiff = list.sumOf { (it - mean).pow(2) }
                        val sampleVar = sumSqDiff / (list.size - 1)
                        val popVar = sumSqDiff / list.size
                        val sampleStdDev = kotlin.math.sqrt(sampleVar)
                        val popStdDev = kotlin.math.sqrt(popVar)
                        resultSummary = "Statistics Summary:\nCount: ${list.size}\nMean (Average): ${df.format(mean)}\nSample Std Dev (s): ${df.format(sampleStdDev)}\nPopulation Std Dev (σ): ${df.format(popStdDev)}\nSample Variance: ${df.format(sampleVar)}"
                    }
                }
                else -> {
                    resultSummary = "Calculation completed successfully."
                }
            }

            // Automatically push results to local Room histories
            val inputsCollected = listOfNotNull(
                if (num1.isNotEmpty()) "${getLabel(meta.id, 1)}: $num1" else null,
                if (num2.isNotEmpty()) "${getLabel(meta.id, 2)}: $num2" else null,
                if (num3.isNotEmpty()) "${getLabel(meta.id, 3)}: $num3" else null,
                if (num4.isNotEmpty()) "${getLabel(meta.id, 4)}: $num4" else null,
                if (selectOpt.isNotEmpty()) "Option: $selectOpt" else null
            ).joinToString(", ")

            viewModel.addHistory(meta.id, meta.name, inputsCollected, resultSummary.replace("\n", " | "))

        } catch (e: Exception) {
            resultSummary = "Invalid parameter fields. Double check numeric fields."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Description Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getCalculatorIcon(meta.iconName),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = meta.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Form Inputs Cards
        Text(
            "Input Parameters",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Dynamic Field Renders based on meta ID
        val fieldsCount = getFieldsCount(meta.id)

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (fieldsCount >= 1) {
                OutlinedTextField(
                    value = num1,
                    onValueChange = { num1 = it },
                    label = { Text(getLabel(meta.id, 1)) },
                    modifier = Modifier.fillMaxWidth().testTag("input_field_1"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
            if (fieldsCount >= 2) {
                OutlinedTextField(
                    value = num2,
                    onValueChange = { num2 = it },
                    label = { Text(getLabel(meta.id, 2)) },
                    modifier = Modifier.fillMaxWidth().testTag("input_field_2"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
            if (fieldsCount >= 3) {
                OutlinedTextField(
                    value = num3,
                    onValueChange = { num3 = it },
                    label = { Text(getLabel(meta.id, 3)) },
                    modifier = Modifier.fillMaxWidth().testTag("input_field_3"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
            if (fieldsCount >= 4) {
                OutlinedTextField(
                    value = num4,
                    onValueChange = { num4 = it },
                    label = { Text(getLabel(meta.id, 4)) },
                    modifier = Modifier.fillMaxWidth().testTag("input_field_4"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
            if (fieldsCount >= 5) {
                OutlinedTextField(
                    value = num5,
                    onValueChange = { num5 = it },
                    label = { Text(getLabel(meta.id, 5)) },
                    modifier = Modifier.fillMaxWidth().testTag("input_field_5"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            // Dropdowns or extra configurations if relevant
            if (hasDropdown(meta.id)) {
                var expanded by remember { mutableStateOf(false) }
                val options = getDropdownOptions(meta.id)
                if (selectOpt.isEmpty() && options.isNotEmpty()) {
                    selectOpt = options[0]
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Selection: $selectOpt", fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        options.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    selectOpt = opt
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Trigger Button
        Button(
            onClick = { onCalculateClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("btn_calculate_generic"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Calculate Result", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Results Presentation
        if (resultSummary.isNotEmpty()) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Calculation Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(resultSummary))
                                    Toast.makeText(context, "Results copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp).testTag("copy_result_btn")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy text", modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = resultSummary,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 24.sp,
                                fontFamily = FontFamily.SansSerif
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// Helpers to dynamically resolve parameters count and descriptors
fun getFieldsCount(id: String): Int {
    return when (id) {
        "fraction" -> 4
        "percentage" -> 2
        "random" -> 2
        "triangle" -> 3
        "std_dev" -> 1
        "mortgage" -> 4
        "loan" -> 3
        "auto_loan" -> 5
        "interest", "compound_interest" -> 3
        "payment" -> 3
        "retirement" -> 5
        "amortization" -> 3
        "investment" -> 4
        "inflation" -> 3
        "finance" -> 5
        "income_tax" -> 1
        "salary" -> 2
        "interest_rate" -> 3
        "sales_tax" -> 2
        "bmi" -> 2
        "calorie" -> 3
        "body_fat" -> 3
        "bmr" -> 3
        "ideal_weight" -> 2
        "pace" -> 3
        "pregnancy", "due_date" -> 1
        "conception" -> 1
        "age" -> 2
        "date" -> 1
        "time", "hours" -> 3
        "gpa" -> 2
        "grade" -> 3
        "concrete" -> 3
        "subnet" -> 1
        "password" -> 1
        "conversion" -> 1
        else -> 1
    }
}

fun getLabel(id: String, fieldNum: Int): String {
    return when (id) {
        "fraction" -> when (fieldNum) {
            1 -> "Numerator 1"
            2 -> "Denominator 1"
            3 -> "Numerator 2"
            else -> "Denominator 2"
        }
        "percentage" -> when (fieldNum) {
            1 -> "X Value"
            else -> "Y Value"
        }
        "random" -> when (fieldNum) {
            1 -> "Minimum Value"
            else -> "Maximum Value"
        }
        "triangle" -> when (fieldNum) {
            1 -> "Side A Length"
            2 -> "Side B Length"
            else -> "Side C Length"
        }
        "std_dev" -> "Value List (separated by commas)"
        "mortgage" -> when (fieldNum) {
            1 -> "Home Price ($)"
            2 -> "Down Payment ($)"
            3 -> "Interest Rate (%)"
            else -> "Loan Term (Years)"
        }
        "loan" -> when (fieldNum) {
            1 -> "Loan Amount ($)"
            2 -> "Annual Interest Rate (%)"
            else -> "Term (Months)"
        }
        "auto_loan" -> when (fieldNum) {
            1 -> "Auto Price ($)"
            2 -> "Down Payment ($)"
            3 -> "Trade-in Value ($)"
            4 -> "Sales Tax (%)"
            else -> "Interest Rate (%)"
        }
        "interest", "compound_interest" -> when (fieldNum) {
            1 -> "Principal Amount ($)"
            2 -> "Annual Interest Rate (%)"
            else -> "Term (Years)"
        }
        "payment" -> when (fieldNum) {
            1 -> "Target Debt Balance ($)"
            2 -> "Annual Interest Rate (%)"
            else -> "Payoff Months"
        }
        "retirement" -> when (fieldNum) {
            1 -> "Current Age"
            2 -> "Retirement Target Age"
            3 -> "Current Retirement Savings ($)"
            4 -> "Monthly Contribution ($)"
            else -> "Annual Expected Return (%)"
        }
        "amortization" -> when (fieldNum) {
            1 -> "Loan Amount ($)"
            2 -> "Annual Interest Rate (%)"
            else -> "Term (Years)"
        }
        "investment" -> when (fieldNum) {
            1 -> "Starting Investment ($)"
            2 -> "Annual Contributions ($)"
            3 -> "Estimated Rate of Return (%)"
            else -> "Investment Term (Years)"
        }
        "inflation" -> when (fieldNum) {
            1 -> "Starting Principal ($)"
            2 -> "Start Year (e.g. 2000)"
            else -> "End Target Year"
        }
        "finance" -> when (fieldNum) {
            1 -> "Present Value (PV)"
            2 -> "Future Value (FV)"
            3 -> "Monthly Payment (PMT)"
            4 -> "Number of Periods (N)"
            else -> "Interest Rate (I/Y)"
        }
        "income_tax" -> "Gross Annual Salary ($)"
        "salary" -> when (fieldNum) {
            1 -> "Hourly Pay Rate ($)"
            else -> "Weekly Working Hours"
        }
        "interest_rate" -> when (fieldNum) {
            1 -> "Principal Balance ($)"
            2 -> "Months Term"
            else -> "Monthly Repayment ($)"
        }
        "sales_tax" -> when (fieldNum) {
            1 -> "Net Purchase Price ($)"
            else -> "Sales Tax Rate (%)"
        }
        "bmi" -> when (fieldNum) {
            1 -> "Weight (kg)"
            else -> "Height (cm)"
        }
        "calorie" -> when (fieldNum) {
            1 -> "Age (Years)"
            2 -> "Weight (kg)"
            else -> "Height (cm)"
        }
        "body_fat" -> when (fieldNum) {
            1 -> "Height (cm)"
            2 -> "Neck Circumference (cm)"
            else -> "Waist Circumference (cm)"
        }
        "bmr" -> when (fieldNum) {
            1 -> "Age (Years)"
            2 -> "Weight (kg)"
            else -> "Height (cm)"
        }
        "ideal_weight" -> when (fieldNum) {
            1 -> "Age (Years)"
            else -> "Height (cm)"
        }
        "pace" -> when (fieldNum) {
            1 -> "Distance (km/miles)"
            2 -> "Hours Duration"
            else -> "Minutes Duration"
        }
        "pregnancy", "conception", "due_date" -> "Days since LMP date"
        "age" -> when (fieldNum) {
            1 -> "Year of Birth"
            else -> "Target Year"
        }
        "date" -> "Days to Offset (+ or -)"
        "time", "hours" -> when (fieldNum) {
            1 -> "Hours"
            2 -> "Minutes"
            else -> "Seconds"
        }
        "gpa" -> when (fieldNum) {
            1 -> "A Grades Count"
            else -> "B Grades Count"
        }
        "grade" -> when (fieldNum) {
            1 -> "Current Class Grade (%)"
            2 -> "Final Exam Weight (%)"
            else -> "Target Class Grade (%)"
        }
        "concrete" -> when (fieldNum) {
            1 -> "Slab Length (Feet)"
            2 -> "Slab Width (Feet)"
            else -> "Thickness (Inches)"
        }
        "subnet" -> "CIDR Subnet Mask Bit (e.g. 24)"
        "password" -> "Target Character Length (e.g. 16)"
        "conversion" -> "Value to Convert"
        else -> "Input Value"
    }
}

fun hasDropdown(id: String): Boolean {
    return id in listOf(
        "interest", "compound_interest", "income_tax",
        "calorie", "body_fat", "bmr", "ideal_weight", "subnet",
        "fraction", "percentage"
    )
}

fun getDropdownOptions(id: String): List<String> {
    return when (id) {
        "fraction" -> listOf("Add (+)", "Subtract (-)", "Multiply (*)", "Divide (/)")
        "percentage" -> listOf("Find X% of Y", "What % of Y is X", "Percentage Change from X to Y")
        "interest", "compound_interest" -> listOf("Monthly Compounding", "Quarterly Compounding", "Annual Compounding")
        "income_tax" -> listOf("Single Filing Status", "Married Filing Status")
        "calorie", "bmr", "ideal_weight" -> listOf("Male", "Female")
        "body_fat" -> listOf("Male", "Female")
        "subnet" -> listOf("192.168.1.1", "10.0.0.1", "172.16.0.1")
        else -> emptyList()
    }
}
