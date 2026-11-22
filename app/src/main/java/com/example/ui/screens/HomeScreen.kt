package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.QuickDropCatalog
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.QuickDropUiState
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: QuickDropUiState,
    onModeChange: (AppMode) -> Unit,
    onSearchClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onSeeAllCategoriesClick: () -> Unit,
    onAddGrocery: (GroceryProduct) -> Unit,
    onDecrementGrocery: (GroceryProduct) -> Unit,
    onSelectCuisine: (String?) -> Unit,
    onToggleFoodFilter: (FoodFilter) -> Unit,
    onOpenRestaurant: (String) -> Unit,
    onOpenTracking: (String) -> Unit,
    bottomContentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = bottomContentPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dual Mode Switcher: Blinkit Groceries vs Zomato Food
        item {
            DualModeSwitcher(
                currentMode = uiState.appMode,
                onModeChange = onModeChange,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Active Order Live Pill Banner (if any order is active)
        uiState.latestOngoingOrder?.let { ongoing ->
            item {
                LiveOrderFloatingBanner(
                    order = ongoing,
                    onClick = { onOpenTracking(ongoing.orderId) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Search & Voice Pill
        item {
            SearchPillBar(
                placeholder = if (uiState.appMode == AppMode.GROCERIES) {
                    "Search \"milk, mangoes, chips, bread...\""
                } else {
                    "Search \"biryani, dosa, pizza, Meghana...\""
                },
                onClick = onSearchClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (uiState.appMode == AppMode.GROCERIES) {
            // Quick Delivery Assurance Banner
            item {
                AssuranceBanner(
                    title = "Delivery in 10-12 mins",
                    subtitle = "Darkstore: ${uiState.selectedAddress.hubName} (${uiState.selectedAddress.distanceKm} km)",
                    isFood = false,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Promotional Offer Carousel
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.groceryPromos, key = { it.id }) { promo ->
                        PromoBannerCard(
                            promo = promo,
                            onClick = {
                                promo.targetCategoryId?.let(onCategoryClick) ?: onSeeAllCategoriesClick()
                            }
                        )
                    }
                }
            }

            // Shop by Category (2 rows x 4 columns)
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Shop by Category",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Filled.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "See All",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onSeeAllCategoriesClick)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .testTag("see_all_categories_button")
                        )
                    }

                    val rows = uiState.categories.chunked(4)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        rows.forEach { rowCats ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowCats.forEach { category ->
                                    CategoryGridTile(
                                        category = category,
                                        onClick = { onCategoryClick(category.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Steal of the Day / Flash Deal
            val stealProduct = uiState.products.firstOrNull { it.isStealDeal && it.isInStock }
            if (stealProduct != null) {
                item {
                    val qty = uiState.cartItems.firstOrNull {
                        it.productId == stealProduct.id
                    }?.quantity ?: 0
                    StealOfTheDayCard(
                        product = stealProduct,
                        secondsLeft = uiState.dealTimerSeconds,
                        quantity = qty,
                        onAdd = { onAddGrocery(stealProduct) },
                        onIncrement = { onAddGrocery(stealProduct) },
                        onDecrement = { onDecrementGrocery(stealProduct) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Bestsellers in 10 Mins (2-Column Dense Grid)
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Bestsellers in 10 Mins",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Filled.Stars,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Most ordered essentials around your street",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val bestsellers = uiState.products.filter {
                it.isBestseller && it.isInStock && (!uiState.isGlobalVegMode || it.isVeg)
            }
            items(bestsellers.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { product ->
                        val safeIdx = product.defaultVariantIndex.coerceIn(0, product.variants.lastIndex)
                        val variant = product.variants[safeIdx]
                        val qty = uiState.cartItems.firstOrNull {
                            it.id == "${product.id}__${variant.id}"
                        }?.quantity ?: 0
                        HomeProductCard(
                            product = product,
                            variant = variant,
                            quantity = qty,
                            onAdd = { onAddGrocery(product) },
                            onIncrement = { onAddGrocery(product) },
                            onDecrement = { onDecrementGrocery(product) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            // Zomato-style Food Teaser Strip
            item {
                FoodModeTeaserBanner(
                    onSwitchToFood = { onModeChange(AppMode.FOOD) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Trust Badges Strip
            item {
                TrustBadgesStrip(modifier = Modifier.padding(horizontal = 16.dp))
            }
        } else {
            // FOOD & DINING (Zomato-style)
            item {
                AssuranceBanner(
                    title = "Hot meals from top kitchens",
                    subtitle = "Average delivery 20-25 mins around ${uiState.selectedAddress.areaShort}",
                    isFood = true,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Food Promotional Carousel
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.foodPromos, key = { it.id }) { promo ->
                        PromoBannerCard(
                            promo = promo,
                            onClick = {
                                promo.targetCategoryId?.let(onOpenRestaurant)
                            }
                        )
                    }
                }
            }

            // Cuisine Cravings Carousel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "What's on your mind?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(QuickDropCatalog.cuisineCravings, key = { it.id }) { craving ->
                            val selected = uiState.selectedCuisineId == craving.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(74.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectCuisine(craving.id) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(60.dp),
                                    shape = CircleShape,
                                    color = if (selected) ZomatoCrimson.copy(alpha = 0.16f) else Color(craving.tintHex),
                                    border = if (selected) BorderStroke(2.dp, ZomatoCrimson) else null
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = iconForCategory(craving.iconKey),
                                            contentDescription = craving.name,
                                            tint = if (selected) ZomatoCrimson else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = craving.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                                    ),
                                    color = if (selected) ZomatoCrimson else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Zomato Filter Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FoodFilter.entries) { filter ->
                        val active = uiState.activeFoodFilters.contains(filter)
                        FilterChip(
                            selected = active,
                            onClick = { onToggleFoodFilter(filter) },
                            label = { Text(filter.label, style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = if (active) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ZomatoCrimsonContainer,
                                selectedLabelColor = ZomatoCrimson,
                                selectedLeadingIconColor = ZomatoCrimson
                            )
                        )
                    }
                }
            }

            val filteredRestaurants = uiState.restaurants.filter { r ->
                val matchesGlobalVeg = !uiState.isGlobalVegMode || r.isPureVeg
                val matchesPureVeg = !uiState.activeFoodFilters.contains(FoodFilter.PURE_VEG) || r.isPureVeg
                val matchesRating = !uiState.activeFoodFilters.contains(FoodFilter.RATING_4_PLUS) || r.rating >= 4.5
                val matchesFast = !uiState.activeFoodFilters.contains(FoodFilter.FAST_DELIVERY) || r.deliveryTimeMins <= 22
                val matchesCuisine = when (uiState.selectedCuisineId) {
                    "c_biryani" -> r.cuisines.contains("Biryani", ignoreCase = true)
                    "c_dosa" -> r.cuisines.contains("South Indian", ignoreCase = true)
                    "c_pizza" -> r.cuisines.contains("Pizza", ignoreCase = true)
                    "c_burger" -> r.cuisines.contains("Burgers", ignoreCase = true)
                    "c_dessert" -> r.cuisines.contains("Desserts", ignoreCase = true)
                    "c_chai" -> r.cuisines.contains("Coffee", ignoreCase = true) || r.cuisines.contains("Cafe", ignoreCase = true)
                    else -> true
                }
                r.isAcceptingOrders && matchesGlobalVeg && matchesPureVeg && matchesRating && matchesFast && matchesCuisine
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredRestaurants.size} Restaurants Delivering",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (uiState.selectedCuisineId != null || uiState.activeFoodFilters.isNotEmpty()) {
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelMedium,
                            color = ZomatoCrimson,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSelectCuisine(null) }
                                .padding(4.dp)
                        )
                    }
                }
            }

            if (filteredRestaurants.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RestaurantMenu,
                                contentDescription = null,
                                tint = ZomatoCrimson,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No restaurants match those filters",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Tap Reset or try turning off Pure Veg / Rating filters.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredRestaurants, key = { it.id }) { restaurant ->
                    RestaurantCard(
                        restaurant = restaurant,
                        onClick = { onOpenRestaurant(restaurant.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DualModeSwitcher(
    currentMode: AppMode,
    onModeChange: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val isGrocery = currentMode == AppMode.GROCERIES
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onModeChange(AppMode.GROCERIES) }
                    .testTag("mode_tab_groceries"),
                color = if (isGrocery) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = if (isGrocery) QuickDropSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "10-Min Groceries",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isGrocery) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            val isFood = currentMode == AppMode.FOOD
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onModeChange(AppMode.FOOD) }
                    .testTag("mode_tab_food"),
                color = if (isFood) ZomatoCrimson else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = if (isFood) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Food & Dining",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isFood) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveOrderFloatingBanner(
    order: ActiveOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stageTitle = when (order.stageIndex) {
        0 -> "Order Confirmed"
        1 -> if (order.isFoodOrder) "Kitchen Preparing" else "Packing at Darkstore"
        2 -> "Rider On The Way"
        else -> "Delivered"
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("live_order_banner"),
        color = MaterialTheme.colorScheme.inverseSurface,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(QuickDropSecondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ElectricMoped,
                        contentDescription = null,
                        tint = QuickDropOnSecondaryFixed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "$stageTitle • ETA ${order.etaMinutes} mins",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    Text(
                        text = "${order.orderId} • ${order.summaryTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (order.stageIndex < 3) {
                        Surface(
                            color = QuickDropSecondaryContainer.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Delivery OTP: ${order.deliveryOtp}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = QuickDropSecondaryContainer,
                                modifier = Modifier
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                    .testTag("home_banner_delivery_otp")
                            )
                        }
                    }
                }
            }
            Text(
                text = "TRACK",
                style = MaterialTheme.typography.labelMedium,
                color = QuickDropSecondaryContainer
            )
        }
    }
}

@Composable
private fun SearchPillBar(
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("home_search_pill"),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = CircleShape,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.cd_search_icon),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = stringResource(R.string.cd_voice_search),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AssuranceBanner(
    title: String,
    subtitle: String,
    isFood: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isFood) ZomatoCrimsonContainer else MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    color = if (isFood) ZomatoCrimson else MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isFood) Icons.Filled.LocalDining else Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = if (isFood) Color.White else QuickDropOnSecondaryFixed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = null,
                        tint = if (isFood) ZomatoCrimson else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFood) ZomatoCrimson else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PromoBannerCard(
    promo: PromoBanner,
    onClick: () -> Unit
) {
    val bgColor = Color(promo.bgHex)
    val textColor = if (promo.contentDark) QuickDropOnSurface else Color.White
    val tagBg = if (promo.contentDark) QuickDropOnSurface else Color.White
    val tagFg = if (promo.contentDark) Color.White else bgColor

    Surface(
        modifier = Modifier
            .width(292.dp)
            .height(132.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.78f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(color = tagBg, shape = CircleShape) {
                        Text(
                            text = promo.tag,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = tagFg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = promo.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = promo.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.88f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = promo.ctaText,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Icon(
                imageVector = if (promo.isFoodPromo) Icons.Filled.RestaurantMenu else Icons.Filled.LocalMall,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.16f),
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun CategoryGridTile(
    category: GroceryCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
            .testTag("category_tile_${category.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(16.dp),
            color = Color(category.tintHex),
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = iconForCategory(category.iconKey),
                    contentDescription = category.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = category.shortName,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 13.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StealOfTheDayCard(
    product: GroceryProduct,
    secondsLeft: Int,
    quantity: Int,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safeIdx = product.defaultVariantIndex.coerceIn(0, product.variants.lastIndex)
    val variant = product.variants[safeIdx]
    val h = secondsLeft / 3600
    val m = (secondsLeft % 3600) / 60
    val s = secondsLeft % 60
    val timerText = String.format(Locale.US, "%02d:%02d:%02d", h, m, s)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.18f)
                        )
                    )
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Steal of the Day",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = timerText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(88.dp)) {
                        ProductArtwork(
                            imageUrl = product.imageUrl,
                            categoryId = product.categoryId,
                            title = product.name,
                            tintColor = Color(product.tintHex),
                            modifier = Modifier.fillMaxSize()
                        )
                        if (variant.discountPercent > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = "${variant.discountPercent}% OFF",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "⚡ ${product.etaMinutes} Mins Delivery",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = variant.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "₹${variant.price}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "₹${variant.mrp}",
                                    style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AddOrStepperButton(
                                quantity = quantity,
                                onAdd = onAdd,
                                onIncrement = onIncrement,
                                onDecrement = onDecrement,
                                compact = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeProductCard(
    product: GroceryProduct,
    variant: ProductVariant,
    quantity: Int,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f)
            ) {
                ProductArtwork(
                    imageUrl = product.imageUrl,
                    categoryId = product.categoryId,
                    title = product.name,
                    tintColor = Color(product.tintHex),
                    modifier = Modifier.fillMaxSize()
                )
                if (variant.discountPercent > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(5.dp)
                    ) {
                        Text(
                            text = "${variant.discountPercent}% OFF",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                } else if (product.badgeText != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(5.dp)
                    ) {
                        Text(
                            text = product.badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "⏱ ${product.etaMinutes} MINS",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = variant.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${variant.price}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (variant.mrp > variant.price) {
                        Text(
                            text = "₹${variant.mrp}",
                            style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                AddOrStepperButton(
                    quantity = quantity,
                    onAdd = onAdd,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                    compact = true
                )
            }
        }
    }
}

@Composable
private fun FoodModeTeaserBanner(
    onSwitchToFood: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSwitchToFood)
            .testTag("food_teaser_banner"),
        color = ZomatoCrimsonContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    color = ZomatoCrimson,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Craving Hot Food?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = QuickDropOnSurface
                    )
                    Text(
                        text = "Order Biryani, Dosa & Pizza from top restaurants in 22 mins",
                        style = MaterialTheme.typography.bodySmall,
                        color = QuickDropOnSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Explore Food",
                tint = ZomatoCrimson
            )
        }
    }
}

@Composable
private fun TrustBadgesStrip(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrustBadgeItem(Icons.Filled.Verified, "100% Fresh", MaterialTheme.colorScheme.primary)
            HorizontalDivider(
                modifier = Modifier
                    .height(26.dp)
                    .width(1.dp)
            )
            TrustBadgeItem(Icons.Filled.ElectricMoped, "No Min Order", MaterialTheme.colorScheme.secondary)
            HorizontalDivider(
                modifier = Modifier
                    .height(26.dp)
                    .width(1.dp)
            )
            TrustBadgeItem(Icons.Filled.AssignmentReturn, "Instant Refund", MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun TrustBadgeItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("restaurant_card_${restaurant.id}"),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp
    ) {
        Column {
            // Top Visual Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.linearGradient(
                            if (restaurant.isPureVeg) {
                                listOf(Color(0xFF155E2E), Color(0xFF2E7D32))
                            } else {
                                listOf(Color(0xFF7A1C24), Color(0xFFC62828))
                            }
                        )
                    )
                    .padding(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DinnerDining,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.16f),
                    modifier = Modifier
                        .size(96.dp)
                        .align(Alignment.CenterEnd)
                )

                // Top Offer & Pure Veg Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = Color(0xFF1D4ED8),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = restaurant.offerBanner,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (restaurant.isPureVeg) {
                        Surface(color = VegGreen, shape = CircleShape) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Eco,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "PURE VEG",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Delivery ETA pill bottom-left
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = CircleShape,
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${restaurant.deliveryTimeMins} mins • ${restaurant.distanceKm} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Details body
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
                        text = restaurant.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = VegGreen,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = restaurant.rating.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = restaurant.cuisines,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "₹${restaurant.priceForTwo} for two",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${restaurant.area} • ${restaurant.ratingCountText} ratings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
