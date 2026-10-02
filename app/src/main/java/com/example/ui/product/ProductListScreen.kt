package com.example.ui.product

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.PosViewModel

@Composable
fun ProductListScreen(
    viewModel: PosViewModel,
    onAddProductClick: () -> Unit,
    onEditProductClick: (ProductEntity) -> Unit
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    val filteredProducts = products.filter { p ->
        val matchCat = selectedCategory == null || p.category.equals(selectedCategory, ignoreCase = true)
        val matchQuery = searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.code.contains(searchQuery, ignoreCase = true)
        matchCat && matchQuery
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("product_list_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProductClick,
                containerColor = NavyPrimary,
                contentColor = PureWhite,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_product")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search & Category Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PureWhite,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_search_input"),
                        placeholder = { Text("Cari produk atau kode...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Cari", tint = TextSecondary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("Semua (${products.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = PureWhite
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        items(categories) { cat ->
                            val count = products.count { it.category.equals(cat, ignoreCase = true) }
                            FilterChip(
                                selected = selectedCategory.equals(cat, ignoreCase = true),
                                onClick = {
                                    selectedCategory = if (selectedCategory.equals(cat, ignoreCase = true)) null else cat
                                },
                                label = { Text("$cat ($count)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = PureWhite
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            // Products List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Produk '$searchQuery' tidak ditemukan" else "Daftar Produk Masih Kosong",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tekan tombol '+' untuk menambahkan produk pertama",
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
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            onEdit = { onEditProductClick(product) },
                            onDelete = { productToDelete = product }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Hapus Produk?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus produk '${prod.name}'? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(prod.id)
                        productToDelete = null
                    }
                ) {
                    Text("Hapus", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isOutOfStock = product.stock <= 0
    val isLowStock = product.stock in 1..product.minStock
    val margin = product.sellingPrice - product.costPrice

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_item_${product.id}"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceVariantLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = product.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyLight
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kode: ${product.code}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Action buttons
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyLight, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = SurfaceBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Pricing and Stock Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Harga Jual: ${Formatters.formatRupiah(product.sellingPrice)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Modal: ${Formatters.formatRupiah(product.costPrice)} • Laba: +${Formatters.formatRupiah(margin)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Stock pill
                val (pillBg, pillColor, pillText) = when {
                    isOutOfStock -> Triple(DangerRed.copy(alpha = 0.15f), DangerRed, "Stok Habis")
                    isLowStock -> Triple(WarningAmber.copy(alpha = 0.2f), WarningAmber, "Sisa ${product.stock} ${product.unit}")
                    else -> Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, "Stok: ${product.stock} ${product.unit}")
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(pillBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = pillText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = pillColor
                    )
                }
            }
        }
    }
}
