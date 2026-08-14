package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CashflowEntity
import com.example.data.local.entity.DailyPortfolioSnapshotEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HoldingEntity
import com.example.data.local.entity.JournalEntity
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WatchlistEntity
import com.example.data.repository.FinanceRepository
import com.example.domain.model.AllocationSlice
import com.example.domain.model.FinancialCalculator
import com.example.domain.model.PortfolioSummary
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class WealthTab(val title: String, val shortTitle: String) {
    DASHBOARD("Dashboard", "Home"),
    PORTFOLIO("Stocks & Funds", "Portfolio"),
    DAILY_UPDATE("Daily Update", "Daily %"),
    TRANSACTIONS("Transactions", "Activity"),
    CASHFLOW("Income & Expense", "Cashflow"),
    GOALS("Financial Goals", "Goals"),
    ANALYTICS("Analytics", "Analytics"),
    LOANS("Loans & Debts", "Loans"),
    WATCHLIST("Watchlist", "Watchlist"),
    JOURNAL("Journal", "Journal"),
    SETTINGS("Settings", "Settings")
}

data class WealthUiState(
    val currentTab: WealthTab = WealthTab.DASHBOARD,
    val holdings: List<HoldingEntity> = emptyList(),
    val snapshots: List<DailyPortfolioSnapshotEntity> = emptyList(),
    val latestSnapshot: DailyPortfolioSnapshotEntity? = null,
    val transactions: List<TransactionEntity> = emptyList(),
    val cashflows: List<CashflowEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val loans: List<LoanEntity> = emptyList(),
    val watchlist: List<WatchlistEntity> = emptyList(),
    val journalEntries: List<JournalEntity> = emptyList(),
    val settings: AppSettingsEntity = AppSettingsEntity(),
    val portfolioSummary: PortfolioSummary = PortfolioSummary(),
    val assetAllocation: List<AllocationSlice> = emptyList(),
    val sectorAllocation: List<AllocationSlice> = emptyList(),
    val dailyUpdateInputs: Map<Long, String> = emptyMap(), // holdingId -> input string (e.g. "+1.5", "-0.8")
    val selectedDailyDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val isSavingDailyUpdate: Boolean = false,
    val selectedTimeRange: String = "1M" // 1W, 1M, 3M, 6M, 1Y, ALL
)

class WealthViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val _currentTab = MutableStateFlow(WealthTab.DASHBOARD)
    private val _dailyInputs = MutableStateFlow<Map<Long, String>>(emptyMap())
    private val _selectedDailyDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    private val _selectedTimeRange = MutableStateFlow("1M")
    private val _isSavingDaily = MutableStateFlow(false)

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val uiState: StateFlow<WealthUiState> = combine(
        _currentTab,
        repository.allHoldings,
        repository.allSnapshots,
        repository.latestSnapshot,
        repository.allTransactions,
        repository.allCashflows,
        repository.allGoals,
        repository.allLoans,
        repository.allWatchlist,
        repository.allJournalEntries,
        repository.settings,
        _dailyInputs,
        _selectedDailyDate,
        _selectedTimeRange,
        _isSavingDaily
    ) { args ->
        val currentTab = args[0] as WealthTab
        @Suppress("UNCHECKED_CAST") val holdings = args[1] as List<HoldingEntity>
        @Suppress("UNCHECKED_CAST") val snapshots = args[2] as List<DailyPortfolioSnapshotEntity>
        val latestSnapshot = args[3] as DailyPortfolioSnapshotEntity?
        @Suppress("UNCHECKED_CAST") val transactions = args[4] as List<TransactionEntity>
        @Suppress("UNCHECKED_CAST") val cashflows = args[5] as List<CashflowEntity>
        @Suppress("UNCHECKED_CAST") val goals = args[6] as List<GoalEntity>
        @Suppress("UNCHECKED_CAST") val loans = args[7] as List<LoanEntity>
        @Suppress("UNCHECKED_CAST") val watchlist = args[8] as List<WatchlistEntity>
        @Suppress("UNCHECKED_CAST") val journalEntries = args[9] as List<JournalEntity>
        val settings = (args[10] as AppSettingsEntity?) ?: AppSettingsEntity()
        @Suppress("UNCHECKED_CAST") val dailyInputs = args[11] as Map<Long, String>
        val selectedDailyDate = args[12] as String
        val selectedTimeRange = args[13] as String
        val isSavingDaily = args[14] as Boolean

        val summary = FinancialCalculator.computePortfolioSummary(
            holdings = holdings,
            latestSnapshot = latestSnapshot,
            cashflows = cashflows,
            loans = loans
        )

        val assetAllocation = FinancialCalculator.calculateAssetAllocation(holdings)
        val sectorAllocation = FinancialCalculator.calculateSectorAllocation(holdings)

        WealthUiState(
            currentTab = currentTab,
            holdings = holdings,
            snapshots = snapshots,
            latestSnapshot = latestSnapshot,
            transactions = transactions,
            cashflows = cashflows,
            goals = goals,
            loans = loans,
            watchlist = watchlist,
            journalEntries = journalEntries,
            settings = settings,
            portfolioSummary = summary,
            assetAllocation = assetAllocation,
            sectorAllocation = sectorAllocation,
            dailyUpdateInputs = dailyInputs,
            selectedDailyDate = selectedDailyDate,
            selectedTimeRange = selectedTimeRange,
            isSavingDailyUpdate = isSavingDaily
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WealthUiState()
    )

    fun selectTab(tab: WealthTab) {
        _currentTab.value = tab
    }

    fun selectTimeRange(range: String) {
        _selectedTimeRange.value = range
    }

    // --- Daily Update Workflow ---
    fun updateDailyInputChange(holdingId: Long, input: String) {
        val current = _dailyInputs.value.toMutableMap()
        current[holdingId] = input
        _dailyInputs.value = current
    }

    fun applyUniformPercentChange(percent: Double) {
        val holdings = uiState.value.holdings
        val current = mutableMapOf<Long, String>()
        for (h in holdings) {
            current[h.id] = String.format(Locale.US, "%.2f", percent)
        }
        _dailyInputs.value = current
    }

    fun setSelectedDailyDate(dateStr: String) {
        _selectedDailyDate.value = dateStr
    }

    fun saveAllDailyUpdates() {
        val currentState = uiState.value
        val holdings = currentState.holdings
        if (holdings.isEmpty()) {
            viewModelScope.launch { _snackbarMessage.emit("No holdings found to update.") }
            return
        }

        viewModelScope.launch {
            _isSavingDaily.value = true
            try {
                val percentMap = mutableMapOf<Long, Double>()
                for (h in holdings) {
                    val inputStr = _dailyInputs.value[h.id]
                    val parsedPct = inputStr?.toDoubleOrNull() ?: 0.0
                    percentMap[h.id] = parsedPct
                }

                repository.saveDailyUpdate(
                    dateString = _selectedDailyDate.value,
                    percentChanges = percentMap,
                    existingHoldings = holdings
                )

                // Clear input map or keep as updated
                _dailyInputs.value = emptyMap()
                _snackbarMessage.emit("Daily Portfolio Update Saved Successfully!")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error saving daily update: ${e.message}")
            } finally {
                _isSavingDaily.value = false
            }
        }
    }

    // --- Holdings Actions ---
    fun addHolding(holding: HoldingEntity) {
        viewModelScope.launch {
            try {
                repository.insertHolding(holding)
                _snackbarMessage.emit("Added ${holding.name}")
            } catch (e: Exception) {
                _snackbarMessage.emit("Failed to add holding: ${e.message}")
            }
        }
    }

    fun updateHolding(holding: HoldingEntity) {
        viewModelScope.launch {
            try {
                repository.updateHolding(holding)
                _snackbarMessage.emit("Updated ${holding.name}")
            } catch (e: Exception) {
                _snackbarMessage.emit("Failed to update holding: ${e.message}")
            }
        }
    }

    fun deleteHolding(holding: HoldingEntity) {
        viewModelScope.launch {
            try {
                repository.deleteHolding(holding)
                _snackbarMessage.emit("Deleted ${holding.name}")
            } catch (e: Exception) {
                _snackbarMessage.emit("Failed to delete holding: ${e.message}")
            }
        }
    }

    // --- Transactions Actions ---
    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                repository.addTransaction(transaction)
                _snackbarMessage.emit("Transaction recorded: ${transaction.assetName}")
            } catch (e: Exception) {
                _snackbarMessage.emit("Failed to add transaction: ${e.message}")
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                _snackbarMessage.emit("Transaction deleted")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error deleting transaction: ${e.message}")
            }
        }
    }

    // --- Cashflows Actions ---
    fun addCashflow(cashflow: CashflowEntity) {
        viewModelScope.launch {
            try {
                repository.insertCashflow(cashflow)
                _snackbarMessage.emit("Recorded ${cashflow.category} (${FinancialCalculator.formatCurrency(cashflow.amount, uiState.value.settings.currencySymbol)})")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error saving cashflow: ${e.message}")
            }
        }
    }

    fun deleteCashflow(cashflow: CashflowEntity) {
        viewModelScope.launch {
            try {
                repository.deleteCashflow(cashflow)
                _snackbarMessage.emit("Cashflow deleted")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error deleting cashflow: ${e.message}")
            }
        }
    }

    // --- Goals Actions ---
    fun addGoal(goal: GoalEntity) {
        viewModelScope.launch {
            try {
                repository.insertGoal(goal)
                _snackbarMessage.emit("Goal created: ${goal.name}")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error creating goal: ${e.message}")
            }
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            try {
                repository.updateGoal(goal)
                _snackbarMessage.emit("Goal updated")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error updating goal: ${e.message}")
            }
        }
    }

    fun contributeToGoal(goal: GoalEntity, addAmount: Double) {
        viewModelScope.launch {
            try {
                val updated = goal.copy(currentAmount = goal.currentAmount + addAmount)
                repository.updateGoal(updated)
                _snackbarMessage.emit("Added ${FinancialCalculator.formatCurrency(addAmount, uiState.value.settings.currencySymbol)} to ${goal.name}")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            try {
                repository.deleteGoal(goal)
                _snackbarMessage.emit("Goal removed")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error: ${e.message}")
            }
        }
    }

    // --- Loans Actions ---
    fun addLoan(loan: LoanEntity) {
        viewModelScope.launch {
            try {
                repository.insertLoan(loan)
                _snackbarMessage.emit("Loan tracked: ${loan.name}")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun updateLoan(loan: LoanEntity) {
        viewModelScope.launch {
            try {
                repository.updateLoan(loan)
                _snackbarMessage.emit("Loan details updated")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun recordEmiPayment(loan: LoanEntity, emiPaid: Double) {
        viewModelScope.launch {
            try {
                val newBal = (loan.outstandingBalance - emiPaid).coerceAtLeast(0.0)
                val newTenure = (loan.tenureMonthsRemaining - 1).coerceAtLeast(0)
                repository.updateLoan(loan.copy(outstandingBalance = newBal, tenureMonthsRemaining = newTenure))
                repository.insertCashflow(
                    CashflowEntity(
                        type = com.example.data.local.entity.CashflowType.EXPENSE,
                        category = "Loan EMI",
                        amount = emiPaid,
                        notes = "EMI paid for ${loan.name}"
                    )
                )
                _snackbarMessage.emit("EMI payment recorded! Outstanding reduced to ${FinancialCalculator.formatCurrency(newBal, uiState.value.settings.currencySymbol)}")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun deleteLoan(loan: LoanEntity) {
        viewModelScope.launch {
            try {
                repository.deleteLoan(loan)
                _snackbarMessage.emit("Loan deleted")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error: ${e.message}")
            }
        }
    }

    // --- Watchlist Actions ---
    fun addWatchlistItem(item: WatchlistEntity) {
        viewModelScope.launch {
            try {
                repository.insertWatchlist(item)
                _snackbarMessage.emit("Added ${item.name} to Watchlist")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun deleteWatchlistItem(item: WatchlistEntity) {
        viewModelScope.launch {
            try {
                repository.deleteWatchlist(item)
                _snackbarMessage.emit("Removed from Watchlist")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun convertWatchlistToHolding(item: WatchlistEntity, quantity: Double, buyPrice: Double) {
        viewModelScope.launch {
            try {
                val newHolding = HoldingEntity(
                    name = item.name,
                    tickerOrCode = item.ticker,
                    category = item.category,
                    sector = item.sector,
                    quantity = quantity,
                    buyPrice = buyPrice,
                    currentPrice = item.currentPrice,
                    notes = item.investmentThesis
                )
                repository.insertHolding(newHolding)
                repository.addTransaction(
                    TransactionEntity(
                        assetName = item.name,
                        type = com.example.data.local.entity.TransactionType.BUY,
                        units = quantity,
                        pricePerUnit = buyPrice,
                        amount = quantity * buyPrice,
                        notes = "Converted from watchlist"
                    ),
                    updateHolding = false
                )
                repository.deleteWatchlist(item)
                _snackbarMessage.emit("Converted ${item.name} into Portfolio Holding!")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error: ${e.message}")
            }
        }
    }

    // --- Journal Actions ---
    fun addJournalEntry(entry: JournalEntity) {
        viewModelScope.launch {
            try {
                repository.insertJournalEntry(entry)
                _snackbarMessage.emit("Journal entry saved")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun deleteJournalEntry(entry: JournalEntity) {
        viewModelScope.launch {
            try {
                repository.deleteJournalEntry(entry)
                _snackbarMessage.emit("Journal entry deleted")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error: ${e.message}")
            }
        }
    }

    // --- Settings & Data Actions ---
    fun updateCurrency(code: String, symbol: String) {
        viewModelScope.launch {
            val cur = uiState.value.settings
            repository.updateSettings(cur.copy(currencyCode = code, currencySymbol = symbol))
            _snackbarMessage.emit("Currency updated to $code ($symbol)")
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
            _snackbarMessage.emit("Data reset successfully")
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            repository.resetAllData()
            repository.seedInitialDataIfEmpty()
            _snackbarMessage.emit("Sample Wealth Portfolio loaded!")
        }
    }

    suspend fun getExportCsv(): String {
        return repository.exportToCsvString()
    }
}
