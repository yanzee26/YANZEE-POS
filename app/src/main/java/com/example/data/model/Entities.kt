package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val name: String,
    val category: String = "Umum",
    val costPrice: Double,
    val sellingPrice: Double,
    val stock: Int,
    val minStock: Int = 5,
    val unit: String = "pcs",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val subtotal: Double,
    val discount: Double = 0.0,
    val total: Double,
    val cashReceived: Double,
    val changeAmount: Double,
    val cashierName: String = "Kasir 1",
    val paymentMethod: String = "Tunai",
    val notes: String = ""
)

@Entity(tableName = "transaction_items")
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val productId: Long,
    val productName: String,
    val costPrice: Double,
    val sellingPrice: Double,
    val quantity: Int,
    val subtotal: Double
)

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: String, // "MASUK", "KELUAR", "PENJUALAN", "PENYESUAIAN"
    val quantity: Int,
    val previousStock: Int,
    val currentStock: Int,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "store_settings")
data class StoreSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

data class TransactionWithItems(
    val transaction: TransactionEntity,
    val items: List<TransactionItemEntity>
)

data class CartItem(
    val product: ProductEntity,
    var quantity: Int
) {
    val subtotal: Double
        get() = product.sellingPrice * quantity

    val totalCost: Double
        get() = product.costPrice * quantity
}

data class TopSellingProduct(
    val productId: Long,
    val productName: String,
    val totalQtySold: Int,
    val totalRevenue: Double
)
