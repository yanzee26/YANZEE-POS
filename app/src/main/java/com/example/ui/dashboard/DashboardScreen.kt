package com.example.ui.dashboard

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.ui.common.Formatters
import com.example.ui.theme.DangerRed
import com.example.ui.theme.InfoBg
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyAccent
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessBg
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningBg
import com.example.ui.viewmodel.PosTab
import com.example.ui.viewmodel.PosViewModel
import java.util.Calendar

@Composable
fun DashboardScreen(
    viewModel: PosViewModel,
    onNavigateTab: (PosTab) -> Unit,
    onAddNewProduct: () -> Unit
) {
    val storeName by viewModel.storeName.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val allTransactionItems by viewModel.allTransactionItems.collectAsStateWithLifecycle()

    // Calculate today's metrics
    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val todayTransactions = transactions.filter { it.timestamp >= startOfToday }
    val todaySales = todayTransactions.sumOf { it.total }
    val todayTxCount = todayTransactions.size

    // Calculate today's profit: today items sellingPrice*qty - costPrice*qty - tx.discount
    val todayTxIds = todayTransactions.map { it.id }.toSet()
    val todayItems = allTransactionItems.filter { it.transactionId in todayTxIds }
    val todayCost = todayItems.sumOf { it.costPrice * it.quantity }
    val todayGrossRevenue = todayTransactions.sumOf { it.subtotal }
    val todayDiscounts = todayTransactions.sumOf { it.discount }
    val todayProfit = (todayGrossRevenue - todayDiscounts - todayCost).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Store Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("store_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NavyPrimary,
                    contentColor = PureWhite
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(NavyLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = "Store Logo",
                            tint = PureWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = storeName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Text(
                            text = Formatters.formatDateOnly(System.currentTimeMillis()),
                            fontSize = 13.sp,
                            color = PureWhite.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Summary Statistics Grid
        item {
            Text(
                text = "Ringkasan Hari Ini",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Penjualan",
                    value = Formatters.formatRupiah(todaySales),
                    subtitle = "$todayTxCount Transaksi",
                    icon = Icons.Default.PointOfSale,
                    accentColor = NavyAccent,
                    bgColor = InfoBg
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Estimasi Laba",
                    value = Formatters.formatRupiah(todayProfit),
                    subtitle = "Laba Bersih Hari Ini",
                    icon = Icons.Default.TrendingUp,
                    accentColor = SuccessGreen,
                    bgColor = SuccessBg
                )
            }
        }

        // Low Stock Alert Banner (if any)
        if (lowStockProducts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateTab(PosTab.STOK) }
                        .testTag("low_stock_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = WarningBg
                    ),
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(WarningAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Peringatan",
                                tint = WarningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Peringatan Stok Menipis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "${lowStockProducts.size} produk berada di bawah batas minimum stok.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Buka Stok",
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Text(
                text = "Aksi Cepat",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ElevatedButton(
                    onClick = { onNavigateTab(PosTab.KASIR) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_quick_cashier"),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = NavyPrimary,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka Kasir", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = onAddNewProduct,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_quick_add_product"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Produk", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Secondary Quick Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(PosTab.STOK) }
                        .testTag("btn_quick_stock"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = NavyLight, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kelola Stok", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                }

                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(PosTab.LAPORAN) }
                        .testTag("btn_quick_report"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = NavyLight, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lihat Laporan", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                }
            }
        }

        // Recent Transactions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaksi Terakhir",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (transactions.isNotEmpty()) {
                    Text(
                        text = "Lihat Semua",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyAccent,
                        modifier = Modifier
                            .clickable { onNavigateTab(PosTab.LAPORAN) }
                            .padding(4.dp)
                    )
                }
            }
        }

        if (todayTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum Ada Transaksi Hari Ini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Gunakan tab Kasir untuk mulai mencatat penjualan baru.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(todayTransactions.take(5), key = { it.id }) { tx ->
                TransactionListItem(
                    transaction = tx,
                    onClick = { viewModel.openReceiptForTransaction(tx) }
                )
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tx_item_${transaction.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(InfoBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = NavyLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.transactionNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${Formatters.formatTimeOnly(transaction.timestamp)} • ${transaction.paymentMethod}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatRupiah(transaction.total),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SuccessGreen
                )
                Text(
                    text = "Lihat Struk",
                    fontSize = 11.sp,
                    color = NavyAccent,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
