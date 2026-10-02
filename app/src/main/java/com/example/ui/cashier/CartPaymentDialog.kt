package com.example.ui.cashier

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CartItem
import com.example.ui.common.Formatters
import com.example.ui.theme.DangerRed
import com.example.ui.theme.InfoBg
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
import com.example.ui.viewmodel.PosViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CartPaymentDialog(
    viewModel: PosViewModel,
    onDismiss: () -> Unit,
    onCheckoutSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val subtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()
    val discountAmount by viewModel.cartDiscountAmount.collectAsStateWithLifecycle()
    val totalAmount by viewModel.cartTotal.collectAsStateWithLifecycle()
    val cashReceived by viewModel.cashReceived.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()

    var discountInput by remember { mutableStateOf("") }
    var isDiscountPercent by remember { mutableStateOf(false) }
    var showDiscountInput by remember { mutableStateOf(false) }

    var customCashInput by remember {
        mutableStateOf(if (cashReceived > 0) cashReceived.toLong().toString() else "")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cart_payment_dialog"),
            topBar = {
                Surface(
                    color = NavyPrimary,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = PureWhite)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Keranjang & Pembayaran",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Text(
                                text = "${cart.sumOf { it.quantity }} barang dipilih",
                                fontSize = 12.sp,
                                color = PureWhite.copy(alpha = 0.8f)
                            )
                        }
                        if (cart.isNotEmpty()) {
                            TextButton(
                                onClick = { viewModel.clearCart() }
                            ) {
                                Text("Kosongkan", color = DangerRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = PureWhite,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Bayar",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = Formatters.formatRupiah(totalAmount),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }

                            val isTunai = paymentMethod == "Tunai"
                            val isCashValid = !isTunai || cashReceived >= totalAmount

                            Button(
                                onClick = {
                                    viewModel.processCheckout(
                                        onSuccess = {
                                            onCheckoutSuccess()
                                        },
                                        onError = onError
                                    )
                                },
                                enabled = cart.isNotEmpty() && isCashValid,
                                modifier = Modifier
                                    .height(50.dp)
                                    .testTag("btn_complete_transaction"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCashValid) NavyPrimary else TextMuted,
                                    contentColor = PureWhite
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Selesaikan Transaksi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceVariantLight)
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cart Items Section
                item {
                    Text(
                        text = "Daftar Item Belanja",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (cart.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PureWhite)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Keranjang Kosong",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                } else {
                    items(cart, key = { it.product.id }) { item ->
                        CartItemRow(
                            item = item,
                            onIncrement = {
                                viewModel.updateCartQuantity(item.product.id, item.quantity + 1)
                            },
                            onDecrement = {
                                viewModel.updateCartQuantity(item.product.id, item.quantity - 1)
                            },
                            onDelete = {
                                viewModel.removeFromCart(item.product.id)
                            }
                        )
                    }
                }

                // Discount Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Diskon Transaksi",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )

                                TextButton(
                                    onClick = { showDiscountInput = !showDiscountInput }
                                ) {
                                    Text(
                                        text = if (showDiscountInput) "Tutup Diskon" else "+ Tambah Diskon",
                                        color = NavyAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            if (showDiscountInput) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = discountInput,
                                        onValueChange = { input ->
                                            if (input.all { it.isDigit() || it == '.' }) {
                                                discountInput = input
                                                val value = input.toDoubleOrNull() ?: 0.0
                                                viewModel.setDiscount(value, isDiscountPercent)
                                            }
                                        },
                                        placeholder = {
                                            Text(if (isDiscountPercent) "Contoh: 10" else "Contoh: 5000")
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_discount_value"),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    // Toggle Rp or %
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(if (!isDiscountPercent) NavyPrimary else PureWhite)
                                                .clickable {
                                                    isDiscountPercent = false
                                                    val value = discountInput.toDoubleOrNull() ?: 0.0
                                                    viewModel.setDiscount(value, false)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 14.dp)
                                        ) {
                                            Text(
                                                "Rp",
                                                color = if (!isDiscountPercent) PureWhite else TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(if (isDiscountPercent) NavyPrimary else PureWhite)
                                                .clickable {
                                                    isDiscountPercent = true
                                                    val value = discountInput.toDoubleOrNull() ?: 0.0
                                                    viewModel.setDiscount(value, true)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 14.dp)
                                        ) {
                                            Text(
                                                "%",
                                                color = if (isDiscountPercent) PureWhite else TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }

                            if (discountAmount > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Potongan Diskon:", fontSize = 13.sp, color = DangerRed)
                                    Text("- ${Formatters.formatRupiah(discountAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                                }
                            }
                        }
                    }
                }

                // Payment Method Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Metode Pembayaran",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val methods = listOf(
                                    Triple("Tunai", Icons.Default.LocalAtm, "Tunai"),
                                    Triple("QRIS", Icons.Default.QrCode, "QRIS"),
                                    Triple("Transfer", Icons.Default.CreditCard, "Transfer")
                                )

                                for ((label, icon, methodKey) in methods) {
                                    val isSelected = paymentMethod == methodKey
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) NavyPrimary else SurfaceVariantLight)
                                            .clickable {
                                                viewModel.setPaymentMethod(methodKey)
                                                if (methodKey != "Tunai") {
                                                    viewModel.setCashReceived(totalAmount)
                                                }
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = if (isSelected) PureWhite else TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = label,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) PureWhite else TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Cash Calculation (if Tunai)
                if (paymentMethod == "Tunai") {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Uang Diterima",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = customCashInput,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() }) {
                                            customCashInput = input
                                            val amount = input.toDoubleOrNull() ?: 0.0
                                            viewModel.setCashReceived(amount)
                                        }
                                    },
                                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                                    placeholder = { Text("0") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_cash_received"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NavyPrimary,
                                        unfocusedBorderColor = SurfaceBorder
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick Cash Denomination Chips
                                Text(
                                    text = "Pilihan Cepat:",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Uang Pas
                                    FilterChip(
                                        selected = cashReceived == totalAmount,
                                        onClick = {
                                            customCashInput = totalAmount.toLong().toString()
                                            viewModel.setCashReceived(totalAmount)
                                        },
                                        label = { Text("Uang Pas (${Formatters.formatRupiah(totalAmount)})") },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SuccessGreen,
                                            selectedLabelColor = PureWhite
                                        )
                                    )

                                    val denominations = listOf(10000.0, 20000.0, 50000.0, 100000.0, 200000.0)
                                    for (denom in denominations) {
                                        if (denom >= totalAmount) {
                                            FilterChip(
                                                selected = cashReceived == denom,
                                                onClick = {
                                                    customCashInput = denom.toLong().toString()
                                                    viewModel.setCashReceived(denom)
                                                },
                                                label = { Text(Formatters.formatRupiah(denom)) },
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Divider(color = SurfaceBorder)
                                Spacer(modifier = Modifier.height(14.dp))

                                // Kembalian Calculation
                                val change = (cashReceived - totalAmount).coerceAtLeast(0.0)
                                val isLunas = cashReceived >= totalAmount

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Kembalian:",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = Formatters.formatRupiah(change),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLunas) SuccessGreen else DangerRed
                                    )
                                }

                                if (!isLunas && cashReceived > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Uang kurang ${Formatters.formatRupiah(totalAmount - cashReceived)}",
                                        fontSize = 12.sp,
                                        color = DangerRed
                                    )
                                }
                            }
                        }
                    }
                }

                // Summary Breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Rincian Pembayaran",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Divider(color = SurfaceBorder)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal:", color = TextSecondary, fontSize = 13.sp)
                                Text(Formatters.formatRupiah(subtotal), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            if (discountAmount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Diskon:", color = DangerRed, fontSize = 13.sp)
                                    Text("- ${Formatters.formatRupiah(discountAmount)}", color = DangerRed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                            Divider(color = SurfaceBorder)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Tagihan:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
                                Text(Formatters.formatRupiah(totalAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_${item.product.id}"),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${Formatters.formatRupiah(item.product.sellingPrice)} / ${item.product.unit}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Subtotal: ${Formatters.formatRupiah(item.subtotal)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NavyPrimary
                )
            }

            // Stepper controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariantLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Kurang",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = onIncrement,
                    enabled = item.quantity < item.product.stock,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (item.quantity < item.product.stock) SurfaceVariantLight else SurfaceVariantLight.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah",
                        tint = if (item.quantity < item.product.stock) TextPrimary else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = DangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
