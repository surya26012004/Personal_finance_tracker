package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AssetCategory(val displayName: String) {
    STOCK("Stocks / Equity"),
    MUTUAL_FUND("Mutual Funds"),
    ETF("ETFs / Index"),
    GOLD("Gold & Precious Metals"),
    FIXED_DEPOSIT("Fixed Deposits / Debt"),
    REAL_ESTATE("Real Estate"),
    CRYPTO("Crypto / Alternative"),
    CASH("Cash / Bank Balance")
}

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val tickerOrCode: String = "",
    val category: AssetCategory = AssetCategory.STOCK,
    val sector: String = "General", // e.g., IT, Banking, Healthcare, Index, Large Cap, etc.
    val quantity: Double,
    val buyPrice: Double,
    val currentPrice: Double,
    val purchaseDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val lastUpdatedDate: Long = System.currentTimeMillis()
) {
    val investedAmount: Double get() = quantity * buyPrice
    val currentValue: Double get() = quantity * currentPrice
    val totalProfitLoss: Double get() = currentValue - investedAmount
    val returnPercentage: Double get() = if (investedAmount > 0) (totalProfitLoss / investedAmount) * 100.0 else 0.0
}
