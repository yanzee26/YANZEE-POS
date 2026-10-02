package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ProductDao
import com.example.data.dao.SettingDao
import com.example.data.dao.StockMovementDao
import com.example.data.dao.TransactionDao
import com.example.data.model.ProductEntity
import com.example.data.model.StockMovementEntity
import com.example.data.model.StoreSettingEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity

@Database(
    entities = [
        ProductEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        StockMovementEntity::class,
        StoreSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yanzee_pos.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
