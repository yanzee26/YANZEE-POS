package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProductEntity
import com.example.data.model.StockMovementEntity
import com.example.data.model.StoreSettingEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE code = :code LIMIT 1")
    suspend fun getProductByCode(code: String): ProductEntity?

    @Query("SELECT * FROM products WHERE stock <= minStock ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("UPDATE products SET stock = :newStock, updatedAt = :timestamp WHERE id = :productId")
    suspend fun updateStock(productId: Long, newStock: Int, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT DISTINCT category FROM products WHERE category IS NOT NULL AND category != '' ORDER BY category ASC")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("DELETE FROM products")
    suspend fun clearAll()
}

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC")
    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transaction_items WHERE transactionId = :txId")
    suspend fun getItemsForTransaction(txId: Long): List<TransactionItemEntity>

    @Query("SELECT * FROM transaction_items WHERE transactionId = :txId")
    fun getItemsForTransactionFlow(txId: Long): Flow<List<TransactionItemEntity>>

    @Query("SELECT * FROM transaction_items")
    fun getAllTransactionItems(): Flow<List<TransactionItemEntity>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("DELETE FROM transaction_items WHERE transactionId = :txId")
    suspend fun deleteItemsForTransaction(txId: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM transaction_items")
    suspend fun clearTransactionItems()
}

@Dao
interface StockMovementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity): Long

    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<StockMovementEntity>>

    @Query("DELETE FROM stock_movements")
    suspend fun clearAll()
}

@Dao
interface SettingDao {
    @Query("SELECT value FROM store_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Query("SELECT * FROM store_settings")
    fun getAllSettings(): Flow<List<StoreSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: StoreSettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAll(settings: List<StoreSettingEntity>)
}
