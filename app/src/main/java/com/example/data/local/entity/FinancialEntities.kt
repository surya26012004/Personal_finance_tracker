package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val displayName: String, val isPositiveCashflow: Boolean) {
    BUY("Buy / New Investment", false),
    SELL("Sell / Liquidation", true),
    SIP("SIP Recurring", false),
    DIVIDEND("Dividend Received", true),
    DEPOSIT("Capital Deposit", true),
    WITHDRAWAL("Capital Withdrawal", false),
    LOAN_EMI("Loan EMI Payment", false),
    INTEREST_INCOME("Interest Payout", true)
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val holdingId: Long? = null,
    val assetName: String,
    val type: TransactionType,
    val units: Double = 0.0,
    val pricePerUnit: Double = 0.0,
    val amount: Double,
    val feesOrTaxes: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)

enum class CashflowType {
    INCOME,
    EXPENSE
}

@Entity(tableName = "cashflows")
data class CashflowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: CashflowType,
    val category: String, // Salary, Dividends, Interest, Rental, Freelance / Housing, Food, Transport, Utilities, EMIs, Healthcare, Leisure, Other
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isRecurring: Boolean = false
)

enum class GoalCategory(val displayName: String) {
    RETIREMENT("Retirement / FIRE"),
    EMERGENCY_FUND("Emergency Safety Net"),
    HOME_PURCHASE("House / Real Estate"),
    EDUCATION("Education"),
    VACATION("Travel & Vacation"),
    VEHICLE("Dream Car / Vehicle"),
    WEALTH_MILESTONE("Net Worth Target"),
    OTHER("Custom Goal")
}

@Entity(tableName = "financial_goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: GoalCategory = GoalCategory.WEALTH_MILESTONE,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: Long, // timestamp
    val createdDate: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val progressPercent: Double
        get() = if (targetAmount > 0) ((currentAmount / targetAmount) * 100.0).coerceIn(0.0, 100.0) else 0.0
    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
}

enum class LoanType(val displayName: String) {
    HOME_LOAN("Home Loan / Mortgage"),
    CAR_LOAN("Auto / Car Loan"),
    PERSONAL_LOAN("Personal Loan"),
    EDUCATION_LOAN("Education Loan"),
    CREDIT_CARD("Credit Card Debt"),
    BUSINESS_LOAN("Business Loan"),
    OTHER("Other Liability")
}

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: LoanType = LoanType.HOME_LOAN,
    val lender: String = "",
    val principalAmount: Double,
    val outstandingBalance: Double,
    val interestRatePercent: Double,
    val monthlyEmi: Double,
    val tenureMonthsRemaining: Int,
    val nextDueDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val notes: String = ""
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val ticker: String,
    val category: AssetCategory = AssetCategory.STOCK,
    val sector: String = "Technology",
    val currentPrice: Double,
    val targetEntryPrice: Double,
    val week52High: Double = 0.0,
    val week52Low: Double = 0.0,
    val investmentThesis: String = "",
    val addedDate: Long = System.currentTimeMillis()
)

enum class Sentiment {
    BULLISH,
    NEUTRAL,
    BEARISH
}

enum class Conviction {
    VERY_HIGH,
    HIGH,
    MODERATE,
    SPECULATIVE
}

@Entity(tableName = "journal_entries")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val tags: String = "#Strategy", // Comma separated or space separated tags
    val sentiment: Sentiment = Sentiment.BULLISH,
    val conviction: Conviction = Conviction.HIGH,
    val relatedAsset: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val currencyCode: String = "INR",
    val currencySymbol: String = "₹",
    val isDarkMode: Boolean? = null, // null means follow system
    val isSampleDataLoaded: Boolean = false,
    val userDisplayName: String = "Investor"
)
