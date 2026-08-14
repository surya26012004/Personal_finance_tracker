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
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoPrimary
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
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            WealthBottomNavBar(
                currentTab = state.currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Natural Tones App Header Bar
            WealthAppHeader(
                currentTab = state.currentTab,
                netWorth = state.portfolioSummary.netWorth,
                todayPL = state.portfolioSummary.todayProfitLoss,
                todayPct = state.portfolioSummary.todayPercentageChange,
                currencySymbol = state.settings.currencySymbol
            )

            // Dynamic Category/Tab scroll bar
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
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, SlateBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NET WORTH",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = FinancialCalculator.formatCurrency(netWorth, currencySymbol, false),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isTodayPos) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isTodayPos) EmeraldProfit else RoseLoss,
                        modifier = Modifier.size(14.dp)
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

            // Person / Avatar Circular Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SlateSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Profile",
                    tint = SlateTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

