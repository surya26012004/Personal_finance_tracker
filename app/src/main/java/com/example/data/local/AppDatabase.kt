package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.FinanceDao
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

@Database(
    entities = [
        HoldingEntity::class,
        DailyHoldingRecordEntity::class,
        DailyPortfolioSnapshotEntity::class,
        TransactionEntity::class,
        CashflowEntity::class,
        GoalEntity::class,
        LoanEntity::class,
        WatchlistEntity::class,
        JournalEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wealthwise_finance_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
