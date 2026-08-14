package com.example.domain.model

import com.example.data.local.entity.AssetCategory
import com.example.data.local.entity.CashflowEntity
import com.example.data.local.entity.CashflowType
import com.example.data.local.entity.DailyPortfolioSnapshotEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HoldingEntity
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.TransactionEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow

data class PortfolioSummary(
    val netWorth: Double = 0.0,
    val totalAssets: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val totalInvested: Double = 0.0,
    val totalCurrentValue: Double = 0.0,
    val totalProfitLoss: Double = 0.0,
    val totalReturnPercentage: Double = 0.0,
    val todayProfitLoss: Double = 0.0,
    val todayPercentageChange: Double = 0.0,
    val portfolioXirr: Double = 0.0,
    val monthlySavingsRate: Double = 0.0,
    val totalMonthlyIncome: Double = 0.0,
    val totalMonthlyExpenses: Double = 0.0,
    val totalActiveHoldings: Int = 0,
    val topPerformer: HoldingEntity? = null,
    val lowestPerformer: HoldingEntity? = null
)

data class AllocationSlice(
    val categoryName: String,
    val value: Double,
    val percentage: Double,
    val count: Int
)

data class FormattedCurrency(
    val formatted: String,
    val symbol: String
)

object FinancialCalculator {

    fun formatCurrency(
        amount: Double,
        currencySymbol: String = "₹",
        includeDecimals: Boolean = true
    ): String {
        val isNegative = amount < 0
        val absAmount = kotlin.math.abs(amount)
        
        val formattedNumber = if (currencySymbol == "₹") {
            // Indian numbering format (Lakhs, Crores) if large
            formatIndianNumbering(absAmount, includeDecimals)
        } else {
            val nf = NumberFormat.getNumberInstance(Locale.US)
            nf.maximumFractionDigits = if (includeDecimals) 2 else 0
            nf.minimumFractionDigits = if (includeDecimals && absAmount % 1.0 != 0.0) 2 else 0
            nf.format(absAmount)
        }

        return if (isNegative) "-$currencySymbol$formattedNumber" else "$currencySymbol$formattedNumber"
    }

    private fun formatIndianNumbering(amount: Double, includeDecimals: Boolean): String {
        val longVal = amount.toLong()
        val decimalPart = if (includeDecimals) {
            val dec = String.format(Locale.US, "%.2f", amount % 1.0)
            if (dec.startsWith("0.")) dec.substring(1) else ""
        } else ""

        val str = longVal.toString()
        if (str.length <= 3) {
            return str + (if (decimalPart.isNotEmpty() && decimalPart != ".00") decimalPart else "")
        }

        val last3 = str.substring(str.length - 3)
        val rest = str.substring(0, str.length - 3)
        val sb = StringBuilder()
        
        var count = 0
        for (i in rest.length - 1 downTo 0) {
            sb.append(rest[i])
            count++
            if (count % 2 == 0 && i > 0) {
                sb.append(",")
            }
        }
        val result = sb.reverse().toString() + "," + last3 + (if (decimalPart.isNotEmpty() && decimalPart != ".00") decimalPart else "")
        return result
    }

    fun formatPercent(percent: Double, showSign: Boolean = true): String {
        val sign = if (showSign && percent > 0) "+" else ""
        return String.format(Locale.US, "$sign%.2f%%", percent)
    }

    fun calculateHoldingCagr(holding: HoldingEntity): Double {
        if (holding.investedAmount <= 0 || holding.currentValue <= 0) return 0.0
        val now = System.currentTimeMillis()
        val daysHeld = ((now - holding.purchaseDate) / (1000.0 * 60 * 60 * 24)).coerceAtLeast(1.0)
        val yearsHeld = daysHeld / 365.25

        if (yearsHeld < 0.08) {
            // Under ~1 month, use absolute return percentage
            return holding.returnPercentage
        }

        return try {
            val multiple = holding.currentValue / holding.investedAmount
            val cagr = (multiple.pow(1.0 / yearsHeld) - 1.0) * 100.0
            cagr.coerceIn(-99.9, 999.9)
        } catch (e: Exception) {
            holding.returnPercentage
        }
    }

