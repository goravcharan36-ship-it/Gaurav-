package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.QuickDropCatalog
import com.example.model.*
import java.security.MessageDigest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BillBreakdown(
    val itemsMrpTotal: Int,
    val productDiscount: Int,
    val itemSubtotal: Int,
    val deliveryFee: Int,
    val baseDeliveryFee: Int,
    val isDeliveryFree: Boolean,
    val handlingFee: Int,
    val surgeFee: Int,
    val couponDiscount: Int,
    val tipAmount: Int,
    val toPay: Int,
    val totalSavings: Int,
    val amountNeededForFreeDelivery: Int,
    val freeDeliveryProgress: Float,
    val freeDeliveryThreshold: Int
)

data class QuickDropUiState(
    val appMode: AppMode = AppMode.GROCERIES,
    val currentTab: BottomTab = BottomTab.HOME,
    val selectedRestaurantId: String? = null,
    val trackingOrderId: String? = null,
    val isAdminMode: Boolean = false,
    val isAdminUnlocked: Boolean = false,
    val adminUnlockError: String? = null,
    val adminTab: AdminTab = AdminTab.OVERVIEW,

    // Live mutable catalog & operational controls (managed via Admin Panel)
    val categories: List<GroceryCategory> = QuickDropCatalog.categories,
    val products: List<GroceryProduct> = QuickDropCatalog.products,
    val groceryPromos: List<PromoBanner> = QuickDropCatalog.groceryPromos,
    val foodPromos: List<PromoBanner> = QuickDropCatalog.foodPromos,
    val restaurants: List<Restaurant> = QuickDropCatalog.restaurants,
    val coupons: List<Coupon> = QuickDropCatalog.coupons,
    val isStoreOnline: Boolean = true,
    val weatherSurgeEnabled: Boolean = false,
    val weatherSurgeAmount: Int = 15,
    val baseDeliveryFeeAmount: Int = 25,
    val handlingFeeAmount: Int = 4,
    val freeDeliveryThreshold: Int = 500,

    val addresses: List<DeliveryAddress> = QuickDropCatalog.addresses,
    val selectedAddress: DeliveryAddress = QuickDropCatalog.addresses.first(),

    // Categories catalog state
    val selectedCategoryId: String = "dairy",
    val selectedSubcategory: String = "All",
    val categorySearchQuery: String = "",
    val activeCatalogFilters: Set<CatalogQuickFilter> = setOf(CatalogQuickFilter.EXPRESS_10_MIN),
    val catalogSort: CatalogSortOption = CatalogSortOption.RELEVANCE,
    val selectedVariantByProductId: Map<String, Int> = emptyMap(),

    // Food discovery state
    val selectedCuisineId: String? = null,
    val activeFoodFilters: Set<FoodFilter> = emptySet(),
    val menuSearchQuery: String = "",
    val menuVegOnly: Boolean = false,
    val menuBestsellerOnly: Boolean = false,

    // Global search state
    val globalSearchQuery: String = "",
    val recentSearches: List<String> = listOf("Alphonso Mangoes", "Meghana Biryani", "Amul Butter"),

    // Cart & Checkout state
    val cartItems: List<CartItem> = defaultSeededCart(),
    val appliedCouponCode: String? = "QUICK100",
    val selectedInstructionIds: Set<String> = setOf("dont_ring"),
    val selectedTipAmount: Int = 50,

    // Timers & Orders
    val dealTimerSeconds: Int = 2 * 3600 + 45 * 60 + 10,
    val orders: List<ActiveOrder> = defaultPastOrders(),

    // User preferences
    val isGlobalVegMode: Boolean = false,
    val isDarkTheme: Boolean = false,
    val statusMessage: String? = null
) {
    val totalCartItemsCount: Int
        get() = cartItems.sumOf { it.quantity }

    val activeTrackingOrder: ActiveOrder?
        get() = orders.firstOrNull { it.orderId == trackingOrderId }

    val latestOngoingOrder: ActiveOrder?
        get() = orders.firstOrNull { it.stageIndex < 3 }

    val selectedRestaurant: Restaurant?
        get() = restaurants.firstOrNull { it.id == selectedRestaurantId }

    val activeCoupons: List<Coupon>
        get() = coupons.filter { it.isActive }

    val bill: BillBreakdown
        get() {
            val mrpTotal = cartItems.sumOf { it.mrp * it.quantity }
            val subtotal = cartItems.sumOf { it.price * it.quantity }
            val productDisc = (mrpTotal - subtotal).coerceAtLeast(0)
            val freeThreshold = freeDeliveryThreshold.coerceAtLeast(1)
            val appliedCoupon = activeCoupons.firstOrNull { it.code == appliedCouponCode }
            val couponValid = appliedCoupon != null && subtotal >= appliedCoupon.minOrder
            val couponFreeDelivery = couponValid && appliedCoupon?.freeDelivery == true
            val freeDelivery = subtotal >= freeThreshold || couponFreeDelivery || subtotal == 0
            val deliveryFee = if (freeDelivery) 0 else baseDeliveryFeeAmount
            val handlingFee = if (subtotal > 0) handlingFeeAmount else 0
            val surgeFee = if (weatherSurgeEnabled && subtotal > 0) weatherSurgeAmount else 0
            val couponDisc = if (couponValid) (appliedCoupon?.discountAmount ?: 0).coerceAtMost(subtotal) else 0
            val tip = if (subtotal > 0) selectedTipAmount else 0
            val toPay = (subtotal + deliveryFee + handlingFee + surgeFee - couponDisc + tip).coerceAtLeast(0)
            val deliverySavings = if (subtotal > 0 && freeDelivery) baseDeliveryFeeAmount else 0
            val totalSavings = productDisc + couponDisc + deliverySavings
            val needed = (freeThreshold - subtotal).coerceAtLeast(0)
            val progress = (subtotal.toFloat() / freeThreshold.toFloat()).coerceIn(0f, 1f)

            return BillBreakdown(
                itemsMrpTotal = mrpTotal,
                productDiscount = productDisc,
                itemSubtotal = subtotal,
                deliveryFee = deliveryFee,
                baseDeliveryFee = baseDeliveryFeeAmount,
                isDeliveryFree = freeDelivery,
                handlingFee = handlingFee,
                surgeFee = surgeFee,
                couponDiscount = couponDisc,
                tipAmount = tip,
                toPay = toPay,
                totalSavings = totalSavings,
                amountNeededForFreeDelivery = needed,
                freeDeliveryProgress = progress,
                freeDeliveryThreshold = freeThreshold
            )
        }
}

