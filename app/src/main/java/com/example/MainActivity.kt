package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.model.AppMode
import com.example.model.BottomTab
import com.example.ui.components.AddressPickerBottomSheet
import com.example.ui.components.FloatingCartSummaryBar
import com.example.ui.components.QuickDropBottomNavBar
import com.example.ui.components.QuickDropTopHeader
import com.example.ui.components.fetchDeviceLocationAddress
import com.example.ui.components.isDeviceLocationEnabled
import com.example.ui.screens.*
import com.example.ui.theme.QuickDropTheme
import com.example.viewmodel.QuickDropViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var appViewModel: QuickDropViewModel
    private var showEnableGpsDialog by mutableStateOf(false)
    private var hasDetectedLiveLocationThisSession = false
    private var awaitingSettingsOrGpsReturn = false

    private val startupLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchLiveLocationOrPromptGps()
        } else {
            appViewModel.setGpsDetecting(false)
            appViewModel.showStatusNotice("Location permission needed — tap top bar anytime for live GPS")
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun fetchLiveLocationOrPromptGps() {
        if (!isDeviceLocationEnabled(this)) {
            appViewModel.setGpsDetecting(false)
            showEnableGpsDialog = true
            return
        }
        showEnableGpsDialog = false
        lifecycleScope.launch {
            appViewModel.setGpsDetecting(true)
            val detected = fetchDeviceLocationAddress(this@MainActivity)
            if (detected != null) {
                hasDetectedLiveLocationThisSession = true
                appViewModel.applyGpsDetectedLocation(detected.areaShort, detected.fullAddress)
            } else {
                appViewModel.setGpsDetecting(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appViewModel = ViewModelProvider(this)[QuickDropViewModel::class.java]

        // Ask immediately for live location permission / GPS as soon as app turns ON
        if (hasLocationPermission()) {
            fetchLiveLocationOrPromptGps()
        } else if (savedInstanceState == null) {
            startupLocationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        setContent {
            val uiState by appViewModel.uiState.collectAsStateWithLifecycle()

            QuickDropTheme(darkTheme = uiState.isDarkTheme) {
                QuickDropApp(
                    viewModel = appViewModel,
                    showEnableGpsDialog = showEnableGpsDialog,
                    onOpenDeviceLocationSettings = {
                        awaitingSettingsOrGpsReturn = true
                        showEnableGpsDialog = false
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    },
                    onDismissEnableGpsDialog = { showEnableGpsDialog = false }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if ((awaitingSettingsOrGpsReturn || !hasDetectedLiveLocationThisSession) &&
            hasLocationPermission() &&
            isDeviceLocationEnabled(this)
        ) {
            awaitingSettingsOrGpsReturn = false
            fetchLiveLocationOrPromptGps()
        }
    }
}

@Composable
fun QuickDropApp(
    viewModel: QuickDropViewModel,
    showEnableGpsDialog: Boolean = false,
    onOpenDeviceLocationSettings: () -> Unit = {},
    onDismissEnableGpsDialog: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddressSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeStatusMessage()
        }
    }

    val activeOrder = uiState.activeTrackingOrder
    val selectedRestaurant = uiState.selectedRestaurant

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isAdminMode) {
            AdminScreen(
                uiState = uiState,
                onBack = { viewModel.closeAdminPanel() },
                onUnlockAdmin = { viewModel.unlockAdmin(it) },
                onLockAdmin = { viewModel.lockAdmin() },
                onClearUnlockError = { viewModel.clearAdminUnlockError() },
                onChangePassword = { current, newPass -> viewModel.changeAdminPassword(current, newPass) },
                onSelectTab = { viewModel.selectAdminTab(it) },
                onToggleStoreOnline = { viewModel.toggleStoreOnline() },
                onToggleWeatherSurge = { viewModel.toggleWeatherSurge() },
                onSetFreeDeliveryThreshold = { viewModel.setFreeDeliveryThreshold(it) },
                onUpdateFeeConfig = { base, handling, surge, threshold ->
                    viewModel.updateFeeConfig(base, handling, surge, threshold)
                },
                onAdvanceOrderStage = { viewModel.advanceOrderStage(it) },
                onAssignRider = { orderId, rider -> viewModel.assignOrderRider(orderId, rider) },
                onUpdateOrderDetails = { orderId, eta, stage, addr ->
                    viewModel.updateOrderDetails(orderId, eta, stage, addr)
                },
                onVerifyShopPickupOtp = { orderId, enteredOtp ->
                    viewModel.verifyShopPickupOtpAndDispatch(orderId, enteredOtp)
                },
                onVerifyOrderOtp = { orderId, enteredOtp ->
                    viewModel.verifyDeliveryOtpAndCompleteOrder(orderId, enteredOtp)
                },
                onDeleteOrder = { viewModel.deleteOrder(it) },
                onApproveShop = { appId -> viewModel.approveShopApplication(appId) },
                onRejectShop = { appId, reason -> viewModel.rejectShopApplication(appId, reason) },
                onAddExtraProductToOrder = { orderId, product ->
                    viewModel.addExtraProductToOrder(orderId, product)
                },
                onAddExtraDishToOrder = { orderId, dish ->
                    viewModel.addExtraDishToOrder(orderId, dish)
                },
                onAddCustomExtraToOrder = { orderId, name, price ->
                    viewModel.addCustomExtraToOrder(orderId, name, price)
                },
            onOpenOrderTracking = { viewModel.openOrderTracking(it) },
            onToggleProductStock = { viewModel.toggleProductStock(it) },
            onUpdateProductFull = { id, brand, name, catId, sub, eta, veg, stock, best, steal, badge, variants ->
                viewModel.updateProductFull(id, brand, name, catId, sub, eta, veg, stock, best, steal, badge, variants)
            },
            onDeleteProduct = { viewModel.deleteProduct(it) },
            onAddProduct = { brand, name, catId, sub, unit, price, mrp, veg, eta, bestseller, badge, missedQuick, variants ->
                viewModel.addNewProduct(
                    brand = brand,
                    name = name,
                    categoryId = catId,
                    subcategory = sub,
                    unitLabel = unit,
                    price = price,
                    mrp = mrp,
                    isVeg = veg,
                    etaMinutes = eta,
                    isBestseller = bestseller,
                    badgeText = badge,
                    isMissedQuickAdd = missedQuick,
                    extraVariants = variants
                )
            },
            onAddCategory = { name, sale -> viewModel.addCategory(name, sale) },
            onUpdateCategory = { id, name, sale -> viewModel.updateCategory(id, name, sale) },
            onDeleteCategory = { viewModel.deleteCategory(it) },
            onToggleRestaurantOnline = { viewModel.toggleRestaurantAcceptingOrders(it) },
            onUpdateRestaurantFull = { id, name, cuisines, area, eta, p2, offer, veg, online ->
                viewModel.updateRestaurantFull(id, name, cuisines, area, eta, p2, offer, veg, online)
            },
            onDeleteRestaurant = { viewModel.deleteRestaurant(it) },
            onAddRestaurant = { name, cuisines, area, eta, p2, offer, veg ->
                viewModel.addNewRestaurant(name, cuisines, area, eta, p2, offer, veg)
            },
            onAddDishToRestaurant = { restId, name, desc, cat, price, mrp, veg, best, spicy ->
                viewModel.addDishToRestaurant(restId, name, desc, cat, price, mrp, veg, best, spicy)
            },
            onUpdateDishInRestaurant = { restId, dishId, name, desc, cat, price, mrp, veg, best, spicy ->
                viewModel.updateDishInRestaurant(restId, dishId, name, desc, cat, price, mrp, veg, best, spicy)
            },
            onDeleteDishFromRestaurant = { restId, dishId ->
                viewModel.deleteDishFromRestaurant(restId, dishId)
            },
            onToggleCouponActive = { viewModel.toggleCouponActive(it) },
            onAddCoupon = { code, title, desc, disc, minOrd, freeDel ->
                viewModel.addNewCoupon(code, title, desc, disc, minOrd, freeDel)
            },
            onUpdateCoupon = { oldCode, title, desc, disc, minOrd, freeDel, active ->
                viewModel.updateCoupon(oldCode, title, desc, disc, minOrd, freeDel, active)
            },
            onDeleteCoupon = { viewModel.deleteCoupon(it) },
            onAddPromoBanner = { tag, title, sub, cta, isFood ->
                viewModel.addPromoBanner(tag, title, sub, cta, isFood)
            },
            onDeletePromoBanner = { id, isFood ->
                viewModel.deletePromoBanner(id, isFood)
            }
        )
    } else if (uiState.isShopkeeperMode) {
        ShopkeeperScreen(
            uiState = uiState,
            onBack = { viewModel.closeShopkeeperPortal() },
            onSelectShop = { viewModel.selectShopApplication(it) },
            onSubmitApplication = { shopName, owner, phone, type, area, addr, eta, gst, listings ->
                viewModel.submitShopApplication(shopName, owner, phone, type, area, addr, eta, gst, listings)
            },
            onAddListing = { appId, title, cat, unit, price, mrp, veg, desc ->
                viewModel.addListingToShop(appId, title, cat, unit, price, mrp, veg, desc)
            },
            onUpdateListingPrice = { appId, listingId, price, mrp ->
                viewModel.updateShopListingPrice(appId, listingId, price, mrp)
            },
            onRemoveListing = { appId, listingId ->
                viewModel.removeShopListing(appId, listingId)
            },
            onMarkOrderPacked = { appId, orderId ->
                viewModel.shopkeeperMarkOrderPacked(appId, orderId)
            }
        )
    } else if (activeOrder != null) {
        OrderTrackingScreen(
            order = activeOrder,
            products = uiState.products,
            restaurants = uiState.restaurants,
            onBack = { viewModel.closeOrderTracking() },
            onAddExtraProduct = { viewModel.addExtraProductToOrder(activeOrder.orderId, it) },
            onAddExtraDish = { viewModel.addExtraDishToOrder(activeOrder.orderId, it) },
            onAddCustomExtra = { name, price -> viewModel.addCustomExtraToOrder(activeOrder.orderId, name, price) },
            onReorder = { viewModel.reorder(activeOrder) }
        )
    } else if (selectedRestaurant != null) {
        RestaurantDetailScreen(
            restaurant = selectedRestaurant,
            uiState = uiState,
            onBack = { viewModel.closeRestaurant() },
            onMenuSearchChange = { viewModel.updateMenuSearch(it) },
            onToggleVegOnly = { viewModel.toggleMenuVegOnly() },
            onToggleBestsellerOnly = { viewModel.toggleMenuBestsellerOnly() },
            onAddDish = { viewModel.addDishToCart(it) },
            onDecrementDish = { viewModel.decrementCartItem(it) },
            onViewCart = { viewModel.selectTab(BottomTab.CART) }
        )
    } else {
        val showFloatingCartBar = uiState.totalCartItemsCount > 0 && uiState.currentTab != BottomTab.CART

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                QuickDropTopHeader(
                    selectedAddress = uiState.selectedAddress,
                    appMode = uiState.appMode,
                    isDetectingGpsLocation = uiState.isDetectingGpsLocation,
                    onLocationClick = { showAddressSheet = true },
                    onProfileClick = { viewModel.selectTab(BottomTab.PROFILE) }
                )
            },
            bottomBar = {
                Column {
                    if (showFloatingCartBar) {
                        FloatingCartSummaryBar(
                            itemCount = uiState.totalCartItemsCount,
                            subtotal = uiState.bill.itemSubtotal,
                            totalSavings = uiState.bill.totalSavings,
                            onViewCart = { viewModel.selectTab(BottomTab.CART) }
                        )
                    }
                    QuickDropBottomNavBar(
                        currentTab = uiState.currentTab,
                        cartItemCount = uiState.totalCartItemsCount,
                        onSelectTab = { viewModel.selectTab(it) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                val contentBottomPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
                when (uiState.currentTab) {
                    BottomTab.HOME -> HomeScreen(
                        uiState = uiState,
                        onModeChange = { viewModel.setAppMode(it) },
                        onSearchClick = { viewModel.selectTab(BottomTab.SEARCH) },
                        onCategoryClick = { viewModel.openCategory(it) },
                        onSeeAllCategoriesClick = { viewModel.selectTab(BottomTab.CATEGORIES) },
                        onAddGrocery = { viewModel.addGroceryToCart(it) },
                        onDecrementGrocery = { viewModel.decrementGroceryFromCart(it) },
                        onSelectCuisine = { viewModel.selectCuisine(it) },
                        onToggleFoodFilter = { viewModel.toggleFoodFilter(it) },
                        onOpenRestaurant = { viewModel.openRestaurant(it) },
                        onOpenTracking = { viewModel.openOrderTracking(it) },
                        bottomContentPadding = contentBottomPadding
                    )

                    BottomTab.CATEGORIES -> CategoriesScreen(
                        uiState = uiState,
                        onSelectCategory = { viewModel.selectCategoryInRail(it) },
                        onSelectSubcategory = { viewModel.selectSubcategory(it) },
                        onSearchQueryChange = { viewModel.updateCategorySearch(it) },
                        onToggleQuickFilter = { viewModel.toggleCatalogFilter(it) },
                        onSelectSort = { viewModel.setCatalogSort(it) },
                        onSelectVariant = { prodId, idx -> viewModel.selectProductVariant(prodId, idx) },
                        onAddGrocery = { viewModel.addGroceryToCart(it) },
                        onDecrementGrocery = { viewModel.decrementGroceryFromCart(it) },
                        bottomContentPadding = contentBottomPadding
                    )

                    BottomTab.SEARCH -> SearchScreen(
                        uiState = uiState,
                        onSearchChange = { viewModel.updateGlobalSearch(it) },
                        onSubmitQuery = { viewModel.submitRecentSearch(it) },
                        onClearRecent = { viewModel.clearRecentSearches() },
                        onAddGrocery = { viewModel.addGroceryToCart(it) },
                        onDecrementGrocery = { viewModel.decrementGroceryFromCart(it) },
                        onOpenRestaurant = { viewModel.openRestaurant(it) },
                        onAddDish = { viewModel.addDishToCart(it) },
                        onDecrementDish = { viewModel.decrementCartItem(it) },
                        bottomContentPadding = contentBottomPadding
                    )

                    BottomTab.CART -> CartScreen(
                        uiState = uiState,
                        onIncrementCartItem = { viewModel.incrementCartItem(it) },
                        onDecrementCartItem = { viewModel.decrementCartItem(it) },
                        onEmptyCart = { viewModel.emptyCart() },
                        onAddQuickProduct = { viewModel.addGroceryToCart(it) },
                        onAddDish = { viewModel.addDishToCart(it) },
                        onAddCustomCartItem = { name, price -> viewModel.addCustomItemToCart(name, price) },
                        onApplyCoupon = { viewModel.applyCoupon(it) },
                        onToggleInstruction = { viewModel.toggleInstruction(it) },
                        onToggleTip = { viewModel.toggleTip(it) },
                        onEditAddressClick = { showAddressSheet = true },
                        onPlaceOrder = { method -> viewModel.placeOrder(method) },
                        onExploreGroceries = {
                            viewModel.setAppMode(AppMode.GROCERIES)
                            viewModel.selectTab(BottomTab.HOME)
                        },
                        onExploreFood = {
                            viewModel.setAppMode(AppMode.FOOD)
                            viewModel.selectTab(BottomTab.HOME)
                        },
                        bottomContentPadding = contentBottomPadding
                    )

                    BottomTab.PROFILE -> ProfileScreen(
                        uiState = uiState,
                        onManageAddressesClick = { showAddressSheet = true },
                        onToggleVegMode = { viewModel.toggleGlobalVegMode() },
                        onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                        onOpenOrderTracking = { viewModel.openOrderTracking(it) },
                        onReorder = { viewModel.reorder(it) },
                        onOpenShopkeeperPortal = { viewModel.openShopkeeperPortal() },
                        onOpenAdminPanel = { viewModel.openAdminPanel() },
                        bottomContentPadding = contentBottomPadding
                    )
                }
            }
        }
    }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        )
    }

    if (showAddressSheet) {
        AddressPickerBottomSheet(
            addresses = uiState.addresses,
            selectedAddress = uiState.selectedAddress,
            onSelectAddress = { viewModel.selectAddress(it) },
            onAddCustomAddress = { label, full, area ->
                viewModel.addCustomAddress(label, full, area)
            },
            onDismiss = { showAddressSheet = false }
        )
    }

    if (showEnableGpsDialog) {
        AlertDialog(
            onDismissRequest = onDismissEnableGpsDialog,
            icon = {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Turn On Live Location (GPS)") },
            text = {
                Text(
                    "Enable your phone's GPS/Location so QuickDrop can detect your live delivery address automatically as soon as the app opens."
                )
            },
            confirmButton = {
                Button(onClick = onOpenDeviceLocationSettings) {
                    Text("Turn On GPS")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissEnableGpsDialog()
                        showAddressSheet = true
                    }
                ) {
                    Text("Pick Manually")
                }
            }
        )
    }
}
