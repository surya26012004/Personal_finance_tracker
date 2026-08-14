package com.example.data.repository

import com.example.data.local.dao.FinanceDao
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.AssetCategory
import com.example.data.local.entity.CashflowEntity
import com.example.data.local.entity.CashflowType
import com.example.data.local.entity.Conviction
import com.example.data.local.entity.DailyHoldingRecordEntity
import com.example.data.local.entity.DailyPortfolioSnapshotEntity
import com.example.data.local.entity.GoalCategory
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HoldingEntity
import com.example.data.local.entity.JournalEntity
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.LoanType
import com.example.data.local.entity.Sentiment
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import com.example.data.local.entity.WatchlistEntity
import com.example.domain.model.FinancialCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FinanceRepository(private val financeDao: FinanceDao) {

    val allHoldings: Flow<List<HoldingEntity>> = financeDao.getAllHoldings()
    val allSnapshots: Flow<List<DailyPortfolioSnapshotEntity>> = financeDao.getAllPortfolioSnapshots()
    val latestSnapshot: Flow<DailyPortfolioSnapshotEntity?> = financeDao.getLatestPortfolioSnapshot()
    val allTransactions: Flow<List<TransactionEntity>> = financeDao.getAllTransactions()
    val allCashflows: Flow<List<CashflowEntity>> = financeDao.getAllCashflows()
    val allGoals: Flow<List<GoalEntity>> = financeDao.getAllGoals()
    val allLoans: Flow<List<LoanEntity>> = financeDao.getAllLoans()
    val allWatchlist: Flow<List<WatchlistEntity>> = financeDao.getAllWatchlist()
    val allJournalEntries: Flow<List<JournalEntity>> = financeDao.getAllJournalEntries()
    val settings: Flow<AppSettingsEntity?> = financeDao.getSettings()

    // --- Daily Update Execution ---
    suspend fun saveDailyUpdate(
        dateString: String,
        percentChanges: Map<Long, Double>, // holdingId -> percentChange (e.g. 1.5 for +1.5%)
        existingHoldings: List<HoldingEntity>
    ) = withContext(Dispatchers.IO) {
        val updatedHoldings = mutableListOf<HoldingEntity>()
        val dailyRecords = mutableListOf<DailyHoldingRecordEntity>()
        var totalInvested = 0.0
        var totalNewValue = 0.0
        var totalDailyPL = 0.0

        for (holding in existingHoldings) {
            val pct = percentChanges[holding.id] ?: 0.0
            val previousPrice = holding.currentPrice
            val newPrice = (previousPrice * (1.0 + (pct / 100.0))).coerceAtLeast(0.01)
            val updatedHolding = holding.copy(
                currentPrice = newPrice,
                lastUpdatedDate = System.currentTimeMillis()
            )
            updatedHoldings.add(updatedHolding)

            val dailyPL = (newPrice - previousPrice) * holding.quantity
            val itemTotalValue = holding.quantity * newPrice

            totalInvested += updatedHolding.investedAmount
            totalNewValue += itemTotalValue
            totalDailyPL += dailyPL

            dailyRecords.add(
                DailyHoldingRecordEntity(
                    holdingId = holding.id,
                    holdingName = holding.name,
                    dateString = dateString,
                    timestamp = System.currentTimeMillis(),
                    closingPrice = newPrice,
                    percentChange = pct,
                    dailyProfitLoss = dailyPL,
                    totalValue = itemTotalValue
                )
            )
        }

        // Save updated holdings prices
        financeDao.insertHoldings(updatedHoldings)

        // Save daily holding records (History is never overwritten for past dates!)
        financeDao.insertDailyHoldingRecords(dailyRecords)

        // Calculate overall portfolio snapshot
        val totalProfitLoss = totalNewValue - totalInvested
        val totalReturnPercent = if (totalInvested > 0) (totalProfitLoss / totalInvested) * 100.0 else 0.0
        val previousPortfolioValue = totalNewValue - totalDailyPL
        val dailyPercentChange = if (previousPortfolioValue > 0) (totalDailyPL / previousPortfolioValue) * 100.0 else 0.0

        val snapshot = DailyPortfolioSnapshotEntity(
            dateString = dateString,
            timestamp = System.currentTimeMillis(),
            totalInvested = totalInvested,
            totalCurrentValue = totalNewValue,
            dailyProfitLoss = totalDailyPL,
            dailyPercentChange = dailyPercentChange,
            totalProfitLoss = totalProfitLoss,
            totalReturnPercent = totalReturnPercent,
            xirrEstimate = 0.0
        )
        financeDao.insertPortfolioSnapshot(snapshot)
    }

    // --- Holdings Operations ---
    suspend fun insertHolding(holding: HoldingEntity): Long = withContext(Dispatchers.IO) {
        financeDao.insertHolding(holding)
    }

    suspend fun updateHolding(holding: HoldingEntity) = withContext(Dispatchers.IO) {
        financeDao.updateHolding(holding)
    }

    suspend fun deleteHolding(holding: HoldingEntity) = withContext(Dispatchers.IO) {
        financeDao.deleteHolding(holding)
    }

    // --- Transactions Operations ---
    suspend fun addTransaction(transaction: TransactionEntity, updateHolding: Boolean = true) = withContext(Dispatchers.IO) {
        financeDao.insertTransaction(transaction)

        if (updateHolding && transaction.type == TransactionType.BUY) {
            val holdings = financeDao.getAllHoldings().firstOrNull() ?: emptyList()
            val existing = holdings.find { it.name.equals(transaction.assetName, ignoreCase = true) }
            if (existing != null) {
                val newQty = existing.quantity + transaction.units
                val newInvested = existing.investedAmount + transaction.amount
                val newAvgPrice = if (newQty > 0) newInvested / newQty else existing.buyPrice
                financeDao.updateHolding(
                    existing.copy(
                        quantity = newQty,
                        buyPrice = newAvgPrice,
                        currentPrice = if (transaction.pricePerUnit > 0) transaction.pricePerUnit else existing.currentPrice,
                        lastUpdatedDate = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        financeDao.deleteTransaction(transaction)
    }

    // --- Cashflows Operations ---
    suspend fun insertCashflow(cashflow: CashflowEntity) = withContext(Dispatchers.IO) {
        financeDao.insertCashflow(cashflow)
    }

    suspend fun deleteCashflow(cashflow: CashflowEntity) = withContext(Dispatchers.IO) {
        financeDao.deleteCashflow(cashflow)
    }

    // --- Goals Operations ---
    suspend fun insertGoal(goal: GoalEntity) = withContext(Dispatchers.IO) {
        financeDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: GoalEntity) = withContext(Dispatchers.IO) {
        financeDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: GoalEntity) = withContext(Dispatchers.IO) {
        financeDao.deleteGoal(goal)
    }

    // --- Loans Operations ---
    suspend fun insertLoan(loan: LoanEntity) = withContext(Dispatchers.IO) {
        financeDao.insertLoan(loan)
    }

    suspend fun updateLoan(loan: LoanEntity) = withContext(Dispatchers.IO) {
        financeDao.updateLoan(loan)
    }

    suspend fun deleteLoan(loan: LoanEntity) = withContext(Dispatchers.IO) {
        financeDao.deleteLoan(loan)
    }

    // --- Watchlist Operations ---
    suspend fun insertWatchlist(item: WatchlistEntity) = withContext(Dispatchers.IO) {
        financeDao.insertWatchlist(item)
    }

    suspend fun deleteWatchlist(item: WatchlistEntity) = withContext(Dispatchers.IO) {
        financeDao.deleteWatchlist(item)
    }

    // --- Journal Operations ---
    suspend fun insertJournalEntry(entry: JournalEntity) = withContext(Dispatchers.IO) {
        financeDao.insertJournalEntry(entry)
    }

    suspend fun deleteJournalEntry(entry: JournalEntity) = withContext(Dispatchers.IO) {
        financeDao.deleteJournalEntry(entry)
    }

    // --- Settings Operations ---
    suspend fun updateSettings(settings: AppSettingsEntity) = withContext(Dispatchers.IO) {
        financeDao.saveSettings(settings)
    }

    // --- Sample Data Seeding for Rich First-Time Experience ---
    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentSettings = financeDao.getSettings().firstOrNull()
        if (currentSettings != null && currentSettings.isSampleDataLoaded) {
            return@withContext
        }

        val existingHoldings = financeDao.getAllHoldings().firstOrNull()
        if (!existingHoldings.isNullOrEmpty()) {
            return@withContext
        }

        // 1. Initial Holdings
        val sampleHoldings = listOf(
            HoldingEntity(
                name = "Reliance Industries Ltd",
                tickerOrCode = "RELIANCE",
                category = AssetCategory.STOCK,
                sector = "Energy & Conglomerate",
                quantity = 75.0,
                buyPrice = 2450.0,
                currentPrice = 2980.0,
                purchaseDate = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000),
                notes = "Core bluechip energy and retail titan"
            ),
            HoldingEntity(
                name = "Tata Consultancy Services",
                tickerOrCode = "TCS",
                category = AssetCategory.STOCK,
                sector = "Information Technology",
                quantity = 50.0,
                buyPrice = 3500.0,
                currentPrice = 4120.0,
                purchaseDate = System.currentTimeMillis() - (240L * 24 * 60 * 60 * 1000),
                notes = "Top tier IT service giant, regular dividend"
            ),
            HoldingEntity(
                name = "HDFC Bank Ltd",
                tickerOrCode = "HDFCBANK",
                category = AssetCategory.STOCK,
                sector = "Banking & Finance",
                quantity = 120.0,
                buyPrice = 1480.0,
                currentPrice = 1660.0,
                purchaseDate = System.currentTimeMillis() - (120L * 24 * 60 * 60 * 1000),
                notes = "Private banking leader, strong credit book"
            ),
            HoldingEntity(
                name = "Tata Motors Ltd",
                tickerOrCode = "TATAMOTORS",
                category = AssetCategory.STOCK,
                sector = "Automotive & EV",
                quantity = 150.0,
                buyPrice = 640.0,
                currentPrice = 1045.0,
                purchaseDate = System.currentTimeMillis() - (300L * 24 * 60 * 60 * 1000),
                notes = "EV market leader with JLR turnaround"
            ),
            HoldingEntity(
                name = "Parag Parikh Flexi Cap Fund",
                tickerOrCode = "PPFAS-DIR-G",
                category = AssetCategory.MUTUAL_FUND,
                sector = "Diversified Equity",
                quantity = 3500.0,
                buyPrice = 52.0,
                currentPrice = 74.5,
                purchaseDate = System.currentTimeMillis() - (360L * 24 * 60 * 60 * 1000),
                notes = "Monthly SIP core flexicap fund"
            ),
            HoldingEntity(
                name = "UTI Nifty 50 Index Fund",
                tickerOrCode = "UTI-NIFTY-DIR",
                category = AssetCategory.MUTUAL_FUND,
                sector = "Large Cap Index",
                quantity = 2800.0,
                buyPrice = 115.0,
                currentPrice = 148.0,
                purchaseDate = System.currentTimeMillis() - (400L * 24 * 60 * 60 * 1000),
                notes = "Passive low tracking error index builder"
            ),
            HoldingEntity(
                name = "Sovereign Gold Bond (SGB)",
                tickerOrCode = "SGB-GOLD",
                category = AssetCategory.GOLD,
                sector = "Precious Metals",
                quantity = 30.0,
                buyPrice = 5400.0,
                currentPrice = 7250.0,
                purchaseDate = System.currentTimeMillis() - (500L * 24 * 60 * 60 * 1000),
                notes = "2.5% semi-annual interest + gold appreciation"
            ),
            HoldingEntity(
                name = "HDFC Triple Star Fixed Deposit",
                tickerOrCode = "FD-HDFC",
                category = AssetCategory.FIXED_DEPOSIT,
                sector = "Debt & Cash",
                quantity = 1.0,
                buyPrice = 300000.0,
                currentPrice = 322500.0,
                purchaseDate = System.currentTimeMillis() - (200L * 24 * 60 * 60 * 1000),
                notes = "7.5% p.a. fixed liquidity reserve"
            )
        )
        financeDao.insertHoldings(sampleHoldings)

        // 2. Historical Daily Portfolio Snapshots (14 days trend)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val snapshots = mutableListOf<DailyPortfolioSnapshotEntity>()
        
        var baseValue = 1850000.0
        val baseInvested = 1520000.0
        val fluctuations = listOf(
            0.0, 0.45, -0.32, 0.88, 0.15, -0.62, 0.74, 1.10, -0.25, 0.55, -0.40, 0.95, 0.30, 0.65
        )

        for (i in 13 downTo 0) {
            val dateCal = Calendar.getInstance()
            dateCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = dateFormat.format(dateCal.time)
            
            val pct = fluctuations[13 - i]
            val prevVal = baseValue
            baseValue = baseValue * (1.0 + (pct / 100.0))
            val dailyPL = baseValue - prevVal
            val totalPL = baseValue - baseInvested

            snapshots.add(
                DailyPortfolioSnapshotEntity(
                    dateString = dateStr,
                    timestamp = dateCal.timeInMillis,
                    totalInvested = baseInvested,
                    totalCurrentValue = baseValue,
                    dailyProfitLoss = dailyPL,
                    dailyPercentChange = pct,
                    totalProfitLoss = totalPL,
                    totalReturnPercent = (totalPL / baseInvested) * 100.0,
                    xirrEstimate = 18.4
                )
            )
        }
        financeDao.insertPortfolioSnapshots(snapshots)

        // 3. Transactions
        val sampleTransactions = listOf(
            TransactionEntity(
                assetName = "Parag Parikh Flexi Cap Fund",
                type = TransactionType.SIP,
                units = 250.0,
                pricePerUnit = 72.0,
                amount = 18000.0,
                date = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000),
                notes = "Auto SIP installment executed"
            ),
            TransactionEntity(
                assetName = "Tata Consultancy Services",
                type = TransactionType.DIVIDEND,
                amount = 4500.0,
                date = System.currentTimeMillis() - (12L * 24 * 60 * 60 * 1000),
                notes = "Interim dividend ₹90/share credited"
            ),
            TransactionEntity(
                assetName = "Reliance Industries Ltd",
                type = TransactionType.BUY,
                units = 25.0,
                pricePerUnit = 2850.0,
                amount = 71250.0,
                date = System.currentTimeMillis() - (25L * 24 * 60 * 60 * 1000),
                notes = "Dip accumulation"
            ),
            TransactionEntity(
                assetName = "UTI Nifty 50 Index Fund",
                type = TransactionType.SIP,
                units = 150.0,
                pricePerUnit = 145.0,
                amount = 21750.0,
                date = System.currentTimeMillis() - (35L * 24 * 60 * 60 * 1000),
                notes = "Monthly index SIP"
            )
        )
        financeDao.insertTransactions(sampleTransactions)

        // 4. Income & Expenses Cashflows
        val sampleCashflows = listOf(
            CashflowEntity(
                type = CashflowType.INCOME,
                category = "Monthly Salary",
                amount = 185000.0,
                date = System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000),
                notes = "Primary software tech salary credit",
                isRecurring = true
            ),
            CashflowEntity(
                type = CashflowType.INCOME,
                category = "Dividends & Payouts",
                amount = 4500.0,
                date = System.currentTimeMillis() - (12L * 24 * 60 * 60 * 1000),
                notes = "TCS dividend",
                isRecurring = false
            ),
            CashflowEntity(
                type = CashflowType.INCOME,
                category = "Interest Income",
                amount = 2100.0,
                date = System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000),
                notes = "Savings bank & FD interest"
            ),
            CashflowEntity(
                type = CashflowType.EXPENSE,
                category = "Home EMI & Housing",
                amount = 42000.0,
                date = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000),
                notes = "Home loan EMI auto debit",
                isRecurring = true
            ),
            CashflowEntity(
                type = CashflowType.EXPENSE,
                category = "Groceries & Food",
                amount = 18500.0,
                date = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000),
                notes = "Monthly groceries and dining"
            ),
            CashflowEntity(
                type = CashflowType.EXPENSE,
                category = "Utilities & Bills",
                amount = 6500.0,
                date = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000),
                notes = "Electricity, Wi-Fi & Maintenance"
            ),
            CashflowEntity(
                type = CashflowType.EXPENSE,
                category = "Transport & Fuel",
                amount = 7200.0,
                date = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000),
                notes = "Car fuel & tolls"
            )
        )
        financeDao.insertCashflows(sampleCashflows)

        // 5. Goals
        val sampleGoals = listOf(
            GoalEntity(
                name = "Financial Independence / Early Retirement",
                category = GoalCategory.RETIREMENT,
                targetAmount = 25000000.0, // 2.5 Crore
                currentAmount = 2245000.0,
                targetDate = System.currentTimeMillis() + (10L * 365 * 24 * 60 * 60 * 1000),
                notes = "Target 30x annual expenses in equity + debt"
            ),
            GoalEntity(
                name = "Emergency Safety Net (12 Months)",
                category = GoalCategory.EMERGENCY_FUND,
                targetAmount = 1000000.0,
                currentAmount = 850000.0,
                targetDate = System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000),
                notes = "Liquid funds and FD buffer"
            ),
            GoalEntity(
                name = "New Electric Luxury SUV",
                category = GoalCategory.VEHICLE,
                targetAmount = 3000000.0,
                currentAmount = 1450000.0,
                targetDate = System.currentTimeMillis() + (400L * 24 * 60 * 60 * 1000),
                notes = "EV upgrade fund"
            )
        )
        financeDao.insertGoals(sampleGoals)

        // 6. Loans & Liabilities
        val sampleLoans = listOf(
            LoanEntity(
                name = "HDFC Home Loan - Luxury Apartment",
                type = LoanType.HOME_LOAN,
                lender = "HDFC Bank",
                principalAmount = 4500000.0,
                outstandingBalance = 3680000.0,
                interestRatePercent = 8.45,
                monthlyEmi = 42000.0,
                tenureMonthsRemaining = 144,
                notes = "Tax benefits under 80C and 24b"
            )
        )
        financeDao.insertLoans(sampleLoans)

        // 7. Watchlist
        val sampleWatchlist = listOf(
            WatchlistEntity(
                name = "Larsen & Toubro Ltd",
                ticker = "LT",
                category = AssetCategory.STOCK,
                sector = "Infrastructure & Defense",
                currentPrice = 3720.0,
                targetEntryPrice = 3450.0,
                week52High = 3900.0,
                week52Low = 2650.0,
                investmentThesis = "Massive order book and infrastructure capex boom"
            ),
            WatchlistEntity(
                name = "Titan Company Ltd",
                ticker = "TITAN",
                category = AssetCategory.STOCK,
                sector = "Consumer & Jewellery",
                currentPrice = 3480.0,
                targetEntryPrice = 3200.0,
                week52High = 3880.0,
                week52Low = 2800.0,
                investmentThesis = "Tanishq wedding season growth and consumer brand loyalty"
            ),
            WatchlistEntity(
                name = "Mirae Asset Large Cap Fund",
                ticker = "MIRAE-LC",
                category = AssetCategory.MUTUAL_FUND,
                sector = "Large Cap",
                currentPrice = 112.4,
                targetEntryPrice = 105.0,
                investmentThesis = "Considering as secondary large-cap mutual fund allocation"
            )
        )
        financeDao.insertWatchlistItems(sampleWatchlist)

        // 8. Journal
        val sampleJournal = listOf(
            JournalEntity(
                title = "Quarterly Portfolio Strategy & Rebalancing",
                content = "Evaluating equity vs debt mix. Equity exposure currently at 75%. Maintained strong conviction in Tata Motors and PPFAS Flexi Cap. Increasing allocation to index on market pullbacks.",
                tags = "#Strategy, #Rebalancing, #Q3",
                sentiment = Sentiment.BULLISH,
                conviction = Conviction.VERY_HIGH,
                relatedAsset = "Overall Portfolio"
            ),
            JournalEntity(
                title = "Decision: Long Term Hold on TCS Dividend Compounding",
                content = "IT sector facing short term headwinds, but TCS balance sheet quality and cash flow generation remain pristine. Reinvesting all interim dividend payouts.",
                tags = "#TCS, #Dividends, #Value",
                sentiment = Sentiment.BULLISH,
                conviction = Conviction.HIGH,
                relatedAsset = "TCS"
            )
        )
        financeDao.insertJournalEntries(sampleJournal)

        // 9. App Settings
        financeDao.saveSettings(
            AppSettingsEntity(
                id = 1,
                currencyCode = "INR",
                currencySymbol = "₹",
                isDarkMode = null,
                isSampleDataLoaded = true,
                userDisplayName = "Surya"
            )
        )
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        financeDao.clearHoldings()
        financeDao.clearDailyHoldingRecords()
        financeDao.clearDailyPortfolioSnapshots()
        financeDao.clearTransactions()
        financeDao.clearCashflows()
        financeDao.clearGoals()
        financeDao.clearLoans()
        financeDao.clearWatchlist()
        financeDao.clearJournal()
        financeDao.saveSettings(
            AppSettingsEntity(
                id = 1,
                currencyCode = "INR",
                currencySymbol = "₹",
                isDarkMode = null,
                isSampleDataLoaded = false
            )
        )
    }

    suspend fun exportToCsvString(): String = withContext(Dispatchers.IO) {
        val holdings = financeDao.getAllHoldings().firstOrNull() ?: emptyList()
        val txs = financeDao.getAllTransactions().firstOrNull() ?: emptyList()
        val sb = StringBuilder()
        sb.append("=== WEALTHWISE PORTFOLIO EXPORT ===\n\n")
        sb.append("--- HOLDINGS ---\n")
        sb.append("ID,Name,Ticker,Category,Sector,Quantity,BuyPrice,CurrentPrice,Invested,CurrentValue,ReturnPct\n")
        for (h in holdings) {
            sb.append("${h.id},\"${h.name}\",\"${h.tickerOrCode}\",${h.category.name},\"${h.sector}\",${h.quantity},${h.buyPrice},${h.currentPrice},${h.investedAmount},${h.currentValue},${String.format(Locale.US, "%.2f", h.returnPercentage)}%\n")
        }
        sb.append("\n--- TRANSACTIONS ---\n")
        sb.append("ID,Asset,Type,Units,PricePerUnit,Amount,Date,Notes\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (t in txs) {
            sb.append("${t.id},\"${t.assetName}\",${t.type.name},${t.units},${t.pricePerUnit},${t.amount},${sdf.format(Date(t.date))},\"${t.notes}\"\n")
        }
        sb.toString()
    }
}
