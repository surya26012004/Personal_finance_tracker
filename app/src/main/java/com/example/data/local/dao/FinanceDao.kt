package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CashflowEntity
import com.example.data.local.entity.DailyHoldingRecordEntity
import com.example.data.local.entity.DailyPortfolioSnapshotEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HoldingEntity
import com.example.data.local.entity.JournalEntity
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    // --- Holdings ---
    @Query("SELECT * FROM holdings ORDER BY (quantity * currentPrice) DESC")
    fun getAllHoldings(): Flow<List<HoldingEntity>>

    @Query("SELECT * FROM holdings WHERE id = :id")
    suspend fun getHoldingById(id: Long): HoldingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: HoldingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoldings(holdings: List<HoldingEntity>)

    @Update
    suspend fun updateHolding(holding: HoldingEntity)

    @Delete
    suspend fun deleteHolding(holding: HoldingEntity)

    @Query("DELETE FROM holdings WHERE id = :id")
    suspend fun deleteHoldingById(id: Long)

    // --- Daily Records ---
    @Query("SELECT * FROM daily_holding_records WHERE dateString = :dateString")
    fun getDailyHoldingRecordsByDate(dateString: String): Flow<List<DailyHoldingRecordEntity>>

    @Query("SELECT * FROM daily_holding_records WHERE holdingId = :holdingId ORDER BY dateString ASC")
    fun getHistoryForHolding(holdingId: Long): Flow<List<DailyHoldingRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyHoldingRecords(records: List<DailyHoldingRecordEntity>)

    @Query("SELECT * FROM daily_portfolio_snapshots ORDER BY dateString ASC")
    fun getAllPortfolioSnapshots(): Flow<List<DailyPortfolioSnapshotEntity>>

    @Query("SELECT * FROM daily_portfolio_snapshots ORDER BY dateString DESC LIMIT 1")
    fun getLatestPortfolioSnapshot(): Flow<DailyPortfolioSnapshotEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolioSnapshot(snapshot: DailyPortfolioSnapshotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolioSnapshots(snapshots: List<DailyPortfolioSnapshotEntity>)

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    // --- Cashflows (Income & Expenses) ---
    @Query("SELECT * FROM cashflows ORDER BY date DESC")
    fun getAllCashflows(): Flow<List<CashflowEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashflow(cashflow: CashflowEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashflows(cashflows: List<CashflowEntity>)

    @Delete
    suspend fun deleteCashflow(cashflow: CashflowEntity)

    // --- Goals ---
    @Query("SELECT * FROM financial_goals ORDER BY targetDate ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    // --- Loans / Liabilities ---
    @Query("SELECT * FROM loans ORDER BY outstandingBalance DESC")
    fun getAllLoans(): Flow<List<LoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<LoanEntity>)

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    @Delete
    suspend fun deleteLoan(loan: LoanEntity)

    // --- Watchlist ---
    @Query("SELECT * FROM watchlist ORDER BY addedDate DESC")
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(item: WatchlistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItems(items: List<WatchlistEntity>)

    @Delete
    suspend fun deleteWatchlist(item: WatchlistEntity)

    // --- Journal ---
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntries(entries: List<JournalEntity>)

    @Delete
    suspend fun deleteJournalEntry(entry: JournalEntity)

    // --- App Settings ---
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettingsEntity)

    // --- Batch Wipe for Reset / Import ---
    @Query("DELETE FROM holdings")
    suspend fun clearHoldings()

    @Query("DELETE FROM daily_holding_records")
    suspend fun clearDailyHoldingRecords()

    @Query("DELETE FROM daily_portfolio_snapshots")
    suspend fun clearDailyPortfolioSnapshots()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM cashflows")
    suspend fun clearCashflows()

    @Query("DELETE FROM financial_goals")
    suspend fun clearGoals()

    @Query("DELETE FROM loans")
    suspend fun clearLoans()

    @Query("DELETE FROM watchlist")
    suspend fun clearWatchlist()

    @Query("DELETE FROM journal_entries")
    suspend fun clearJournal()
}
