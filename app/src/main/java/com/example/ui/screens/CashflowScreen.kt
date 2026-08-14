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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CashflowEntity
import com.example.data.local.entity.CashflowType
import com.example.domain.model.FinancialCalculator
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.EmeraldProfitBg
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseLoss
import com.example.ui.theme.RoseLossBg
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.WealthUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CashflowScreen(
    state: WealthUiState,
    onAddCashflow: (CashflowEntity) -> Unit,
    onDeleteCashflow: (CashflowEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf<CashflowType?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val symbol = state.settings.currencySymbol
    val summary = state.portfolioSummary

    val filteredList = remember(state.cashflows, selectedType) {
        if (selectedType == null) state.cashflows
        else state.cashflows.filter { it.type == selectedType }
    }

    val netSavings = summary.totalMonthlyIncome - summary.totalMonthlyExpenses

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = IndigoPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Income/Expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Title
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "Income & Expenses",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )
                Text(
                    text = "Track cashflow and optimize monthly savings rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTextSecondary
                )
            }

            // Monthly Savings Gauge Card in Natural Tones
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = BorderStroke(1.dp, SlateBorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "THIS MONTH'S SAVINGS RATE",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateTextMuted
                            )
                            Text(
                                text = "${String.format("%.1f", summary.monthlySavingsRate)}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.monthlySavingsRate >= 30) EmeraldProfit else SlateTextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "NET SAVINGS",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateTextMuted
                            )
                            Text(
                                text = FinancialCalculator.formatCurrency(netSavings, symbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (netSavings >= 0) EmeraldProfit else RoseLoss
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (summary.monthlySavingsRate / 100f).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldProfit,
                        trackColor = SlateBorderSubtle
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Income: ${FinancialCalculator.formatCurrency(summary.totalMonthlyIncome, symbol)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldProfit,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Expenses: ${FinancialCalculator.formatCurrency(summary.totalMonthlyExpenses, symbol)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = RoseLoss,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                    label = { Text("All (${state.cashflows.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.height(30.dp)
                )
                FilterChip(
                    selected = selectedType == CashflowType.INCOME,
                    onClick = { selectedType = if (selectedType == CashflowType.INCOME) null else CashflowType.INCOME },
                    label = { Text("Income", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.height(30.dp)
                )
                FilterChip(
                    selected = selectedType == CashflowType.EXPENSE,
                    onClick = { selectedType = if (selectedType == CashflowType.EXPENSE) null else CashflowType.EXPENSE },
                    label = { Text("Expenses", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cashflows list
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No cashflow records found. Tap '+' to add salary or expense.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateTextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        CashflowRowItem(
                            item = item,
                            currencySymbol = symbol,
                            onDelete = { onDeleteCashflow(item) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCashflowDialog(
            currencySymbol = symbol,
            onDismiss = { showAddDialog = false },
            onSave = {
                onAddCashflow(it)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CashflowRowItem(
    item: CashflowEntity,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val isIncome = item.type == CashflowType.INCOME

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = BorderStroke(1.dp, SlateBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isIncome) EmeraldProfitBg else RoseLossBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isIncome) EmeraldProfit else RoseLoss,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                    Text(
                        text = "${if (isIncome) "Income" else "Expense"} • ${sdf.format(Date(item.date))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextMuted,
                        fontSize = 11.sp
                    )
                    if (item.notes.isNotBlank()) {
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val sign = if (isIncome) "+" else "-"
                val amountColor = if (isIncome) EmeraldProfit else RoseLoss
                Text(
                    text = "$sign${FinancialCalculator.formatCurrency(item.amount, currencySymbol)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = SlateTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCashflowDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (CashflowEntity) -> Unit
) {
    var type by remember { mutableStateOf(CashflowType.EXPENSE) }
    var category by remember { mutableStateOf("Groceries & Food") }
    var amountStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val isValid = category.isNotBlank() && amount > 0

    val commonCategories = if (type == CashflowType.INCOME) {
        listOf("Monthly Salary", "Dividends & Payouts", "Interest Income", "Rental Income", "Freelance / Business", "Bonus", "Other Income")
    } else {
        listOf("Home EMI & Rent", "Groceries & Food", "Utilities & Bills", "Transport & Fuel", "Healthcare", "Shopping", "Entertainment", "Dining Out", "Other Expense")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${if (type == CashflowType.INCOME) "Income" else "Expense"}", fontWeight = FontWeight.Bold, color = SlateTextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Type selector
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == CashflowType.INCOME,
                        onClick = {
                            type = CashflowType.INCOME
                            category = "Monthly Salary"
                        },
                        label = { Text("Income (+)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == CashflowType.EXPENSE,
                        onClick = {
                            type = CashflowType.EXPENSE
                            category = "Groceries & Food"
                        },
                        label = { Text("Expense (-)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($currencySymbol) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category Preset:", style = MaterialTheme.typography.labelSmall, color = SlateTextSecondary)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    commonCategories.chunked(3).forEach { rowList ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            rowList.forEach { catName ->
                                FilterChip(
                                    selected = category == catName,
                                    onClick = { category = catName },
                                    label = { Text(catName, fontSize = 10.sp, maxLines = 1) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IndigoPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.height(26.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category Name *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        CashflowEntity(
                            type = type,
                            category = category.trim(),
                            amount = amount,
                            notes = notes.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                enabled = isValid
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SlateTextSecondary) }
        }
    )
}
