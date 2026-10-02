package com.example.ui.cashier

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionWithItems
import com.example.ui.common.Formatters
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
import com.example.ui.viewmodel.PosViewModel

@Composable
fun ReceiptDialog(
    viewModel: PosViewModel,
    receipt: TransactionWithItems,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val storeName by viewModel.storeName.collectAsStateWithLifecycle()
    val storeAddress by viewModel.storeAddress.collectAsStateWithLifecycle()
    val storePhone by viewModel.storePhone.collectAsStateWithLifecycle()
    val receiptFooter by viewModel.receiptFooter.collectAsStateWithLifecycle()

    val tx = receipt.transaction

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("receipt_dialog"),
            color = PureWhite,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Success Badge and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Transaksi Berhasil",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SuccessGreen
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Printable Receipt Canvas
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder)
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Store Info
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = storeName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = storeAddress,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Telp: $storePhone",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "------------------------------------------",
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Transaction Meta
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("No. Struk :", fontSize = 11.sp, color = TextSecondary)
                                Text(tx.transactionNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Waktu     :", fontSize = 11.sp, color = TextSecondary)
                                Text(Formatters.formatDateTime(tx.timestamp), fontSize = 11.sp, color = TextPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Kasir     :", fontSize = 11.sp, color = TextSecondary)
                                Text(tx.cashierName, fontSize = 11.sp, color = TextPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Metode    :", fontSize = 11.sp, color = TextSecondary)
                                Text(tx.paymentMethod, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            Text(
                                text = "------------------------------------------",
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        // Items
                        items(receipt.items) { item ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text(
                                    text = item.productName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "  ${item.quantity} x ${Formatters.formatRupiah(item.sellingPrice)}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = Formatters.formatRupiah(item.subtotal),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        // Total Calculation
                        item {
                            Text(
                                text = "------------------------------------------",
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", fontSize = 12.sp, color = TextSecondary)
                                Text(Formatters.formatRupiah(tx.subtotal), fontSize = 12.sp, color = TextPrimary)
                            }

                            if (tx.discount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Diskon", fontSize = 12.sp, color = TextSecondary)
                                    Text("- ${Formatters.formatRupiah(tx.discount)}", fontSize = 12.sp, color = TextPrimary)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TOTAL", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                Text(Formatters.formatRupiah(tx.total), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Bayar (${tx.paymentMethod})", fontSize = 12.sp, color = TextSecondary)
                                Text(Formatters.formatRupiah(tx.cashReceived), fontSize = 12.sp, color = TextPrimary)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Kembalian", fontSize = 12.sp, color = TextSecondary)
                                Text(Formatters.formatRupiah(tx.changeAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }

                            Text(
                                text = "------------------------------------------",
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted,
                                fontSize = 12.sp
                            )

                            // Footer Note
                            Text(
                                text = receiptFooter,
                                fontSize = 10.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Share / Print / Done
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.shareReceipt(context, receipt)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_share_receipt"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan Struk", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Mencetak ke printer thermal Bluetooth...", Toast.LENGTH_SHORT).show()
                            viewModel.shareReceipt(context, receipt)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_print_receipt"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak Struk", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_done_receipt"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyPrimary,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Transaksi Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
