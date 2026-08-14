package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Payment
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
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.LoanType
import com.example.domain.model.FinancialCalculator
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseLoss
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.WealthUiState

@Composable
fun LoansScreen(
    state: WealthUiState,
    onAddLoan: (LoanEntity) -> Unit,
    onRecordEmiPayment: (LoanEntity, Double) -> Unit,
    onDeleteLoan: (LoanEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var payingLoan by remember { mutableStateOf<LoanEntity?>(null) }
    val symbol = state.settings.currencySymbol

    val totalLiabilities = state.loans.sumOf { it.outstandingBalance }
    val totalMonthlyEmis = state.loans.sumOf { it.monthlyEmi }

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
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Loan")
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
                    text = "Loans & Liabilities",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )
                Text(
                    text = "Track outstanding debt, interest rates and monthly EMIs",
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
                        Text(text = "TOTAL OUTSTANDING DEBT", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                        Text(
                            text = FinancialCalculator.formatCurrency(totalLiabilities, symbol),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (totalLiabilities > 0) RoseLoss else SlateTextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "MONTHLY EMI COMMITMENT", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                        Text(
                            text = FinancialCalculator.formatCurrency(totalMonthlyEmis, symbol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Loans List
            if (state.loans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active loans or liabilities recorded. Debt-free!",
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
                    items(state.loans, key = { it.id }) { loan ->
                        LoanItemCard(
                            loan = loan,
                            currencySymbol = symbol,
                            onPayEmi = { payingLoan = loan },
                            onDelete = { onDeleteLoan(loan) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddLoanDialog(
            currencySymbol = symbol,
            onDismiss = { showAddDialog = false },
            onSave = {
                onAddLoan(it)
                showAddDialog = false
            }
        )
    }

    if (payingLoan != null) {
        AlertDialog(
            onDismissRequest = { payingLoan = null },
            title = { Text("Confirm EMI Payment", fontWeight = FontWeight.Bold, color = SlateTextPrimary) },
            text = {
                Text(
                    text = "Record monthly EMI of ${FinancialCalculator.formatCurrency(payingLoan!!.monthlyEmi, symbol)} for ${payingLoan!!.name}? This will reduce outstanding balance and log an expense transaction.",
                    color = SlateTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRecordEmiPayment(payingLoan!!, payingLoan!!.monthlyEmi)
                        payingLoan = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Confirm Payment", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { payingLoan = null }) { Text("Cancel", color = SlateTextSecondary) }
            }
        )
    }
}

@Composable
fun LoanItemCard(
    loan: LoanEntity,
    currencySymbol: String,
    onPayEmi: () -> Unit,
    onDelete: () -> Unit
) {
    val paidAmount = (loan.principalAmount - loan.outstandingBalance).coerceAtLeast(0.0)
    val progress = if (loan.principalAmount > 0) (paidAmount / loan.principalAmount).toFloat().coerceIn(0f, 1f) else 0f

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
                        text = loan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                    Text(
                        text = "${loan.type.displayName} • ${loan.interestRatePercent}% p.a. • ${loan.tenureMonthsRemaining} mos left",
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

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = EmeraldProfit,
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
                        text = "Outstanding: ${FinancialCalculator.formatCurrency(loan.outstandingBalance, currencySymbol)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                    Text(
                        text = "EMI: ${FinancialCalculator.formatCurrency(loan.monthlyEmi, currencySymbol)} / mo",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateTextSecondary
                    )
                }

                FilledTonalButton(
                    onClick = onPayEmi,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Pay EMI", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AddLoanDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (LoanEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(LoanType.HOME_LOAN) }
    var lender by remember { mutableStateOf("") }
    var principalStr by remember { mutableStateOf("") }
    var outstandingStr by remember { mutableStateOf("") }
    var interestRateStr by remember { mutableStateOf("8.5") }
    var emiStr by remember { mutableStateOf("") }
    var tenureStr by remember { mutableStateOf("120") }

    val principal = principalStr.toDoubleOrNull() ?: 0.0
    val outstanding = outstandingStr.toDoubleOrNull() ?: principal
    val interest = interestRateStr.toDoubleOrNull() ?: 0.0
    val emi = emiStr.toDoubleOrNull() ?: 0.0
    val tenure = tenureStr.toIntOrNull() ?: 0

    val isValid = name.isNotBlank() && principal > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Track New Loan / Liability", fontWeight = FontWeight.Bold, color = SlateTextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Loan Name *") },
                    placeholder = { Text("e.g. Home Loan, Car Loan") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = principalStr,
                        onValueChange = {
                            principalStr = it
                            if (outstandingStr.isBlank()) outstandingStr = it
                        },
                        label = { Text("Principal ($currencySymbol) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = outstandingStr,
                        onValueChange = { outstandingStr = it },
                        label = { Text("Outstanding ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = interestRateStr,
                        onValueChange = { interestRateStr = it },
                        label = { Text("Interest Rate %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = emiStr,
                        onValueChange = { emiStr = it },
                        label = { Text("Monthly EMI ($currencySymbol)") },
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
                    value = tenureStr,
                    onValueChange = { tenureStr = it },
                    label = { Text("Months Remaining") },
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
                    onSave(
                        LoanEntity(
                            name = name.trim(),
                            type = type,
                            lender = lender.trim(),
                            principalAmount = principal,
                            outstandingBalance = outstanding,
                            interestRatePercent = interest,
                            monthlyEmi = emi,
                            tenureMonthsRemaining = tenure
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                enabled = isValid
            ) {
                Text("Track Loan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SlateTextSecondary) }
        }
    )
}
