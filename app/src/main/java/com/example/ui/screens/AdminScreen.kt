package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.components.AddExtraItemsBottomSheet
import com.example.ui.components.VegNonVegBadge
import com.example.ui.theme.*
import com.example.viewmodel.QuickDropUiState

private sealed interface DeleteTarget {
    data class Product(val product: GroceryProduct) : DeleteTarget
    data class Category(val category: GroceryCategory) : DeleteTarget
    data class RestaurantTarget(val restaurant: Restaurant) : DeleteTarget
    data class Order(val order: ActiveOrder) : DeleteTarget
    data class CouponTarget(val coupon: Coupon) : DeleteTarget
    data class Banner(val promo: PromoBanner) : DeleteTarget
}

@Composable
fun AdminScreen(
    uiState: QuickDropUiState,
    onBack: () -> Unit,
    onUnlockAdmin: (String) -> Unit,
    onLockAdmin: () -> Unit,
    onClearUnlockError: () -> Unit,
    onChangePassword: (String, String) -> Boolean,
    onSelectTab: (AdminTab) -> Unit,
    onToggleStoreOnline: () -> Unit,
    onToggleWeatherSurge: () -> Unit,
    onSetFreeDeliveryThreshold: (Int) -> Unit,
    onUpdateFeeConfig: (Int, Int, Int, Int) -> Unit,
    onAdvanceOrderStage: (String) -> Unit,
    onAssignRider: (String, String) -> Unit,
    onUpdateOrderDetails: (String, Int, Int, String) -> Unit,
    onVerifyShopPickupOtp: (orderId: String, enteredOtp: String) -> Boolean,
    onVerifyOrderOtp: (orderId: String, enteredOtp: String) -> Boolean,
    onDeleteOrder: (String) -> Unit,
    onApproveShop: (appId: String) -> Unit,
    onRejectShop: (appId: String, reason: String) -> Unit,
    onAddExtraProductToOrder: (orderId: String, product: GroceryProduct) -> Unit,
    onAddExtraDishToOrder: (orderId: String, dish: MenuItem) -> Unit,
    onAddCustomExtraToOrder: (orderId: String, name: String, price: Int) -> Unit,
    onOpenOrderTracking: (String) -> Unit,
    onToggleProductStock: (String) -> Unit,
    onUpdateProductFull: (
        productId: String,
        brand: String,
        name: String,
        categoryId: String,
        subcategory: String,
        etaMinutes: Int,
        isVeg: Boolean,
        isInStock: Boolean,
        isBestseller: Boolean,
        isStealDeal: Boolean,
        badgeText: String?,
        variants: List<ProductVariant>
    ) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onAddProduct: (String, String, String, String, String, Int, Int, Boolean, Int, Boolean, String?, Boolean, List<ProductVariant>) -> Unit,
    onAddCategory: (String, Boolean) -> Unit,
    onUpdateCategory: (String, String, Boolean) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onToggleRestaurantOnline: (String) -> Unit,
    onUpdateRestaurantFull: (String, String, String, String, Int, Int, String, Boolean, Boolean) -> Unit,
    onDeleteRestaurant: (String) -> Unit,
    onAddRestaurant: (String, String, String, Int, Int, String, Boolean) -> Unit,
    onAddDishToRestaurant: (String, String, String, String, Int, Int, Boolean, Boolean, Boolean) -> Unit,
    onUpdateDishInRestaurant: (String, String, String, String, String, Int, Int, Boolean, Boolean, Boolean) -> Unit,
    onDeleteDishFromRestaurant: (String, String) -> Unit,
    onToggleCouponActive: (String) -> Unit,
    onAddCoupon: (String, String, String, Int, Int, Boolean) -> Unit,
    onUpdateCoupon: (String, String, String, Int, Int, Boolean, Boolean) -> Unit,
    onDeleteCoupon: (String) -> Unit,
    onAddPromoBanner: (String, String, String, String, Boolean) -> Unit,
    onDeletePromoBanner: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    if (!uiState.isAdminUnlocked) {
        AdminLockScreen(
            errorMessage = uiState.adminUnlockError,
            onBack = onBack,
            onPasswordTyped = onClearUnlockError,
            onUnlock = onUnlockAdmin,
            modifier = modifier
        )
        return
    }

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showFeeConfigDialog by remember { mutableStateOf(false) }
    var editingOrder by remember { mutableStateOf<ActiveOrder?>(null) }
    var addingExtraToOrder by remember { mutableStateOf<ActiveOrder?>(null) }
    var shopPickupVerifyingOrder by remember { mutableStateOf<ActiveOrder?>(null) }
    var otpVerifyingOrder by remember { mutableStateOf<ActiveOrder?>(null) }
    var rejectingShopApp by remember { mutableStateOf<ShopkeeperApplication?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<GroceryProduct?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<GroceryCategory?>(null) }
    var showAddRestaurantDialog by remember { mutableStateOf(false) }
    var editingRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    var managingMenuRestaurantId by remember { mutableStateOf<String?>(null) }
    var showCouponDialog by remember { mutableStateOf(false) }
    var editingCoupon by remember { mutableStateOf<Coupon?>(null) }
    var showAddPromoBannerDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<DeleteTarget?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.inverseSurface,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("admin_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_navigate_back),
                                    tint = Color.White
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "QuickDrop Ops Admin",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(
                                        color = if (uiState.isStoreOnline) VegGreen else ZomatoCrimson,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = if (uiState.isStoreOnline) "LIVE" else "PAUSED",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Full Catalog, Prices, Kitchens & Orders",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.75f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showChangePasswordDialog = true },
                                modifier = Modifier.testTag("admin_change_password_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Key,
                                    contentDescription = "Change Admin Password",
                                    tint = QuickDropSecondaryContainer
                                )
                            }
                            IconButton(
                                onClick = onLockAdmin,
                                modifier = Modifier.testTag("admin_lock_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Lock Admin Panel",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    ScrollableTabRow(
                        selectedTabIndex = uiState.adminTab.ordinal,
                        containerColor = Color.Transparent,
                        contentColor = QuickDropSecondaryContainer,
                        edgePadding = 12.dp,
                        divider = {}
                    ) {
                        AdminTab.entries.forEach { tab ->
                            val selected = uiState.adminTab == tab
                            val tabTitle = if (tab == AdminTab.SELLERS && uiState.pendingShopApplicationsCount > 0) {
                                "${tab.label} (${uiState.pendingShopApplicationsCount})"
                            } else {
                                tab.label
                            }
                            Tab(
                                selected = selected,
                                onClick = { onSelectTab(tab) },
                                modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}"),
                                text = {
                                    Text(
                                        text = tabTitle,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                                        ),
                                        color = if (selected) QuickDropSecondaryContainer else Color.White.copy(alpha = 0.72f)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            when (uiState.adminTab) {
                AdminTab.INVENTORY -> FloatingActionButton(
                    onClick = { showAddProductDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_add_product_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Grocery SKU")
                }

                AdminTab.PARTNERS -> FloatingActionButton(
                    onClick = { showAddRestaurantDialog = true },
                    containerColor = ZomatoCrimson,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_add_restaurant_fab")
                ) {
                    Icon(Icons.Filled.AddBusiness, contentDescription = "Add Restaurant")
                }

                else -> Unit
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .navigationBarsPadding()
        ) {
            when (uiState.adminTab) {
                AdminTab.OVERVIEW -> AdminOverviewTab(
                    uiState = uiState,
                    onToggleStoreOnline = onToggleStoreOnline,
                    onToggleWeatherSurge = onToggleWeatherSurge,
                    onSetFreeDeliveryThreshold = onSetFreeDeliveryThreshold,
                    onOpenFeeConfig = { showFeeConfigDialog = true },
                    onJumpToTab = onSelectTab,
                    onOpenAddProduct = { showAddProductDialog = true },
                    onOpenAddCoupon = {
                        editingCoupon = null
                        showCouponDialog = true
                    },
                    onOpenAddBanner = { showAddPromoBannerDialog = true }
                )

                AdminTab.ORDERS -> AdminOrdersTab(
                    orders = uiState.orders,
                    onAdvanceStage = onAdvanceOrderStage,
                    onAssignRider = onAssignRider,
                    onEditOrder = { editingOrder = it },
                    onVerifyShopPickupOtpOrder = { shopPickupVerifyingOrder = it },
                    onVerifyOtpOrder = { otpVerifyingOrder = it },
                    onAddExtraToOrder = { addingExtraToOrder = it },
                    onDeleteOrder = { pendingDelete = DeleteTarget.Order(it) },
                    onOpenTracking = onOpenOrderTracking
                )

                AdminTab.SELLERS -> AdminShopApprovalsTab(
                    applications = uiState.shopApplications,
                    onApprove = onApproveShop,
                    onRequestReject = { rejectingShopApp = it }
                )

                AdminTab.INVENTORY -> AdminInventoryTab(
                    categories = uiState.categories,
                    products = uiState.products,
                    onToggleStock = onToggleProductStock,
                    onEditProduct = { editingProduct = it },
                    onDeleteProduct = { pendingDelete = DeleteTarget.Product(it) },
                    onAddCategory = {
                        editingCategory = null
                        showCategoryDialog = true
                    },
                    onEditCategory = {
                        editingCategory = it
                        showCategoryDialog = true
                    },
                    onDeleteCategory = { pendingDelete = DeleteTarget.Category(it) }
                )

                AdminTab.PARTNERS -> AdminPartnersTab(
                    restaurants = uiState.restaurants,
                    coupons = uiState.coupons,
                    groceryPromos = uiState.groceryPromos,
                    foodPromos = uiState.foodPromos,
                    onToggleRestaurantOnline = onToggleRestaurantOnline,
                    onEditRestaurant = { editingRestaurant = it },
                    onManageDishes = { managingMenuRestaurantId = it.id },
                    onDeleteRestaurant = { pendingDelete = DeleteTarget.RestaurantTarget(it) },
                    onToggleCouponActive = onToggleCouponActive,
                    onOpenAddCoupon = {
                        editingCoupon = null
                        showCouponDialog = true
                    },
                    onEditCoupon = {
                        editingCoupon = it
                        showCouponDialog = true
                    },
                    onDeleteCoupon = { pendingDelete = DeleteTarget.CouponTarget(it) },
                    onOpenAddBanner = { showAddPromoBannerDialog = true },
                    onDeleteBanner = { pendingDelete = DeleteTarget.Banner(it) }
                )
            }
        }
    }

    if (showChangePasswordDialog) {
        AdminChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onChangePassword = onChangePassword
        )
    }

    if (showFeeConfigDialog) {
        AdminFeeConfigDialog(
            baseDeliveryFee = uiState.baseDeliveryFeeAmount,
            handlingFee = uiState.handlingFeeAmount,
            weatherSurgeFee = uiState.weatherSurgeAmount,
            freeThreshold = uiState.freeDeliveryThreshold,
            onDismiss = { showFeeConfigDialog = false },
            onSave = { base, handling, surge, threshold ->
                onUpdateFeeConfig(base, handling, surge, threshold)
                showFeeConfigDialog = false
            }
        )
    }

    editingOrder?.let { ord ->
        AdminEditOrderDialog(
            order = ord,
            onDismiss = { editingOrder = null },
            onSave = { eta, stage, addr ->
                onUpdateOrderDetails(ord.orderId, eta, stage, addr)
                editingOrder = null
            }
        )
    }

    shopPickupVerifyingOrder?.let { targetOrder ->
        val liveOrder = uiState.orders.firstOrNull { it.orderId == targetOrder.orderId } ?: targetOrder
        AdminShopPickupOtpDialog(
            order = liveOrder,
            onDismiss = { shopPickupVerifyingOrder = null },
            onVerifyPickupOtp = { enteredOtp ->
                onVerifyShopPickupOtp(liveOrder.orderId, enteredOtp)
            }
        )
    }

    otpVerifyingOrder?.let { targetOrder ->
        val liveOrder = uiState.orders.firstOrNull { it.orderId == targetOrder.orderId } ?: targetOrder
        AdminRiderOtpDialog(
            order = liveOrder,
            onDismiss = { otpVerifyingOrder = null },
            onVerifyOtp = { enteredOtp ->
                onVerifyOrderOtp(liveOrder.orderId, enteredOtp)
            }
        )
    }

    rejectingShopApp?.let { targetApp ->
        AdminRejectShopDialog(
            application = targetApp,
            onDismiss = { rejectingShopApp = null },
            onReject = { reason ->
                onRejectShop(targetApp.id, reason)
                rejectingShopApp = null
            }
        )
    }

    addingExtraToOrder?.let { targetOrder ->
        val liveOrder = uiState.orders.firstOrNull { it.orderId == targetOrder.orderId } ?: targetOrder
        AddExtraItemsBottomSheet(
            title = "Admin: Add Extra to ${liveOrder.orderId}",
            subtitle = "Add groceries, dishes, or custom items to this order",
            products = uiState.products,
            restaurants = uiState.restaurants,
            allowCustomItem = true,
            onAddProduct = { prod -> onAddExtraProductToOrder(liveOrder.orderId, prod) },
            onAddDish = { dish -> onAddExtraDishToOrder(liveOrder.orderId, dish) },
            onAddCustomExtra = { name, price -> onAddCustomExtraToOrder(liveOrder.orderId, name, price) },
            onDismiss = { addingExtraToOrder = null }
        )
    }

    if (showAddProductDialog && uiState.categories.isNotEmpty()) {
        AdminAddProductDialog(
            categories = uiState.categories,
            onDismiss = { showAddProductDialog = false },
            onConfirm = { brand, name, catId, sub, unit, price, mrp, veg, eta, bestseller, badge, missedQuick, variants, keepOpen ->
                onAddProduct(brand, name, catId, sub, unit, price, mrp, veg, eta, bestseller, badge, missedQuick, variants)
                if (!keepOpen) {
                    showAddProductDialog = false
                }
            }
        )
    }

    editingProduct?.let { prod ->
        if (uiState.categories.isNotEmpty()) {
            AdminEditProductFullDialog(
                product = prod,
                categories = uiState.categories,
                onDismiss = { editingProduct = null },
                onConfirm = { brand, name, catId, sub, eta, veg, stock, bestseller, steal, badge, variants ->
                    onUpdateProductFull(prod.id, brand, name, catId, sub, eta, veg, stock, bestseller, steal, badge, variants)
                    editingProduct = null
                }
            )
        }
    }

    if (showCategoryDialog) {
        AdminCategoryDialog(
            existingCategory = editingCategory,
            onDismiss = { showCategoryDialog = false; editingCategory = null },
            onConfirm = { name, hasSale ->
                val current = editingCategory
                if (current == null) {
                    onAddCategory(name, hasSale)
                } else {
                    onUpdateCategory(current.id, name, hasSale)
                }
                showCategoryDialog = false
                editingCategory = null
            }
        )
    }

    if (showAddRestaurantDialog) {
        AdminAddRestaurantDialog(
            onDismiss = { showAddRestaurantDialog = false },
            onConfirm = { name, cuisines, area, eta, p2, offer, veg ->
                onAddRestaurant(name, cuisines, area, eta, p2, offer, veg)
                showAddRestaurantDialog = false
            }
        )
    }

    editingRestaurant?.let { rest ->
        AdminEditRestaurantFullDialog(
            restaurant = rest,
            onDismiss = { editingRestaurant = null },
            onConfirm = { name, cuisines, area, eta, p2, offer, veg, online ->
                onUpdateRestaurantFull(rest.id, name, cuisines, area, eta, p2, offer, veg, online)
                editingRestaurant = null
            }
        )
    }

    managingMenuRestaurantId?.let { restId ->
        val liveRest = uiState.restaurants.firstOrNull { it.id == restId }
        if (liveRest == null) {
            managingMenuRestaurantId = null
        } else {
            AdminMenuDishesDialog(
                restaurant = liveRest,
                onDismiss = { managingMenuRestaurantId = null },
                onAddDish = { name, desc, cat, price, mrp, veg, bestseller, spicy ->
                    onAddDishToRestaurant(liveRest.id, name, desc, cat, price, mrp, veg, bestseller, spicy)
                },
                onUpdateDish = { dishId, name, desc, cat, price, mrp, veg, bestseller, spicy ->
                    onUpdateDishInRestaurant(liveRest.id, dishId, name, desc, cat, price, mrp, veg, bestseller, spicy)
                },
                onDeleteDish = { dishId ->
                    onDeleteDishFromRestaurant(liveRest.id, dishId)
                }
            )
        }
    }

    if (showCouponDialog) {
        AdminCouponDialog(
            existingCoupon = editingCoupon,
            onDismiss = { showCouponDialog = false; editingCoupon = null },
            onConfirm = { code, title, desc, disc, minOrd, freeDel, active ->
                val existing = editingCoupon
                if (existing == null) {
                    onAddCoupon(code, title, desc, disc, minOrd, freeDel)
                } else {
                    onUpdateCoupon(existing.code, title, desc, disc, minOrd, freeDel, active)
                }
                showCouponDialog = false
                editingCoupon = null
            }
        )
    }

    if (showAddPromoBannerDialog) {
        AdminPromoBannerDialog(
            onDismiss = { showAddPromoBannerDialog = false },
            onConfirm = { tag, title, sub, cta, isFood ->
                onAddPromoBanner(tag, title, sub, cta, isFood)
                showAddPromoBannerDialog = false
            }
        )
    }

    pendingDelete?.let { target ->
        val (title, msg) = when (target) {
            is DeleteTarget.Product -> "Remove SKU?" to "Remove \"${target.product.name}\" from Darkstore inventory and active carts?"
            is DeleteTarget.Category -> "Remove Category?" to "Remove \"${target.category.name}\" from QuickDrop categories?"
            is DeleteTarget.RestaurantTarget -> "Remove Restaurant?" to "Remove \"${target.restaurant.name}\" and all its menu items?"
            is DeleteTarget.Order -> "Delete Order?" to "Remove order ${target.order.orderId} from dispatch history?"
            is DeleteTarget.CouponTarget -> "Delete Coupon?" to "Remove promo code ${target.coupon.code}?"
            is DeleteTarget.Banner -> "Remove Banner?" to "Remove promo banner \"${target.promo.title}\" from home screen?"
        }
        AdminDeleteConfirmDialog(
            title = title,
            message = msg,
            onDismiss = { pendingDelete = null },
            onConfirm = {
                when (target) {
                    is DeleteTarget.Product -> onDeleteProduct(target.product.id)
                    is DeleteTarget.Category -> onDeleteCategory(target.category.id)
                    is DeleteTarget.RestaurantTarget -> onDeleteRestaurant(target.restaurant.id)
                    is DeleteTarget.Order -> onDeleteOrder(target.order.orderId)
                    is DeleteTarget.CouponTarget -> onDeleteCoupon(target.coupon.code)
                    is DeleteTarget.Banner -> onDeletePromoBanner(target.promo.id, target.promo.isFoodPromo)
                }
                pendingDelete = null
            }
        )
    }
}

@Composable
private fun AdminLockScreen(
    errorMessage: String?,
    onBack: () -> Unit,
    onPasswordTyped: () -> Unit,
    onUnlock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("admin_lock_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_navigate_back),
                        tint = Color.White
                    )
                }
                Text(
                    text = "Restricted Admin Access",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = "Admin Vault",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "QuickDrop Owner Vault",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Enter your administrator passcode to edit prices, add/remove items, manage restaurants, and dispatch orders.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (errorMessage != null) onPasswordTyped()
                        },
                        label = { Text("Admin Password") },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { if (password.isNotEmpty()) onUnlock(password) }
                        ),
                        isError = errorMessage != null,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input")
                    )

                    if (errorMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onUnlock(password) },
                        enabled = password.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_unlock_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Unlock Admin Panel", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOverviewTab(
    uiState: QuickDropUiState,
    onToggleStoreOnline: () -> Unit,
    onToggleWeatherSurge: () -> Unit,
    onSetFreeDeliveryThreshold: (Int) -> Unit,
    onOpenFeeConfig: () -> Unit,
    onJumpToTab: (AdminTab) -> Unit,
    onOpenAddProduct: () -> Unit,
    onOpenAddCoupon: () -> Unit,
    onOpenAddBanner: () -> Unit
) {
    val totalRevenue = uiState.orders.sumOf { it.totalPaid }
    val activeOrdersCount = uiState.orders.count { it.stageIndex < 3 }
    val inStockCount = uiState.products.count { it.isInStock }
    val onlineRestCount = uiState.restaurants.count { it.isAcceptingOrders }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Live Operations Pulse",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminKpiCard(
                        title = "Total Revenue",
                        value = "₹$totalRevenue",
                        subtitle = "${uiState.orders.size} orders tracked",
                        icon = Icons.Filled.Payments,
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        title = "Active Dispatch",
                        value = activeOrdersCount.toString(),
                        subtitle = "Tap to edit orders",
                        icon = Icons.Filled.ElectricMoped,
                        accent = ZomatoCrimson,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onJumpToTab(AdminTab.ORDERS) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminKpiCard(
                        title = "Darkstore SKUs",
                        value = "$inStockCount/${uiState.products.size}",
                        subtitle = "${uiState.categories.size} categories",
                        icon = Icons.Filled.Inventory2,
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onJumpToTab(AdminTab.INVENTORY) }
                    )
                    AdminKpiCard(
                        title = "Live Kitchens",
                        value = "$onlineRestCount/${uiState.restaurants.size}",
                        subtitle = "${uiState.restaurants.sumOf { it.menu.size }} dishes live",
                        icon = Icons.Filled.Restaurant,
                        accent = ZomatoCrimson,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onJumpToTab(AdminTab.PARTNERS) }
                    )
                }
                if (uiState.shopApplications.isNotEmpty()) {
                    AdminKpiCard(
                        title = "Shopkeeper Applications",
                        value = "${uiState.pendingShopApplicationsCount} Pending",
                        subtitle = "${uiState.shopApplications.count { it.status == ShopApplicationStatus.APPROVED }} approved • Tap to review",
                        icon = Icons.Filled.Storefront,
                        accent = if (uiState.pendingShopApplicationsCount > 0) Color(0xFFB26A00) else VegGreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onJumpToTab(AdminTab.SELLERS) }
                    )
                }
            }
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Storefront & Pricing Controls",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        FilledTonalButton(
                            onClick = onOpenFeeConfig,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("admin_edit_fees_button")
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit Fees")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Accepting Customer Orders", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "Pause checkout across all hubs & restaurants",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.isStoreOnline,
                            onCheckedChange = { onToggleStoreOnline() },
                            modifier = Modifier.testTag("admin_store_online_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Rain / Peak Weather Surge (+₹${uiState.weatherSurgeAmount})",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Base delivery ₹${uiState.baseDeliveryFeeAmount} • Handling ₹${uiState.handlingFeeAmount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.weatherSurgeEnabled,
                            onCheckedChange = { onToggleWeatherSurge() },
                            modifier = Modifier.testTag("admin_surge_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Free Delivery Threshold (Current: ₹${uiState.freeDeliveryThreshold})",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(299, 500, 699).forEach { threshold ->
                                val selected = uiState.freeDeliveryThreshold == threshold
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSetFreeDeliveryThreshold(threshold) },
                                    label = { Text("₹$threshold") }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Quick Admin Actions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenAddProduct,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add SKU")
                        }
                        OutlinedButton(
                            onClick = onOpenAddCoupon,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.LocalOffer, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("New Promo")
                        }
                    }
                    OutlinedButton(
                        onClick = onOpenAddBanner,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Home Carousel Banner")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = accent
            )
        }
    }
}