private fun defaultSeededCart(): List<CartItem> = listOf(
    CartItem(
        id = "prod_amul_milk__amul_milk_500",
        productId = "prod_amul_milk",
        variantId = "amul_milk_500",
        title = "Taaza Toned Milk",
        brandOrRestaurant = "Amul",
        unitLabel = "500 ml pouch",
        price = 27,
        mrp = 29,
        quantity = 2,
        isGrocery = true,
        isVeg = true,
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBwfKehYVHmHJiKpWSDUl8AmIrX7nn1JSg3m-LcMaSaOnbHR5Dzn59EdZ0piOM629Vt5RpEto75nASQposClL02Tczs7oBaFRDVT1H16ofvHvXnjCo2vSlPWGionuyMR3hJjG349mnRKajerPrtIsIjywV-AGw6S23ZCrLxYYCEfi8kUnNELwrEusuecjxs7s6II7shkqo1UAzjWW9URfiMmvbLx3l3uVGp6Dl04MuE"
    ),
    CartItem(
        id = "prod_alphonso_mango__mango_1kg",
        productId = "prod_alphonso_mango",
        variantId = "mango_1kg",
        title = "Ratnagiri Alphonso Mangoes",
        brandOrRestaurant = "Farm Fresh",
        unitLabel = "1 kg (3-4 pcs)",
        price = 349,
        mrp = 399,
        quantity = 1,
        isGrocery = true,
        isVeg = true,
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBufLCKE0OK9kx7GVelcv6j6Nr6N6auoruR7fpqSMWMsbPiOAk0GcsldUlB2NRNOUgAE6ANokL0VsOQZ4mEPJMEby_8ywhPl2YR1WUFmIJOHBVeajRhaTQMEiNcRfeS25ZH2PAVFtHi517c2yIVpSEAOhZlhCyo8frYHNRhkPkOriHj3UmxV1K4Nzdrb8wRaFirLvV-_wA1OnnmoBCsuoXOhY57JbkTk65xn4gwYMd_"
    ),
    CartItem(
        id = "prod_brown_bread__bread_400",
        productId = "prod_brown_bread",
        variantId = "bread_400",
        title = "100% Whole Wheat Brown Bread",
        brandOrRestaurant = "English Oven",
        unitLabel = "400 g",
        price = 45,
        mrp = 50,
        quantity = 1,
        isGrocery = true,
        isVeg = true,
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBH8RIfzNQx3Op94XzEtHtZbNuRnnf7UBnhCdEYwgs6xriMX-Ac4HY1dqshmeZqzVqEUx8W11q7bAlguRaZMJoeb1fyATCWjGxO4l_-myaN_z7M3l2V6dkzlrnH189uOlG4QbF048vUbqIAduZeoehz8KzZQWwdAxRnrNp8PciL0D7HfUWRxq4kuLrNmLATiX6RmOgf9A7-iqncoJhYuvQoujzSxRtmnAkn9wSSwWvz"
    )
)

private fun defaultPastOrders(): List<ActiveOrder> = listOf(
    ActiveOrder(
        orderId = "QD-84921",
        placedTimeText = "5 mins ago • Google Pay UPI",
        summaryTitle = "Amul Taaza Milk x2 & Ratnagiri Mangoes",
        itemsCount = 3,
        totalPaid = 403,
        totalSaved = 110,
        etaMinutes = 7,
        stageIndex = 1,
        riderName = "Suresh Kumar",
        riderRating = 4.9,
        vehicleLabel = "QuickDrop EV • KA-01-EV-4821",
        hubOrKitchen = "Indiranagar Hub",
        deliveryAddress = "Flat 402, Green Glen Layout, Bellandur",
        isFoodOrder = false
    ),
    ActiveOrder(
        orderId = "QD-84920",
        placedTimeText = "Yesterday, 8:15 PM",
        summaryTitle = "Meghana Foods • Veg Biryani & Gobi",
        itemsCount = 2,
        totalPaid = 450,
        totalSaved = 110,
        etaMinutes = 0,
        stageIndex = 3,
        riderName = "Karthik R.",
        riderRating = 4.9,
        vehicleLabel = "Electric Scooter • KA-03-EV-2910",
        hubOrKitchen = "Meghana Foods, Indiranagar",
        deliveryAddress = "Flat 402, Green Glen Layout, Bellandur",
        isFoodOrder = true,
        reorderCartItems = listOf(
            CartItem(
                id = "dish_meghana_veg_biryani",
                productId = "dish_meghana_veg_biryani",
                variantId = "regular",
                title = "Meghana Special Veg Biryani",
                brandOrRestaurant = "Meghana Foods",
                unitLabel = "1 portion",
                price = 285,
                mrp = 320,
                quantity = 1,
                isGrocery = false,
                isVeg = true
            )
        )
    )
)

