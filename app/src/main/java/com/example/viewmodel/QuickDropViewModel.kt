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
    val isShopkeeperMode: Boolean = false,
    val shopApplications: List<ShopkeeperApplication> = defaultSeededShopApplications(),
    val activeShopApplicationId: String? = "shop_app_seed_1",

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
    val isDetectingGpsLocation: Boolean = false,
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

    val pendingShopApplicationsCount: Int
        get() = shopApplications.count { it.status == ShopApplicationStatus.PENDING_REVIEW }

    val activeShopApplication: ShopkeeperApplication?
        get() = shopApplications.firstOrNull { it.id == activeShopApplicationId } ?: shopApplications.firstOrNull()

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
        isFoodOrder = false,
        deliveryOtp = "4829",
        isOtpVerified = false
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
        deliveryOtp = "7315",
        isOtpVerified = true,
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

private fun defaultSeededShopApplications(): List<ShopkeeperApplication> = listOf(
    ShopkeeperApplication(
        id = "shop_app_seed_1",
        shopName = "Sharma Fresh Kirana & Dairy",
        ownerName = "Rajesh Sharma",
        phone = "9876543210",
        shopType = ShopType.GROCERY,
        area = "Indiranagar 100ft Rd",
        fullAddress = "Shop 14, 12th Main, HAL 2nd Stage, Indiranagar",
        etaMins = 12,
        gstOrLicenseNote = "GSTIN 29ABCPS1234F1Z2 • FSSAI Verified",
        submittedAtText = "10 mins ago",
        status = ShopApplicationStatus.PENDING_REVIEW,
        listings = listOf(
            ShopListingDraft(
                id = "draft_seed_1",
                title = "Farm Fresh Desi Cow A2 Milk",
                categoryOrCuisine = "dairy",
                unitOrPortion = "500 ml bottle",
                price = 48,
                mrp = 55,
                isVeg = true,
                description = "Morning farm-bottled pure A2 milk."
            ),
            ShopListingDraft(
                id = "draft_seed_2",
                title = "Chakki Fresh Sharbati Atta",
                categoryOrCuisine = "atta_rice",
                unitOrPortion = "5 kg pack",
                price = 265,
                mrp = 310,
                isVeg = true,
                description = "Stone-ground MP Sharbati whole wheat flour."
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
                isAdminMode = false,
                isShopkeeperMode = false
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
                isAdminMode = false,
                isShopkeeperMode = false
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

    fun setGpsDetecting(detecting: Boolean) {
        _uiState.update { it.copy(isDetectingGpsLocation = detecting) }
    }

    fun showStatusNotice(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    fun applyGpsDetectedLocation(areaShort: String, fullAddress: String) {
        val cleanArea = areaShort.trim().ifEmpty { "Current GPS Area" }
        val cleanFull = fullAddress.trim().ifEmpty { cleanArea }
        val gpsAddr = DeliveryAddress(
            id = "addr_gps_live",
            label = "Current GPS",
            fullAddress = cleanFull,
            areaShort = cleanArea,
            hubName = "Nearest QuickDrop Hub",
            distanceKm = 0.6
        )
        _uiState.update { state ->
            val withoutOldGps = state.addresses.filterNot { it.id == "addr_gps_live" }
            state.copy(
                addresses = listOf(gpsAddr) + withoutOldGps,
                selectedAddress = gpsAddr,
                isDetectingGpsLocation = false,
                statusMessage = "GPS location detected: $cleanArea"
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
                    imageUrl = product.imageUrl,
                    sellerShopId = product.sellerShopId
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
            val restShopId = state.restaurants.firstOrNull { it.id == dish.restaurantId }?.sellerShopId
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
                    imageUrl = dish.imageUrl,
                    sellerShopId = restShopId
                )
            }
            state.copy(cartItems = updated)
        }
    }

    fun addCustomItemToCart(itemName: String, price: Int, unitLabel: String = "1 extra") {
        val cleanName = itemName.trim()
        if (cleanName.isEmpty()) return
        val safePrice = price.coerceAtLeast(1)
        val itemId = nextId("cart_extra")
        val customCartItem = CartItem(
            id = itemId,
            productId = itemId,
            variantId = "custom",
            title = cleanName,
            brandOrRestaurant = "Extra Item",
            unitLabel = unitLabel.trim().ifEmpty { "1 extra" },
            price = safePrice,
            mrp = safePrice,
            quantity = 1,
            isGrocery = true,
            isVeg = true
        )
        _uiState.update { state ->
            state.copy(
                cartItems = state.cartItems + customCartItem,
                statusMessage = "Added \"$cleanName\" (₹$safePrice) to cart"
            )
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
        val otp = (1000..9999).random().toString()
        var pickupOtp = (1000..9999).random().toString()
        if (pickupOtp == otp) {
            pickupOtp = (((pickupOtp.toInt() + 137) % 9000) + 1000).toString()
        }
        val matchedShop = snapshot.cartItems.firstNotNullOfOrNull { item ->
            item.sellerShopId?.let { sid ->
                snapshot.shopApplications.firstOrNull { it.id == sid && it.status == ShopApplicationStatus.APPROVED }
            }
        } ?: snapshot.shopApplications.firstOrNull { app ->
            app.status == ShopApplicationStatus.APPROVED &&
                snapshot.cartItems.any { it.brandOrRestaurant.equals(app.shopName, ignoreCase = true) }
        }

        val newOrder = ActiveOrder(
            orderId = orderId,
            placedTimeText = "Just now • Paid via $paymentMethod",
            summaryTitle = summary,
            itemsCount = snapshot.totalCartItemsCount,
            totalPaid = bill.toPay,
            totalSaved = bill.totalSavings,
            etaMinutes = matchedShop?.etaMins ?: if (hasFood) 22 else 10,
            stageIndex = 0,
            riderName = if (hasFood) "Arjun Shetty" else "Suresh Kumar",
            riderRating = 4.9,
            vehicleLabel = "QuickDrop EV • KA-01-EV-4821",
            hubOrKitchen = matchedShop?.shopName ?: if (hasFood) {
                snapshot.cartItems.firstOrNull { !it.isGrocery }?.brandOrRestaurant ?: "Partner Kitchen"
            } else {
                snapshot.selectedAddress.hubName
            },
            deliveryAddress = snapshot.selectedAddress.fullAddress,
            isFoodOrder = hasFood,
            shopId = matchedShop?.id,
            shopPickupOtp = pickupOtp,
            isShopPickupOtpVerified = false,
            deliveryOtp = otp,
            isOtpVerified = false,
            reorderCartItems = snapshot.cartItems
        )

        _uiState.update { state ->
            state.copy(
                cartItems = emptyList(),
                orders = listOf(newOrder) + state.orders,
                trackingOrderId = orderId,
                selectedRestaurantId = null,
                statusMessage = if (matchedShop != null) {
                    "Order $orderId sent to ${matchedShop.shopName}! Delivery OTP: $otp"
                } else {
                    "Order $orderId placed! Delivery OTP: $otp"
                }
            )
        }
    }

    fun advanceOrderStage(orderId: String) {
        if (!_uiState.value.isAdminUnlocked) {
            _uiState.update { it.copy(statusMessage = "Only Admin can update order status") }
            return
        }
        val targetOrder = _uiState.value.orders.firstOrNull { it.orderId == orderId } ?: return
        if (targetOrder.stageIndex >= 2) {
            _uiState.update {
                it.copy(statusMessage = "Enter customer's 4-digit OTP to complete delivery")
            }
            return
        }
        _uiState.update { state ->
            state.copy(
                orders = state.orders.map {
                    if (it.orderId == orderId && it.stageIndex < 2) {
                        val nextStage = it.stageIndex + 1
                        val nextEta = when (nextStage) {
                            1 -> if (it.isFoodOrder) 16 else 7
                            else -> if (it.isFoodOrder) 6 else 2
                        }
                        it.copy(stageIndex = nextStage, etaMinutes = nextEta)
                    } else it
                }
            )
        }
    }

    fun verifyDeliveryOtpAndCompleteOrder(orderId: String, enteredOtp: String): Boolean {
        val stateSnapshot = _uiState.value
        if (!stateSnapshot.isAdminUnlocked) {
            _uiState.update { it.copy(statusMessage = "Only Admin / Rider can verify delivery OTP") }
            return false
        }
        val target = stateSnapshot.orders.firstOrNull { it.orderId == orderId } ?: return false
        val cleanOtp = enteredOtp.trim()
        if (cleanOtp.length != 4 || cleanOtp != target.deliveryOtp) {
            return false
        }
        _uiState.update { state ->
            state.copy(
                orders = state.orders.map {
                    if (it.orderId == orderId) {
                        it.copy(
                            stageIndex = 3,
                            etaMinutes = 0,
                            isOtpVerified = true
                        )
                    } else it
                },
                statusMessage = "OTP verified! Ride finished & $orderId delivered"
            )
        }
        return true
    }

    fun addExtraProductToOrder(orderId: String, product: GroceryProduct) {
        val safeIdx = product.defaultVariantIndex.coerceIn(0, product.variants.lastIndex)
        val variant = product.variants[safeIdx]
        val extraCartItem = CartItem(
            id = "${product.id}__${variant.id}",
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
            imageUrl = product.imageUrl,
            sellerShopId = product.sellerShopId
        )
        appendExtraCartItemToOrder(orderId, extraCartItem)
    }

    fun addExtraDishToOrder(orderId: String, dish: MenuItem) {
        val restShopId = _uiState.value.restaurants.firstOrNull { it.id == dish.restaurantId }?.sellerShopId
        val extraCartItem = CartItem(
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
            imageUrl = dish.imageUrl,
            sellerShopId = restShopId
        )
        appendExtraCartItemToOrder(orderId, extraCartItem)
    }

    fun addCustomExtraToOrder(orderId: String, itemName: String, price: Int, unitLabel: String = "1 extra") {
        val cleanName = itemName.trim()
        if (cleanName.isEmpty()) return
        val safePrice = price.coerceAtLeast(1)
        val customId = nextId("extra_item")
        val extraCartItem = CartItem(
            id = customId,
            productId = customId,
            variantId = "custom",
            title = cleanName,
            brandOrRestaurant = "Extra Item",
            unitLabel = unitLabel.trim().ifEmpty { "1 extra" },
            price = safePrice,
            mrp = safePrice,
            quantity = 1,
            isGrocery = true,
            isVeg = true
        )
        appendExtraCartItemToOrder(orderId, extraCartItem)
    }

    private fun appendExtraCartItemToOrder(orderId: String, extraItem: CartItem) {
        _uiState.update { state ->
            val target = state.orders.firstOrNull { it.orderId == orderId } ?: return@update state
            if (target.stageIndex >= 2 && !state.isAdminUnlocked) {
                return@update state.copy(statusMessage = "Order is already out for delivery")
            }
            val updatedItems = if (target.reorderCartItems.any { it.id == extraItem.id }) {
                target.reorderCartItems.map {
                    if (it.id == extraItem.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                target.reorderCartItems + extraItem
            }
            val newItemsCount = target.itemsCount + 1
            val savedDelta = (extraItem.mrp - extraItem.price).coerceAtLeast(0)
            val newSummary = "${target.summaryTitle} + ${extraItem.title}"
            state.copy(
                orders = state.orders.map {
                    if (it.orderId == orderId) {
                        it.copy(
                            summaryTitle = newSummary,
                            itemsCount = newItemsCount,
                            totalPaid = it.totalPaid + extraItem.price,
                            totalSaved = it.totalSaved + savedDelta,
                            reorderCartItems = updatedItems
                        )
                    } else it
                },
                statusMessage = "Added ${extraItem.title} (+₹${extraItem.price}) to $orderId"
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
                isShopkeeperMode = false,
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
        if (!_uiState.value.isAdminUnlocked) {
            _uiState.update { it.copy(statusMessage = "Only Admin can assign rider") }
            return
        }
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
        if (!_uiState.value.isAdminUnlocked) {
            _uiState.update { it.copy(statusMessage = "Only Admin can edit order details") }
            return
        }
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
        if (!_uiState.value.isAdminUnlocked) return
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
        isBestseller: Boolean = true,
        badgeText: String? = "NEW",
        isMissedQuickAdd: Boolean = false,
        extraVariants: List<ProductVariant> = emptyList()
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        val safePrice = price.coerceAtLeast(1)
        val safeMrp = mrp.coerceAtLeast(safePrice)
        val prodId = nextId("prod_custom")
        val primaryVariant = ProductVariant(
            id = "${prodId}_v1",
            label = unitLabel.trim().ifEmpty { "1 pack" },
            price = safePrice,
            mrp = safeMrp
        )
        val allVariants = if (extraVariants.isNotEmpty()) {
            extraVariants.mapIndexed { idx, v ->
                val vp = v.price.coerceAtLeast(1)
                v.copy(
                    id = "${prodId}_v_${idx + 1}",
                    label = v.label.trim().ifEmpty { "Pack ${idx + 1}" },
                    price = vp,
                    mrp = v.mrp.coerceAtLeast(vp)
                )
            }
        } else {
            listOf(primaryVariant)
        }
        val newProd = GroceryProduct(
            id = prodId,
            brand = brand.trim().ifEmpty { "QuickDrop Fresh" },
            name = cleanName,
            categoryId = categoryId,
            subcategory = subcategory.ifBlank { "All" },
            variants = allVariants,
            etaMinutes = etaMinutes.coerceIn(5, 60),
            isVeg = isVeg,
            isInStock = true,
            isBestseller = isBestseller,
            isMissedQuickAdd = isMissedQuickAdd,
            badgeText = badgeText?.trim()?.takeIf { it.isNotEmpty() },
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

    // ==================== SHOPKEEPER APPLICATION, LISTING & PICKUP OTP ====================

    fun openShopkeeperPortal() {
        _uiState.update {
            it.copy(
                isShopkeeperMode = true,
                isAdminMode = false,
                selectedRestaurantId = null,
                trackingOrderId = null
            )
        }
    }

    fun closeShopkeeperPortal() {
        _uiState.update { it.copy(isShopkeeperMode = false) }
    }

    fun selectShopApplication(appId: String) {
        _uiState.update { it.copy(activeShopApplicationId = appId) }
    }

    fun submitShopApplication(
        shopName: String,
        ownerName: String,
        phone: String,
        shopType: ShopType,
        area: String,
        fullAddress: String,
        etaMins: Int,
        gstOrLicenseNote: String,
        initialListings: List<ShopListingDraft>
    ): String? {
        val cleanShop = shopName.trim()
        val cleanOwner = ownerName.trim()
        val cleanPhone = phone.trim()
        if (cleanShop.isEmpty() || cleanOwner.isEmpty() || cleanPhone.isEmpty()) {
            _uiState.update { it.copy(statusMessage = "Please fill Shop Name, Owner Name & Phone") }
            return null
        }
        val appId = nextId("shop_app")
        val cleanListings = initialListings.mapIndexed { idx, item ->
            val safePrice = item.price.coerceAtLeast(1)
            item.copy(
                id = if (item.id.isBlank()) "${appId}_draft_${idx + 1}" else item.id,
                title = item.title.trim().ifEmpty { "Shop Item ${idx + 1}" },
                price = safePrice,
                mrp = item.mrp.coerceAtLeast(safePrice),
                unitOrPortion = item.unitOrPortion.trim().ifEmpty { "1 pack" },
                publishedTargetId = null
            )
        }
        val application = ShopkeeperApplication(
            id = appId,
            shopName = cleanShop,
            ownerName = cleanOwner,
            phone = cleanPhone,
            shopType = shopType,
            area = area.trim().ifEmpty { "Local Market" },
            fullAddress = fullAddress.trim().ifEmpty { area.trim().ifEmpty { "Main Market Road" } },
            etaMins = etaMins.coerceIn(8, 60),
            gstOrLicenseNote = gstOrLicenseNote.trim(),
            submittedAtText = "Just now",
            status = ShopApplicationStatus.PENDING_REVIEW,
            listings = cleanListings
        )
        _uiState.update { state ->
            state.copy(
                shopApplications = listOf(application) + state.shopApplications,
                activeShopApplicationId = appId,
                statusMessage = "Application submitted for \"$cleanShop\"! Waiting for Admin approval."
            )
        }
        return appId
    }

    fun addListingToShop(
        appId: String,
        title: String,
        categoryOrCuisine: String,
        unitOrPortion: String,
        price: Int,
        mrp: Int,
        isVeg: Boolean,
        description: String = ""
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val safePrice = price.coerceAtLeast(1)
        val safeMrp = mrp.coerceAtLeast(safePrice)
        val listingId = nextId("shop_item")

        _uiState.update { state ->
            val app = state.shopApplications.firstOrNull { it.id == appId } ?: return@update state
            val draft = ShopListingDraft(
                id = listingId,
                title = cleanTitle,
                categoryOrCuisine = categoryOrCuisine.ifBlank {
                    if (app.shopType == ShopType.GROCERY) "dairy" else "Recommended"
                },
                unitOrPortion = unitOrPortion.trim().ifEmpty {
                    if (app.shopType == ShopType.GROCERY) "1 pack" else "1 portion"
                },
                price = safePrice,
                mrp = safeMrp,
                isVeg = isVeg,
                description = description.trim()
            )

            if (app.status != ShopApplicationStatus.APPROVED) {
                val updatedApps = state.shopApplications.map {
                    if (it.id == appId) it.copy(listings = listOf(draft) + it.listings) else it
                }
                return@update state.copy(
                    shopApplications = updatedApps,
                    statusMessage = "Added \"$cleanTitle\" to application (goes live after Admin approval)"
                )
            }

            // Already approved -> publish live immediately
            if (app.shopType == ShopType.GROCERY) {
                val prodId = nextId("prod_shop")
                val validCategoryId = if (state.categories.any { it.id == draft.categoryOrCuisine }) {
                    draft.categoryOrCuisine
                } else {
                    state.categories.firstOrNull()?.id ?: "dairy"
                }
                val liveProduct = GroceryProduct(
                    id = prodId,
                    brand = app.shopName,
                    name = draft.title,
                    categoryId = validCategoryId,
                    subcategory = "All",
                    variants = listOf(
                        ProductVariant(
                            id = "${prodId}_v1",
                            label = draft.unitOrPortion,
                            price = draft.price,
                            mrp = draft.mrp
                        )
                    ),
                    etaMinutes = app.etaMins.coerceIn(8, 45),
                    isVeg = draft.isVeg,
                    isInStock = true,
                    isBestseller = true,
                    badgeText = "LOCAL SHOP",
                    tintHex = 0xFFE8F5E9,
                    sellerShopId = app.id
                )
                val publishedDraft = draft.copy(publishedTargetId = prodId)
                state.copy(
                    products = listOf(liveProduct) + state.products,
                    shopApplications = state.shopApplications.map {
                        if (it.id == appId) it.copy(listings = listOf(publishedDraft) + it.listings) else it
                    },
                    statusMessage = "\"$cleanTitle\" published LIVE on QuickDrop!"
                )
            } else {
                val dishId = nextId("dish_shop")
                val restId = app.linkedRestaurantId ?: nextId("rest_shop")
                val liveDish = MenuItem(
                    id = dishId,
                    restaurantId = restId,
                    restaurantName = app.shopName,
                    name = draft.title,
                    description = draft.description.ifEmpty { "Freshly prepared by ${app.shopName}" },
                    price = draft.price,
                    mrp = draft.mrp,
                    category = draft.categoryOrCuisine.ifBlank { "Recommended" },
                    isVeg = draft.isVeg,
                    isBestseller = true
                )
                val hasRest = state.restaurants.any { it.id == restId }
                val updatedRestaurants = if (hasRest) {
                    state.restaurants.map { rest ->
                        if (rest.id == restId) rest.copy(menu = listOf(liveDish) + rest.menu) else rest
                    }
                } else {
                    listOf(
                        Restaurant(
                            id = restId,
                            name = app.shopName,
                            cuisines = draft.categoryOrCuisine.ifBlank { "Local Kitchen" },
                            rating = 4.6,
                            ratingCountText = "New Partner",
                            deliveryTimeMins = app.etaMins.coerceIn(12, 60),
                            distanceKm = 1.2,
                            priceForTwo = (draft.price * 2).coerceAtLeast(150),
                            offerBanner = "Partner Launch Offer",
                            area = app.area,
                            isPureVeg = draft.isVeg,
                            isAcceptingOrders = true,
                            menu = listOf(liveDish),
                            sellerShopId = app.id
                        )
                    ) + state.restaurants
                }
                val publishedDraft = draft.copy(publishedTargetId = dishId)
                state.copy(
                    restaurants = updatedRestaurants,
                    shopApplications = state.shopApplications.map {
                        if (it.id == appId) {
                            it.copy(
                                linkedRestaurantId = restId,
                                listings = listOf(publishedDraft) + it.listings
                            )
                        } else it
                    },
                    statusMessage = "\"$cleanTitle\" added LIVE to ${app.shopName} menu!"
                )
            }
        }
    }

    fun updateShopListingPrice(appId: String, listingId: String, newPrice: Int, newMrp: Int) {
        val safePrice = newPrice.coerceAtLeast(1)
        val safeMrp = newMrp.coerceAtLeast(safePrice)
        _uiState.update { state ->
            val app = state.shopApplications.firstOrNull { it.id == appId } ?: return@update state
            val targetListing = app.listings.firstOrNull { it.id == listingId } ?: return@update state
            val updatedApps = state.shopApplications.map {
                if (it.id == appId) {
                    it.copy(
                        listings = it.listings.map { item ->
                            if (item.id == listingId) item.copy(price = safePrice, mrp = safeMrp) else item
                        }
                    )
                } else it
            }

            val targetId = targetListing.publishedTargetId
            if (app.status != ShopApplicationStatus.APPROVED || targetId == null) {
                return@update state.copy(
                    shopApplications = updatedApps,
                    statusMessage = "Updated draft price to ₹$safePrice"
                )
            }

            if (app.shopType == ShopType.GROCERY) {
                val updatedProds = state.products.map { prod ->
                    if (prod.id == targetId) {
                        prod.copy(
                            variants = prod.variants.mapIndexed { idx, v ->
                                if (idx == 0) v.copy(price = safePrice, mrp = safeMrp) else v
                            }
                        )
                    } else prod
                }
                val updatedCart = state.cartItems.map { ci ->
                    if (ci.productId == targetId) ci.copy(price = safePrice, mrp = safeMrp) else ci
                }
                state.copy(
                    shopApplications = updatedApps,
                    products = updatedProds,
                    cartItems = updatedCart,
                    statusMessage = "Live price updated to ₹$safePrice"
                )
            } else {
                val updatedRests = state.restaurants.map { rest ->
                    if (rest.id == app.linkedRestaurantId) {
                        rest.copy(
                            menu = rest.menu.map { dish ->
                                if (dish.id == targetId) dish.copy(price = safePrice, mrp = safeMrp) else dish
                            }
                        )
                    } else rest
                }
                val updatedCart = state.cartItems.map { ci ->
                    if (ci.productId == targetId) ci.copy(price = safePrice, mrp = safeMrp) else ci
                }
                state.copy(
                    shopApplications = updatedApps,
                    restaurants = updatedRests,
                    cartItems = updatedCart,
                    statusMessage = "Live dish price updated to ₹$safePrice"
                )
            }
        }
    }

    fun removeShopListing(appId: String, listingId: String) {
        _uiState.update { state ->
            val app = state.shopApplications.firstOrNull { it.id == appId } ?: return@update state
            val targetListing = app.listings.firstOrNull { it.id == listingId } ?: return@update state
            val targetId = targetListing.publishedTargetId
            val updatedApps = state.shopApplications.map {
                if (it.id == appId) {
                    it.copy(listings = it.listings.filterNot { item -> item.id == listingId })
                } else it
            }
            if (targetId == null) {
                return@update state.copy(shopApplications = updatedApps, statusMessage = "Listing removed")
            }
            val updatedProds = state.products.filterNot { it.id == targetId }
            val updatedRests = state.restaurants.map { rest ->
                if (rest.id == app.linkedRestaurantId) {
                    rest.copy(menu = rest.menu.filterNot { it.id == targetId })
                } else rest
            }
            state.copy(
                shopApplications = updatedApps,
                products = updatedProds,
                restaurants = updatedRests,
                cartItems = state.cartItems.filterNot { it.productId == targetId },
                statusMessage = "Removed \"${targetListing.title}\""
            )
        }
    }

    fun approveShopApplication(appId: String, reviewNote: String = "Verified & activated by Admin") {
        if (!_uiState.value.isAdminUnlocked) {
            _uiState.update { it.copy(statusMessage = "Only Admin can approve shopkeepers") }
            return
        }
        _uiState.update { state ->
            val app = state.shopApplications.firstOrNull { it.id == appId } ?: return@update state

            // Avoid duplicate publishing if already approved
            if (app.status == ShopApplicationStatus.APPROVED) {
                return@update state.copy(statusMessage = "\"${app.shopName}\" is already LIVE")
            }

            if (app.shopType == ShopType.GROCERY) {
                val existingIds = state.products.map { it.id }.toSet()
                val newProducts = mutableListOf<GroceryProduct>()
                val updatedListings = app.listings.map { draft ->
                    val prodId = draft.publishedTargetId ?: nextId("prod_shop")
                    val validCatId = if (state.categories.any { it.id == draft.categoryOrCuisine }) {
                        draft.categoryOrCuisine
                    } else {
                        state.categories.firstOrNull()?.id ?: "dairy"
                    }
                    if (!existingIds.contains(prodId)) {
                        newProducts += GroceryProduct(
                            id = prodId,
                            brand = app.shopName,
                            name = draft.title,
                            categoryId = validCatId,
                            subcategory = "All",
                            variants = listOf(
                                ProductVariant(
                                    id = "${prodId}_v1",
                                    label = draft.unitOrPortion,
                                    price = draft.price,
                                    mrp = draft.mrp
                                )
                            ),
                            etaMinutes = app.etaMins.coerceIn(8, 45),
                            isVeg = draft.isVeg,
                            isInStock = true,
                            isBestseller = true,
                            badgeText = "VERIFIED SHOP",
                            tintHex = 0xFFE8F5E9,
                            sellerShopId = app.id
                        )
                    }
                    draft.copy(publishedTargetId = prodId)
                }
                val updatedApps = state.shopApplications.map {
                    if (it.id == appId) {
                        it.copy(
                            status = ShopApplicationStatus.APPROVED,
                            adminReviewNote = reviewNote.trim().ifEmpty { "Approved by Admin" },
                            listings = updatedListings
                        )
                    } else it
                }
                state.copy(
                    products = newProducts + state.products,
                    shopApplications = updatedApps,
                    statusMessage = "Approved \"${app.shopName}\"! ${newProducts.size} items & Order/OTP system now LIVE."
                )
            } else {
                val restId = app.linkedRestaurantId ?: nextId("rest_shop")
                val updatedListings = app.listings.map { draft ->
                    draft.copy(publishedTargetId = draft.publishedTargetId ?: nextId("dish_shop"))
                }
                val menuItems = updatedListings.map { draft ->
                    MenuItem(
                        id = draft.publishedTargetId!!,
                        restaurantId = restId,
                        restaurantName = app.shopName,
                        name = draft.title,
                        description = draft.description.ifEmpty { "Prepared fresh by ${app.shopName}" },
                        price = draft.price,
                        mrp = draft.mrp,
                        category = draft.categoryOrCuisine.ifBlank { "Recommended" },
                        isVeg = draft.isVeg,
                        isBestseller = true
                    )
                }.ifEmpty {
                    listOf(
                        MenuItem(
                            id = nextId("dish_shop"),
                            restaurantId = restId,
                            restaurantName = app.shopName,
                            name = "${app.shopName} Special Combo",
                            description = "House special freshly prepared meal.",
                            price = 229,
                            mrp = 279,
                            category = "Recommended",
                            isVeg = true,
                            isBestseller = true
                        )
                    )
                }
                val avgPrice = (menuItems.sumOf { it.price } / menuItems.size.coerceAtLeast(1)) * 2
                val liveRestaurant = Restaurant(
                    id = restId,
                    name = app.shopName,
                    cuisines = updatedListings.map { it.categoryOrCuisine }.distinct().joinToString(", ").ifEmpty { "Partner Kitchen" },
                    rating = 4.6,
                    ratingCountText = "Verified Partner",
                    deliveryTimeMins = app.etaMins.coerceIn(12, 60),
                    distanceKm = 1.3,
                    priceForTwo = avgPrice.coerceAtLeast(150),
                    offerBanner = "Flat 20% OFF Launch Offer",
                    area = app.area,
                    isPureVeg = menuItems.all { it.isVeg },
                    isAcceptingOrders = true,
                    menu = menuItems,
                    sellerShopId = app.id
                )
                val updatedRests = listOf(liveRestaurant) + state.restaurants.filterNot { it.id == restId }
                val updatedApps = state.shopApplications.map {
                    if (it.id == appId) {
                        it.copy(
                            status = ShopApplicationStatus.APPROVED,
                            adminReviewNote = reviewNote.trim().ifEmpty { "Approved by Admin" },
                            linkedRestaurantId = restId,
                            listings = updatedListings
                        )
                    } else it
                }
                state.copy(
                    restaurants = updatedRests,
                    shopApplications = updatedApps,
                    statusMessage = "Approved \"${app.shopName}\"! Restaurant, menu & Order/OTP system now LIVE."
                )
            }
        }
    }

    fun rejectShopApplication(appId: String, reason: String) {
        if (!_uiState.value.isAdminUnlocked) {
            _uiState.update { it.copy(statusMessage = "Only Admin can reject shop applications") }
            return
        }
        _uiState.update { state ->
            val app = state.shopApplications.firstOrNull { it.id == appId } ?: return@update state
            val cleanReason = reason.trim().ifEmpty { "Needs verification details before approval." }
            val updatedApps = state.shopApplications.map {
                if (it.id == appId) {
                    it.copy(
                        status = ShopApplicationStatus.REJECTED,
                        adminReviewNote = cleanReason,
                        listings = it.listings.map { d -> d.copy(publishedTargetId = null) }
                    )
                } else it
            }
            val updatedProducts = state.products.filterNot { it.sellerShopId == appId }
            val updatedRestaurants = state.restaurants.filterNot { it.sellerShopId == appId }
            val updatedCart = state.cartItems.filterNot { it.sellerShopId == appId }
            state.copy(
                shopApplications = updatedApps,
                products = updatedProducts,
                restaurants = updatedRestaurants,
                cartItems = updatedCart,
                selectedRestaurantId = if (state.selectedRestaurantId == app.linkedRestaurantId) null else state.selectedRestaurantId,
                statusMessage = "Rejected \"${app.shopName}\""
            )
        }
    }

    fun shopkeeperMarkOrderPacked(appId: String, orderId: String) {
        _uiState.update { state ->
            val shop = state.shopApplications.firstOrNull { it.id == appId }
            if (shop == null || shop.status != ShopApplicationStatus.APPROVED) {
                return@update state.copy(statusMessage = "Shop must be approved by Admin before handling orders")
            }
            val order = state.orders.firstOrNull { it.orderId == orderId } ?: return@update state
            val belongsToShop = order.shopId == appId || order.reorderCartItems.any { it.sellerShopId == appId }
            if (!belongsToShop) return@update state
            if (order.stageIndex != 0) return@update state
            state.copy(
                orders = state.orders.map {
                    if (it.orderId == orderId) {
                        it.copy(
                            stageIndex = 1,
                            etaMinutes = if (it.isFoodOrder) 14 else 6
                        )
                    } else it
                },
                statusMessage = "Order $orderId marked Packed! Share Pickup OTP ${order.shopPickupOtp} with Rider ${order.riderName}."
            )
        }
    }

    fun verifyShopPickupOtpAndDispatch(orderId: String, enteredOtp: String): Boolean {
        val stateSnapshot = _uiState.value
        if (!stateSnapshot.isAdminUnlocked) {
            _uiState.update { it.copy(statusMessage = "Only Admin / Rider can verify Shop Pickup OTP") }
            return false
        }
        val target = stateSnapshot.orders.firstOrNull { it.orderId == orderId } ?: return false
        val cleanOtp = enteredOtp.trim()
        if (cleanOtp.length != 4 || cleanOtp != target.shopPickupOtp) {
            return false
        }
        _uiState.update { state ->
            state.copy(
                orders = state.orders.map {
                    if (it.orderId == orderId) {
                        it.copy(
                            stageIndex = it.stageIndex.coerceAtLeast(2).coerceAtMost(2),
                            etaMinutes = if (it.isFoodOrder) 8 else 4,
                            isShopPickupOtpVerified = true
                        )
                    } else it
                },
                statusMessage = "Shop Pickup OTP verified! Rider ${target.riderName} picked up $orderId."
            )
        }
        return true
    }
}
