package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppMode
import com.example.model.BottomTab
import com.example.ui.components.AddressPickerBottomSheet
import com.example.ui.components.FloatingCartSummaryBar
import com.example.ui.components.QuickDropBottomNavBar
import com.example.ui.components.QuickDropTopHeader
import com.example.ui.screens.*
import com.example.ui.theme.QuickDropTheme
import com.example.viewmodel.QuickDropViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: QuickDropViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            QuickDropTheme(darkTheme = uiState.isDarkTheme) {
                QuickDropApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun QuickDropApp(viewModel: QuickDropViewModel) {
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
            onDeleteOrder = { viewModel.deleteOrder(it) },
            onOpenOrderTracking = { viewModel.openOrderTracking(it) },
            onToggleProductStock = { viewModel.toggleProductStock(it) },
            onUpdateProductFull = { id, brand, name, catId, sub, eta, veg, stock, best, steal, badge, variants ->
                viewModel.updateProductFull(id, brand, name, catId, sub, eta, veg, stock, best, steal, badge, variants)
            },
            onDeleteProduct = { viewModel.deleteProduct(it) },
            onAddProduct = { brand, name, catId, sub, unit, price, mrp, veg, eta, bestseller ->
                viewModel.addNewProduct(brand, name, catId, sub, unit, price, mrp, veg, eta, bestseller)
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
    } else if (activeOrder != null) {
        OrderTrackingScreen(
            order = activeOrder,
            onBack = { viewModel.closeOrderTracking() },
            onAdvanceStage = { viewModel.advanceOrderStage(activeOrder.orderId) },
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
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                QuickDropTopHeader(
                    selectedAddress = uiState.selectedAddress,
                    appMode = uiState.appMode,
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
                        onOpenAdminPanel = { viewModel.openAdminPanel() },
                        bottomContentPadding = contentBottomPadding
                    )
                }
            }
        }
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
}
