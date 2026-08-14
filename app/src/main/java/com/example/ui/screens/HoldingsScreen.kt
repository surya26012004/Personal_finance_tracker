package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.local.entity.AssetCategory
import com.example.data.local.entity.HoldingEntity
import com.example.domain.model.FinancialCalculator
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.EmeraldProfitBg
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

@Composable
fun HoldingsScreen(
    state: WealthUiState,
    onAddHolding: (HoldingEntity) -> Unit,
    onUpdateHolding: (HoldingEntity) -> Unit,
    onDeleteHolding: (HoldingEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<AssetCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingHolding by remember { mutableStateOf<HoldingEntity?>(null) }

    val symbol = state.settings.currencySymbol
    val filteredHoldings = remember(state.holdings, selectedCategory, searchQuery) {
        state.holdings.filter { h ->
            val matchCat = selectedCategory == null || h.category == selectedCategory
            val matchQuery = searchQuery.isBlank() ||
                    h.name.contains(searchQuery, ignoreCase = true) ||
                    h.tickerOrCode.contains(searchQuery, ignoreCase = true) ||
                    h.sector.contains(searchQuery, ignoreCase = true)
            matchCat && matchQuery
        }
    }

    val totalInvested = filteredHoldings.sumOf { it.investedAmount }
    val totalCurrentVal = filteredHoldings.sumOf { it.currentValue }
    val totalPL = totalCurrentVal - totalInvested
    val totalReturnPct = if (totalInvested > 0) (totalPL / totalInvested) * 100.0 else 0.0

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
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Holding")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Title & Search
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "Holdings & Assets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )
                Text(
                    text = "${filteredHoldings.size} positions tracked",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTextSecondary
                )
            }

            // Summary Card in Natural Tones
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = BorderStroke(1.dp, SlateBorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            text = "TOTAL VALUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateTextMuted
                        )
                        Text(
                            text = FinancialCalculator.formatCurrency(totalCurrentVal, symbol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TOTAL PROFIT / LOSS",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateTextMuted
                        )
                        val isPos = totalPL >= 0
                        Text(
                            text = "${FinancialCalculator.formatCurrency(totalPL, symbol)} (${FinancialCalculator.formatPercent(totalReturnPct)})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPos) EmeraldProfit else RoseLoss
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search stocks, funds, tickers...", fontSize = 13.sp, color = SlateTextMuted) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SlateTextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = SlateSurface,
                    unfocusedContainerColor = SlateSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All (${state.holdings.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.height(30.dp)
                )

                AssetCategory.values().forEach { cat ->
                    val count = state.holdings.count { it.category == cat }
                    if (count > 0 || cat == AssetCategory.STOCK || cat == AssetCategory.MUTUAL_FUND) {
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                            label = { Text("${cat.displayName} ($count)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IndigoPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Holdings List
            if (filteredHoldings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No holdings found. Tap '+' to add a stock or mutual fund.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateTextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHoldings, key = { it.id }) { holding ->
                        HoldingCard(
                            holding = holding,
                            currencySymbol = symbol,
                            onEdit = { editingHolding = holding },
                            onDelete = { onDeleteHolding(holding) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog || editingHolding != null) {
        HoldingFormDialog(
            holding = editingHolding,
            currencySymbol = symbol,
            onDismiss = {
                showAddDialog = false
                editingHolding = null
            },
            onSave = { savedHolding ->
                if (editingHolding != null) {
                    onUpdateHolding(savedHolding)
                } else {
                    onAddHolding(savedHolding)
                }
                showAddDialog = false
                editingHolding = null
            }
        )
    }
}

@Composable
fun HoldingCard(
    holding: HoldingEntity,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isPos = holding.totalProfitLoss >= 0
    val cagr = FinancialCalculator.calculateHoldingCagr(holding)

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = holding.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${holding.category.displayName} • ${holding.sector}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateTextMuted,
                            fontSize = 11.sp
                        )
                        if (holding.tickerOrCode.isNotBlank()) {
                            Text(
                                text = " • ${holding.tickerOrCode}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = SlateTextMuted)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Holding") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = RoseLoss) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseLoss) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Units / Qty", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                    Text(text = String.format("%.2f", holding.quantity), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SlateTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Buy Avg Price", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                    Text(text = FinancialCalculator.formatCurrency(holding.buyPrice, currencySymbol), style = MaterialTheme.typography.bodyMedium, color = SlateTextSecondary)
                }

                Column {
                    Text(text = "Invested", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                    Text(text = FinancialCalculator.formatCurrency(holding.investedAmount, currencySymbol), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SlateTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Current Price", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                    Text(text = FinancialCalculator.formatCurrency(holding.currentPrice, currencySymbol), style = MaterialTheme.typography.bodyMedium, color = SlateTextSecondary)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Current Value", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                    Text(text = FinancialCalculator.formatCurrency(holding.currentValue, currencySymbol), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Return / CAGR", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                    Text(
                        text = "${FinancialCalculator.formatPercent(holding.returnPercentage)} (${String.format("%.1f", cagr)}%)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPos) EmeraldProfit else RoseLoss
                    )
                }
            }
        }
    }
}

@Composable
fun HoldingFormDialog(
    holding: HoldingEntity?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (HoldingEntity) -> Unit
) {
    var name by remember { mutableStateOf(holding?.name ?: "") }
    var ticker by remember { mutableStateOf(holding?.tickerOrCode ?: "") }
    var category by remember { mutableStateOf(holding?.category ?: AssetCategory.STOCK) }
    var sector by remember { mutableStateOf(holding?.sector ?: "General") }
    var quantityStr by remember { mutableStateOf(holding?.quantity?.toString() ?: "") }
    var buyPriceStr by remember { mutableStateOf(holding?.buyPrice?.toString() ?: "") }
    var currentPriceStr by remember { mutableStateOf(holding?.currentPrice?.toString() ?: "") }
    var notes by remember { mutableStateOf(holding?.notes ?: "") }

    val qty = quantityStr.toDoubleOrNull() ?: 0.0
    val buyPrice = buyPriceStr.toDoubleOrNull() ?: 0.0
    val curPrice = currentPriceStr.toDoubleOrNull() ?: buyPrice
    val isValid = name.isNotBlank() && qty > 0 && buyPrice > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (holding != null) "Edit Holding" else "Add New Holding", fontWeight = FontWeight.Bold, color = SlateTextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Asset / Stock Name *") },
                    placeholder = { Text("e.g. Reliance Industries, Parag Parikh Flexicap") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ticker,
                        onValueChange = { ticker = it },
                        label = { Text("Ticker / Code") },
                        placeholder = { Text("e.g. RELIANCE") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sector,
                        onValueChange = { sector = it },
                        label = { Text("Sector") },
                        placeholder = { Text("e.g. IT, Banking") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Category selector chips
                Text("Asset Category", style = MaterialTheme.typography.labelSmall, color = SlateTextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AssetCategory.values().forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.displayName, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IndigoPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Units / Qty *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = buyPriceStr,
                        onValueChange = { buyPriceStr = it },
                        label = { Text("Buy Price ($currencySymbol) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = currentPriceStr,
                    onValueChange = { currentPriceStr = it },
                    label = { Text("Current Market Price ($currencySymbol)") },
                    placeholder = { Text("Defaults to buy price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    val entity = (holding ?: HoldingEntity(
                        name = name.trim(),
                        quantity = qty,
                        buyPrice = buyPrice,
                        currentPrice = curPrice
                    )).copy(
                        name = name.trim(),
                        tickerOrCode = ticker.trim(),
                        category = category,
                        sector = sector.trim().ifBlank { "General" },
                        quantity = qty,
                        buyPrice = buyPrice,
                        currentPrice = curPrice,
                        notes = notes.trim(),
                        lastUpdatedDate = System.currentTimeMillis()
                    )
                    onSave(entity)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                enabled = isValid
            ) {
                Text("Save Holding", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SlateTextSecondary) }
        }
    )
}
