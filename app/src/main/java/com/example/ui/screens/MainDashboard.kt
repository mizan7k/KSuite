package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.HistoryEntry
import com.example.ui.viewmodel.CalculatorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredCalculators by viewModel.filteredCalculators.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val history by viewModel.history.collectAsState()
    val focusManager = LocalFocusManager.current

    var selectedTab by remember { mutableStateOf(0) } // 0 = Calculators, 1 = History

    val themeGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themeGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Hero Header with Image Banner (M3 Edge-to-Edge friendly)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Background generated hero banner
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner_1783686268860),
                    contentDescription = "Mathematical Header",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Overlapping gradient scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                // GitHub badge in the top-right corner
                val context = androidx.compose.ui.platform.LocalContext.current
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                        .clickable {
                            try {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://github.com/mizan7k")
                                ).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // fallback
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "GitHub Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Built with ❤️ by mizan7k",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // App Branding Text inside Header
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "KSuite",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "All-in-one Calculation Suite",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Top Tab Selectors (Calculators vs History Logs)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calculators", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_calculators")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("History (${history.size})", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_history")
                )
            }

            // Content Screens based on tab
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally { width -> if (targetState > initialState) width else -width } togetherWith
                            slideOutHorizontally { width -> if (targetState > initialState) -width else width }
                },
                label = "dashboard_tab_content",
                modifier = Modifier.weight(1f)
            ) { targetTab ->
                when (targetTab) {
                    0 -> {
                        // Calculators Directory
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Search Bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.searchQuery.value = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .testTag("search_bar"),
                                placeholder = { Text("Search 30+ calculators...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(28.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                ),
                                singleLine = true
                            )

                            // Category scrollable selection chips
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedCategory == null,
                                        onClick = { viewModel.selectedCategory.value = null },
                                        label = { Text("All Tools") },
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.testTag("category_chip_all")
                                    )
                                }
                                items(CalculatorCategory.entries.toTypedArray()) { category ->
                                    FilterChip(
                                        selected = selectedCategory == category,
                                        onClick = { viewModel.selectedCategory.value = category },
                                        label = { Text(category.displayName) },
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                                    )
                                }
                            }

                            // Favorite Pins Row (displayed if any calculators are favorited and search isn't active)
                            if (favorites.isNotEmpty() && searchQuery.isEmpty() && selectedCategory == null) {
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    Text(
                                        text = "Your Pins",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        val favoriteMetas = CalculatorRegistry.list.filter { meta ->
                                            favorites.any { it.calculatorId == meta.id }
                                        }
                                        items(favoriteMetas) { meta ->
                                            PinCard(meta = meta, onClick = { viewModel.navigateTo(meta.id) })
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }

                            // Main list/grid of calculators
                            if (filteredCalculators.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.SearchOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            "No calculators found",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "Try searching for another keyword",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 165.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .testTag("calculators_grid"),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(filteredCalculators) { meta ->
                                        val isPinned = favorites.any { it.calculatorId == meta.id }
                                        CalculatorGridItem(
                                            meta = meta,
                                            isPinned = isPinned,
                                            onPinClick = { viewModel.toggleFavorite(meta.id) },
                                            onClick = {
                                                focusManager.clearFocus()
                                                viewModel.navigateTo(meta.id)
                                            }
                                        )
                                    }
                                }

                                FaqSection(
                                    title = "Calculators & Tools FAQ",
                                    faqs = listOf(
                                        "What tools are included in KSuite?" to "KSuite houses 30+ calculators covering Finance, Health, Algebra, Standard Math, and Statistics, plus an advanced Scientific & Matrix Solver.",
                                        "How do I pin my favorite calculators?" to "Tap the Pin icon on the top right of any calculator screen or dashboard card to pin it to your 'Your Pins' panel on the home screen.",
                                        "Is my data stored securely?" to "Yes, KSuite is 100% offline-first. Your calculation parameters and history logs are kept private inside your local database."
                                    )
                                )
                            }
                        }
                    }
                    1 -> {
                        // History Logs Panel
                        if (history.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                                    Icon(
                                        Icons.Default.HistoryToggleOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(72.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "No calculations recorded yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Use any calculator and your previous queries will be stored here safely offline.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }

                                FaqSection(
                                    title = "History Log FAQ",
                                    faqs = listOf(
                                        "What is stored in History?" to "KSuite automatically records the parameters, selected modes, formulas, and final answers of any calculation you run so you can keep track of them.",
                                        "How do I reload a previous calculation?" to "Simply tap on any calculation log card in the history list to quickly return to that calculator with your parameters auto-filled.",
                                        "How do I clear my history logs?" to "You can swipe/tap delete on individual history items, or click the 'Clear All' button in the top-right of this panel to reset your log."
                                    )
                                )
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Calculations Log",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    TextButton(
                                        onClick = { viewModel.clearAllHistory() },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Clear All")
                                    }
                                }
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(history) { entry ->
                                        HistoryRowItem(
                                            entry = entry,
                                            onDelete = { viewModel.deleteHistory(entry.id) },
                                            onReload = { viewModel.navigateTo(entry.calculatorId) }
                                        )
                                    }
                                }

                                FaqSection(
                                    title = "History Log FAQ",
                                    faqs = listOf(
                                        "What is stored in History?" to "KSuite automatically records the parameters, selected modes, formulas, and final answers of any calculation you run so you can keep track of them.",
                                        "How do I reload a previous calculation?" to "Simply tap on any calculation log card in the history list to quickly return to that calculator with your parameters auto-filled.",
                                        "How do I clear my history logs?" to "You can swipe/tap delete on individual history items, or click the 'Clear All' button in the top-right of this panel to reset your log."
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinCard(
    meta: CalculatorMeta,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(130.dp)
            .clickable(onClick = onClick)
            .testTag("pin_${meta.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCalculatorIcon(meta.iconName),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = meta.name,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = meta.category.displayName,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            )
        }
    }
}

@Composable
fun CalculatorGridItem(
    meta: CalculatorMeta,
    isPinned: Boolean,
    onPinClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("calc_item_${meta.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Pin indicator button
            IconButton(
                onClick = onPinClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .padding(4.dp)
                    .testTag("pin_btn_${meta.id}")
            ) {
                Icon(
                    imageVector = if (isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                    contentDescription = if (isPinned) "Unpin" else "Pin",
                    tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = when (meta.category) {
                                CalculatorCategory.FINANCIAL -> MaterialTheme.colorScheme.tertiaryContainer
                                CalculatorCategory.HEALTH -> Color(0xFFFCE4EC) // soft pink
                                CalculatorCategory.MATH -> MaterialTheme.colorScheme.primaryContainer
                                CalculatorCategory.OTHER -> MaterialTheme.colorScheme.secondaryContainer
                            },
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCalculatorIcon(meta.iconName),
                        contentDescription = null,
                        tint = when (meta.category) {
                            CalculatorCategory.FINANCIAL -> MaterialTheme.colorScheme.onTertiaryContainer
                            CalculatorCategory.HEALTH -> Color(0xFFC2185B) // bright dark pink
                            CalculatorCategory.MATH -> MaterialTheme.colorScheme.onPrimaryContainer
                            CalculatorCategory.OTHER -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = meta.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = meta.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        lineHeight = 14.sp
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun HistoryRowItem(
    entry: HistoryEntry,
    onDelete: () -> Unit,
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateString = remember(entry.timestamp) {
        try {
            val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
            sdf.format(Date(entry.timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onReload)
            .testTag("history_item_${entry.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val meta = CalculatorRegistry.list.find { it.id == entry.calculatorId }
                        Icon(
                            imageVector = getCalculatorIcon(meta?.iconName ?: "calculate"),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.calculatorName,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Delete entry",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Inputs layout
            Text(
                text = "Inputs: ${entry.inputs}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Results layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = entry.result,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Map string icon names to standard Material icons beautifully
fun getCalculatorIcon(name: String): ImageVector {
    return when (name) {
        "calculate" -> Icons.Default.Calculate
        "home" -> Icons.Default.HomeWork
        "credit_card" -> Icons.Default.CreditCard
        "directions_car" -> Icons.Default.DirectionsCar
        "trending_up" -> Icons.Default.TrendingUp
        "payment" -> Icons.Default.Payments
        "savings" -> Icons.Default.Savings
        "table_chart" -> Icons.Default.TableChart
        "insights" -> Icons.Default.Insights
        "price_change" -> Icons.Default.PriceChange
        "monetization_on" -> Icons.Default.MonetizationOn
        "account_balance" -> Icons.Default.AccountBalance
        "show_chart" -> Icons.Default.ShowChart
        "work" -> Icons.Default.Work
        "receipt" -> Icons.Default.Receipt
        "accessibility" -> Icons.Default.AccessibilityNew
        "restaurant" -> Icons.Default.Restaurant
        "fitness_center" -> Icons.Default.FitnessCenter
        "bolt" -> Icons.Default.Bolt
        "scale" -> Icons.Default.Scale
        "speed" -> Icons.Default.Speed
        "child_care" -> Icons.Default.ChildCare
        "calendar_today" -> Icons.Default.CalendarToday
        "event" -> Icons.Default.Event
        "cake" -> Icons.Default.Cake
        "date_range" -> Icons.Default.DateRange
        "schedule" -> Icons.Default.Schedule
        "hourglass_empty" -> Icons.Default.HourglassEmpty
        "school" -> Icons.Default.School
        "assignment" -> Icons.Default.Assignment
        "foundation" -> Icons.Default.Handyman
        "dns" -> Icons.Default.Dns
        "vpn_key" -> Icons.Default.VpnKey
        "compare_arrows" -> Icons.AutoMirrored.Filled.CompareArrows
        "fraction" -> Icons.Default.PlusOne
        "percent" -> Icons.Default.Percent
        "shuffle" -> Icons.Default.Shuffle
        "change_history" -> Icons.Default.ChangeHistory
        "analytics" -> Icons.Default.Analytics
        else -> Icons.Default.Calculate
    }
}

@Composable
fun FaqSection(
    title: String,
    faqs: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    faqs.forEach { (question, answer) ->
                        Column {
                            Text(
                                text = "Q: $question",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "A: $answer",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
