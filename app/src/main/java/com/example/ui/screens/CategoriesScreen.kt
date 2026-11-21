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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuickDropCatalog
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.QuickDropOnSecondaryFixed
import com.example.ui.theme.VegGreen
import com.example.viewmodel.QuickDropUiState

@Composable
fun CategoriesScreen(
    uiState: QuickDropUiState,
    onSelectCategory: (String) -> Unit,
    onSelectSubcategory: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleQuickFilter: (CatalogQuickFilter) -> Unit,
    onSelectSort: (CatalogSortOption) -> Unit,
    onSelectVariant: (String, Int) -> Unit,
    onAddGrocery: (GroceryProduct) -> Unit,
    onDecrementGrocery: (GroceryProduct) -> Unit,
    bottomContentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val activeCategory = uiState.categories.firstOrNull {
        it.id == uiState.selectedCategoryId
    } ?: uiState.categories.first()

    var showSortMenu by remember { mutableStateOf(false) }

    val filteredProducts = remember(
        uiState.products,
        uiState.selectedCategoryId,
        uiState.selectedSubcategory,
        uiState.categorySearchQuery,
        uiState.activeCatalogFilters,
        uiState.catalogSort,
        uiState.isGlobalVegMode
    ) {
        val base = uiState.products.filter { prod ->
            val matchCat = prod.categoryId == activeCategory.id
            val matchSub = uiState.selectedSubcategory == "All" ||
                prod.subcategory.equals(uiState.selectedSubcategory, ignoreCase = true)
            val matchSearch = uiState.categorySearchQuery.isBlank() ||
                prod.name.contains(uiState.categorySearchQuery, ignoreCase = true) ||
                prod.brand.contains(uiState.categorySearchQuery, ignoreCase = true)
            val matchExpress = !uiState.activeCatalogFilters.contains(CatalogQuickFilter.EXPRESS_10_MIN) ||
                prod.etaMinutes <= 12
            val matchStock = !uiState.activeCatalogFilters.contains(CatalogQuickFilter.IN_STOCK) ||
                prod.isInStock
            val matchDiscount = !uiState.activeCatalogFilters.contains(CatalogQuickFilter.DISCOUNT_OVER_20) ||
                prod.variants.any { it.discountPercent >= 20 }
            val matchVeg = (!uiState.activeCatalogFilters.contains(CatalogQuickFilter.VEG_ONLY) && !uiState.isGlobalVegMode) ||
                prod.isVeg
            matchCat && matchSub && matchSearch && matchExpress && matchStock && matchDiscount && matchVeg
        }

        when (uiState.catalogSort) {
            CatalogSortOption.RELEVANCE -> base
            CatalogSortOption.PRICE_LOW_HIGH -> base.sortedBy { it.variants.first().price }
            CatalogSortOption.PRICE_HIGH_LOW -> base.sortedByDescending { it.variants.first().price }
            CatalogSortOption.DISCOUNT -> base.sortedByDescending { it.variants.first().discountPercent }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Sticky Search & Quick Filter Chips
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.categorySearchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search in ${activeCategory.name}...",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (uiState.categorySearchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_search_input")
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CatalogQuickFilter.entries) { filter ->
                        val active = uiState.activeCatalogFilters.contains(filter)
                        Surface(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onToggleQuickFilter(filter) },
                            color = if (active) MaterialTheme.colorScheme.surfaceContainerHigh
                            else MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = CircleShape,
                            shadowElevation = if (active) 0.dp else 1.dp,
                            border = if (active) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                when (filter) {
                                    CatalogQuickFilter.EXPRESS_10_MIN -> Icon(
                                        Icons.Filled.Bolt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    CatalogQuickFilter.IN_STOCK -> Icon(
                                        Icons.Filled.Inventory2,
                                        contentDescription = null,
                                        tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    CatalogQuickFilter.DISCOUNT_OVER_20 -> Icon(
                                        Icons.Filled.LocalOffer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    CatalogQuickFilter.VEG_ONLY -> Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(VegGreen, CircleShape)
                                    )
                                }
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dual Column Catalog Split Layout
        Row(modifier = Modifier.fillMaxSize()) {
            // LEFT VERTICAL NAVIGATION RAIL
            LazyColumn(
                modifier = Modifier
                    .width(92.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentPadding = PaddingValues(
                    top = 6.dp,
                    bottom = bottomContentPadding.calculateBottomPadding() + 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.categories, key = { it.id }) { cat ->
                    val selected = cat.id == activeCategory.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selected) MaterialTheme.colorScheme.surfaceContainerLowest
                                else Color.Transparent
                            )
                            .clickable { onSelectCategory(cat.id) }
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                            .testTag("rail_category_${cat.id}")
                    ) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .width(4.dp)
                                    .height(34.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                                    )
                            )
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.14f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = iconForCategory(cat.iconKey),
                                        contentDescription = cat.name,
                                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Text(
                                text = cat.shortName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                lineHeight = 12.sp
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
                    }
                }
            }

            // RIGHT MAIN PRODUCT GRID
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 10.dp,
                    top = 8.dp,
                    bottom = bottomContentPadding.calculateBottomPadding() + 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Subcategory Pills
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(activeCategory.subcategories) { sub ->
                            val selected = uiState.selectedSubcategory == sub
                            Surface(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onSelectSubcategory(sub) },
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerLowest,
                                shape = CircleShape,
                                shadowElevation = 1.dp
                            ) {
                                Text(
                                    text = sub,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Category Context Header + Sort
                item {
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
                            Text(
                                text = activeCategory.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "${filteredProducts.size} items",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showSortMenu = true }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "Sort",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = "Sort options",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                CatalogSortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.label) },
                                        trailingIcon = {
                                            if (uiState.catalogSort == option) {
                                                Icon(Icons.Filled.Check, contentDescription = null)
                                            }
                                        },
                                        onClick = {
                                            onSelectSort(option)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredProducts.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "No matching items",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Try clearing filters or selecting All subcategories.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredProducts.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { product ->
                                val selectedIdx = (uiState.selectedVariantByProductId[product.id]
                                    ?: product.defaultVariantIndex).coerceIn(0, product.variants.lastIndex)
                                val activeVariant = product.variants[selectedIdx]
                                val qty = uiState.cartItems.firstOrNull {
                                    it.id == "${product.id}__${activeVariant.id}"
                                }?.quantity ?: 0

                                CatalogProductCard(
                                    product = product,
                                    selectedVariantIndex = selectedIdx,
                                    quantity = qty,
                                    onSelectVariant = { idx -> onSelectVariant(product.id, idx) },
                                    onAdd = { onAddGrocery(product) },
                                    onIncrement = { onAddGrocery(product) },
                                    onDecrement = { onDecrementGrocery(product) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                // Cold-chain Assurance Footer
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(38.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.14f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cold-chain delivered < 4°C",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "100% farm-fresh or instant replacement",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogProductCard(
    product: GroceryProduct,
    selectedVariantIndex: Int,
    quantity: Int,
    onSelectVariant: (Int) -> Unit,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val variant = product.variants[selectedVariantIndex]
    var variantMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
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
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(5.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "${variant.discountPercent}% OFF",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                } else if (product.badgeText != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(5.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = product.badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = QuickDropOnSecondaryFixed,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "${product.etaMinutes} MINS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                VegNonVegBadge(
                    isVeg = product.isVeg,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )
            }

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Interactive Variant Dropdown
            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(enabled = product.variants.size > 1) {
                            variantMenuExpanded = true
                        },
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = variant.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (product.variants.size > 1) {
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = "Select pack size",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = variantMenuExpanded,
                    onDismissRequest = { variantMenuExpanded = false }
                ) {
                    product.variants.forEachIndexed { idx, v ->
                        DropdownMenuItem(
                            text = { Text("${v.label} — ₹${v.price}") },
                            onClick = {
                                onSelectVariant(idx)
                                variantMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${variant.price}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (variant.mrp > variant.price) {
                        Text(
                            text = "₹${variant.mrp}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                textDecoration = TextDecoration.LineThrough
                            ),
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
