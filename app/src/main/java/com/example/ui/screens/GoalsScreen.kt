package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GoalCategory
import com.example.data.local.entity.GoalEntity
import com.example.domain.model.FinancialCalculator
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.WealthUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun GoalsScreen(
    state: WealthUiState,
    onAddGoal: (GoalEntity) -> Unit,
    onContributeToGoal: (GoalEntity, Double) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var contributeGoal by remember { mutableStateOf<GoalEntity?>(null) }
    var contributeAmountStr by remember { mutableStateOf("") }
    val symbol = state.settings.currencySymbol

    val totalGoalTarget = state.goals.sumOf { it.targetAmount }
    val totalGoalSaved = state.goals.sumOf { it.currentAmount }
    val overallGoalProgress = if (totalGoalTarget > 0) ((totalGoalSaved / totalGoalTarget) * 100.0).coerceIn(0.0, 100.0) else 0.0

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
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Goal")
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
                    text = "Financial Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )
                Text(
                    text = "Track target milestones, FIRE, and asset dreams",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTextSecondary
                )
            }

            // Summary Card
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
                            Text(text = "TOTAL GOALS ACCUMULATION", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                            Text(
                                text = "${FinancialCalculator.formatCurrency(totalGoalSaved, symbol)} / ${FinancialCalculator.formatCurrency(totalGoalTarget, symbol, false)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextPrimary
                            )
                        }

                        Text(
                            text = String.format("%.1f%%", overallGoalProgress),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldProfit
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (overallGoalProgress / 100f).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldProfit,
                        trackColor = SlateBorderSubtle
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Goals List
            if (state.goals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No financial goals yet. Tap '+' to create your first target.",
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
                    items(state.goals, key = { it.id }) { goal ->
                        GoalItemCard(
                            goal = goal,
                            currencySymbol = symbol,
                            onContribute = { contributeGoal = goal },
                            onDelete = { onDeleteGoal(goal) }
                        )
                    }
                }
            }
        }
    }

    // Add Goal Dialog
    if (showAddDialog) {
        AddGoalDialog(
            currencySymbol = symbol,
            onDismiss = { showAddDialog = false },
            onSave = {
                onAddGoal(it)
                showAddDialog = false
            }
        )
    }

    // Contribute / Allocate Dialog
    if (contributeGoal != null) {
        AlertDialog(
            onDismissRequest = { contributeGoal = null },
            title = { Text("Contribute to ${contributeGoal!!.name}", fontWeight = FontWeight.Bold, color = SlateTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Current saved: ${FinancialCalculator.formatCurrency(contributeGoal!!.currentAmount, symbol)} / ${FinancialCalculator.formatCurrency(contributeGoal!!.targetAmount, symbol)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextSecondary
                    )
                    OutlinedTextField(
                        value = contributeAmountStr,
                        onValueChange = { contributeAmountStr = it },
                        label = { Text("Additional Contribution ($symbol)") },
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
                    onClick = {
                        val addAmt = contributeAmountStr.toDoubleOrNull() ?: 0.0
                        if (addAmt > 0) {
                            onContributeToGoal(contributeGoal!!, addAmt)
                        }
                        contributeGoal = null
                        contributeAmountStr = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    enabled = (contributeAmountStr.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text("Add Funds", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { contributeGoal = null }) { Text("Cancel", color = SlateTextSecondary) }
            }
        )
    }
}

@Composable
fun GoalItemCard(
    goal: GoalEntity,
    currencySymbol: String,
    onContribute: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    val targetDateStr = sdf.format(Date(goal.targetDate))
    val monthsRemaining = (((goal.targetDate - System.currentTimeMillis()) / (1000L * 60 * 60 * 24 * 30))).coerceAtLeast(1)
    val monthlyRequired = goal.remainingAmount / monthsRemaining

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
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                    Text(
                        text = "${goal.category.displayName} • Target: $targetDateStr",
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
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (goal.progressPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (goal.progressPercent >= 100.0) EmeraldProfit else IndigoPrimary,
                trackColor = SlateBorderSubtle
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${FinancialCalculator.formatCurrency(goal.currentAmount, currencySymbol)} of ${FinancialCalculator.formatCurrency(goal.targetAmount, currencySymbol)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                    Text(
                        text = "Req: ${FinancialCalculator.formatCurrency(monthlyRequired, currencySymbol, false)} / mo ($monthsRemaining mos left)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateTextSecondary,
                        fontSize = 10.sp
                    )
                }

                FilledTonalButton(
                    onClick = onContribute,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Contribute", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (GoalEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(GoalCategory.RETIREMENT) }
    var targetAmountStr by remember { mutableStateOf("") }
    var currentAmountStr by remember { mutableStateOf("0") }
    var targetDate by remember { mutableStateOf(System.currentTimeMillis() + (3L * 365 * 24 * 60 * 60 * 1000)) }
    var notes by remember { mutableStateOf("") }

    val context = LocalContext.current
    val targetAmount = targetAmountStr.toDoubleOrNull() ?: 0.0
    val currentAmount = currentAmountStr.toDoubleOrNull() ?: 0.0
    val isValid = name.isNotBlank() && targetAmount > 0

    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Financial Goal", fontWeight = FontWeight.Bold, color = SlateTextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Title *") },
                    placeholder = { Text("e.g. Early Retirement FIRE, House Downpayment") },
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
                        value = targetAmountStr,
                        onValueChange = { targetAmountStr = it },
                        label = { Text("Target Amount ($currencySymbol) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currentAmountStr,
                        onValueChange = { currentAmountStr = it },
                        label = { Text("Saved Already ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Target Date picker
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = targetDate
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val sel = Calendar.getInstance()
                                    sel.set(y, m, d)
                                    targetDate = sel.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = SlateSurface),
                    border = BorderStroke(1.dp, SlateBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Target Date", style = MaterialTheme.typography.labelSmall, color = SlateTextSecondary)
                            Text(sdf.format(Date(targetDate)), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Strategy (Optional)") },
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
                        GoalEntity(
                            name = name.trim(),
                            category = category,
                            targetAmount = targetAmount,
                            currentAmount = currentAmount,
                            targetDate = targetDate,
                            notes = notes.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                enabled = isValid
            ) {
                Text("Create Goal", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SlateTextSecondary) }
        }
    )
}
