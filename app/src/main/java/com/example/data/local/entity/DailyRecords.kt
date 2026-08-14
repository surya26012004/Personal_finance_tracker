package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_holding_records",
    indices = [Index(value = ["holdingId", "dateString"], unique = true)]
)
data class DailyHoldingRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val holdingId: Long,
    val holdingName: String,
    val dateString: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val closingPrice: Double,
    val percentChange: Double,
    val dailyProfitLoss: Double,
    val totalValue: Double
)

@Entity(
    tableName = "daily_portfolio_snapshots",
    indices = [Index(value = ["dateString"], unique = true)]
)
data class DailyPortfolioSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val totalInvested: Double,
    val totalCurrentValue: Double,
    val dailyProfitLoss: Double,
    val dailyPercentChange: Double,
    val totalProfitLoss: Double,
    val totalReturnPercent: Double,
    val xirrEstimate: Double = 0.0
)
