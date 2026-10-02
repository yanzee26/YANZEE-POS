package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.database.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.ProductEntity
import com.example.data.model.StockMovementEntity
import com.example.data.model.StoreSettingEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionWithItems
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PosRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val transactionDao = database.transactionDao()
    private val stockMovementDao = database.stockMovementDao()
    private val settingDao = database.settingDao()

    // --- PRODUCTS ---
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    val categories: Flow<List<String>> = productDao.getCategories()

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)
    suspend fun getProductByCode(code: String): ProductEntity? = productDao.getProductByCode(code)

    suspend fun saveProduct(product: ProductEntity): Long {
        return if (product.id == 0L) {
            val id = productDao.insertProduct(product)
            // Log initial stock movement
            if (product.stock > 0) {
                stockMovementDao.insertMovement(
                    StockMovementEntity(
                        productId = id,
                        productName = product.name,
                        type = "MASUK",
                        quantity = product.stock,
                        previousStock = 0,
                        currentStock = product.stock,
                        note = "Stok Awal Produk Baru",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            id
        } else {
            productDao.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
            product.id
        }
    }

    suspend fun deleteProduct(id: Long) {
        productDao.deleteProductById(id)
    }

    // --- STOCK MOVEMENTS ---
    val allStockMovements: Flow<List<StockMovementEntity>> = stockMovementDao.getAllMovements()

    suspend fun adjustStock(
        productId: Long,
        type: String, // "MASUK", "KELUAR", "PENYESUAIAN"
        quantityChange: Int,
        note: String
    ): Boolean {
        return database.withTransaction {
            val product = productDao.getProductById(productId) ?: return@withTransaction false
            val currentStock = product.stock
            val newStock = when (type) {
                "MASUK" -> currentStock + quantityChange
                "KELUAR" -> (currentStock - quantityChange).coerceAtLeast(0)
                "PENYESUAIAN" -> quantityChange.coerceAtLeast(0)
                else -> currentStock
            }

            productDao.updateStock(productId, newStock)
            stockMovementDao.insertMovement(
                StockMovementEntity(
                    productId = productId,
                    productName = product.name,
                    type = type,
                    quantity = if (type == "PENYESUAIAN") kotlin.math.abs(newStock - currentStock) else quantityChange,
                    previousStock = currentStock,
                    currentStock = newStock,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )
            )
            true
        }
    }

    // --- TRANSACTIONS ---
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allTransactionItems: Flow<List<TransactionItemEntity>> = transactionDao.getAllTransactionItems()

    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsBetween(start, end)
    }

    suspend fun getItemsForTransaction(transactionId: Long): List<TransactionItemEntity> {
        return transactionDao.getItemsForTransaction(transactionId)
    }

    suspend fun executeCheckout(
        cartItems: List<CartItem>,
        subtotal: Double,
        discount: Double,
        total: Double,
        cashReceived: Double,
        changeAmount: Double,
        cashierName: String,
        paymentMethod: String,
        notes: String
    ): TransactionWithItems {
        return database.withTransaction {
            val dateStr = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
            val txNumber = "TRX-$dateStr"

            val txEntity = TransactionEntity(
                transactionNumber = txNumber,
                timestamp = System.currentTimeMillis(),
                subtotal = subtotal,
                discount = discount,
                total = total,
                cashReceived = cashReceived,
                changeAmount = changeAmount,
                cashierName = cashierName,
                paymentMethod = paymentMethod,
                notes = notes
            )
            val txId = transactionDao.insertTransaction(txEntity)

            val itemEntities = cartItems.map { cart ->
                TransactionItemEntity(
                    transactionId = txId,
                    productId = cart.product.id,
                    productName = cart.product.name,
                    costPrice = cart.product.costPrice,
                    sellingPrice = cart.product.sellingPrice,
                    quantity = cart.quantity,
                    subtotal = cart.subtotal
                )
            }
            transactionDao.insertTransactionItems(itemEntities)

            // Deduct stock & log stock movements
            for (cart in cartItems) {
                val currentProduct = productDao.getProductById(cart.product.id)
                val prevStock = currentProduct?.stock ?: cart.product.stock
                val newStock = (prevStock - cart.quantity).coerceAtLeast(0)
                productDao.updateStock(cart.product.id, newStock)

                stockMovementDao.insertMovement(
                    StockMovementEntity(
                        productId = cart.product.id,
                        productName = cart.product.name,
                        type = "PENJUALAN",
                        quantity = cart.quantity,
                        previousStock = prevStock,
                        currentStock = newStock,
                        note = "Penjualan No: $txNumber",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            TransactionWithItems(
                transaction = txEntity.copy(id = txId),
                items = itemEntities
            )
        }
    }

    suspend fun deleteTransaction(txId: Long) {
        database.withTransaction {
            transactionDao.deleteItemsForTransaction(txId)
            transactionDao.deleteTransaction(txId)
        }
    }

    // --- STORE SETTINGS ---
    val allSettings: Flow<List<StoreSettingEntity>> = settingDao.getAllSettings()

    suspend fun getSetting(key: String, defaultValue: String): String {
        return settingDao.getSetting(key) ?: defaultValue
    }

    suspend fun setSetting(key: String, value: String) {
        settingDao.setSetting(StoreSettingEntity(key, value))
    }

    // --- BACKUP & RESTORE ---
    suspend fun exportDataAsJson(): String {
        val root = JSONObject()

        // 1. Settings
        val settingsObj = JSONObject()
        settingsObj.put("store_name", getSetting("store_name", "Toko Yanzee"))
        settingsObj.put("store_address", getSetting("store_address", "Jl. Merdeka No. 45, Jakarta"))
        settingsObj.put("store_phone", getSetting("store_phone", "0812-3456-7890"))
        settingsObj.put("cashier_name", getSetting("cashier_name", "Kasir Utama"))
        settingsObj.put("receipt_footer", getSetting("receipt_footer", "Terima kasih telah berbelanja!"))
        root.put("settings", settingsObj)

        // 2. Products
        val products = mutableListOf<ProductEntity>()
        // Read directly
        val pArray = JSONArray()
        database.withTransaction {
            // we can retrieve all via dao
        }
        root.put("version", 1)
        root.put("exported_at", System.currentTimeMillis())
        return root.toString(2)
    }

    suspend fun seedInitialProductsIfEmpty() {
        val count = productDao.getProductCount()
        if (count == 0) {
            val initial = listOf(
                ProductEntity(
                    code = "BRG-001",
                    name = "Minyak Goreng Sania 2L",
                    category = "Sembako",
                    costPrice = 33000.0,
                    sellingPrice = 37000.0,
                    stock = 24,
                    minStock = 5,
                    unit = "pouch"
                ),
                ProductEntity(
                    code = "BRG-002",
                    name = "Beras Rojolele Super 5kg",
                    category = "Sembako",
                    costPrice = 67000.0,
                    sellingPrice = 75000.0,
                    stock = 15,
                    minStock = 5,
                    unit = "sak"
                ),
                ProductEntity(
                    code = "BRG-003",
                    name = "Gula Pasir Gulaku 1kg",
                    category = "Sembako",
                    costPrice = 15500.0,
                    sellingPrice = 18000.0,
                    stock = 30,
                    minStock = 8,
                    unit = "kg"
                ),
                ProductEntity(
                    code = "BRG-004",
                    name = "Indomie Goreng Spesial",
                    category = "Makanan",
                    costPrice = 2800.0,
                    sellingPrice = 3500.0,
                    stock = 80,
                    minStock = 20,
                    unit = "pcs"
                ),
                ProductEntity(
                    code = "BRG-005",
                    name = "Kopi Kapal Api Spesial 165g",
                    category = "Minuman",
                    costPrice = 12500.0,
                    sellingPrice = 15000.0,
                    stock = 4, // low stock
                    minStock = 6,
                    unit = "bungkus"
                ),
                ProductEntity(
                    code = "BRG-006",
                    name = "Aqua Botol 600ml",
                    category = "Minuman",
                    costPrice = 3000.0,
                    sellingPrice = 4000.0,
                    stock = 36,
                    minStock = 12,
                    unit = "botol"
                ),
                ProductEntity(
                    code = "BRG-007",
                    name = "Telur Ayam Negeri 1kg",
                    category = "Sembako",
                    costPrice = 26000.0,
                    sellingPrice = 30000.0,
                    stock = 3, // low stock
                    minStock = 5,
                    unit = "kg"
                ),
                ProductEntity(
                    code = "BRG-008",
                    name = "Sabun Mandi Lifebuoy 85g",
                    category = "Kebutuhan Rumah",
                    costPrice = 3800.0,
                    sellingPrice = 5000.0,
                    stock = 18,
                    minStock = 6,
                    unit = "pcs"
                )
            )
            for (item in initial) {
                saveProduct(item)
            }

            // Seed default store settings
            setSetting("store_name", "YANZEE STORE")
            setSetting("store_address", "Jl. Ahmad Yani No. 88, Jakarta")
            setSetting("store_phone", "0812-9876-5432")
            setSetting("cashier_name", "Kasir Yanzee")
            setSetting("receipt_footer", "Terima kasih telah berbelanja!\nBarang yang sudah dibeli tidak dapat ditukar.")
        }
    }

    suspend fun resetDatabase() {
        database.withTransaction {
            transactionDao.clearTransactionItems()
            transactionDao.clearTransactions()
            stockMovementDao.clearAll()
            productDao.clearAll()
        }
        seedInitialProductsIfEmpty()
    }
}
