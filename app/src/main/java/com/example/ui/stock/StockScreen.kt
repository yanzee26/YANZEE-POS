package com.example.ui.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ProductEntity
import com.example.data.model.StockMovementEntity
import com.example.ui.common.Formatters
import com.example.ui.theme.DangerBg
import com.example.ui.theme.DangerRed
import com.example.ui.theme.InfoBg
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyAccent
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessBg
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningBg
import com.example.ui.viewmodel.PosViewModel

@Composable
fun StockScreen(
    viewModel: PosViewModel
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val stockMovements by viewModel.allStockMovements.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var productForAdjustment by remember { mutableStateOf<ProductEntity?>(null) }
    var adjustType by remember { mutableStateOf("MASUK") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("stock_screen")
    ) {
        // Tab Selector
        Surface(
            color = PureWhite,
            shadowElevation = 2.dp
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = PureWhite,
                contentColor = NavyPrimary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Daftar & Peringatan", fontWeight = FontWeight.Bold)
                            if (lowStockProducts.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(DangerRed)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${lowStockProducts.size}",
                                        color = PureWhite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Riwayat Mutasi Stok", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedTabIndex == 0) {
            // Stock List & Low Stock Alerts
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Low Stock Alert Banner
                if (lowStockProducts.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = WarningBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(WarningAmber.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Perhatian: ${lowStockProducts.size} Produk Perlu Ditambah!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Stok berada pada atau di bawah batas minimum.",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Kelola Stok Barang (${products.size} Produk)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(products, key = { it.id }) { product ->
                    StockProductCard(
                        product = product,
                        onAddStock = {
                            productForAdjustment = product
                            adjustType = "MASUK"
                        },
                        onReduceStock = {
                            productForAdjustment = product
                            adjustType = "KELUAR"
                        },
                        onAdjust = {
                            productForAdjustment = product
                            adjustType = "PENYESUAIAN"
                        }
                    )
                }
            }
        } else {
            // Stock Movement History
            if (stockMovements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Belum Ada Riwayat Perubahan Stok",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Perubahan stok masuk, keluar, dan penjualan akan tercatat otomatis di sini.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(stockMovements, key = { it.id }) { movement ->
                        StockMovementItem(movement = movement)
                    }
                }
            }
        }
    }

    // Stock Adjustment Dialog
    productForAdjustment?.let { product ->
        StockAdjustDialog(
            product = product,
            initialType = adjustType,
            onDismiss = { productForAdjustment = null },
            onConfirm = { type, qty, note ->
                viewModel.adjustStock(
                    productId = product.id,
                    type = type,
                    quantity = qty,
                    note = note,
                    onDone = {
                        productForAdjustment = null
                    }
                )
            }
        )
    }
}

@Composable
fun StockProductCard(
    product: ProductEntity,
    onAddStock: () -> Unit,
    onReduceStock: () -> Unit,
    onAdjust: () -> Unit
) {
    val isOutOfStock = product.stock <= 0
    val isLowStock = product.stock in 1..product.minStock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stock_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${product.category} • Kode: ${product.code}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Current stock badge
                val (bgCol, textCol, label) = when {
                    isOutOfStock -> Triple(DangerBg, DangerRed, "Habis (0 ${product.unit})")
                    isLowStock -> Triple(WarningBg, WarningAmber, "Kritis (${product.stock} ${product.unit})")
                    else -> Triple(SuccessBg, SuccessGreen, "${product.stock} ${product.unit}")
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgCol)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textCol
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Batas Minimum: ${product.minStock} ${product.unit}",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onAddStock,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Masuk", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onReduceStock,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Keluar", fontSize = 11.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onAdjust,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = NavyLight, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Koreksi", fontSize = 11.sp, color = NavyLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StockMovementItem(movement: StockMovementEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("movement_${movement.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, tint, bg) = when (movement.type) {
                "MASUK" -> Triple(Icons.Default.ArrowUpward, SuccessGreen, SuccessBg)
                "KELUAR" -> Triple(Icons.Default.ArrowDownward, DangerRed, DangerBg)
                "PENJUALAN" -> Triple(Icons.Default.Inventory, NavyAccent, InfoBg)
                else -> Triple(Icons.Default.Tune, WarningAmber, WarningBg)
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movement.productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${Formatters.formatDateTime(movement.timestamp)} • ${movement.note.ifBlank { movement.type }}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val prefix = when (movement.type) {
                    "MASUK" -> "+"
                    "KELUAR", "PENJUALAN" -> "-"
                    else -> "±"
                }
                Text(
                    text = "$prefix${movement.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = tint
                )
                Text(
                    text = "${movement.previousStock} -> ${movement.currentStock}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun StockAdjustDialog(
    product: ProductEntity,
    initialType: String,
    onDismiss: () -> Unit,
    onConfirm: (type: String, quantity: Int, note: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var qtyStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("stock_adjust_dialog"),
            color = PureWhite,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kelola Stok",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NavyPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Stok saat ini: ${product.stock} ${product.unit}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Type chips
                Text("Jenis Perubahan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf(
                        "MASUK" to "Stok Masuk",
                        "KELUAR" to "Stok Keluar",
                        "PENYESUAIAN" to "Opname Fisik"
                    )
                    for ((key, label) in types) {
                        val isSelected = type == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NavyPrimary else SurfaceVariantLight)
                                .clickable { type = key }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PureWhite else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val qtyLabel = if (type == "PENYESUAIAN") "Jumlah Stok Fisik Riil (${product.unit})" else "Jumlah (${product.unit})"
                Text(qtyLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = { if (it.all { char -> char.isDigit() }) qtyStr = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_stock_qty"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Masukkan angka") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Catatan / Keterangan (Opsional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_stock_note"),
                    placeholder = {
                        Text(
                            when (type) {
                                "MASUK" -> "Contoh: Kulakan dari Agen Jaya"
                                "KELUAR" -> "Contoh: Rusak / Kadaluarsa"
                                else -> "Contoh: Stok opname akhir minggu"
                            }
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMsg ?: "", color = DangerRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            val qty = qtyStr.toIntOrNull()
                            if (qty == null || qty <= 0) {
                                errorMsg = "Jumlah harus lebih besar dari 0!"
                                return@Button
                            }
                            if (type == "KELUAR" && qty > product.stock) {
                                errorMsg = "Jumlah keluar melebihi stok yang ada (${product.stock})!"
                                return@Button
                            }
                            onConfirm(type, qty, note.ifBlank { "Penyesuaian stok $type" })
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_confirm_stock_adjust"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyPrimary,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
