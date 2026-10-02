package com.example.ui.cashier

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CartItem
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
import com.example.ui.theme.WarningBg
import com.example.ui.viewmodel.PosViewModel
import kotlinx.coroutines.launch

@Composable
fun CashierScreen(
    viewModel: PosViewModel,
    snackbarHostState: SnackbarHostState,
    onOpenCartPayment: () -> Unit
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val totalAmount by viewModel.cartTotal.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // Filter products
    val filteredProducts = products.filter { product ->
        val matchesCategory = selectedCategory == null || product.category.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.code.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    val totalCartCount = cart.sumOf { it.quantity }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cashier_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (totalCartCount > 0) 80.dp else 0.dp)
        ) {
            // Search and Barcode Bar
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
                            .testTag("cashier_search_input"),
                        placeholder = { Text("Cari nama produk atau kode barcode...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Cari", tint = TextSecondary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus teks")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = SurfaceVariantLight
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Categories Horizontal Chips
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
                                    selectedLabelColor = PureWhite,
                                    containerColor = SurfaceVariantLight,
                                    labelColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        items(categories) { category ->
                            val count = products.count { it.category.equals(category, ignoreCase = true) }
                            FilterChip(
                                selected = selectedCategory.equals(category, ignoreCase = true),
                                onClick = {
                                    selectedCategory = if (selectedCategory.equals(category, ignoreCase = true)) null else category
                                },
                                label = { Text("$category ($count)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = PureWhite,
                                    containerColor = SurfaceVariantLight,
                                    labelColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            // Products Grid
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Produk '$searchQuery' tidak ditemukan" else "Belum ada produk",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Silakan tambah produk baru di tab Produk",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        val cartItem = cart.find { it.product.id == product.id }
                        val inCartQty = cartItem?.quantity ?: 0
                        ProductCashierCard(
                            product = product,
                            inCartQty = inCartQty,
                            onAddToCart = {
                                if (product.stock <= 0) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Stok produk habis!")
                                    }
                                } else if (inCartQty >= product.stock) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Jumlah melebihi stok yang tersedia (${product.stock})")
                                    }
                                } else {
                                    viewModel.addToCart(product)
                                }
                            },
                            onIncrement = {
                                if (inCartQty < product.stock) {
                                    viewModel.updateCartQuantity(product.id, inCartQty + 1)
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Maksimal stok tercapai (${product.stock})")
                                    }
                                }
                            },
                            onDecrement = {
                                viewModel.updateCartQuantity(product.id, inCartQty - 1)
                            }
                        )
                    }
                }
            }
        }

        // Floating Bottom Cart Summary Bar
        if (totalCartCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("floating_cart_bar"),
                color = NavyPrimary,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = WarningAmber,
                                    contentColor = NavyPrimary
                                ) {
                                    Text(
                                        text = "$totalCartCount",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Keranjang",
                                tint = PureWhite,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Total Tagihan",
                                fontSize = 12.sp,
                                color = PureWhite.copy(alpha = 0.7f)
                            )
                            Text(
                                text = Formatters.formatRupiah(totalAmount),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                    }

                    Button(
                        onClick = onOpenCartPayment,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("btn_open_cart_payment"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyAccent,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Bayar Sekarang",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCashierCard(
    product: ProductEntity,
    inCartQty: Int,
    onAddToCart: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val isOutOfStock = product.stock <= 0
    val isLowStock = product.stock in 1..product.minStock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isOutOfStock) { onAddToCart() }
            .testTag("cashier_product_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (inCartQty > 0) InfoBg else PureWhite
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (inCartQty > 0) NavyAccent else SurfaceBorder
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Category & Stock Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.category,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Stock Badge
                val badgeBg = when {
                    isOutOfStock -> DangerRed.copy(alpha = 0.15f)
                    isLowStock -> WarningAmber.copy(alpha = 0.2f)
                    else -> SuccessGreen.copy(alpha = 0.15f)
                }
                val badgeColor = when {
                    isOutOfStock -> DangerRed
                    isLowStock -> WarningAmber
                    else -> SuccessGreen
                }
                val badgeText = when {
                    isOutOfStock -> "Habis"
                    else -> "Sisa ${product.stock}"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Product Name
            Text(
                text = product.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Text(
                text = product.code,
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Selling Price
            Text(
                text = Formatters.formatRupiah(product.sellingPrice),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Add to Cart Button or Qty Stepper
            if (inCartQty > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyPrimary)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Kurang",
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "$inCartQty",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier.size(28.dp),
                        enabled = inCartQty < product.stock
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah",
                            tint = if (inCartQty < product.stock) PureWhite else PureWhite.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onAddToCart,
                    enabled = !isOutOfStock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyPrimary,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOutOfStock) "Habis" else "Tambah",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