@Composable
private fun AdminOrdersTab(
    orders: List<ActiveOrder>,
    onAdvanceStage: (String) -> Unit,
    onAssignRider: (String, String) -> Unit,
    onEditOrder: (ActiveOrder) -> Unit,
    onVerifyShopPickupOtpOrder: (ActiveOrder) -> Unit,
    onVerifyOtpOrder: (ActiveOrder) -> Unit,
    onAddExtraToOrder: (ActiveOrder) -> Unit,
    onDeleteOrder: (ActiveOrder) -> Unit,
    onOpenTracking: (String) -> Unit
) {
    var filterMode by remember { mutableStateOf("All") }
    val riderRoster = listOf("Suresh Kumar", "Arjun Shetty", "Karthik R.", "Deepak Verma")

    val displayedOrders = remember(orders, filterMode) {
        when (filterMode) {
            "Active" -> orders.filter { it.stageIndex < 3 }
            "Delivered" -> orders.filter { it.stageIndex >= 3 }
            else -> orders
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Active", "Delivered").forEach { tab ->
                    FilterChip(
                        selected = filterMode == tab,
                        onClick = { filterMode = tab },
                        label = { Text(tab) }
                    )
                }
            }
        }

        items(displayedOrders, key = { it.orderId }) { order ->
            var riderDropdownExpanded by remember { mutableStateOf(false) }
            val stageLabel = when (order.stageIndex) {
                0 -> "Confirmed"
                1 -> if (order.isFoodOrder) "Preparing" else "Packing"
                2 -> "Out for Delivery"
                else -> "Delivered"
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${order.orderId} • ${order.hubOrKitchen}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${order.placedTimeText} • ETA ${order.etaMinutes} mins",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (order.stageIndex >= 3) VegGreen.copy(alpha = 0.14f)
                                else MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = if (order.stageIndex >= 3) "$stageLabel ✓" else stageLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (order.stageIndex >= 3) VegGreen else Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            if (order.stageIndex < 3) {
                                IconButton(
                                    onClick = { onVerifyOtpOrder(order) },
                                    modifier = Modifier.testTag("admin_otp_icon_${order.orderId}")
                                ) {
                                    Icon(Icons.Filled.Pin, contentDescription = "Verify Delivery OTP", tint = VegGreen)
                                }
                            }
                            IconButton(onClick = { onEditOrder(order) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit Order", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onDeleteOrder(order) }) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove Order", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Text(text = order.summaryTitle, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Address: ${order.deliveryAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₹${order.totalPaid} • ${order.itemsCount} items",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Box {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { riderDropdownExpanded = true },
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.DeliveryDining, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(order.riderName, style = MaterialTheme.typography.labelMedium)
                                    Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = riderDropdownExpanded,
                                onDismissRequest = { riderDropdownExpanded = false }
                            ) {
                                riderRoster.forEach { rider ->
                                    DropdownMenuItem(
                                        text = { Text(rider) },
                                        onClick = {
                                            onAssignRider(order.orderId, rider)
                                            riderDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (order.stageIndex < 2) {
                            Button(
                                onClick = { onVerifyShopPickupOtpOrder(order) },
                                modifier = Modifier
                                    .weight(1.35f)
                                    .testTag("admin_shop_pickup_otp_${order.orderId}"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("Shop OTP", maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { onAdvanceStage(order.orderId) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_advance_${order.orderId}"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("Advance", maxLines = 1)
                            }
                        } else if (order.stageIndex == 2) {
                            Button(
                                onClick = { onVerifyOtpOrder(order) },
                                colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("admin_otp_complete_${order.orderId}"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("End Ride (OTP)", maxLines = 1)
                            }
                        }
                        if (order.stageIndex < 3) {
                            FilledTonalButton(
                                onClick = { onAddExtraToOrder(order) },
                                modifier = Modifier
                                    .weight(0.95f)
                                    .testTag("admin_add_extra_${order.orderId}"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("+ Extra", maxLines = 1)
                            }
                        }
                        OutlinedButton(
                            onClick = { onOpenTracking(order.orderId) },
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("Track", maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminInventoryTab(
    categories: List<GroceryCategory>,
    products: List<GroceryProduct>,
    onToggleStock: (String) -> Unit,
    onEditProduct: (GroceryProduct) -> Unit,
    onDeleteProduct: (GroceryProduct) -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (GroceryCategory) -> Unit,
    onDeleteCategory: (GroceryCategory) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedCatId by remember { mutableStateOf("all") }
    var showCategoryManager by remember { mutableStateOf(false) }

    val filtered = remember(products, search, selectedCatId) {
        products.filter { prod ->
            val matchCat = selectedCatId == "all" || prod.categoryId == selectedCatId
            val matchSearch = search.isBlank() ||
                prod.name.contains(search, ignoreCase = true) ||
                prod.brand.contains(search, ignoreCase = true)
            matchCat && matchSearch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Filter darkstore SKUs...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Category Manager Section
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryManager = !showCategoryManager },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Manage Categories (${categories.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = onAddCategory,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("Add")
                            }
                            Icon(
                                imageVector = if (showCategoryManager) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = "Toggle category manager"
                            )
                        }
                    }

                    if (showCategoryManager) {
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            categories.forEach { cat ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = cat.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (cat.hasSaleBadge) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                    shape = CircleShape
                                                ) {
                                                    Text(
                                                        text = "SALE",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                        color = QuickDropOnSecondaryFixed,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Row {
                                            IconButton(
                                                onClick = { onEditCategory(cat) },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Edit,
                                                    contentDescription = "Edit Category",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteCategory(cat) },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.DeleteOutline,
                                                    contentDescription = "Remove Category",
                                                    tint = MaterialTheme.colorScheme.error,
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
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCatId == "all",
                        onClick = { selectedCatId = "all" },
                        label = { Text("All (${products.size})") }
                    )
                }
                items(categories, key = { it.id }) { cat ->
                    FilterChip(
                        selected = selectedCatId == cat.id,
                        onClick = { selectedCatId = cat.id },
                        label = { Text(cat.name) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { product ->
            val variant = product.variants[product.defaultVariantIndex.coerceIn(0, product.variants.lastIndex)]
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            VegNonVegBadge(isVeg = product.isVeg)
                            Text(
                                text = product.brand.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (product.isStealDeal) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "STEAL DEAL",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "₹${variant.price}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (variant.mrp > variant.price) {
                                Text(
                                    text = "₹${variant.mrp}",
                                    style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "• ${variant.label} (${product.variants.size} packs)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { onEditProduct(product) },
                            modifier = Modifier.testTag("edit_product_${product.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Details & Price",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { onDeleteProduct(product) },
                            modifier = Modifier.testTag("delete_product_${product.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Remove SKU",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Switch(
                                checked = product.isInStock,
                                onCheckedChange = { onToggleStock(product.id) },
                                modifier = Modifier.testTag("stock_switch_${product.id}")
                            )
                            Text(
                                text = if (product.isInStock) "In Stock" else "Out",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = if (product.isInStock) VegGreen else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPartnersTab(
    restaurants: List<Restaurant>,
    coupons: List<Coupon>,
    groceryPromos: List<PromoBanner>,
    foodPromos: List<PromoBanner>,
    onToggleRestaurantOnline: (String) -> Unit,
    onEditRestaurant: (Restaurant) -> Unit,
    onManageDishes: (Restaurant) -> Unit,
    onDeleteRestaurant: (Restaurant) -> Unit,
    onToggleCouponActive: (String) -> Unit,
    onOpenAddCoupon: () -> Unit,
    onEditCoupon: (Coupon) -> Unit,
    onDeleteCoupon: (Coupon) -> Unit,
    onOpenAddBanner: () -> Unit,
    onDeleteBanner: (PromoBanner) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Restaurant Partners (${restaurants.size})",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        items(restaurants, key = { it.id }) { rest ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = rest.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${rest.cuisines} • ${rest.area}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "★ ${rest.rating} • ${rest.deliveryTimeMins} mins • ₹${rest.priceForTwo} for 2",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = rest.offerBanner,
                                style = MaterialTheme.typography.labelSmall,
                                color = ZomatoCrimson,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Switch(
                            checked = rest.isAcceptingOrders,
                            onCheckedChange = { onToggleRestaurantOnline(rest.id) },
                            modifier = Modifier.testTag("rest_switch_${rest.id}")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onManageDishes(rest) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.RestaurantMenu, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Menu (${rest.menu.size})", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { onEditRestaurant(rest) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit Info", maxLines = 1)
                        }
                        IconButton(
                            onClick = { onDeleteRestaurant(rest) },
                            modifier = Modifier.testTag("delete_rest_${rest.id}")
                        ) {
                            Icon(
                                Icons.Filled.DeleteOutline,
                                contentDescription = "Remove Restaurant",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Promo Coupons (${coupons.size})",
                    style = MaterialTheme.typography.headlineSmall
                )
                TextButton(
                    onClick = onOpenAddCoupon,
                    modifier = Modifier.testTag("admin_new_coupon_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Create Coupon")
                }
            }
        }

        items(coupons, key = { it.code }) { coupon ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = coupon.code,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = QuickDropOnSecondaryFixed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = coupon.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = "${coupon.description} • Min ₹${coupon.minOrder}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onEditCoupon(coupon) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Coupon", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onDeleteCoupon(coupon) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete Coupon", tint = MaterialTheme.colorScheme.error)
                        }
                        Switch(
                            checked = coupon.isActive,
                            onCheckedChange = { onToggleCouponActive(coupon.code) }
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Home Banners (${groceryPromos.size + foodPromos.size})",
                    style = MaterialTheme.typography.headlineSmall
                )
                TextButton(onClick = onOpenAddBanner) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Banner")
                }
            }
        }

        items(groceryPromos + foodPromos, key = { it.id }) { promo ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "${if (promo.isFoodPromo) "FOOD" else "GROCERY"} • ${promo.tag}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (promo.isFoodPromo) ZomatoCrimson else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = promo.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = promo.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { onDeleteBanner(promo) }) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = "Remove Banner",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminShopApprovalsTab(
    applications: List<ShopkeeperApplication>,
    onApprove: (appId: String) -> Unit,
    onRequestReject: (ShopkeeperApplication) -> Unit
) {
    var statusFilter by remember { mutableStateOf("All") }
    val filtered = remember(applications, statusFilter) {
        when (statusFilter) {
            "Pending" -> applications.filter { it.status == ShopApplicationStatus.PENDING_REVIEW }
            "Approved" -> applications.filter { it.status == ShopApplicationStatus.APPROVED }
            "Rejected" -> applications.filter { it.status == ShopApplicationStatus.REJECTED }
            else -> applications
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "Pending", "Approved", "Rejected")) { tab ->
                    FilterChip(
                        selected = statusFilter == tab,
                        onClick = { statusFilter = tab },
                        label = { Text(tab) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Storefront, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("No shop applications in this filter", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { app ->
            val (badgeBg, badgeText) = when (app.status) {
                ShopApplicationStatus.PENDING_REVIEW -> Color(0xFFFFF4E0) to Color(0xFFB26A00)
                ShopApplicationStatus.APPROVED -> VegGreen.copy(alpha = 0.14f) to VegGreen
                ShopApplicationStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.testTag("admin_shop_card_${app.id}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.shopName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                text = "${app.shopType.label} • ${app.area}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(color = badgeBg, shape = CircleShape) {
                            Text(
                                text = app.status.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = badgeText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Owner: ${app.ownerName} • Ph: ${app.phone} • ETA ${app.etaMins}m",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Address: ${app.fullAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (app.gstOrLicenseNote.isNotBlank()) {
                        Text(
                            text = "Docs: ${app.gstOrLicenseNote}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Submitted Listings Preview for Admin Inspection
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Submitted Listings (${app.listings.size})",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            if (app.listings.isEmpty()) {
                                Text(
                                    text = "No initial items added yet (shopkeeper can add after approval).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                app.listings.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            VegNonVegBadge(isVeg = item.isVeg)
                                            Text(
                                                text = "${item.title} (${item.unitOrPortion})",
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "₹${item.price}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!app.adminReviewNote.isNullOrBlank()) {
                        Text(
                            text = "Review Note: ${app.adminReviewNote}",
                            style = MaterialTheme.typography.bodySmall,
                            color = badgeText
                        )
                    }

                    if (app.status != ShopApplicationStatus.APPROVED) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onApprove(app.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("admin_approve_shop_${app.id}"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Approve & Activate")
                            }
                            if (app.status != ShopApplicationStatus.REJECTED) {
                                OutlinedButton(
                                    onClick = { onRequestReject(app) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("admin_reject_shop_${app.id}"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onRequestReject(app) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.PauseCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Pause / Revoke Shop Approval")
                        }
                    }
                }
            }
        }
    }
}