class QuickDropViewModel : ViewModel() {
    companion object {
        private const val ADMIN_PASSCODE_SALT = "qd_admin_salt:"
        private const val DEFAULT_ADMIN_HASH =
            "6097ce5b8ff16e5e08cc80842fbcd4e49e6596358113b61bbe8611fa8cd43683"
    }

    private var adminPasswordHash: String = DEFAULT_ADMIN_HASH
    private var idSeq: Long = 1L

    private fun nextId(prefix: String): String = "${prefix}_${System.currentTimeMillis()}_${idSeq++}"

    private val _uiState = MutableStateFlow(QuickDropUiState())
    val uiState: StateFlow<QuickDropUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update { state ->
                    val nextDeal = if (state.dealTimerSeconds > 0) state.dealTimerSeconds - 1 else 2 * 3600 + 45 * 60 + 10
                    state.copy(dealTimerSeconds = nextDeal)
                }
            }
        }
    }

    private fun hashPasscode(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest((ADMIN_PASSCODE_SALT + raw).toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun setAppMode(mode: AppMode) {
        _uiState.update { it.copy(appMode = mode) }
    }

    fun selectTab(tab: BottomTab) {
        _uiState.update {
            it.copy(
                currentTab = tab,
                selectedRestaurantId = null,
                trackingOrderId = null,
                isAdminMode = false
            )
        }
    }

    fun openCategory(categoryId: String) {
        _uiState.update {
            it.copy(
                currentTab = BottomTab.CATEGORIES,
                selectedCategoryId = categoryId,
                selectedSubcategory = "All",
                selectedRestaurantId = null,
                trackingOrderId = null,
                isAdminMode = false
            )
        }
    }

    fun selectCategoryInRail(categoryId: String) {
        _uiState.update {
            it.copy(
                selectedCategoryId = categoryId,
                selectedSubcategory = "All"
            )
        }
    }

    fun selectSubcategory(sub: String) {
        _uiState.update { it.copy(selectedSubcategory = sub) }
    }

    fun updateCategorySearch(query: String) {
        _uiState.update { it.copy(categorySearchQuery = query) }
    }

    fun toggleCatalogFilter(filter: CatalogQuickFilter) {
        _uiState.update { state ->
            val updated = state.activeCatalogFilters.toMutableSet()
            if (updated.contains(filter)) updated.remove(filter) else updated.add(filter)
            state.copy(activeCatalogFilters = updated)
        }
    }

    fun setCatalogSort(sort: CatalogSortOption) {
        _uiState.update { it.copy(catalogSort = sort) }
    }

    fun selectProductVariant(productId: String, variantIndex: Int) {
        _uiState.update { state ->
            val updated = state.selectedVariantByProductId.toMutableMap()
            updated[productId] = variantIndex
            state.copy(selectedVariantByProductId = updated)
        }
    }

    fun openRestaurant(restaurantId: String) {
        _uiState.update {
            it.copy(
                selectedRestaurantId = restaurantId,
                menuSearchQuery = "",
                menuVegOnly = false,
                menuBestsellerOnly = false
            )
        }
    }

    fun closeRestaurant() {
        _uiState.update { it.copy(selectedRestaurantId = null) }
    }

    fun updateMenuSearch(query: String) {
        _uiState.update { it.copy(menuSearchQuery = query) }
    }

    fun toggleMenuVegOnly() {
        _uiState.update { it.copy(menuVegOnly = !it.menuVegOnly) }
    }

    fun toggleMenuBestsellerOnly() {
        _uiState.update { it.copy(menuBestsellerOnly = !it.menuBestsellerOnly) }
    }

    fun selectCuisine(cuisineId: String?) {
        _uiState.update { state ->
            val next = if (state.selectedCuisineId == cuisineId) null else cuisineId
            state.copy(selectedCuisineId = next)
        }
    }

    fun toggleFoodFilter(filter: FoodFilter) {
        _uiState.update { state ->
            val updated = state.activeFoodFilters.toMutableSet()
            if (updated.contains(filter)) updated.remove(filter) else updated.add(filter)
            state.copy(activeFoodFilters = updated)
        }
    }

    fun updateGlobalSearch(query: String) {
        _uiState.update { it.copy(globalSearchQuery = query) }
    }

    fun submitRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { state ->
            val updated = (listOf(trimmed) + state.recentSearches.filterNot { it.equals(trimmed, ignoreCase = true) }).take(6)
            state.copy(globalSearchQuery = trimmed, recentSearches = updated)
        }
    }

    fun clearRecentSearches() {
        _uiState.update { it.copy(recentSearches = emptyList()) }
    }

    fun selectAddress(address: DeliveryAddress) {
        _uiState.update {
            it.copy(
                selectedAddress = address,
                statusMessage = "Delivery location updated to ${address.areaShort}"
            )
        }
    }

    fun addCustomAddress(label: String, fullAddress: String, areaShort: String) {
        val cleanFull = fullAddress.trim()
        if (cleanFull.isEmpty()) return
        val newAddr = DeliveryAddress(
            id = nextId("addr"),
            label = label.ifBlank { "Other" },
            fullAddress = cleanFull,
            areaShort = areaShort.ifBlank { "Bengaluru" },
            hubName = "Nearby QuickDrop Hub",
            distanceKm = 0.9
        )
        _uiState.update { state ->
            state.copy(
                addresses = listOf(newAddr) + state.addresses,
                selectedAddress = newAddr,
                statusMessage = "Saved & selected ${newAddr.label} address"
            )
        }
    }

    fun addGroceryToCart(product: GroceryProduct) {
        val idx = (_uiState.value.selectedVariantByProductId[product.id] ?: product.defaultVariantIndex)
            .coerceIn(0, product.variants.lastIndex)
        val variant = product.variants[idx]
        val cartKey = "${product.id}__${variant.id}"
        _uiState.update { state ->
            val existing = state.cartItems.firstOrNull { it.id == cartKey }
            val updated = if (existing != null) {
                state.cartItems.map { if (it.id == cartKey) it.copy(quantity = it.quantity + 1) else it }
            } else {
                state.cartItems + CartItem(
                    id = cartKey,
                    productId = product.id,
                    variantId = variant.id,
                    title = product.name,
                    brandOrRestaurant = product.brand,
                    unitLabel = variant.label,
                    price = variant.price,
                    mrp = variant.mrp,
                    quantity = 1,
                    isGrocery = true,
                    isVeg = product.isVeg,
                    imageUrl = product.imageUrl
                )
            }
            state.copy(cartItems = updated)
        }
    }

    fun decrementGroceryFromCart(product: GroceryProduct) {
        val idx = (_uiState.value.selectedVariantByProductId[product.id] ?: product.defaultVariantIndex)
            .coerceIn(0, product.variants.lastIndex)
        val variant = product.variants[idx]
        val cartKey = "${product.id}__${variant.id}"
        decrementCartItem(cartKey)
    }

    fun addDishToCart(dish: MenuItem) {
        _uiState.update { state ->
            val existing = state.cartItems.firstOrNull { it.id == dish.id }
            val updated = if (existing != null) {
                state.cartItems.map { if (it.id == dish.id) it.copy(quantity = it.quantity + 1) else it }
            } else {
                state.cartItems + CartItem(
                    id = dish.id,
                    productId = dish.id,
                    variantId = "regular",
                    title = dish.name,
                    brandOrRestaurant = dish.restaurantName,
                    unitLabel = "1 portion",
                    price = dish.price,
                    mrp = dish.mrp,
                    quantity = 1,
                    isGrocery = false,
                    isVeg = dish.isVeg,
                    imageUrl = dish.imageUrl
                )
            }
            state.copy(cartItems = updated)
        }
    }

    fun incrementCartItem(cartItemId: String) {
        _uiState.update { state ->
            state.copy(
                cartItems = state.cartItems.map {
                    if (it.id == cartItemId) it.copy(quantity = it.quantity + 1) else it
                }
            )
        }
    }

    fun decrementCartItem(cartItemId: String) {
        _uiState.update { state ->
            val updated = state.cartItems.mapNotNull {
                if (it.id == cartItemId) {
                    if (it.quantity > 1) it.copy(quantity = it.quantity - 1) else null
                } else it
            }
            state.copy(cartItems = updated)
        }
    }

    fun emptyCart() {
        _uiState.update { it.copy(cartItems = emptyList(), statusMessage = "Cart cleared") }
    }

    fun applyCoupon(code: String?) {
        _uiState.update { state ->
            if (code == null) {
                state.copy(appliedCouponCode = null, statusMessage = "Coupon removed")
            } else {
                val coupon = state.activeCoupons.firstOrNull { it.code == code }
                val subtotal = state.bill.itemSubtotal
                if (coupon != null && subtotal < coupon.minOrder) {
                    state.copy(
                        statusMessage = "Add ₹${coupon.minOrder - subtotal} more to unlock ${coupon.code}"
                    )
                } else {
                    state.copy(
                        appliedCouponCode = code,
                        statusMessage = "Coupon $code applied!"
                    )
                }
            }
        }
    }

    fun toggleInstruction(id: String) {
        _uiState.update { state ->
            val updated = state.selectedInstructionIds.toMutableSet()
            if (updated.contains(id)) updated.remove(id) else updated.add(id)
            state.copy(selectedInstructionIds = updated)
        }
    }

    fun toggleTip(amount: Int) {
        _uiState.update { state ->
            val next = if (state.selectedTipAmount == amount) 0 else amount
            state.copy(selectedTipAmount = next)
        }
    }

    fun placeOrder(paymentMethod: String) {
        val snapshot = _uiState.value
        if (!snapshot.isStoreOnline) {
            _uiState.update { it.copy(statusMessage = "Store orders are temporarily paused by Admin") }
            return
        }
        if (snapshot.cartItems.isEmpty()) return
        val bill = snapshot.bill
        val firstTitle = snapshot.cartItems.firstOrNull()?.title ?: "QuickDrop Order"
        val extraCount = snapshot.totalCartItemsCount - 1
        val summary = if (extraCount > 0) "$firstTitle + $extraCount more" else firstTitle
        val hasFood = snapshot.cartItems.any { !it.isGrocery }
        val orderId = "QD-${(10000..99999).random()}"
        val newOrder = ActiveOrder(
            orderId = orderId,
            placedTimeText = "Just now • Paid via $paymentMethod",
            summaryTitle = summary,
            itemsCount = snapshot.totalCartItemsCount,
            totalPaid = bill.toPay,
            totalSaved = bill.totalSavings,
            etaMinutes = if (hasFood) 22 else 10,
            stageIndex = 0,
            riderName = if (hasFood) "Arjun Shetty" else "Suresh Kumar",
            riderRating = 4.9,
            vehicleLabel = "QuickDrop EV • KA-01-EV-4821",
            hubOrKitchen = if (hasFood) {
                snapshot.cartItems.firstOrNull { !it.isGrocery }?.brandOrRestaurant ?: "Partner Kitchen"
            } else {
                snapshot.selectedAddress.hubName
            },
            deliveryAddress = snapshot.selectedAddress.fullAddress,
            isFoodOrder = hasFood,
            reorderCartItems = snapshot.cartItems
        )

        _uiState.update { state ->
            state.copy(
                cartItems = emptyList(),
                orders = listOf(newOrder) + state.orders,
                trackingOrderId = orderId,
                selectedRestaurantId = null,
                statusMessage = "Order $orderId placed!"
            )
        }

        viewModelScope.launch {
            delay(3500L)
            updateOrderStage(orderId, 1, eta = if (hasFood) 16 else 7)
            delay(4500L)
            updateOrderStage(orderId, 2, eta = if (hasFood) 8 else 3)
        }
    }

    private fun updateOrderStage(orderId: String, stage: Int, eta: Int) {
        _uiState.update { state ->
            state.copy(
                orders = state.orders.map {
                    if (it.orderId == orderId && it.stageIndex < stage) {
                        it.copy(stageIndex = stage, etaMinutes = eta)
                    } else it
                }
            )
        }
    }

    fun advanceOrderStage(orderId: String) {
        _uiState.update { state ->
            state.copy(
                orders = state.orders.map {
                    if (it.orderId == orderId && it.stageIndex < 3) {
                        val nextStage = it.stageIndex + 1
                        val nextEta = when (nextStage) {
                            1 -> if (it.isFoodOrder) 16 else 7
                            2 -> if (it.isFoodOrder) 6 else 2
                            else -> 0
                        }
                        it.copy(stageIndex = nextStage, etaMinutes = nextEta)
                    } else it
                }
            )
        }
    }

    fun openOrderTracking(orderId: String) {
        _uiState.update { it.copy(trackingOrderId = orderId, isAdminMode = false) }
    }

    fun closeOrderTracking() {
        _uiState.update { it.copy(trackingOrderId = null) }
    }

    fun reorder(order: ActiveOrder) {
        if (order.reorderCartItems.isEmpty()) return
        _uiState.update { state ->
            state.copy(
                cartItems = order.reorderCartItems,
                currentTab = BottomTab.CART,
                trackingOrderId = null,
                isAdminMode = false,
                statusMessage = "Items added to cart from ${order.orderId}"
            )
        }
    }

    fun toggleGlobalVegMode() {
        _uiState.update { state ->
            val next = !state.isGlobalVegMode
            state.copy(
                isGlobalVegMode = next,
                statusMessage = if (next) "Pure Veg mode enabled" else "Pure Veg mode disabled"
            )
        }
    }

    fun toggleDarkTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun consumeStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    // ==================== ADMIN SECURITY & LOCK ====================

    fun openAdminPanel() {
        _uiState.update {
            it.copy(
                isAdminMode = true,
                isAdminUnlocked = false,
                adminUnlockError = null,
                selectedRestaurantId = null,
                trackingOrderId = null
            )
        }
    }

    fun unlockAdmin(password: String) {
        val candidateHash = hashPasscode(password.trim())
        if (candidateHash == adminPasswordHash) {
            _uiState.update {
                it.copy(
                    isAdminUnlocked = true,
                    adminUnlockError = null,
                    statusMessage = "Admin Panel unlocked"
                )
            }
        } else {
            _uiState.update {
                it.copy(adminUnlockError = "Incorrect admin password. Access denied.")
            }
        }
    }

    fun lockAdmin() {
        _uiState.update {
            it.copy(
                isAdminUnlocked = false,
                isAdminMode = false,
                adminUnlockError = null,
                statusMessage = "Admin Panel locked"
            )
        }
    }

    fun closeAdminPanel() {
        _uiState.update {
            it.copy(
                isAdminMode = false,
                isAdminUnlocked = false,
                adminUnlockError = null
            )
        }
    }

    fun clearAdminUnlockError() {
        _uiState.update { it.copy(adminUnlockError = null) }
    }

    fun changeAdminPassword(currentPassword: String, newPassword: String): Boolean {
        if (hashPasscode(currentPassword.trim()) != adminPasswordHash || newPassword.trim().length < 4) {
            return false
        }
        adminPasswordHash = hashPasscode(newPassword.trim())
        _uiState.update { it.copy(statusMessage = "Admin password updated") }
        return true
    }

    fun selectAdminTab(tab: AdminTab) {
        _uiState.update { it.copy(adminTab = tab) }
    }

    // ==================== STORE & DELIVERY FEES ====================

    fun toggleStoreOnline() {
        _uiState.update { state ->
            val next = !state.isStoreOnline
            state.copy(
                isStoreOnline = next,
                statusMessage = if (next) "QuickDrop Darkstores & Kitchens LIVE" else "Orders paused by Admin"
            )
        }
    }

    fun toggleWeatherSurge() {
        _uiState.update { state ->
            val next = !state.weatherSurgeEnabled
            state.copy(
                weatherSurgeEnabled = next,
                statusMessage = if (next) "Weather surge (+₹${state.weatherSurgeAmount}) enabled" else "Weather surge removed"
            )
        }
    }

    fun updateFeeConfig(
        baseDeliveryFee: Int,
        handlingFee: Int,
        weatherSurgeFee: Int,
        freeThreshold: Int
    ) {
        _uiState.update {
            it.copy(
                baseDeliveryFeeAmount = baseDeliveryFee.coerceAtLeast(0),
                handlingFeeAmount = handlingFee.coerceAtLeast(0),
                weatherSurgeAmount = weatherSurgeFee.coerceAtLeast(0),
                freeDeliveryThreshold = freeThreshold.coerceAtLeast(50),
                statusMessage = "Delivery fees & threshold updated"
            )
        }
    }

    fun setFreeDeliveryThreshold(threshold: Int) {
        _uiState.update {
            it.copy(
                freeDeliveryThreshold = threshold.coerceAtLeast(50),
                statusMessage = "Free delivery threshold set to ₹$threshold"
            )
        }
    }

    // ==================== ORDER DISPATCH CRUD ====================

    fun assignOrderRider(orderId: String, riderName: String) {
        _uiState.update { state ->
            state.copy(
                orders = state.orders.map {
                    if (it.orderId == orderId) it.copy(riderName = riderName) else it
                },
                statusMessage = "Assigned $riderName to $orderId"
            )
        }
    }

    fun updateOrderDetails(orderId: String, etaMinutes: Int, stageIndex: Int, deliveryAddress: String) {
        _uiState.update { state ->
            state.copy(
                orders = state.orders.map {
                    if (it.orderId == orderId) {
                        it.copy(
                            etaMinutes = etaMinutes.coerceAtLeast(0),
                            stageIndex = stageIndex.coerceIn(0, 3),
                            deliveryAddress = deliveryAddress.trim().ifEmpty { it.deliveryAddress }
                        )
                    } else it
                },
                statusMessage = "Updated order $orderId"
            )
        }
    }

    fun deleteOrder(orderId: String) {
        _uiState.update { state ->
            state.copy(
                orders = state.orders.filterNot { it.orderId == orderId },
                trackingOrderId = if (state.trackingOrderId == orderId) null else state.trackingOrderId,
                statusMessage = "Removed order $orderId"
            )
        }
    }

    // ==================== GROCERY PRODUCT & CATEGORY CRUD ====================

    fun toggleProductStock(productId: String) {
        _uiState.update { state ->
            val updated = state.products.map {
                if (it.id == productId) it.copy(isInStock = !it.isInStock) else it
            }
            state.copy(products = updated)
        }
    }

    fun updateProductFull(
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
    ) {
        val cleanVariants = variants.map {
            val p = it.price.coerceAtLeast(1)
            it.copy(price = p, mrp = it.mrp.coerceAtLeast(p))
        }
        if (cleanVariants.isEmpty()) return
        _uiState.update { state ->
            val updated = state.products.map { prod ->
                if (prod.id == productId) {
                    prod.copy(
                        brand = brand.trim().ifEmpty { prod.brand },
                        name = name.trim().ifEmpty { prod.name },
                        categoryId = categoryId,
                        subcategory = subcategory.ifBlank { "All" },
                        etaMinutes = etaMinutes.coerceIn(5, 60),
                        isVeg = isVeg,
                        isInStock = isInStock,
                        isBestseller = isBestseller,
                        isStealDeal = isStealDeal,
                        badgeText = badgeText?.trim()?.takeIf { it.isNotEmpty() },
                        variants = cleanVariants,
                        defaultVariantIndex = prod.defaultVariantIndex.coerceIn(0, cleanVariants.lastIndex)
                    )
                } else {
                    if (isStealDeal) prod.copy(isStealDeal = false) else prod
                }
            }
            // Keep cart item prices synced with updated default/matching variant
            val updatedCart = state.cartItems.map { cartItem ->
                if (cartItem.productId == productId && cartItem.isGrocery) {
                    val matchedProd = updated.firstOrNull { it.id == productId }
                    val matchedVar = matchedProd?.variants?.firstOrNull { it.id == cartItem.variantId }
                    if (matchedProd != null && matchedVar != null) {
                        cartItem.copy(
                            title = matchedProd.name,
                            brandOrRestaurant = matchedProd.brand,
                            unitLabel = matchedVar.label,
                            price = matchedVar.price,
                            mrp = matchedVar.mrp,
                            isVeg = matchedProd.isVeg
                        )
                    } else cartItem
                } else cartItem
            }
            state.copy(
                products = updated,
                cartItems = updatedCart,
                statusMessage = "Updated \"$name\""
            )
        }
    }

    fun deleteProduct(productId: String) {
        _uiState.update { state ->
            val targetName = state.products.firstOrNull { it.id == productId }?.name ?: "Product"
            state.copy(
                products = state.products.filterNot { it.id == productId },
                cartItems = state.cartItems.filterNot { it.productId == productId },
                statusMessage = "Removed \"$targetName\""
            )
        }
    }

    fun addNewProduct(
        brand: String,
        name: String,
        categoryId: String,
        subcategory: String,
        unitLabel: String,
        price: Int,
        mrp: Int,
        isVeg: Boolean,
        etaMinutes: Int = 10,
        isBestseller: Boolean = true
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        val safePrice = price.coerceAtLeast(1)
        val safeMrp = mrp.coerceAtLeast(safePrice)
        val prodId = nextId("prod_custom")
        val newProd = GroceryProduct(
            id = prodId,
            brand = brand.trim().ifEmpty { "QuickDrop Fresh" },
            name = cleanName,
            categoryId = categoryId,
            subcategory = subcategory.ifBlank { "All" },
            variants = listOf(
                ProductVariant(
                    id = "${prodId}_v1",
                    label = unitLabel.trim().ifEmpty { "1 pack" },
                    price = safePrice,
                    mrp = safeMrp
                )
            ),
            etaMinutes = etaMinutes.coerceIn(5, 60),
            isVeg = isVeg,
            isInStock = true,
            isBestseller = isBestseller,
            badgeText = "NEW",
            tintHex = 0xFFE8F5E9
        )
        _uiState.update { state ->
            state.copy(
                products = listOf(newProd) + state.products,
                statusMessage = "Added \"$cleanName\" to inventory"
            )
        }
    }

    fun addCategory(name: String, hasSaleBadge: Boolean) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        val id = nextId("cat")
        val newCat = GroceryCategory(
            id = id,
            name = cleanName,
            shortName = cleanName.replace(" & ", "\n& "),
            iconKey = "shopping_basket",
            hasSaleBadge = hasSaleBadge,
            tintHex = 0xFFE8F5E9,
            subcategories = listOf("All", cleanName)
        )
        _uiState.update { state ->
            state.copy(
                categories = state.categories + newCat,
                statusMessage = "Category \"$cleanName\" added"
            )
        }
    }

    fun updateCategory(categoryId: String, name: String, hasSaleBadge: Boolean) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        _uiState.update { state ->
            state.copy(
                categories = state.categories.map {
                    if (it.id == categoryId) {
                        it.copy(
                            name = cleanName,
                            shortName = cleanName.replace(" & ", "\n& "),
                            hasSaleBadge = hasSaleBadge
                        )
                    } else it
                },
                statusMessage = "Category updated"
            )
        }
    }

    fun deleteCategory(categoryId: String) {
        _uiState.update { state ->
            if (state.categories.size <= 1) {
                return@update state.copy(statusMessage = "At least 1 category is required")
            }
            val remaining = state.categories.filterNot { it.id == categoryId }
            val nextSelected = if (state.selectedCategoryId == categoryId) remaining.first().id else state.selectedCategoryId
            state.copy(
                categories = remaining,
                selectedCategoryId = nextSelected,
                statusMessage = "Category removed"
            )
        }
    }

    // ==================== RESTAURANT & MENU DISH CRUD ====================

    fun toggleRestaurantAcceptingOrders(restaurantId: String) {
        _uiState.update { state ->
            state.copy(
                restaurants = state.restaurants.map {
                    if (it.id == restaurantId) it.copy(isAcceptingOrders = !it.isAcceptingOrders) else it
                }
            )
        }
    }

    fun updateRestaurantFull(
        restaurantId: String,
        name: String,
        cuisines: String,
        area: String,
        etaMins: Int,
        priceForTwo: Int,
        offerBanner: String,
        isPureVeg: Boolean,
        isAcceptingOrders: Boolean
    ) {
        _uiState.update { state ->
            state.copy(
                restaurants = state.restaurants.map {
                    if (it.id == restaurantId) {
                        it.copy(
                            name = name.trim().ifEmpty { it.name },
                            cuisines = cuisines.trim().ifEmpty { it.cuisines },
                            area = area.trim().ifEmpty { it.area },
                            deliveryTimeMins = etaMins.coerceIn(10, 90),
                            priceForTwo = priceForTwo.coerceAtLeast(50),
                            offerBanner = offerBanner.trim().ifEmpty { it.offerBanner },
                            isPureVeg = isPureVeg,
                            isAcceptingOrders = isAcceptingOrders
                        )
                    } else it
                },
                statusMessage = "Updated \"$name\""
            )
        }
    }

    fun deleteRestaurant(restaurantId: String) {
        _uiState.update { state ->
            val targetName = state.restaurants.firstOrNull { it.id == restaurantId }?.name ?: "Restaurant"
            val dishIds = state.restaurants.firstOrNull { it.id == restaurantId }?.menu?.map { it.id }?.toSet() ?: emptySet()
            state.copy(
                restaurants = state.restaurants.filterNot { it.id == restaurantId },
                selectedRestaurantId = if (state.selectedRestaurantId == restaurantId) null else state.selectedRestaurantId,
                cartItems = state.cartItems.filterNot { dishIds.contains(it.productId) },
                statusMessage = "Removed \"$targetName\""
            )
        }
    }

    fun addNewRestaurant(
        name: String,
        cuisines: String,
        area: String,
        etaMins: Int,
        priceForTwo: Int,
        offerBanner: String,
        isPureVeg: Boolean
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        val restId = nextId("rest_custom")
        val starterDish = MenuItem(
            id = nextId("dish"),
            restaurantId = restId,
            restaurantName = cleanName,
            name = "$cleanName Signature Bowl",
            description = "Chef's freshly prepared signature platter.",
            price = 249,
            mrp = 299,
            category = "Recommended",
            isVeg = isPureVeg,
            isBestseller = true,
            rating = 4.6,
            ratingCount = "120+"
        )
        val newRest = Restaurant(
            id = restId,
            name = cleanName,
            cuisines = cuisines.trim().ifEmpty { "North Indian, Bowls" },
            rating = 4.5,
            ratingCountText = "500+",
            deliveryTimeMins = etaMins.coerceIn(12, 60),
            distanceKm = 1.4,
            priceForTwo = priceForTwo.coerceAtLeast(100),
            offerBanner = offerBanner.trim().ifEmpty { "Flat 20% OFF" },
            area = area.trim().ifEmpty { "Indiranagar" },
            isPureVeg = isPureVeg,
            menu = listOf(starterDish)
        )
        _uiState.update { state ->
            state.copy(
                restaurants = listOf(newRest) + state.restaurants,
                statusMessage = "Onboarded \"$cleanName\""
            )
        }
    }

    fun addDishToRestaurant(
        restaurantId: String,
        name: String,
        description: String,
        category: String,
        price: Int,
        mrp: Int,
        isVeg: Boolean,
        isBestseller: Boolean,
        isSpicy: Boolean
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        val safePrice = price.coerceAtLeast(1)
        val safeMrp = mrp.coerceAtLeast(safePrice)
        _uiState.update { state ->
            state.copy(
                restaurants = state.restaurants.map { rest ->
                    if (rest.id == restaurantId) {
                        val dish = MenuItem(
                            id = nextId("dish"),
                            restaurantId = rest.id,
                            restaurantName = rest.name,
                            name = cleanName,
                            description = description.trim().ifEmpty { "Freshly prepared house specialty." },
                            price = safePrice,
                            mrp = safeMrp,
                            category = category.ifBlank { "Recommended" },
                            isVeg = isVeg,
                            isBestseller = isBestseller,
                            isSpicy = isSpicy
                        )
                        rest.copy(menu = listOf(dish) + rest.menu)
                    } else rest
                },
                statusMessage = "Added dish \"$cleanName\""
            )
        }
    }

    fun updateDishInRestaurant(
        restaurantId: String,
        dishId: String,
        name: String,
        description: String,
        category: String,
        price: Int,
        mrp: Int,
        isVeg: Boolean,
        isBestseller: Boolean,
        isSpicy: Boolean
    ) {
        val safePrice = price.coerceAtLeast(1)
        val safeMrp = mrp.coerceAtLeast(safePrice)
        _uiState.update { state ->
            val updatedRests = state.restaurants.map { rest ->
                if (rest.id == restaurantId) {
                    rest.copy(
                        menu = rest.menu.map { dish ->
                            if (dish.id == dishId) {
                                dish.copy(
                                    name = name.trim().ifEmpty { dish.name },
                                    description = description.trim().ifEmpty { dish.description },
                                    category = category.trim().ifEmpty { dish.category },
                                    price = safePrice,
                                    mrp = safeMrp,
                                    isVeg = isVeg,
                                    isBestseller = isBestseller,
                                    isSpicy = isSpicy
                                )
                            } else dish
                        }
                    )
                } else rest
            }
            val updatedCart = state.cartItems.map { ci ->
                if (ci.id == dishId) {
                    ci.copy(
                        title = name.trim().ifEmpty { ci.title },
                        price = safePrice,
                        mrp = safeMrp,
                        isVeg = isVeg
                    )
                } else ci
            }
            state.copy(
                restaurants = updatedRests,
                cartItems = updatedCart,
                statusMessage = "Updated dish \"$name\""
            )
        }
    }

    fun deleteDishFromRestaurant(restaurantId: String, dishId: String) {
        _uiState.update { state ->
            state.copy(
                restaurants = state.restaurants.map { rest ->
                    if (rest.id == restaurantId) {
                        rest.copy(menu = rest.menu.filterNot { it.id == dishId })
                    } else rest
                },
                cartItems = state.cartItems.filterNot { it.id == dishId },
                statusMessage = "Removed dish from menu"
            )
        }
    }

    // ==================== COUPONS & PROMO BANNERS CRUD ====================

    fun toggleCouponActive(code: String) {
        _uiState.update { state ->
            state.copy(
                coupons = state.coupons.map {
                    if (it.code == code) it.copy(isActive = !it.isActive) else it
                }
            )
        }
    }

    fun addNewCoupon(
        code: String,
        title: String,
        description: String,
        discountAmount: Int,
        minOrder: Int,
        freeDelivery: Boolean
    ) {
        val cleanCode = code.trim().uppercase().replace(" ", "")
        if (cleanCode.isEmpty()) return
        val coupon = Coupon(
            code = cleanCode,
            title = title.trim().ifEmpty { "Flat ₹$discountAmount OFF" },
            description = description.trim().ifEmpty { "Valid on orders above ₹$minOrder" },
            discountAmount = discountAmount.coerceAtLeast(0),
            minOrder = minOrder.coerceAtLeast(0),
            freeDelivery = freeDelivery,
            isActive = true
        )
        _uiState.update { state ->
            state.copy(
                coupons = listOf(coupon) + state.coupons.filterNot { it.code == cleanCode },
                statusMessage = "Promo code $cleanCode published"
            )
        }
    }

    fun updateCoupon(
        oldCode: String,
        title: String,
        description: String,
        discountAmount: Int,
        minOrder: Int,
        freeDelivery: Boolean,
        isActive: Boolean
    ) {
        _uiState.update { state ->
            state.copy(
                coupons = state.coupons.map {
                    if (it.code == oldCode) {
                        it.copy(
                            title = title.trim().ifEmpty { it.title },
                            description = description.trim().ifEmpty { it.description },
                            discountAmount = discountAmount.coerceAtLeast(0),
                            minOrder = minOrder.coerceAtLeast(0),
                            freeDelivery = freeDelivery,
                            isActive = isActive
                        )
                    } else it
                },
                statusMessage = "Updated coupon $oldCode"
            )
        }
    }

    fun deleteCoupon(code: String) {
        _uiState.update { state ->
            state.copy(
                coupons = state.coupons.filterNot { it.code == code },
                appliedCouponCode = if (state.appliedCouponCode == code) null else state.appliedCouponCode,
                statusMessage = "Deleted coupon $code"
            )
        }
    }

    fun addPromoBanner(tag: String, title: String, subtitle: String, ctaText: String, isFoodPromo: Boolean) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val banner = PromoBanner(
            id = nextId("promo"),
            tag = tag.trim().uppercase().ifEmpty { "ADMIN OFFER" },
            title = cleanTitle,
            subtitle = subtitle.trim().ifEmpty { "Limited time offer on QuickDrop" },
            ctaText = ctaText.trim().ifEmpty { "Shop Now" },
            targetCategoryId = null,
            isFoodPromo = isFoodPromo,
            bgHex = if (isFoodPromo) 0xFFE23744 else 0xFF0C831F,
            contentDark = false
        )
        _uiState.update { state ->
            if (isFoodPromo) {
                state.copy(foodPromos = listOf(banner) + state.foodPromos, statusMessage = "Added food banner")
            } else {
                state.copy(groceryPromos = listOf(banner) + state.groceryPromos, statusMessage = "Added grocery banner")
            }
        }
    }

    fun deletePromoBanner(promoId: String, isFoodPromo: Boolean) {
        _uiState.update { state ->
            if (isFoodPromo) {
                state.copy(foodPromos = state.foodPromos.filterNot { it.id == promoId }, statusMessage = "Removed food banner")
            } else {
                state.copy(groceryPromos = state.groceryPromos.filterNot { it.id == promoId }, statusMessage = "Removed grocery banner")
            }
        }
    }
}
