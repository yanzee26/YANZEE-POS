package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ProductEntity
import com.example.ui.cashier.CartPaymentDialog
import com.example.ui.cashier.CashierScreen
import com.example.ui.cashier.ReceiptDialog
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.product.ProductAddEditDialog
import com.example.ui.product.ProductListScreen
import com.example.ui.reports.ReportsScreen
import com.example.ui.stock.StockScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyAccent
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.PosTab
import com.example.ui.viewmodel.PosViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                YanzeePosApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YanzeePosApp(
    viewModel: PosViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val storeName by viewModel.storeName.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val showReceiptDialog by viewModel.showReceiptDialog.collectAsStateWithLifecycle()
    val activeReceipt by viewModel.activeReceipt.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var showProductAddEditDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var showCartPaymentDialog by remember { mutableStateOf(false) }

    // Intercept hardware back button to navigate to Dashboard first
    BackHandler(enabled = currentTab != PosTab.BERANDA) {
        viewModel.setTab(PosTab.BERANDA)
    }

    val cartItemCount = cart.sumOf { it.quantity }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "YANZEE POS",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = PureWhite,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(NavyAccent)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OFFLINE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        }
                        Text(
                            text = when (currentTab) {
                                PosTab.BERANDA -> storeName
                                PosTab.KASIR -> "Kasir Penjualan"
                                PosTab.PRODUK -> "Manajemen Produk"
                                PosTab.STOK -> "Manajemen Stok"
                                PosTab.LAPORAN -> "Laporan & Riwayat"
                                PosTab.PENGATURAN -> "Pengaturan Toko"
                            },
                            fontSize = 12.sp,
                            color = PureWhite.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    if (currentTab != PosTab.KASIR && cartItemCount > 0) {
                        IconButton(
                            onClick = {
                                viewModel.setTab(PosTab.KASIR)
                                showCartPaymentDialog = true
                            }
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = WarningAmber,
                                        contentColor = NavyPrimary
                                    ) {
                                        Text("$cartItemCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Keranjang",
                                    tint = PureWhite
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyPrimary,
                    titleContentColor = PureWhite,
                    actionIconContentColor = PureWhite
                )
            )
        },
        bottomBar = {
            Surface(
                color = PureWhite,
                shadowElevation = 8.dp
            ) {
                NavigationBar(
                    containerColor = PureWhite,
                    tonalElevation = 0.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val tabs = listOf(
                        Triple(PosTab.BERANDA, Icons.Default.Home, "Beranda"),
                        Triple(PosTab.KASIR, Icons.Default.PointOfSale, "Kasir"),
                        Triple(PosTab.PRODUK, Icons.Default.Inventory2, "Produk"),
                        Triple(PosTab.STOK, Icons.Default.Inventory, "Stok"),
                        Triple(PosTab.LAPORAN, Icons.Default.Assessment, "Laporan"),
                        Triple(PosTab.PENGATURAN, Icons.Default.Settings, "Setelan")
                    )

                    for ((tab, icon, label) in tabs) {
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setTab(tab) },
                            icon = {
                                if (tab == PosTab.KASIR && cartItemCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = WarningAmber,
                                                contentColor = NavyPrimary
                                            ) {
                                                Text("$cartItemCount", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = icon, contentDescription = label)
                                    }
                                } else {
                                    Icon(imageVector = icon, contentDescription = label)
                                }
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PureWhite,
                                selectedTextColor = NavyPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = NavyPrimary
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                PosTab.BERANDA -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateTab = { tab -> viewModel.setTab(tab) },
                        onAddNewProduct = {
                            productToEdit = null
                            showProductAddEditDialog = true
                        }
                    )
                }

                PosTab.KASIR -> {
                    CashierScreen(
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState,
                        onOpenCartPayment = { showCartPaymentDialog = true }
                    )
                }

                PosTab.PRODUK -> {
                    ProductListScreen(
                        viewModel = viewModel,
                        onAddProductClick = {
                            productToEdit = null
                            showProductAddEditDialog = true
                        },
                        onEditProductClick = { product ->
                            productToEdit = product
                            showProductAddEditDialog = true
                        }
                    )
                }

                PosTab.STOK -> {
                    StockScreen(viewModel = viewModel)
                }

                PosTab.LAPORAN -> {
                    ReportsScreen(viewModel = viewModel)
                }

                PosTab.PENGATURAN -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (showProductAddEditDialog) {
        ProductAddEditDialog(
            initialProduct = productToEdit,
            existingCategories = categories,
            onDismiss = {
                showProductAddEditDialog = false
                productToEdit = null
            },
            onSave = { product ->
                viewModel.saveProduct(product) {
                    showProductAddEditDialog = false
                    productToEdit = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Produk '${product.name}' berhasil disimpan!")
                    }
                }
            }
        )
    }

    if (showCartPaymentDialog) {
        CartPaymentDialog(
            viewModel = viewModel,
            onDismiss = { showCartPaymentDialog = false },
            onCheckoutSuccess = {
                showCartPaymentDialog = false
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showReceiptDialog && activeReceipt != null) {
        ReceiptDialog(
            viewModel = viewModel,
            receipt = activeReceipt!!,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }
}
