package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ActiveOrder
import com.example.model.GroceryProduct
import com.example.model.MenuItem
import com.example.model.Restaurant
import com.example.ui.components.AddExtraItemsBottomSheet
import com.example.ui.components.LiveDeliveryRouteMap
import com.example.ui.theme.QuickDropOnSecondaryFixed
import com.example.ui.theme.QuickDropSecondaryContainer
import com.example.ui.theme.VegGreen
import com.example.ui.theme.ZomatoCrimson

@Composable
fun OrderTrackingScreen(
    order: ActiveOrder,
    products: List<GroceryProduct>,
    restaurants: List<Restaurant>,
    onBack: () -> Unit,
    onAddExtraProduct: (GroceryProduct) -> Unit,
    onAddExtraDish: (MenuItem) -> Unit,
    onAddCustomExtra: (name: String, price: Int) -> Unit,
    onReorder: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var showExtraItemsSheet by remember { mutableStateOf(false) }

    val stages = listOf(
        "Order Confirmed" to "We received your order at ${order.hubOrKitchen}",
        (if (order.isFoodOrder) "Kitchen Preparing Meal" else "Packing at Darkstore") to "Freshly picking & sealing your ${order.itemsCount} items",
        "Rider On The Way" to if (order.isShopPickupOtpVerified) {
            "${order.riderName} verified shop pickup OTP & is heading to your doorstep"
        } else {
            "${order.riderName} is heading to your doorstep"
        },
        "Delivered" to "Enjoy your order! Total saved: ₹${order.totalSaved}"
    )

    val headerColor = if (order.isFoodOrder) ZomatoCrimson else MaterialTheme.colorScheme.primary

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Surface(color = headerColor, contentColor = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("tracking_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back),
                            tint = Color.White
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (order.stageIndex >= 3) "Order Delivered!" else "Arriving in ${order.etaMinutes} mins",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Text(
                            text = "${order.orderId} • ${order.hubOrKitchen}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 14.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interactive Live Delivery Map
            item {
                LiveDeliveryRouteMap(order = order)
            }

            // Live Stage Timeline Card (Read-only for customers; Admin-controlled)
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        headerColor.copy(alpha = 0.08f),
                                        MaterialTheme.colorScheme.surfaceContainerLowest
                                    )
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = QuickDropSecondaryContainer,
                                shape = CircleShape
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ElectricMoped,
                                        contentDescription = null,
                                        tint = QuickDropOnSecondaryFixed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (order.stageIndex >= 3) "COMPLETED" else "LIVE STATUS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = QuickDropOnSecondaryFixed
                                    )
                                }
                            }
                            Text(
                                text = "₹${order.totalPaid} Paid",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { ((order.stageIndex + 1).toFloat() / 4f).coerceIn(0.25f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = headerColor,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )

                        stages.forEachIndexed { idx, (title, subtitle) ->
                            val reached = idx <= order.stageIndex
                            val current = idx == order.stageIndex
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(28.dp),
                                    shape = CircleShape,
                                    color = if (reached) headerColor else MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (reached) Icons.Filled.Check else Icons.Filled.Circle,
                                            contentDescription = null,
                                            tint = if (reached) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(if (reached) 16.dp else 8.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = if (current) FontWeight.ExtraBold else FontWeight.SemiBold
                                        ),
                                        color = if (reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Packing & dispatch stages are updated exclusively by Store Admin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Delivery Partner Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.DeliveryDining,
                                        contentDescription = null,
                                        tint = headerColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "${order.riderName} • ★ ${order.riderRating}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = order.vehicleLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = "Verified Partner",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            // Order Summary & Address
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Delivery Details (${order.itemsCount} items)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = order.summaryTitle,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Delivering to: ${order.deliveryAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        )

                        // In-App Delivery OTP Card right underneath Order Details
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (order.stageIndex >= 3) {
                                VegGreen.copy(alpha = 0.10f)
                            } else {
                                QuickDropSecondaryContainer.copy(alpha = 0.35f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (order.stageIndex >= 3) VegGreen.copy(alpha = 0.45f)
                                else QuickDropOnSecondaryFixed.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("order_delivery_otp_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
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
                                            imageVector = if (order.stageIndex >= 3) Icons.Filled.Verified else Icons.Filled.Pin,
                                            contentDescription = null,
                                            tint = if (order.stageIndex >= 3) VegGreen else QuickDropOnSecondaryFixed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (order.stageIndex >= 3) "OTP Verified • Ride Finished" else "Delivery Handover OTP",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = if (order.stageIndex >= 3) VegGreen else QuickDropOnSecondaryFixed
                                        )
                                    }
                                    Surface(
                                        color = if (order.stageIndex >= 3) VegGreen else Color(0xFF1A2229),
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = if (order.stageIndex >= 3) "VERIFIED" else "4-DIGIT PIN",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (order.stageIndex < 3) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("order_delivery_otp_value"),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        order.deliveryOtp.forEach { digit ->
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(46.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = digit.toString(),
                                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Share this OTP with ${order.riderName} at doorstep to verify & complete your delivery.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = QuickDropOnSecondaryFixed
                                    )
                                } else {
                                    Text(
                                        text = "Verified OTP ${order.deliveryOtp} with ${order.riderName}. Thank you for ordering with QuickDrop!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VegGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Add Extra Items (only before rider leaves for delivery) or Reorder
            item {
                if (order.stageIndex < 2) {
                    Button(
                        onClick = { showExtraItemsSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("add_extra_items_to_order_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Extra Items to This Order")
                    }
                } else if (order.stageIndex >= 3 && order.reorderCartItems.isNotEmpty()) {
                    Button(
                        onClick = onReorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Reorder Items")
                    }
                }
            }
        }
    }

    if (showExtraItemsSheet) {
        AddExtraItemsBottomSheet(
            title = "Add Extra Items to ${order.orderId}",
            subtitle = "Add missed groceries, food dishes, or custom extra items before dispatch",
            products = products,
            restaurants = restaurants,
            allowCustomItem = true,
            onAddProduct = onAddExtraProduct,
            onAddDish = onAddExtraDish,
            onAddCustomExtra = onAddCustomExtra,
            onDismiss = { showExtraItemsSheet = false }
        )
    }
}
