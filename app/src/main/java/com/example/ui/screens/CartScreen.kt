package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.*
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
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.VegGreen
import com.example.viewmodel.QuickDropUiState

@Composable
fun CartScreen(
    uiState: QuickDropUiState,
    onIncrementCartItem: (String) -> Unit,
    onDecrementCartItem: (String) -> Unit,
    onEmptyCart: () -> Unit,
    onAddQuickProduct: (GroceryProduct) -> Unit,
    onApplyCoupon: (String?) -> Unit,
    onToggleInstruction: (String) -> Unit,
    onToggleTip: (Int) -> Unit,
    onEditAddressClick: () -> Unit,
    onPlaceOrder: (String) -> Unit,
    onExploreGroceries: () -> Unit,
    onExploreFood: () -> Unit,
    bottomContentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var showCouponSheet by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }

    val bill = uiState.bill

    if (uiState.cartItems.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(bottom = bottomContentPadding.calculateBottomPadding())
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(88.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
                Text(
                    text = "Your QuickDrop cart is empty",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Add fresh groceries in 10 mins or order hot meals from your favorite restaurants.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onExploreGroceries,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("empty_cart_groceries_btn")
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Shop Groceries")
                    }
                    OutlinedButton(
                        onClick = onExploreFood,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("empty_cart_food_btn")
                    ) {
                        Icon(Icons.Filled.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Order Food")
                    }
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = bottomContentPadding.calculateBottomPadding())
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Milestone Free Delivery Banner
            item {
                val animatedProgress by animateFloatAsState(
                    targetValue = bill.freeDeliveryProgress,
                    label = "free_delivery_progress"
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    imageVector = Icons.Filled.ElectricMoped,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Free Delivery Milestone",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "TARGET: ₹${bill.freeDeliveryThreshold}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = if (bill.amountNeededForFreeDelivery > 0) {
                                "Add items worth ₹${bill.amountNeededForFreeDelivery} more to unlock FREE Delivery!"
                            } else {
                                "Unlocked! You get FREE Delivery on this order."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = if (bill.amountNeededForFreeDelivery == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cart: ₹${bill.itemSubtotal}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(animatedProgress * 100).toInt()}% reached",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Cart Items List Section
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Items in Cart",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = uiState.totalCartItemsCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "Empty Cart",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onEmptyCart)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("empty_cart_button")
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            uiState.cartItems.forEachIndexed { idx, item ->
                                CartItemRow(
                                    item = item,
                                    onIncrement = { onIncrementCartItem(item.id) },
                                    onDecrement = { onDecrementCartItem(item.id) }
                                )
                                if (idx < uiState.cartItems.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Missed Something? Quick Add Carousel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Missed Something?",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "IN 10 MINS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    val quickAddItems = uiState.products.filter { it.isMissedQuickAdd && it.isInStock }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(quickAddItems, key = { it.id }) { prod ->
                            val safeIdx = prod.defaultVariantIndex.coerceIn(0, prod.variants.lastIndex)
                            val variant = prod.variants[safeIdx]
                            Surface(
                                modifier = Modifier.width(140.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                shape = RoundedCornerShape(14.dp),
                                shadowElevation = 1.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ProductArtwork(
                                        imageUrl = prod.imageUrl,
                                        categoryId = prod.categoryId,
                                        title = prod.name,
                                        tintColor = Color(prod.tintHex),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.1f)
                                    )
                                    Text(
                                        text = prod.name,
                                        style = MaterialTheme.typography.titleSmall,
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
                                        Text(
                                            text = "₹${variant.price}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onAddQuickProduct(prod) },
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "+ ADD",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Coupons & Offers Section
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    modifier = Modifier.size(28.dp),
                                    shape = CircleShape,
                                    color = if (bill.couponDiscount > 0 || (uiState.appliedCouponCode == "FREEDEL" && bill.isDeliveryFree)) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (uiState.appliedCouponCode != null) Icons.Filled.Check else Icons.Filled.LocalOffer,
                                            contentDescription = null,
                                            tint = if (uiState.appliedCouponCode != null) Color.White else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (uiState.appliedCouponCode != null) {
                                            "${uiState.appliedCouponCode} Applied"
                                        } else {
                                            "Apply Promo Coupon"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (bill.couponDiscount > 0) {
                                            "₹${bill.couponDiscount} instant savings applied!"
                                        } else if (uiState.appliedCouponCode == "FREEDEL") {
                                            "Free delivery unlocked!"
                                        } else {
                                            "Save up to ₹100 with promo codes"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = "Change Coupon",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showCouponSheet = true }
                                    .padding(6.dp)
                                    .testTag("change_coupon_button")
                            )
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.activeCoupons, key = { it.code }) { coupon ->
                                val active = uiState.appliedCouponCode == coupon.code
                                Surface(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { onApplyCoupon(coupon.code) },
                                    color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = CircleShape,
                                    border = if (active) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalActivity,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (coupon.discountAmount > 0) "${coupon.code} (-₹${coupon.discountAmount})" else coupon.code,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Delivery Instructions 2x2 Grid
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Delivery Instructions",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Optional",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        QuickDropCatalog.deliveryInstructions.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { instr ->
                                    val selected = uiState.selectedInstructionIds.contains(instr.id)
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onToggleInstruction(instr.id) },
                                        color = if (selected) MaterialTheme.colorScheme.surfaceContainer
                                        else MaterialTheme.colorScheme.surfaceContainerLow,
                                        shape = RoundedCornerShape(12.dp),
                                        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = iconForCategory(instr.iconKey),
                                                contentDescription = null,
                                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = instr.title,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = instr.subtitle,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
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

            // Tip Your Delivery Partner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    imageVector = Icons.Filled.VolunteerActivism,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Tip Delivery Partner",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "100% goes to partner",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Your kindness powers our 10-minute delivery speed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickDropCatalog.tipOptions.forEach { (amount, tag) ->
                                val selected = uiState.selectedTipAmount == amount
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onToggleTip(amount) },
                                    color = if (selected) MaterialTheme.colorScheme.surfaceContainer
                                    else MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "₹$amount",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Detailed Bill Summary Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bill Summary",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "BREAKDOWN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        BillRow("Items Total (MRP)", "₹${bill.itemsMrpTotal}")
                        if (bill.productDiscount > 0) {
                            BillRow("Product Discount", "-₹${bill.productDiscount}", isHighlight = true)
                        }
                        BillRow("Item Subtotal", "₹${bill.itemSubtotal}", boldValue = true)
                        BillRow(
                            label = "Delivery Partner Fee",
                            value = if (bill.isDeliveryFree) "FREE" else "₹${bill.deliveryFee}",
                            strikethroughValue = if (bill.isDeliveryFree) "₹${bill.baseDeliveryFee}" else null,
                            isHighlight = bill.isDeliveryFree
                        )
                        BillRow("Dark Store Handling Fee", "₹${bill.handlingFee}")
                        BillRow(
                            label = "Small Cart / Weather Surge",
                            value = if (bill.surgeFee > 0) "₹${bill.surgeFee}" else "₹0",
                            isHighlight = bill.surgeFee == 0
                        )
                        if (bill.couponDiscount > 0) {
                            BillRow(
                                label = "Coupon Discount (${uiState.appliedCouponCode})",
                                value = "-₹${bill.couponDiscount}",
                                isHighlight = true
                            )
                        }
                        if (bill.tipAmount > 0) {
                            BillRow("Delivery Tip", "₹${bill.tipAmount}")
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "To Pay",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Inclusive of all operational taxes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${bill.toPay}",
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (bill.itemsMrpTotal > bill.toPay) {
                                    Text(
                                        text = "₹${bill.itemsMrpTotal}",
                                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (bill.totalSavings > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Celebration,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "You saved ₹${bill.totalSavings} on this order!",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = "SUPER SAVER",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 100% Cancellation & Refund Guarantee
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "100% Cancellation & Refund Guarantee",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Full instant refund to original source if cancelled before store packing begins (first 60 seconds).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Sticky Bottom Checkout Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Delivering to: ${uiState.selectedAddress.fullAddress}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "EDIT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onEditAddressClick)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Button(
                    onClick = { showPaymentSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("proceed_to_pay_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Proceed to Pay ₹${bill.toPay}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Total ${uiState.totalCartItemsCount} items • Delivery in ${if (uiState.cartItems.any { !it.isGrocery }) "22" else "10"} mins",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "UPI / CARD",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
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

    if (showCouponSheet) {
        CouponPickerSheet(
            coupons = uiState.activeCoupons,
            appliedCode = uiState.appliedCouponCode,
            subtotal = bill.itemSubtotal,
            onSelectCoupon = { code ->
                onApplyCoupon(code)
                showCouponSheet = false
            },
            onDismiss = { showCouponSheet = false }
        )
    }

    if (showPaymentSheet) {
        PaymentMethodSheet(
            amountToPay = bill.toPay,
            onConfirmPayment = { method ->
                showPaymentSheet = false
                onPlaceOrder(method)
            },
            onDismiss = { showPaymentSheet = false }
        )
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductArtwork(
                imageUrl = item.imageUrl,
                categoryId = if (item.isGrocery) "dairy" else "c_biryani",
                title = item.title,
                tintColor = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.size(62.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    VegNonVegBadge(isVeg = item.isVeg)
                    Text(
                        text = item.brandOrRestaurant.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.unitLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "₹${item.price * item.quantity}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.mrp > item.price) {
                        Text(
                            text = "₹${item.mrp * item.quantity}",
                            style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(8.dp))
        AddOrStepperButton(
            quantity = item.quantity,
            onAdd = onIncrement,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            compact = true
        )
    }
}

@Composable
private fun BillRow(
    label: String,
    value: String,
    strikethroughValue: String? = null,
    isHighlight: Boolean = false,
    boldValue: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (strikethroughValue != null) {
                Text(
                    text = strikethroughValue,
                    style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isHighlight || boldValue) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CouponPickerSheet(
    coupons: List<Coupon>,
    appliedCode: String?,
    subtotal: Int,
    onSelectCoupon: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Coupons",
                    style = MaterialTheme.typography.headlineSmall
                )
                if (appliedCode != null) {
                    TextButton(onClick = { onSelectCoupon(null) }) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            coupons.forEach { coupon ->
                val isSelected = appliedCode == coupon.code
                val eligible = subtotal >= coupon.minOrder
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = eligible) { onSelectCoupon(coupon.code) },
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(14.dp),
                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
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
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = coupon.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = coupon.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!eligible) {
                                Text(
                                    text = "Add ₹${coupon.minOrder - subtotal} more to unlock",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        if (isSelected) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Applied", tint = VegGreen)
                        } else if (eligible) {
                            Text(
                                text = "APPLY",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodSheet(
    amountToPay: Int,
    onConfirmPayment: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val methods = listOf(
        "Google Pay UPI" to "Instant 1-tap UPI checkout",
        "PhonePe / Paytm UPI" to "Fast verified UPI transfer",
        "HDFC / ICICI Card" to "Credit & Debit cards supported",
        "Cash on Delivery" to "Pay via UPI or cash at doorstep"
    )
    var selectedMethod by remember { mutableStateOf(methods.first().first) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Payment Method",
                style = MaterialTheme.typography.headlineSmall
            )
            methods.forEach { (title, subtitle) ->
                val active = selectedMethod == title
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedMethod = title },
                    color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(14.dp),
                    border = if (active) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RadioButton(selected = active, onClick = { selectedMethod = title })
                    }
                }
            }

            Button(
                onClick = { onConfirmPayment(selectedMethod) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_pay_order_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Pay ₹$amountToPay & Place Order",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
