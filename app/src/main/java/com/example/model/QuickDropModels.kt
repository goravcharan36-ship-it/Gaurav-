package com.example.model

enum class AppMode {
    GROCERIES,
    FOOD
}

enum class BottomTab(val label: String) {
    HOME("Home"),
    CATEGORIES("Categories"),
    SEARCH("Search"),
    CART("Cart"),
    PROFILE("Profile")
}

enum class AdminTab(val label: String) {
    OVERVIEW("Overview"),
    ORDERS("Orders"),
    INVENTORY("Inventory"),
    PARTNERS("Partners")
}

enum class CatalogSortOption(val label: String) {
    RELEVANCE("Relevance"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    DISCOUNT("Highest Discount")
}

enum class CatalogQuickFilter(val label: String) {
    EXPRESS_10_MIN("Express 10 Min"),
    IN_STOCK("In Stock"),
    DISCOUNT_OVER_20("Discounts > 20%"),
    VEG_ONLY("Veg Only")
}

enum class FoodFilter(val label: String) {
    PURE_VEG("Pure Veg"),
    RATING_4_PLUS("Rating 4.2+"),
    FAST_DELIVERY("Under 25 Mins"),
    GREAT_OFFERS("Great Offers")
}

data class ProductVariant(
    val id: String,
    val label: String,
    val price: Int,
    val mrp: Int
) {
    val discountPercent: Int
        get() = if (mrp > price && mrp > 0) ((mrp - price) * 100) / mrp else 0
}

data class GroceryProduct(
    val id: String,
    val brand: String,
    val name: String,
    val categoryId: String,
    val subcategory: String,
    val variants: List<ProductVariant>,
    val defaultVariantIndex: Int = 0,
    val etaMinutes: Int = 10,
    val isVeg: Boolean = true,
    val isInStock: Boolean = true,
    val isBestseller: Boolean = false,
    val isStealDeal: Boolean = false,
    val isMissedQuickAdd: Boolean = false,
    val badgeText: String? = null,
    val imageUrl: String = "",
    val tintHex: Long = 0xFFF0F3FF
)

data class GroceryCategory(
    val id: String,
    val name: String,
    val shortName: String,
    val iconKey: String,
    val hasSaleBadge: Boolean = false,
    val tintHex: Long = 0xFFF0F3FF,
    val subcategories: List<String>
)

data class PromoBanner(
    val id: String,
    val tag: String,
    val title: String,
    val subtitle: String,
    val ctaText: String,
    val targetCategoryId: String?,
    val isFoodPromo: Boolean = false,
    val bgHex: Long,
    val contentDark: Boolean = false,
    val imageUrl: String = ""
)

data class CuisineCraving(
    val id: String,
    val name: String,
    val iconKey: String,
    val tintHex: Long
)

data class MenuItem(
    val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val name: String,
    val description: String,
    val price: Int,
    val mrp: Int,
    val category: String,
    val isVeg: Boolean,
    val isBestseller: Boolean = false,
    val isSpicy: Boolean = false,
    val rating: Double = 4.4,
    val ratingCount: String = "180+",
    val imageUrl: String = ""
)

data class Restaurant(
    val id: String,
    val name: String,
    val cuisines: String,
    val rating: Double,
    val ratingCountText: String,
    val deliveryTimeMins: Int,
    val distanceKm: Double,
    val priceForTwo: Int,
    val offerBanner: String,
    val area: String,
    val isPureVeg: Boolean = false,
    val isPromoted: Boolean = false,
    val isAcceptingOrders: Boolean = true,
    val imageUrl: String = "",
    val menu: List<MenuItem>
)

data class CartItem(
    val id: String,
    val productId: String,
    val variantId: String,
    val title: String,
    val brandOrRestaurant: String,
    val unitLabel: String,
    val price: Int,
    val mrp: Int,
    val quantity: Int,
    val isGrocery: Boolean,
    val isVeg: Boolean,
    val imageUrl: String = ""
)

data class Coupon(
    val code: String,
    val title: String,
    val description: String,
    val discountAmount: Int,
    val minOrder: Int,
    val freeDelivery: Boolean = false,
    val isActive: Boolean = true
)

data class DeliveryAddress(
    val id: String,
    val label: String,
    val fullAddress: String,
    val areaShort: String,
    val hubName: String,
    val distanceKm: Double
)

data class DeliveryInstruction(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconKey: String
)

data class ActiveOrder(
    val orderId: String,
    val placedTimeText: String,
    val summaryTitle: String,
    val itemsCount: Int,
    val totalPaid: Int,
    val totalSaved: Int,
    val etaMinutes: Int,
    val stageIndex: Int, // 0=Confirmed, 1=Packing/Preparing, 2=Out for Delivery, 3=Delivered
    val riderName: String,
    val riderRating: Double,
    val vehicleLabel: String,
    val hubOrKitchen: String,
    val deliveryAddress: String,
    val isFoodOrder: Boolean,
    val reorderCartItems: List<CartItem> = emptyList()
)
