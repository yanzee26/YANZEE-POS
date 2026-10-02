package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.ProductEntity
import com.example.data.model.StockMovementEntity
import com.example.data.model.TopSellingProduct
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionWithItems
import com.example.data.repository.PosRepository
import com.example.ui.common.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

enum class PosTab(val title: String) {
    BERANDA("Beranda"),
    KASIR("Kasir"),
    PRODUK("Produk"),
    STOK("Stok"),
    LAPORAN("Laporan"),
    PENGATURAN("Pengaturan")
}

enum class ReportPeriod(val label: String) {
    HARI_INI("Hari Ini"),
    TUJUH_HARI("7 Hari"),
    BULAN_INI("Bulan Ini"),
    SEMUA("Semua")
}

data class DashboardSummary(
    val todaySales: Double = 0.0,
    val todayTransactionsCount: Int = 0,
    val todayProfit: Double = 0.0,
    val lowStockCount: Int = 0,
    val recentTransactions: List<TransactionEntity> = emptyList()
)

data class ReportSummary(
    val totalRevenue: Double = 0.0,
    val totalCost: Double = 0.0,
    val estimatedProfit: Double = 0.0,
    val transactionCount: Int = 0,
    val averageTicket: Double = 0.0,
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val topSelling: List<TopSellingProduct> = emptyList()
)

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PosRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PosRepository(database)
        viewModelScope.launch {
            repository.seedInitialProductsIfEmpty()
            loadSettings()
        }
    }

    // Navigation Tab
    private val _currentTab = MutableStateFlow(PosTab.BERANDA)
    val currentTab: StateFlow<PosTab> = _currentTab.asStateFlow()

    fun setTab(tab: PosTab) {
        _currentTab.value = tab
    }

    // Products
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions & Movements
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactionItems: StateFlow<List<TransactionItemEntity>> = repository.allTransactionItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStockMovements: StateFlow<List<StockMovementEntity>> = repository.allStockMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart State
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _discountValue = MutableStateFlow(0.0)
    val discountValue: StateFlow<Double> = _discountValue.asStateFlow()

    private val _discountIsPercent = MutableStateFlow(false)
    val discountIsPercent: StateFlow<Boolean> = _discountIsPercent.asStateFlow()

    private val _paymentMethod = MutableStateFlow("Tunai")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _cashReceived = MutableStateFlow(0.0)
    val cashReceived: StateFlow<Double> = _cashReceived.asStateFlow()

    private val _activeReceipt = MutableStateFlow<TransactionWithItems?>(null)
    val activeReceipt: StateFlow<TransactionWithItems?> = _activeReceipt.asStateFlow()

    private val _showReceiptDialog = MutableStateFlow(false)
    val showReceiptDialog: StateFlow<Boolean> = _showReceiptDialog.asStateFlow()

    // Store Settings
    private val _storeName = MutableStateFlow("YANZEE STORE")
    val storeName: StateFlow<String> = _storeName.asStateFlow()

    private val _storeAddress = MutableStateFlow("Jl. Ahmad Yani No. 88, Jakarta")
    val storeAddress: StateFlow<String> = _storeAddress.asStateFlow()

    private val _storePhone = MutableStateFlow("0812-9876-5432")
    val storePhone: StateFlow<String> = _storePhone.asStateFlow()

    private val _cashierName = MutableStateFlow("Kasir Yanzee")
    val cashierName: StateFlow<String> = _cashierName.asStateFlow()

    private val _receiptFooter = MutableStateFlow("Terima kasih telah berbelanja!")
    val receiptFooter: StateFlow<String> = _receiptFooter.asStateFlow()

    // Reports period filter
    private val _reportPeriod = MutableStateFlow(ReportPeriod.HARI_INI)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()

    fun setReportPeriod(period: ReportPeriod) {
        _reportPeriod.value = period
    }

    private suspend fun loadSettings() {
        _storeName.value = repository.getSetting("store_name", "YANZEE STORE")
        _storeAddress.value = repository.getSetting("store_address", "Jl. Ahmad Yani No. 88, Jakarta")
        _storePhone.value = repository.getSetting("store_phone", "0812-9876-5432")
        _cashierName.value = repository.getSetting("cashier_name", "Kasir Yanzee")
        _receiptFooter.value = repository.getSetting("receipt_footer", "Terima kasih telah berbelanja!\nBarang yang sudah dibeli tidak dapat ditukar.")
    }

    fun updateSettings(name: String, address: String, phone: String, cashier: String, footer: String) {
        viewModelScope.launch {
            repository.setSetting("store_name", name)
            repository.setSetting("store_address", address)
            repository.setSetting("store_phone", phone)
            repository.setSetting("cashier_name", cashier)
            repository.setSetting("receipt_footer", footer)
            _storeName.value = name
            _storeAddress.value = address
            _storePhone.value = phone
            _cashierName.value = cashier
            _receiptFooter.value = footer
        }
    }

    // Cart calculations
    val cartSubtotal: StateFlow<Double> = combine(_cart) { items ->
        items[0].sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartDiscountAmount: StateFlow<Double> = combine(_cart, _discountValue, _discountIsPercent) { items, discVal, isPct ->
        val sub = items.sumOf { it.subtotal }
        if (isPct) {
            (sub * (discVal / 100.0)).coerceAtMost(sub)
        } else {
            discVal.coerceAtMost(sub)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTotal: StateFlow<Double> = combine(cartSubtotal, cartDiscountAmount) { sub, disc ->
        (sub - disc).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartChange: StateFlow<Double> = combine(cartTotal, _cashReceived) { total, cash ->
        if (cash >= total) cash - total else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addToCart(product: ProductEntity) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val item = currentList[index]
            if (item.quantity < product.stock) {
                currentList[index] = item.copy(quantity = item.quantity + 1)
                _cart.value = currentList
            }
        } else {
            if (product.stock > 0) {
                currentList.add(CartItem(product = product, quantity = 1))
                _cart.value = currentList
            }
        }
    }

    fun updateCartQuantity(productId: Long, newQuantity: Int) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            val maxStock = currentList[index].product.stock
            if (newQuantity <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = currentList[index].copy(quantity = newQuantity.coerceAtMost(maxStock))
            }
            _cart.value = currentList
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _discountValue.value = 0.0
        _cashReceived.value = 0.0
    }

    fun setDiscount(value: Double, isPercent: Boolean) {
        _discountValue.value = value
        _discountIsPercent.value = isPercent
    }

    fun setCashReceived(amount: Double) {
        _cashReceived.value = amount
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun processCheckout(onSuccess: (TransactionWithItems) -> Unit, onError: (String) -> Unit) {
        val items = _cart.value
        if (items.isEmpty()) {
            onError("Keranjang belanja masih kosong!")
            return
        }

        val total = cartTotal.value
        val cash = _cashReceived.value
        val method = _paymentMethod.value

        if (method == "Tunai" && cash < total) {
            onError("Uang tunai yang diterima kurang dari total belanja!")
            return
        }

        val actualCash = if (method == "Tunai") cash else total
        val change = if (method == "Tunai") (cash - total).coerceAtLeast(0.0) else 0.0

        viewModelScope.launch {
            try {
                val receipt = repository.executeCheckout(
                    cartItems = items,
                    subtotal = cartSubtotal.value,
                    discount = cartDiscountAmount.value,
                    total = total,
                    cashReceived = actualCash,
                    changeAmount = change,
                    cashierName = _cashierName.value,
                    paymentMethod = method,
                    notes = ""
                )
                clearCart()
                _activeReceipt.value = receipt
                _showReceiptDialog.value = true
                onSuccess(receipt)
            } catch (e: Exception) {
                onError(e.message ?: "Gagal memproses transaksi")
            }
        }
    }

    fun showReceipt(receipt: TransactionWithItems) {
        _activeReceipt.value = receipt
        _showReceiptDialog.value = true
    }

    fun dismissReceipt() {
        _showReceiptDialog.value = false
    }

    fun openReceiptForTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            val items = repository.getItemsForTransaction(transaction.id)
            _activeReceipt.value = TransactionWithItems(transaction, items)
            _showReceiptDialog.value = true
        }
    }

    // Product Management
    fun saveProduct(product: ProductEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.saveProduct(product)
            onDone()
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    // Stock Management
    fun adjustStock(productId: Long, type: String, quantity: Int, note: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.adjustStock(productId, type, quantity, note)
            onDone()
        }
    }

    // Delete Transaction
    fun deleteTransaction(txId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(txId)
        }
    }

    // Reset Data
    fun resetDatabase(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.resetDatabase()
            clearCart()
            loadSettings()
            onDone()
        }
    }

    // Sharing / Printing Receipt as formatted text
    fun shareReceipt(context: Context, receipt: TransactionWithItems) {
        val tx = receipt.transaction
        val itemsText = receipt.items.joinToString("\n") { item ->
            String.format(
                "%-18s\n  %2d x %-10s = %s",
                item.productName.take(18),
                item.quantity,
                Formatters.formatRupiah(item.sellingPrice),
                Formatters.formatRupiah(item.subtotal)
            )
        }

        val text = buildString {
            appendLine("================================")
            appendLine("         ${_storeName.value}         ")
            appendLine("   ${_storeAddress.value}   ")
            appendLine("        Telp: ${_storePhone.value}        ")
            appendLine("================================")
            appendLine("No. Struk : ${tx.transactionNumber}")
            appendLine("Waktu     : ${Formatters.formatDateTime(tx.timestamp)}")
            appendLine("Kasir     : ${tx.cashierName}")
            appendLine("Metode    : ${tx.paymentMethod}")
            appendLine("--------------------------------")
            appendLine(itemsText)
            appendLine("--------------------------------")
            appendLine("Subtotal  : ${Formatters.formatRupiah(tx.subtotal)}")
            if (tx.discount > 0) {
                appendLine("Diskon    : -${Formatters.formatRupiah(tx.discount)}")
            }
            appendLine("TOTAL     : ${Formatters.formatRupiah(tx.total)}")
            appendLine("Bayar     : ${Formatters.formatRupiah(tx.cashReceived)}")
            appendLine("Kembali   : ${Formatters.formatRupiah(tx.changeAmount)}")
            appendLine("================================")
            appendLine(_receiptFooter.value)
            appendLine("================================")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Bagikan Struk Transaksi")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    // CSV Export
    fun exportReportCsv(context: Context, transactions: List<TransactionEntity>) {
        val header = "No,Nomor Struk,Tanggal,Waktu,Kasir,Metode,Subtotal,Diskon,Total,Bayar,Kembalian\n"
        val rows = transactions.mapIndexed { idx, tx ->
            val date = Formatters.formatDateShort(tx.timestamp)
            val time = Formatters.formatTimeOnly(tx.timestamp)
            "${idx + 1},${tx.transactionNumber},$date,$time,\"${tx.cashierName}\",${tx.paymentMethod},${tx.subtotal.toLong()},${tx.discount.toLong()},${tx.total.toLong()},${tx.cashReceived.toLong()},${tx.changeAmount.toLong()}"
        }.joinToString("\n")

        val csvContent = header + rows

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Laporan_Penjualan_YanzeePOS.csv")
            putExtra(Intent.EXTRA_TEXT, csvContent)
            type = "text/csv"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Ekspor Laporan Penjualan (CSV)")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    // Backup JSON Export
    fun exportBackupJson(context: Context) {
        viewModelScope.launch {
            val root = JSONObject()
            root.put("app", "YANZEE POS")
            root.put("version", "1.0")
            root.put("backup_date", System.currentTimeMillis())

            val settingsObj = JSONObject().apply {
                put("store_name", _storeName.value)
                put("store_address", _storeAddress.value)
                put("store_phone", _storePhone.value)
                put("cashier_name", _cashierName.value)
                put("receipt_footer", _receiptFooter.value)
            }
            root.put("settings", settingsObj)

            val pList = allProducts.value
            val pArr = JSONArray()
            for (p in pList) {
                val pObj = JSONObject().apply {
                    put("id", p.id)
                    put("code", p.code)
                    put("name", p.name)
                    put("category", p.category)
                    put("costPrice", p.costPrice)
                    put("sellingPrice", p.sellingPrice)
                    put("stock", p.stock)
                    put("minStock", p.minStock)
                    put("unit", p.unit)
                }
                pArr.put(pObj)
            }
            root.put("products", pArr)

            val jsonString = root.toString(2)
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, "Backup_Data_YanzeePOS.json")
                putExtra(Intent.EXTRA_TEXT, jsonString)
                type = "application/json"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Cadangkan Data (Backup)")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        }
    }

    fun restoreBackupJson(jsonString: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val root = JSONObject(jsonString)
                if (root.has("settings")) {
                    val s = root.getJSONObject("settings")
                    updateSettings(
                        s.optString("store_name", _storeName.value),
                        s.optString("store_address", _storeAddress.value),
                        s.optString("store_phone", _storePhone.value),
                        s.optString("cashier_name", _cashierName.value),
                        s.optString("receipt_footer", _receiptFooter.value)
                    )
                }
                if (root.has("products")) {
                    val pArr = root.getJSONArray("products")
                    for (i in 0 until pArr.length()) {
                        val obj = pArr.getJSONObject(i)
                        val p = ProductEntity(
                            code = obj.optString("code", "BRG-${i + 1}"),
                            name = obj.getString("name"),
                            category = obj.optString("category", "Umum"),
                            costPrice = obj.optDouble("costPrice", 0.0),
                            sellingPrice = obj.getDouble("sellingPrice"),
                            stock = obj.getInt("stock"),
                            minStock = obj.optInt("minStock", 5),
                            unit = obj.optString("unit", "pcs")
                        )
                        repository.saveProduct(p)
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Format file backup tidak valid")
            }
        }
    }
}
