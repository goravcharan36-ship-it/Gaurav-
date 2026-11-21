package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuickDropCatalog
import com.example.model.GroceryProduct
import com.example.model.MenuItem
import com.example.ui.components.AddOrStepperButton
import com.example.ui.components.ProductArtwork
import com.example.ui.components.VegNonVegBadge
import com.example.ui.theme.ZomatoCrimson
import com.example.viewmodel.QuickDropUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    uiState: QuickDropUiState,
    onSearchChange: (String) -> Unit,
    onSubmitQuery: (String) -> Unit,
    onClearRecent: () -> Unit,
    onAddGrocery: (GroceryProduct) -> Unit,
    onDecrementGrocery: (GroceryProduct) -> Unit,
    onOpenRestaurant: (String) -> Unit,
    onAddDish: (MenuItem) -> Unit,
    onDecrementDish: (String) -> Unit,
    bottomContentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val query = uiState.globalSearchQuery.trim()

    val matchingGroceries = remember(query, uiState.isGlobalVegMode, uiState.products) {
        if (query.isEmpty()) emptyList()
        else uiState.products.filter { prod ->
            prod.isInStock && (!uiState.isGlobalVegMode || prod.isVeg) && (
                prod.name.contains(query, ignoreCase = true) ||
                    prod.brand.contains(query, ignoreCase = true) ||
                    prod.subcategory.contains(query, ignoreCase = true)
                )
        }
    }

    val matchingRestaurants = remember(query, uiState.isGlobalVegMode, uiState.restaurants) {
        if (query.isEmpty()) emptyList()
        else uiState.restaurants.filter { r ->
            r.isAcceptingOrders && (!uiState.isGlobalVegMode || r.isPureVeg) && (
                r.name.contains(query, ignoreCase = true) ||
                    r.cuisines.contains(query, ignoreCase = true)
                )
        }
    }

    val matchingDishes = remember(query, uiState.isGlobalVegMode, uiState.restaurants) {
        if (query.isEmpty()) emptyList()
        else uiState.restaurants.filter { it.isAcceptingOrders }.flatMap { it.menu }.filter { d ->
            (!uiState.isGlobalVegMode || d.isVeg) && (
                d.name.contains(query, ignoreCase = true) ||
                    d.restaurantName.contains(query, ignoreCase = true) ||
                    d.description.contains(query, ignoreCase = true)
                )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 10.dp,
            bottom = bottomContentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = uiState.globalSearchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search groceries, biryani, pizza, milk...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (uiState.globalSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("global_search_input")
            )
        }

        if (query.isEmpty()) {
            if (uiState.recentSearches.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Searches",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Clear",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(onClick = onClearRecent)
                                    .padding(4.dp)
                            )
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.recentSearches) { term ->
                                AssistChip(
                                    onClick = { onSubmitQuery(term) },
                                    label = { Text(term) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.History,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Trending Now",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickDropCatalog.trendingSearches.forEach { term ->
                            SuggestionChip(
                                onClick = { onSubmitQuery(term) },
                                label = { Text(term, style = MaterialTheme.typography.labelMedium) }
                            )
                        }
                    }
                }
            }
        } else {
            if (matchingGroceries.isEmpty() && matchingRestaurants.isEmpty() && matchingDishes.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "No results for \"$query\"",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Try searching for Milk, Mangoes, Biryani, Dosa, or Pizza.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (matchingGroceries.isNotEmpty()) {
                item {
                    Text(
                        text = "Groceries in 10 Mins (${matchingGroceries.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(matchingGroceries, key = { it.id }) { prod ->
                    val safeIdx = prod.defaultVariantIndex.coerceIn(0, prod.variants.lastIndex)
                    val variant = prod.variants[safeIdx]
                    val qty = uiState.cartItems.firstOrNull {
                        it.id == "${prod.id}__${variant.id}"
                    }?.quantity ?: 0

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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ProductArtwork(
                                    imageUrl = prod.imageUrl,
                                    categoryId = prod.categoryId,
                                    title = prod.name,
                                    tintColor = Color(prod.tintHex),
                                    modifier = Modifier.size(60.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "⚡ ${prod.etaMinutes} MINS • ${prod.brand}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = prod.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "₹${variant.price}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                                        )
                                        if (variant.mrp > variant.price) {
                                            Text(
                                                text = "₹${variant.mrp}",
                                                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "• ${variant.label}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            AddOrStepperButton(
                                quantity = qty,
                                onAdd = { onAddGrocery(prod) },
                                onIncrement = { onAddGrocery(prod) },
                                onDecrement = { onDecrementGrocery(prod) },
                                compact = true
                            )
                        }
                    }
                }
            }

            if (matchingRestaurants.isNotEmpty()) {
                item {
                    Text(
                        text = "Restaurants (${matchingRestaurants.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ZomatoCrimson
                    )
                }
                items(matchingRestaurants, key = { it.id }) { rest ->
                    RestaurantCard(
                        restaurant = rest,
                        onClick = { onOpenRestaurant(rest.id) }
                    )
                }
            }

            if (matchingDishes.isNotEmpty()) {
                item {
                    Text(
                        text = "Dishes (${matchingDishes.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ZomatoCrimson
                    )
                }
                items(matchingDishes, key = { it.id }) { dish ->
                    val qty = uiState.cartItems.firstOrNull { it.id == dish.id }?.quantity ?: 0
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(14.dp),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenRestaurant(dish.restaurantId) }
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
                                    VegNonVegBadge(isVeg = dish.isVeg)
                                    Text(
                                        text = dish.restaurantName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ZomatoCrimson
                                    )
                                }
                                Text(
                                    text = dish.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "₹${dish.price} • ★ ${dish.rating}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AddOrStepperButton(
                                quantity = qty,
                                onAdd = { onAddDish(dish) },
                                onIncrement = { onAddDish(dish) },
                                onDecrement = { onDecrementDish(dish.id) },
                                accentColor = ZomatoCrimson,
                                compact = true
                            )
                        }
                    }
                }
            }
        }
    }
}
