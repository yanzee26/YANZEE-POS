package com.example.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.ProductEntity
import com.example.ui.common.Formatters
import com.example.ui.theme.DangerRed
import com.example.ui.theme.InfoBg
import com.example.ui.theme.NavyAccent
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductAddEditDialog(
    initialProduct: ProductEntity? = null,
    existingCategories: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    val isEdit = initialProduct != null

    var code by remember { mutableStateOf(initialProduct?.code ?: "BRG-${Random.nextInt(1000, 9999)}") }
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Sembako") }
    var costPriceStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.costPrice.toLong().toString() else "") }
    var sellingPriceStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.sellingPrice.toLong().toString() else "") }
    var stockStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.stock.toString() else "10") }
    var minStockStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.minStock.toString() else "5") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "pcs") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cost = costPriceStr.toDoubleOrNull() ?: 0.0
    val selling = sellingPriceStr.toDoubleOrNull() ?: 0.0
    val margin = selling - cost

    val defaultCategories = listOf("Sembako", "Makanan", "Minuman", "Kebutuhan Rumah", "Rokok", "Elektronik", "Lainnya")
    val allCategories = (defaultCategories + existingCategories).distinct()

    val defaultUnits = listOf("pcs", "kg", "botol", "bungkus", "sak", "pack", "liter")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("product_add_edit_dialog"),
            color = PureWhite,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEdit) "Ubah Produk" else "Tambah Produk Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NavyPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Barcode / Code
                    item {
                        Text("Kode / Barcode Produk *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = code,
                                onValueChange = { code = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_product_code"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                placeholder = { Text("Kode unik barang") }
                            )
                            OutlinedButton(
                                onClick = { code = "BRG-${Random.nextInt(1000, 9999)}" },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Acak", fontSize = 12.sp)
                            }
                        }
                    }

                    // Product Name
                    item {
                        Text("Nama Produk *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_product_name"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("Contoh: Minyak Goreng 2L") }
                        )
                    }

                    // Category
                    item {
                        Text("Kategori Produk", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_product_category"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("Pilih atau ketik kategori baru") }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allCategories.take(6).forEach { cat ->
                                FilterChip(
                                    selected = category.equals(cat, ignoreCase = true),
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    // Cost Price & Selling Price
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Harga Modal (HPP)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = costPriceStr,
                                    onValueChange = { if (it.all { char -> char.isDigit() }) costPriceStr = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_product_cost_price"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    prefix = { Text("Rp ", fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Harga Jual *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = sellingPriceStr,
                                    onValueChange = { if (it.all { char -> char.isDigit() }) sellingPriceStr = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_product_selling_price"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    prefix = { Text("Rp ", fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        // Margin preview
                        if (selling > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = InfoBg)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Perkiraan Laba per unit:", fontSize = 12.sp, color = NavyPrimary)
                                    Text(
                                        text = "${Formatters.formatRupiah(margin)} (${if (cost > 0) String.format("%.1f%%", (margin / cost) * 100) else "100%"})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (margin >= 0) SuccessGreen else DangerRed
                                    )
                                }
                            }
                        }
                    }

                    // Stock, Min Stock, Unit
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Jumlah Stok", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = stockStr,
                                    onValueChange = { if (it.all { char -> char.isDigit() }) stockStr = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_product_stock"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Batas Min. Stok", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = minStockStr,
                                    onValueChange = { if (it.all { char -> char.isDigit() }) minStockStr = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_product_min_stock"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Satuan Barang", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_product_unit"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                placeholder = { Text("Contoh: pcs") }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            defaultUnits.forEach { u ->
                                FilterChip(
                                    selected = unit.equals(u, ignoreCase = true),
                                    onClick = { unit = u },
                                    label = { Text(u, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        item {
                            Text(
                                text = errorMessage ?: "",
                                color = DangerRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Nama produk wajib diisi!"
                                return@Button
                            }
                            if (selling <= 0) {
                                errorMessage = "Harga jual harus lebih dari 0!"
                                return@Button
                            }

                            val productToSave = ProductEntity(
                                id = initialProduct?.id ?: 0L,
                                code = code.ifBlank { "BRG-${Random.nextInt(1000, 9999)}" },
                                name = name.trim(),
                                category = category.trim().ifBlank { "Umum" },
                                costPrice = cost,
                                sellingPrice = selling,
                                stock = stockStr.toIntOrNull() ?: 0,
                                minStock = minStockStr.toIntOrNull() ?: 5,
                                unit = unit.trim().ifBlank { "pcs" }
                            )
                            onSave(productToSave)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_save_product"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyPrimary,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isEdit) "Simpan Perubahan" else "Tambah Produk", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
