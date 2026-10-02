package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.DangerBg
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
import com.example.ui.viewmodel.PosViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: PosViewModel,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentStoreName by viewModel.storeName.collectAsStateWithLifecycle()
    val currentStoreAddress by viewModel.storeAddress.collectAsStateWithLifecycle()
    val currentStorePhone by viewModel.storePhone.collectAsStateWithLifecycle()
    val currentCashierName by viewModel.cashierName.collectAsStateWithLifecycle()
    val currentReceiptFooter by viewModel.receiptFooter.collectAsStateWithLifecycle()

    var name by remember(currentStoreName) { mutableStateOf(currentStoreName) }
    var address by remember(currentStoreAddress) { mutableStateOf(currentStoreAddress) }
    var phone by remember(currentStorePhone) { mutableStateOf(currentStorePhone) }
    var cashier by remember(currentCashierName) { mutableStateOf(currentCashierName) }
    var footer by remember(currentReceiptFooter) { mutableStateOf(currentReceiptFooter) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Profil Toko & Struk
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(InfoBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = NavyLight, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Profil Toko & Struk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Nama Toko *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_setting_store_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Alamat Toko", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_setting_store_address"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Nomor Telepon / WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_setting_store_phone"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Nama Kasir Default", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = cashier,
                        onValueChange = { cashier = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_setting_cashier_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Pesan Catatan Kaki Struk", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = footer,
                        onValueChange = { footer = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_setting_receipt_footer"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.updateSettings(
                                name = name.trim().ifBlank { "YANZEE STORE" },
                                address = address.trim(),
                                phone = phone.trim(),
                                cashier = cashier.trim().ifBlank { "Kasir" },
                                footer = footer.trim()
                            )
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Pengaturan toko berhasil disimpan!")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_save_settings"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyPrimary,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Pengaturan Toko", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Mata Uang & Format
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pengaturan Mata Uang",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Format Mata Uang", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text("Rupiah Indonesia (Rp)", fontSize = 12.sp, color = TextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(InfoBg)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Rp (Aktif)", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section: Backup & Restore
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cadangkan & Pulihkan Data",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Amankan data produk dan pengaturan Anda ke file JSON atau bagikan ke media penyimpanan lain.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.exportBackupJson(context) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_backup_data"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cadangkan Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_restore_data"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pulihkan Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Reset Data
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DangerBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Reset Database & Isi Ulang Contoh",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DangerRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Menghapus semua riwayat transaksi saat ini dan memuat ulang produk awal yang bersih.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = PureWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("btn_reset_database"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset & Isi Ulang Produk", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Section: App Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YANZEE POS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Versi 1.0 • Aplikasi Kasir Modern & Ringan",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Penyimpanan Lokal Offline (SQLite / Room Database)",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }

    // Reset Database Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed) },
            title = { Text("Konfirmasi Reset Database", fontWeight = FontWeight.Bold) },
            text = {
                Text("Semua riwayat transaksi dan mutasi stok akan dihapus. Daftar produk akan diatur ulang ke produk standar awal. Lanjutkan?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetDatabase {
                            showResetDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Database berhasil direset!")
                            }
                        }
                    }
                ) {
                    Text("Ya, Reset Data", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        Dialog(
            onDismissRequest = { showRestoreDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("restore_data_dialog"),
                color = PureWhite,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Pulihkan Data dari JSON",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tempel teks JSON dari file cadangan Anda di bawah ini:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("input_restore_json"),
                        placeholder = { Text("{\"app\": \"YANZEE POS\", ...}") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRestoreDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal")
                        }

                        Button(
                            onClick = {
                                if (restoreJsonText.isBlank()) {
                                    Toast.makeText(context, "Teks JSON tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.restoreBackupJson(
                                    jsonString = restoreJsonText,
                                    onSuccess = {
                                        showRestoreDialog = false
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Data berhasil dipulihkan!")
                                        }
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavyPrimary,
                                contentColor = PureWhite
                            )
                        ) {
                            Text("Pulihkan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
