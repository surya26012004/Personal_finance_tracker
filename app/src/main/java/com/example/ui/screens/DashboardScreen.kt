package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FinancialCalculator
import com.example.ui.components.AllocationDonutChart
import com.example.ui.components.PortfolioLineChart
import com.example.ui.components.WealthMetricCard
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.EmeraldProfitBg
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.IndigoTextLight
import com.example.ui.theme.NaturalBlue
import com.example.ui.theme.NaturalBlueBg
import com.example.ui.theme.NaturalPurple
import com.example.ui.theme.NaturalPurpleBg
import com.example.ui.theme.NaturalTeal
import com.example.ui.theme.NaturalTealBg
import com.example.ui.theme.RoseLoss
import com.example.ui.theme.RoseLossBg
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberBg
import com.example.ui.theme.WarmOrange
import com.example.ui.theme.WarmOrangeBg
import com.example.ui.viewmodel.WealthTab
import com.example.ui.viewmodel.WealthUiState

@Composable
fun DashboardScreen(
    state: WealthUiState,
    onNavigateTab: (WealthTab) -> Unit,
    onTimeRangeSelected: (String) -> Unit,
    onQuickAddHolding: () -> Unit,
    onQuickAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = state.portfolioSummary
    val symbol = state.settings.currencySymbol

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Natural Tones Daily Update Hero Banner (bg-indigo-900 style)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    IndigoDark,
                                    IndigoPrimary
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "DAILY UPDATE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IndigoTextLight,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Fast Portfolio Movement",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = WarmAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Text(
                            text = "Enter today's percentage change for each stock & fund to auto-calculate portfolio value and track daily P/L.",
                            style = MaterialTheme.typography.bodySmall,
                            color = IndigoTextLight,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // High contrast CTA button
                        Button(
                            onClick = { onNavigateTab(WealthTab.DAILY_UPDATE) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = IndigoDark
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Daily Workflow",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Key Financial Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WealthMetricCard(
                        title = "Invested Capital",
                        value = FinancialCalculator.formatCurrency(summary.totalInvested, symbol),
                        subtitle = "${state.holdings.size} active assets",
                        icon = Icons.Default.AccountBalance,
                        modifier = Modifier.weight(1f)
                    )
                    WealthMetricCard(
                        title = "Current Value",
                        value = FinancialCalculator.formatCurrency(summary.totalCurrentValue, symbol),
                        badgeText = FinancialCalculator.formatPercent(summary.totalReturnPercentage),
                        isPositiveBadge = summary.totalProfitLoss >= 0,
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WealthMetricCard(
                        title = "Portfolio XIRR",
                        value = FinancialCalculator.formatPercent(summary.portfolioXirr),
                        subtitle = "Overall P/L: ${FinancialCalculator.formatCurrency(summary.totalProfitLoss, symbol)}",
                        isPositiveBadge = summary.totalProfitLoss >= 0,
                        icon = Icons.Default.ShowChart,
                        modifier = Modifier.weight(1f)
                    )
                    WealthMetricCard(
                        title = "Monthly Savings",
                        value = "${String.format("%.1f", summary.monthlySavingsRate)}%",
                        subtitle = "Income: ${FinancialCalculator.formatCurrency(summary.totalMonthlyIncome, symbol, false)}",
                        icon = Icons.Default.Savings,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Asset Class Quick Breakdown Cards (Design HTML Style)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Portfolio Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                    Text(
                        text = "See All",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary,
                        modifier = Modifier.clickable { onNavigateTab(WealthTab.PORTFOLIO) }
                    )
                }

                // Stocks Asset Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateTab(WealthTab.PORTFOLIO) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = BorderStroke(1.dp, SlateBorderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NaturalBlueBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = NaturalBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Stocks Portfolio",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextPrimary
                            )
                            Text(
                                text = "${state.holdings.count { it.category.name == "STOCKS" }} Holdings",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val stocksTotal = state.holdings.filter { it.category.name == "STOCKS" }.sumOf { it.currentValue }
                            Text(
                                text = FinancialCalculator.formatCurrency(stocksTotal, symbol),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextPrimary
                            )
                            Text(
                                text = "Direct Equities",
                                style = MaterialTheme.typography.labelSmall,
                                color = NaturalBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Mutual Funds Asset Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateTab(WealthTab.PORTFOLIO) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = BorderStroke(1.dp, SlateBorderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(WarmOrangeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = WarmOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mutual Funds & SIPs",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextPrimary
                            )
                            Text(
                                text = "${state.holdings.count { it.category.name == "MUTUAL_FUNDS" }} Funds Active",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val mfTotal = state.holdings.filter { it.category.name == "MUTUAL_FUNDS" }.sumOf { it.currentValue }
                            Text(
                                text = FinancialCalculator.formatCurrency(mfTotal, symbol),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextPrimary
                            )
                            Text(
                                text = "Managed Funds",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarmOrange,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Interactive Portfolio Performance Trend Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = BorderStroke(1.dp, SlateBorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Portfolio Growth Trend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextPrimary
                        )
                        Text(
                            text = "Historical Net Worth",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateTextSecondary
                        )
                    }

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

        // 5. Asset Allocation Breakdown
        item {
            AllocationDonutChart(
                slices = state.assetAllocation,
                currencySymbol = symbol,
                title = "Portfolio Asset Distribution"
            )
        }

        // 6. Quick Action Shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onQuickAddHolding,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IndigoPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Holding", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = onQuickAddTransaction,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Transaction", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 7. Top Winners & Losers Quick Peek
        if (summary.topPerformer != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = BorderStroke(1.dp, SlateBorderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Top Holding Highlight",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Top Performer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = summary.topPerformer.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextPrimary
                                )
                                Text(
                                    text = summary.topPerformer.category.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SlateTextSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = FinancialCalculator.formatCurrency(summary.topPerformer.currentValue, symbol),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextPrimary
                                )
                                Text(
                                    text = "+${FinancialCalculator.formatPercent(summary.topPerformer.returnPercentage)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldProfit
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

