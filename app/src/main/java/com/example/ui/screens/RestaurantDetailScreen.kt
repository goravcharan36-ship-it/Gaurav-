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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.MenuItem
import com.example.model.Restaurant
import com.example.ui.components.AddOrStepperButton
import com.example.ui.components.FloatingCartSummaryBar
import com.example.ui.components.VegNonVegBadge
import com.example.ui.theme.*
import com.example.viewmodel.QuickDropUiState

@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    uiState: QuickDropUiState,
    onBack: () -> Unit,
    onMenuSearchChange: (String) -> Unit,
    onToggleVegOnly: () -> Unit,
    onToggleBestsellerOnly: () -> Unit,
    onAddDish: (MenuItem) -> Unit,
    onDecrementDish: (String) -> Unit,
    onViewCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val filteredMenu = restaurant.menu.filter { dish ->
        val matchesSearch = uiState.menuSearchQuery.isBlank() ||
            dish.name.contains(uiState.menuSearchQuery, ignoreCase = true) ||
            dish.description.contains(uiState.menuSearchQuery, ignoreCase = true)
        val matchesVeg = (!uiState.menuVegOnly && !uiState.isGlobalVegMode) || dish.isVeg
        val matchesBestseller = !uiState.menuBestsellerOnly || dish.isBestseller
        matchesSearch && matchesVeg && matchesBestseller
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            if (uiState.totalCartItemsCount > 0) {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    FloatingCartSummaryBar(
                        itemCount = uiState.totalCartItemsCount,
                        subtotal = uiState.bill.itemSubtotal,
                        totalSavings = uiState.bill.totalSavings,
                        onViewCart = onViewCart
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Zomato-style Restaurant Hero Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ZomatoCrimson.copy(alpha = 0.14f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape)
                                .testTag("restaurant_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_navigate_back)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = RoundedCornerShape(18.dp),
                            shadowElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = restaurant.name,
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = restaurant.cuisines,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        color = VegGreen,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = restaurant.rating.toString(),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = Color.White
                                                )
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                            Text(
                                                text = restaurant.ratingCountText,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                color = Color.White.copy(alpha = 0.9f)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "${restaurant.deliveryTimeMins} mins • ${restaurant.distanceKm} km",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                    Text(
                                        text = "• ${restaurant.area}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    color = ZomatoCrimsonContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalOffer,
                                            contentDescription = null,
                                            tint = ZomatoCrimson,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = restaurant.offerBanner,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = ZomatoCrimson
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Menu Search & Filters
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.menuSearchQuery,
                        onValueChange = onMenuSearchChange,
                        placeholder = { Text("Search dishes in ${restaurant.name}...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = uiState.menuVegOnly,
                                onClick = onToggleVegOnly,
                                label = { Text("Veg Only") },
                                leadingIcon = { VegNonVegBadge(isVeg = true) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.menuBestsellerOnly,
                                onClick = onToggleBestsellerOnly,
                                label = { Text("Bestsellers") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.WorkspacePremium,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Dish Items
            items(filteredMenu, key = { it.id }) { dish ->
                val qty = uiState.cartItems.firstOrNull { it.id == dish.id }?.quantity ?: 0
                DishMenuCard(
                    dish = dish,
                    quantity = qty,
                    onAdd = { onAddDish(dish) },
                    onIncrement = { onAddDish(dish) },
                    onDecrement = { onDecrementDish(dish.id) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun DishMenuCard(
    dish: MenuItem,
    quantity: Int,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VegNonVegBadge(isVeg = dish.isVeg)
                    if (dish.isBestseller) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            Text(
                                text = "★ Bestseller",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = QuickDropOnSecondaryFixed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (dish.isSpicy) {
                        Text(
                            text = "🌶 Spicy",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = ZomatoCrimson
                        )
                    }
                }

                Text(
                    text = dish.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "₹${dish.price}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (dish.mrp > dish.price) {
                        Text(
                            text = "₹${dish.mrp}",
                            style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "• ★ ${dish.rating} (${dish.ratingCount})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = dish.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(76.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = ZomatoCrimsonContainer.copy(alpha = 0.65f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = dish.name,
                            tint = ZomatoCrimson,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                AddOrStepperButton(
                    quantity = quantity,
                    onAdd = onAdd,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                    accentColor = ZomatoCrimson,
                    compact = true
                )
            }
        }
    }
}
