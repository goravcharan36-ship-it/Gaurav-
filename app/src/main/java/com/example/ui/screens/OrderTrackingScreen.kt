package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ActiveOrder
import com.example.ui.theme.QuickDropOnSecondaryFixed
import com.example.ui.theme.QuickDropSecondaryContainer
import com.example.ui.theme.ZomatoCrimson

@Composable
fun OrderTrackingScreen(
    order: ActiveOrder,
    onBack: () -> Unit,
    onAdvanceStage: () -> Unit,
    onReorder: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val stages = listOf(
        "Order Confirmed" to "We received your order at ${order.hubOrKitchen}",
        (if (order.isFoodOrder) "Kitchen Preparing Meal" else "Packing at Darkstore") to "Freshly picking & sealing your ${order.itemsCount} items",
        "Rider On The Way" to "${order.riderName} is heading to your doorstep",
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
            // Live Route Illustration Card
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
                                        headerColor.copy(alpha = 0.1f),
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
                                        text = if (order.stageIndex >= 3) "COMPLETED" else "LIVE TRACKING",
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
                            text = "Delivery Details",
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
                    }
                }
            }

            // Interactive Demo Progression / Reorder Actions
            item {
                if (order.stageIndex < 3) {
                    OutlinedButton(
                        onClick = onAdvanceStage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("advance_delivery_stage_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.FastForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Advance Live Delivery Step")
                    }
                } else if (order.reorderCartItems.isNotEmpty()) {
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
}
