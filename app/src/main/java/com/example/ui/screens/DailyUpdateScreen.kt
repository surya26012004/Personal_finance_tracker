package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.HoldingEntity
import com.example.domain.model.FinancialCalculator
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.EmeraldProfitBg
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseLoss
import com.example.ui.theme.RoseLossBg
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.WealthUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DailyUpdateScreen(
    state: WealthUiState,
    onInputChange: (holdingId: Long, input: String) -> Unit,
    onApplyUniformPercent: (Double) -> Unit,
    onDateSelected: (String) -> Unit,
    onSaveAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val symbol = state.settings.currencySymbol
    val holdings = state.holdings
    val inputs = state.dailyUpdateInputs

    // Calculate live preview totals
    var liveTotalDailyPL = 0.0
    var liveTotalNewValue = 0.0
    var liveTotalInvested = 0.0

    for (h in holdings) {
        val pct = inputs[h.id]?.toDoubleOrNull() ?: 0.0
        val newPrice = (h.currentPrice * (1.0 + (pct / 100.0))).coerceAtLeast(0.01)
        val dailyPL = (newPrice - h.currentPrice) * h.quantity
        val itemVal = h.quantity * newPrice

        liveTotalDailyPL += dailyPL
        liveTotalNewValue += itemVal
        liveTotalInvested += h.investedAmount
    }

    val prevTotalVal = liveTotalNewValue - liveTotalDailyPL
    val liveDailyPct = if (prevTotalVal > 0) (liveTotalDailyPL / prevTotalVal) * 100.0 else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 1. Header & Date Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daily % Portfolio Update",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateTextPrimary
                )
                Text(
                    text = "Enter today's % change for instant portfolio calculation",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTextSecondary
                )
            }

            // Date Picker Chip
            OutlinedCard(
                modifier = Modifier.clickable {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val selected = Calendar.getInstance()
                            selected.set(year, month, day)
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            onDateSelected(sdf.format(selected.time))
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SlateBorderSubtle),
                colors = CardDefaults.outlinedCardColors(containerColor = SlateSurface)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state.selectedDailyDate,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                }
            }
        }

        // 2. Quick Uniform Preset Shortcuts
        Text(
            text = "QUICK BENCHMARK PRESETS",
            style = MaterialTheme.typography.labelSmall,
            color = SlateTextMuted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val presets = listOf(2.0, 1.5, 1.0, 0.5, 0.0, -0.5, -1.0, -2.0)
            presets.forEach { p ->
                val sign = if (p > 0) "+" else ""
                val text = "$sign$p%"
                val isPositive = p >= 0
                FilterChip(
                    selected = false,
                    onClick = {
                        onApplyUniformPercent(p)
                        focusManager.clearFocus()
                    },
                    label = {
                        Text(
                            text = text,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (p == 0.0) SlateTextPrimary else if (isPositive) EmeraldProfit else RoseLoss
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SlateSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = SlateBorderSubtle,
                        enabled = true,
                        selected = false
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Live Total Impact Preview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (liveTotalDailyPL >= 0) IndigoDark else Color(0xFF450A0A)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ESTIMATED TODAY'S P/L",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (liveTotalDailyPL >= 0) Color(0xFFA5B4FC) else Color(0xFFFCA5A5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    val sign = if (liveTotalDailyPL > 0) "+" else ""
                    Text(
                        text = "$sign${FinancialCalculator.formatCurrency(liveTotalDailyPL, symbol)} (${FinancialCalculator.formatPercent(liveDailyPct)})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (liveTotalDailyPL >= 0) Color(0xFF6EE7B7) else Color(0xFFFCA5A5)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "NEW TOTAL VALUE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Text(
                        text = FinancialCalculator.formatCurrency(liveTotalNewValue, symbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Holdings Fast Update Table / List
        if (holdings.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No holdings in portfolio. Add holdings to start daily tracking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateTextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(holdings, key = { it.id }) { holding ->
                    DailyHoldingRowItem(
                        holding = holding,
                        inputValue = inputs[holding.id] ?: "",
                        currencySymbol = symbol,
                        onValueChange = { newVal ->
                            onInputChange(holding.id, newVal)
                        }
                    )
                }
            }
        }

        // 5. High-Impact "SAVE ALL" Button (Sticky at bottom)
        Button(
            onClick = {
                focusManager.clearFocus()
                onSaveAll()
            },
            enabled = !state.isSavingDailyUpdate && holdings.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = IndigoPrimary,
                contentColor = Color.White
            )
        ) {
            if (state.isSavingDailyUpdate) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saving & Computing Snapshots...", fontWeight = FontWeight.Bold)
            } else {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SAVE TODAY'S SNAPSHOT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun DailyHoldingRowItem(
    holding: HoldingEntity,
    inputValue: String,
    currencySymbol: String,
    onValueChange: (String) -> Unit
) {
    val pct = inputValue.toDoubleOrNull() ?: 0.0
    val newPrice = (holding.currentPrice * (1.0 + (pct / 100.0))).coerceAtLeast(0.01)
    val dailyPL = (newPrice - holding.currentPrice) * holding.quantity
    val isPos = dailyPL >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurface
        ),
        border = BorderStroke(1.dp, SlateBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left info: Name, Qty, Current Price
            Column(modifier = Modifier.weight(1.3f)) {
                Text(
                    text = holding.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "${holding.category.displayName} • Qty: ${holding.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CMP: ${FinancialCalculator.formatCurrency(holding.currentPrice, currencySymbol)} → New: ${FinancialCalculator.formatCurrency(newPrice, currencySymbol)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateTextMuted,
                    fontSize = 10.sp
                )
                if (inputValue.isNotBlank()) {
                    Text(
                        text = "P/L: ${FinancialCalculator.formatCurrency(dailyPL, currencySymbol)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPos) EmeraldProfit else RoseLoss,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right input: Today's % Change TextField
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(0.9f)
            ) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { str ->
                        // Allow digits, single decimal point, and leading minus
                        if (str.isEmpty() || str == "-" || str.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                            onValueChange(str)
                        }
                    },
                    label = { Text("Today %", fontSize = 10.sp, color = SlateTextMuted) },
                    placeholder = { Text("0.0", fontSize = 11.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.width(105.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (pct >= 0) EmeraldProfit else RoseLoss,
                        unfocusedBorderColor = SlateBorder,
                        focusedContainerColor = SlateSurfaceVariant,
                        unfocusedContainerColor = SlateSurface
                    )
                )
            }
        }
    }
}

