package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.HoldingEntity
import com.example.domain.model.FinancialCalculator
import com.example.ui.components.AllocationDonutChart
import com.example.ui.components.PortfolioLineChart
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.RoseLoss
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.WealthUiState

@Composable
fun DailyAnalyticsScreen(
    state: WealthUiState,
    onTimeRangeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val symbol = state.settings.currencySymbol
    val summary = state.portfolioSummary
    val holdings = state.holdings

    val sortedByReturn = holdings.sortedByDescending { it.returnPercentage }
    val topWinners = sortedByReturn.take(4).filter { it.returnPercentage > 0 }
    val topLosers = sortedByReturn.takeLast(4).filter { it.returnPercentage < 0 }.reversed()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "Portfolio Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )
                Text(
                    text = "Performance trends, sector exposure and return metrics",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTextSecondary
                )
            }
        }

        // 1. Line Chart Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = BorderStroke(1.dp, SlateBorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Historical Valuation Curve",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PortfolioLineChart(
                        snapshots = state.snapshots,
                        currencySymbol = symbol,
                        selectedRange = state.selectedTimeRange,
                        onRangeSelected = onTimeRangeSelected
                    )
                }
            }
        }

        // 2. Asset Allocation & Sector Allocation
        item {
            AllocationDonutChart(
                slices = state.assetAllocation,
                currencySymbol = symbol,
                title = "Asset Class Allocation"
            )
        }

        item {
            AllocationDonutChart(
                slices = state.sectorAllocation,
                currencySymbol = symbol,
                title = "Sector Allocation Breakdown"
            )
        }

        // 3. Top Winners
        if (topWinners.isNotEmpty()) {
            item {
                AnalyticsRankingCard(
                    title = "Top Gainers",
                    items = topWinners,
                    currencySymbol = symbol,
                    isPositive = true
                )
            }
        }

        // 4. Top Losers / Underperformers
        if (topLosers.isNotEmpty()) {
            item {
                AnalyticsRankingCard(
                    title = "Underperformers",
                    items = topLosers,
                    currencySymbol = symbol,
                    isPositive = false
                )
            }
        }

        // 5. Performance Statistics Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = BorderStroke(1.dp, SlateBorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Financial Returns Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    StatRow("Overall Profit / Loss", "${FinancialCalculator.formatCurrency(summary.totalProfitLoss, symbol)} (${FinancialCalculator.formatPercent(summary.totalReturnPercentage)})", summary.totalProfitLoss >= 0)
                    StatRow("Estimated Portfolio XIRR", FinancialCalculator.formatPercent(summary.portfolioXirr), summary.portfolioXirr >= 0)
                    StatRow("Today's Net Movement", "${FinancialCalculator.formatCurrency(summary.todayProfitLoss, symbol)} (${FinancialCalculator.formatPercent(summary.todayPercentageChange)})", summary.todayProfitLoss >= 0)
                    StatRow("Total Capital Invested", FinancialCalculator.formatCurrency(summary.totalInvested, symbol), null)
                    StatRow("Current Portfolio Value", FinancialCalculator.formatCurrency(summary.totalCurrentValue, symbol), null)
                    StatRow("Active Tracked Holdings", "${holdings.size} assets", null)
                }
            }
        }
    }
}

@Composable
fun AnalyticsRankingCard(
    title: String,
    items: List<HoldingEntity>,
    currencySymbol: String,
    isPositive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = BorderStroke(1.dp, SlateBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { h ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = h.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SlateTextPrimary)
                            Text(text = "${h.category.displayName} • ${h.sector}", style = MaterialTheme.typography.bodySmall, color = SlateTextMuted, fontSize = 11.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = FinancialCalculator.formatCurrency(h.currentValue, currencySymbol), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                            Text(
                                text = "${FinancialCalculator.formatPercent(h.returnPercentage)} (${FinancialCalculator.formatCurrency(h.totalProfitLoss, currencySymbol)})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) EmeraldProfit else RoseLoss
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, isPositive: Boolean?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = SlateTextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = when (isPositive) {
                true -> EmeraldProfit
                false -> RoseLoss
                null -> SlateTextPrimary
            }
        )
    }
}
