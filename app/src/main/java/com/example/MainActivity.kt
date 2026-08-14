package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.entity.HoldingEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.FinanceRepository
import com.example.domain.model.FinancialCalculator
import com.example.ui.components.WealthBottomNavBar
import com.example.ui.components.WealthTopScrollableNav
import com.example.ui.screens.AddTransactionDialog
import com.example.ui.screens.CashflowScreen
import com.example.ui.screens.DailyAnalyticsScreen
import com.example.ui.screens.DailyUpdateScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HoldingFormDialog
import com.example.ui.screens.HoldingsScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.LoansScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.CosmicMidnight
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.LunarCyan
import com.example.ui.theme.LunarCyanGlow
import com.example.ui.theme.LunarGold
import com.example.ui.theme.LunarIndigo
import com.example.ui.theme.MoonMuted
import com.example.ui.theme.MoonSilver
import com.example.ui.theme.MoonStarlight
import com.example.ui.theme.RoseLoss
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.WealthWiseTheme
import com.example.ui.viewmodel.WealthTab
import com.example.ui.viewmodel.WealthViewModel
import com.example.ui.viewmodel.WealthViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: WealthViewModel by viewModels {
        val db = AppDatabase.getDatabase(applicationContext)
        val repo = FinanceRepository(db.financeDao())
        WealthViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WealthWiseTheme {
                WealthWiseApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun WealthWiseApp(viewModel: WealthViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showQuickAddHolding by remember { mutableStateOf(false) }
    var showQuickAddTx by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CosmicVoid,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            WealthBottomNavBar(
                currentTab = state.currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CosmicMidnight,
                            CosmicVoid,
                            Color(0xFF0A0F24)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                // Moonlit Liquid Glass App Header Bar
                WealthAppHeader(
                    currentTab = state.currentTab,
                    netWorth = state.portfolioSummary.netWorth,
                    todayPL = state.portfolioSummary.todayProfitLoss,
                    todayPct = state.portfolioSummary.todayPercentageChange,
                    currencySymbol = state.settings.currencySymbol
                )

                // Frosted Top Category Navigation Pills
                WealthTopScrollableNav(
                    currentTab = state.currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )

                // Screen Body with animated fade transition
                AnimatedContent(
                    targetState = state.currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TabContent"
                ) { targetTab ->
                    when (targetTab) {
                        WealthTab.DASHBOARD -> {
                            DashboardScreen(
                                state = state,
                                onNavigateTab = { viewModel.selectTab(it) },
                                onTimeRangeSelected = { viewModel.selectTimeRange(it) },
                                onQuickAddHolding = { showQuickAddHolding = true },
                                onQuickAddTransaction = { showQuickAddTx = true }
                            )
                        }
                        WealthTab.PORTFOLIO -> {
                            HoldingsScreen(
                                state = state,
                                onAddHolding = { viewModel.addHolding(it) },
                                onUpdateHolding = { viewModel.updateHolding(it) },
                                onDeleteHolding = { viewModel.deleteHolding(it) }
                            )
                        }
                        WealthTab.DAILY_UPDATE -> {
                            DailyUpdateScreen(
                                state = state,
                                onInputChange = { id, input -> viewModel.updateDailyInputChange(id, input) },
                                onApplyUniformPercent = { viewModel.applyUniformPercentChange(it) },
                                onDateSelected = { viewModel.setSelectedDailyDate(it) },
                                onSaveAll = { viewModel.saveAllDailyUpdates() }
                            )
                        }
                        WealthTab.TRANSACTIONS -> {
                            TransactionsScreen(
                                state = state,
                                onAddTransaction = { viewModel.addTransaction(it) },
                                onDeleteTransaction = { viewModel.deleteTransaction(it) }
                            )
                        }
                        WealthTab.CASHFLOW -> {
                            CashflowScreen(
                                state = state,
                                onAddCashflow = { viewModel.addCashflow(it) },
                                onDeleteCashflow = { viewModel.deleteCashflow(it) }
                            )
                        }
                        WealthTab.GOALS -> {
                            GoalsScreen(
                                state = state,
                                onAddGoal = { viewModel.addGoal(it) },
                                onContributeToGoal = { goal, amt -> viewModel.contributeToGoal(goal, amt) },
                                onDeleteGoal = { viewModel.deleteGoal(it) }
                            )
                        }
                        WealthTab.ANALYTICS -> {
                            DailyAnalyticsScreen(
                                state = state,
                                onTimeRangeSelected = { viewModel.selectTimeRange(it) }
                            )
                        }
                        WealthTab.LOANS -> {
                            LoansScreen(
                                state = state,
                                onAddLoan = { viewModel.addLoan(it) },
                                onRecordEmiPayment = { loan, amt -> viewModel.recordEmiPayment(loan, amt) },
                                onDeleteLoan = { viewModel.deleteLoan(it) }
                            )
                        }
                        WealthTab.WATCHLIST -> {
                            WatchlistScreen(
                                state = state,
                                onAddWatchlist = { viewModel.addWatchlistItem(it) },
                                onDeleteWatchlist = { viewModel.deleteWatchlistItem(it) },
                                onConvertHolding = { item, qty, price -> viewModel.convertWatchlistToHolding(item, qty, price) }
                            )
                        }
                        WealthTab.JOURNAL -> {
                            JournalScreen(
                                state = state,
                                onAddJournal = { viewModel.addJournalEntry(it) },
                                onDeleteJournal = { viewModel.deleteJournalEntry(it) }
                            )
                        }
                        WealthTab.SETTINGS -> {
                            SettingsScreen(
                                state = state,
                                onUpdateCurrency = { c, s -> viewModel.updateCurrency(c, s) },
                                onResetData = { viewModel.loadSampleData() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Quick Add Holding Dialog
    if (showQuickAddHolding) {
        HoldingFormDialog(
            holding = null,
            currencySymbol = state.settings.currencySymbol,
            onDismiss = { showQuickAddHolding = false },
            onSave = {
                viewModel.addHolding(it)
                showQuickAddHolding = false
            }
        )
    }

    // Quick Add Transaction Dialog
    if (showQuickAddTx) {
        AddTransactionDialog(
            holdings = state.holdings,
            currencySymbol = state.settings.currencySymbol,
            onDismiss = { showQuickAddTx = false },
            onSave = {
                viewModel.addTransaction(it)
                showQuickAddTx = false
            }
        )
    }
}

@Composable
fun WealthAppHeader(
    currentTab: WealthTab,
    netWorth: Double,
    todayPL: Double,
    todayPct: Double,
    currencySymbol: String
) {
    val isTodayPos = todayPL >= 0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(24.dp)),
        color = Color(0x33121A3A), // Liquid frosted surface
        shadowElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x4DFFFFFF), // Upper rim specular highlight
                    Color(0x1AFFFFFF),
                    Color(0x1038BDF8)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NightlightRound,
                        contentDescription = null,
                        tint = LunarCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TOTAL VALUATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MoonSilver,
                        letterSpacing = 1.2.sp,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = FinancialCalculator.formatCurrency(netWorth, currencySymbol, false),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MoonStarlight,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isTodayPos) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isTodayPos) EmeraldProfit else RoseLoss,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val sign = if (todayPL > 0) "+" else ""
                    Text(
                        text = "$sign${FinancialCalculator.formatCurrency(todayPL, currencySymbol, false)} (${FinancialCalculator.formatPercent(todayPct)}) Today",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isTodayPos) EmeraldProfit else RoseLoss,
                        fontSize = 11.sp
                    )
                }
            }

            // Glowing Liquid Glass Moon Icon Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                LunarCyan.copy(alpha = 0.35f),
                                LunarIndigo.copy(alpha = 0.2f),
                                Color(0x10FFFFFF)
                            )
                        )
                    )
                    .drawBehind {
                        drawCircle(
                            color = Color(0x40FFFFFF),
                            radius = size.minDimension / 2,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Moonlit Aura",
                    tint = LunarCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


