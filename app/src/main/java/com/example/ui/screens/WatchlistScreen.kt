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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
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
import com.example.data.local.entity.WatchlistEntity
import com.example.domain.model.FinancialCalculator
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.EmeraldProfitBg
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NaturalBlue
import com.example.ui.theme.RoseLoss
import com.example.ui.theme.RoseLossBg
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.WealthUiState
import kotlin.math.abs

@Composable
fun WatchlistScreen(
    state: WealthUiState,
    onAddWatchlist: (WatchlistEntity) -> Unit,
    onDeleteWatchlist: (WatchlistEntity) -> Unit,
    onConvertHolding: (WatchlistEntity, Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var convertingItem by remember { mutableStateOf<WatchlistEntity?>(null) }
    val symbol = state.settings.currencySymbol

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
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Watchlist")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "Asset Watchlist",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )
                Text(
                    text = "Monitor target buy prices and entry levels",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTextSecondary
                )
            }

            if (state.watchlist.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No assets on watchlist. Tap '+' to monitor target buy prices.",
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
                    items(state.watchlist, key = { it.id }) { item ->
                        WatchlistItemCard(
                            item = item,
                            currencySymbol = symbol,
                            onConvert = { convertingItem = item },
                            onDelete = { onDeleteWatchlist(item) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddWatchlistDialog(
            currencySymbol = symbol,
            onDismiss = { showAddDialog = false },
            onSave = {
                onAddWatchlist(it)
                showAddDialog = false
            }
        )
    }

    if (convertingItem != null) {
        ConvertWatchlistDialog(
            item = convertingItem!!,
            currencySymbol = symbol,
            onDismiss = { convertingItem = null },
            onConfirm = { qty, price ->
                onConvertHolding(convertingItem!!, qty, price)
                convertingItem = null
            }
        )
    }
}

@Composable
fun WatchlistItemCard(
    item: WatchlistEntity,
    currencySymbol: String,
    onConvert: () -> Unit,
    onDelete: () -> Unit
) {
    val distancePct = if (item.currentPrice > 0) {
        ((item.targetEntryPrice - item.currentPrice) / item.currentPrice) * 100.0
    } else 0.0
    val isNearTarget = abs(distancePct) < 5.0

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
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                    Text(
                        text = "${item.category.displayName} • ${item.ticker}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextMuted,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = SlateTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CMP: ${FinancialCalculator.formatCurrency(item.currentPrice, currencySymbol)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateTextSecondary
                    )
                    Text(
                        text = "Target Buy: ${FinancialCalculator.formatCurrency(item.targetEntryPrice, currencySymbol)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary
                    )
                    if (item.investmentThesis.isNotBlank()) {
                        Text(
                            text = item.investmentThesis,
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateTextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onConvert,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Buy Holding", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ConvertWatchlistDialog(
    item: WatchlistEntity,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Double, price: Double) -> Unit
) {
    var quantityStr by remember { mutableStateOf("10") }
    var priceStr by remember { mutableStateOf(item.currentPrice.toString()) }

    val qty = quantityStr.toDoubleOrNull() ?: 0.0
    val prc = priceStr.toDoubleOrNull() ?: 0.0
    val isValid = qty > 0 && prc > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${item.name} to Portfolio", fontWeight = FontWeight.Bold, color = SlateTextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("Quantity / Units *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Buy Price ($currencySymbol) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                onClick = { onConfirm(qty, prc) },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                enabled = isValid
            ) {
                Text("Confirm Purchase", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SlateTextSecondary) }
        }
    )
}

@Composable
fun AddWatchlistDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (WatchlistEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ticker by remember { mutableStateOf("") }
    var cmpStr by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var thesis by remember { mutableStateOf("") }

    val cmp = cmpStr.toDoubleOrNull() ?: 0.0
    val target = targetStr.toDoubleOrNull() ?: 0.0
    val isValid = name.isNotBlank() && target > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Watchlist", fontWeight = FontWeight.Bold, color = SlateTextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Asset Name *") },
                    placeholder = { Text("e.g. L&T, Tata Motors") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ticker,
                    onValueChange = { ticker = it },
                    label = { Text("Ticker / Code") },
                    placeholder = { Text("e.g. LT") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cmpStr,
                        onValueChange = { cmpStr = it },
                        label = { Text("Current ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetStr,
                        onValueChange = { targetStr = it },
                        label = { Text("Target ($currencySymbol) *") },
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
                    value = thesis,
                    onValueChange = { thesis = it },
                    label = { Text("Investment Thesis (Optional)") },
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
                        WatchlistEntity(
                            name = name.trim(),
                            ticker = ticker.trim(),
                            currentPrice = cmp,
                            targetEntryPrice = target,
                            investmentThesis = thesis.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                enabled = isValid
            ) {
                Text("Add to Watchlist", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SlateTextSecondary) }
        }
    )
}