    fun computePortfolioSummary(
        holdings: List<HoldingEntity>,
        latestSnapshot: DailyPortfolioSnapshotEntity?,
        cashflows: List<CashflowEntity>,
        loans: List<LoanEntity>
    ): PortfolioSummary {
        val totalInvested = holdings.sumOf { it.investedAmount }
        val totalCurrentValue = holdings.sumOf { it.currentValue }
        val totalPL = totalCurrentValue - totalInvested
        val totalReturnPct = if (totalInvested > 0) (totalPL / totalInvested) * 100.0 else 0.0

        val totalLiabilities = loans.sumOf { it.outstandingBalance }

        // Liquid cash from cashflows
        val totalIncomeEver = cashflows.filter { it.type == CashflowType.INCOME }.sumOf { it.amount }
        val totalExpenseEver = cashflows.filter { it.type == CashflowType.EXPENSE }.sumOf { it.amount }
        val netCashflowBalance = (totalIncomeEver - totalExpenseEver).coerceAtLeast(0.0)

        val totalAssets = totalCurrentValue + (if (netCashflowBalance > 0) netCashflowBalance else 0.0)
        val netWorth = totalAssets - totalLiabilities

        // Monthly cashflow
        val currentMonthPrefix = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val monthlyCashflows = cashflows.filter {
            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.date)) == currentMonthPrefix
        }
        val monthlyIncome = monthlyCashflows.filter { it.type == CashflowType.INCOME }.sumOf { it.amount }
        val monthlyExpenses = monthlyCashflows.filter { it.type == CashflowType.EXPENSE }.sumOf { it.amount }
        val savingsRate = if (monthlyIncome > 0) {
            (((monthlyIncome - monthlyExpenses) / monthlyIncome) * 100.0).coerceIn(-100.0, 100.0)
        } else 0.0

        // Today's PL from snapshot if available
        val todayPL = latestSnapshot?.dailyProfitLoss ?: 0.0
        val todayPct = latestSnapshot?.dailyPercentChange ?: 0.0

        // Estimate portfolio weighted XIRR
        val weightedXirr = if (holdings.isNotEmpty() && totalInvested > 0) {
            val sumWeightedCagr = holdings.sumOf { calculateHoldingCagr(it) * it.investedAmount }
            sumWeightedCagr / totalInvested
        } else {
            0.0
        }

        val topPerformer = holdings.maxByOrNull { it.returnPercentage }
        val lowestPerformer = holdings.minByOrNull { it.returnPercentage }

        return PortfolioSummary(
            netWorth = netWorth,
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            totalInvested = totalInvested,
            totalCurrentValue = totalCurrentValue,
            totalProfitLoss = totalPL,
            totalReturnPercentage = totalReturnPct,
            todayProfitLoss = todayPL,
            todayPercentageChange = todayPct,
            portfolioXirr = weightedXirr,
            monthlySavingsRate = savingsRate,
            totalMonthlyIncome = monthlyIncome,
            totalMonthlyExpenses = monthlyExpenses,
            totalActiveHoldings = holdings.size,
            topPerformer = topPerformer,
            lowestPerformer = lowestPerformer
        )
    }

    fun calculateAssetAllocation(holdings: List<HoldingEntity>): List<AllocationSlice> {
        val totalValue = holdings.sumOf { it.currentValue }
        if (totalValue <= 0) return emptyList()

        return holdings.groupBy { it.category }
            .map { (cat, list) ->
                val catValue = list.sumOf { it.currentValue }
                AllocationSlice(
                    categoryName = cat.displayName,
                    value = catValue,
                    percentage = (catValue / totalValue) * 100.0,
                    count = list.size
                )
            }.sortedByDescending { it.value }
    }

    fun calculateSectorAllocation(holdings: List<HoldingEntity>): List<AllocationSlice> {
        val totalValue = holdings.sumOf { it.currentValue }
        if (totalValue <= 0) return emptyList()

        return holdings.groupBy { it.sector.ifBlank { "Other / General" } }
            .map { (sector, list) ->
                val secValue = list.sumOf { it.currentValue }
                AllocationSlice(
                    categoryName = sector,
                    value = secValue,
                    percentage = (secValue / totalValue) * 100.0,
                    count = list.size
                )
            }.sortedByDescending { it.value }
    }
}
