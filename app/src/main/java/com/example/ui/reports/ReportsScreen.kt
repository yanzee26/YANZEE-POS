package com.example.ui.reports

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TopSellingProduct
import com.example.data.model.TransactionEntity
import com.example.ui.common.Formatters
import com.example.ui.theme.DangerBg
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
import com.example.ui.viewmodel.ReportPeriod
import java.util.Calendar

@Composable
fun ReportsScreen(
    viewModel: PosViewModel
) {
    val context = LocalContext.current
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allTransactionItems by viewModel.allTransactionItems.collectAsStateWithLifecycle()
    val period by viewModel.reportPeriod.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Ringkasan & Terlaris, 1: Riwayat Transaksi
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    // Filter transactions based on period
    val now = Calendar.getInstance()
    val startTimestamp = when (period) {
        ReportPeriod.HARI_INI -> {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        ReportPeriod.TUJUH_HARI -> {
            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -7)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }.timeInMillis
        }
        ReportPeriod.BULAN_INI -> {
            Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }.timeInMillis
        }
        ReportPeriod.SEMUA -> 0L
    }

    val filteredTransactions = allTransactions.filter { it.timestamp >= startTimestamp }
    val filteredTxIds = filteredTransactions.map { it.id }.toSet()
    val filteredItems = allTransactionItems.filter { it.transactionId in filteredTxIds }

    // Metrics calculations
    val totalRevenue = filteredTransactions.sumOf { it.total }
    val totalDiscounts = filteredTransactions.sumOf { it.discount }
    val totalCost = filteredItems.sumOf { it.costPrice * it.quantity }
    val estimatedProfit = (totalRevenue - totalCost).coerceAtLeast(0.0)
    val transactionCount = filteredTransactions.size
    val averageTicket = if (transactionCount > 0) totalRevenue / transactionCount else 0.0

    // Top Selling products calculation
    val topSellingList = filteredItems
        .groupBy { it.productId }
        .map { (prodId, items) ->
            TopSellingProduct(
                productId = prodId,
                productName = items.firstOrNull()?.productName ?: "Produk",
                totalQtySold = items.sumOf { it.quantity },
                totalRevenue = items.sumOf { it.subtotal }
            )
        }
        .sortedByDescending { it.totalQtySold }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen")
    ) {
        // Period Filter Bar
        Surface(
            color = PureWhite,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Periode Laporan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Export CSV Button
                    OutlinedButton(
                        onClick = { viewModel.exportReportCsv(context, filteredTransactions) },
                        enabled = filteredTransactions.isNotEmpty(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp).testTag("btn_export_csv"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ekspor CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (p in ReportPeriod.values()) {
                        FilterChip(
                            selected = period == p,
                            onClick = { viewModel.setReportPeriod(p) },
                            label = { Text(p.label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
            }
        }

        // Subtabs: Ringkasan vs Riwayat Transaksi
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = PureWhite,
            contentColor = NavyPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Ringkasan & Terlaris", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Riwayat Transaksi (${filteredTransactions.size})", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Key Financial Metrics
                item {
                    Text(
                        text = "Ringkasan Finansial (${period.label})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReportMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Pendapatan",
                            value = Formatters.formatRupiah(totalRevenue),
                            subtitle = "$transactionCount Transaksi",
                            valueColor = NavyPrimary,
                            bgColor = InfoBg
                        )

                        ReportMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Estimasi Laba Bersih",
                            value = Formatters.formatRupiah(estimatedProfit),
                            subtitle = "Laba dari penjualan",
                            valueColor = SuccessGreen,
                            bgColor = SuccessBg
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReportMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Modal (HPP)",
                            value = Formatters.formatRupiah(totalCost),
                            subtitle = "Harga Pokok Penjualan",
                            valueColor = TextSecondary,
                            bgColor = SurfaceVariantLight
                        )

                        ReportMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Rata-rata / Belanja",
                            value = Formatters.formatRupiah(averageTicket),
                            subtitle = "Nilai per transaksi",
                            valueColor = NavyAccent,
                            bgColor = InfoBg
                        )
                    }
                }

                // Top Selling Products Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Produk Terlaris (${period.label})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (topSellingList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PureWhite)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada produk terjual pada periode ini.",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(topSellingList.take(10)) { index, item ->
                        TopSellingRow(index = index + 1, item = item)
                    }
                }
            }
        } else {
            // Transaction History Tab
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak Ada Transaksi di Periode Ini",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openReceiptForTransaction(tx) }
                                .testTag("report_tx_${tx.id}"),
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.transactionNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${Formatters.formatDateTime(tx.timestamp)} • ${tx.paymentMethod}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "Kasir: ${tx.cashierName}",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = Formatters.formatRupiah(tx.total),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = "Lihat Struk",
                                            fontSize = 11.sp,
                                            color = NavyAccent,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = { txToDelete = tx },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Transaksi",
                                            tint = DangerRed.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Transaction confirmation
    txToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("Hapus Transaksi?") },
            text = { Text("Apakah Anda yakin ingin menghapus data transaksi ${tx.transactionNumber}? Catatan penjualan akan dihapus dari laporan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(tx.id)
                        txToDelete = null
                    }
                ) {
                    Text("Hapus", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ReportMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun TopSellingRow(
    index: Int,
    item: TopSellingProduct
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("top_selling_${item.productId}"),
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
            val rankBg = when (index) {
                1 -> Color(0xFFFFD700)
                2 -> Color(0xFFC0C0C0)
                3 -> Color(0xFFCD7F32)
                else -> SurfaceVariantLight
            }
            val rankTextCol = if (index <= 3) NavyPrimary else TextSecondary

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(rankBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = rankTextCol
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Total Omzet: ${Formatters.formatRupiah(item.totalRevenue)}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(InfoBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${item.totalQtySold} Terjual",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyAccent
                )
            }
        }
    }
}
