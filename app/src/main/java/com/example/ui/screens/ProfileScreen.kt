package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ActiveOrder
import com.example.ui.theme.QuickDropOnSecondaryFixed
import com.example.ui.theme.QuickDropSecondaryContainer
import com.example.ui.theme.VegGreen
import com.example.ui.theme.ZomatoCrimson
import com.example.viewmodel.QuickDropUiState

@Composable
fun ProfileScreen(
    uiState: QuickDropUiState,
    onManageAddressesClick: () -> Unit,
    onToggleVegMode: () -> Unit,
    onToggleDarkTheme: () -> Unit,
    onOpenOrderTracking: (String) -> Unit,
    onReorder: (ActiveOrder) -> Unit,
    onOpenShopkeeperPortal: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    bottomContentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var showRefundInfoDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    val totalLifetimeSavings = uiState.orders.sumOf { it.totalSaved } + 340

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = bottomContentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Profile Header with discreet Three-Dot Menu
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Account",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box {
                    IconButton(
                        onClick = { showOverflowMenu = true },
                        modifier = Modifier.testTag("profile_three_dot_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Manage Addresses") },
                            leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                            onClick = {
                                showOverflowMenu = false
                                onManageAddressesClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Refund Policy") },
                            leadingIcon = { Icon(Icons.Filled.VerifiedUser, contentDescription = null) },
                            onClick = {
                                showOverflowMenu = false
                                showRefundInfoDialog = true
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Admin Portal") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.testTag("menu_open_admin_panel"),
                            onClick = {
                                showOverflowMenu = false
                                onOpenAdminPanel()
                            }
                        )
                    }
                }
            }
        }

        // User Profile & Savings Hero Card
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "QuickDrop Member",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.selectedAddress.areaShort} • Active",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = QuickDropSecondaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "GOLD",
                                style = MaterialTheme.typography.labelSmall,
                                color = QuickDropOnSecondaryFixed,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Savings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Total QuickDrop Savings",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                text = "₹$totalLifetimeSavings",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Dukaandar / Partner Shopkeeper Portal Entry Card
        item {
            val approvedCount = uiState.shopApplications.count { it.status == com.example.model.ShopApplicationStatus.APPROVED }
            val pendingCount = uiState.pendingShopApplicationsCount
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenShopkeeperPortal)
                    .testTag("profile_open_shopkeeper_portal"),
                color = QuickDropSecondaryContainer.copy(alpha = 0.48f),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Storefront,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Sell on QuickDrop • Partner Shop",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = QuickDropOnSecondaryFixed
                            )
                            Text(
                                text = when {
                                    approvedCount > 0 -> "$approvedCount shop LIVE • View orders & Rider Pickup OTP"
                                    pendingCount > 0 -> "$pendingCount application in review • Add listings anytime"
                                    else -> "Apply for your shop, add listings & receive orders"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Open Shopkeeper Portal",
                        tint = QuickDropOnSecondaryFixed
                    )
                }
            }
        }

        // Preferences
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "App Preferences",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

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
                            Icon(Icons.Filled.Eco, contentDescription = null, tint = VegGreen)
                            Column {
                                Text("Pure Veg Mode", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Hide non-veg items across groceries & food",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = uiState.isGlobalVegMode,
                            onCheckedChange = { onToggleVegMode() },
                            modifier = Modifier.testTag("profile_veg_switch")
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    )

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
                            Icon(
                                Icons.Filled.DarkMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text("Dark Theme", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Switch between light & dark appearance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = uiState.isDarkTheme,
                            onCheckedChange = { onToggleDarkTheme() },
                            modifier = Modifier.testTag("profile_dark_switch")
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onManageAddressesClick)
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Saved Addresses (${uiState.addresses.size})", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    uiState.selectedAddress.fullAddress,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showRefundInfoDialog = true }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Cancellation & Refund Guarantee", style = MaterialTheme.typography.titleSmall)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    }
                }
            }
        }

        // Order History
        item {
            Text(
                text = "Your Orders (${uiState.orders.size})",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(uiState.orders, key = { it.orderId }) { order ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
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
                            Surface(
                                color = if (order.isFoodOrder) ZomatoCrimson.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (order.isFoodOrder) Icons.Filled.Restaurant else Icons.Filled.Bolt,
                                    contentDescription = null,
                                    tint = if (order.isFoodOrder) ZomatoCrimson else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(6.dp).size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = order.hubOrKitchen,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${order.orderId} • ${order.placedTimeText}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = if (order.stageIndex >= 3) MaterialTheme.colorScheme.surfaceContainerLow
                            else MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = if (order.stageIndex >= 3) "Delivered" else "Live • ${order.etaMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (order.stageIndex >= 3) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = order.summaryTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        color = if (order.stageIndex >= 3) {
                            VegGreen.copy(alpha = 0.1f)
                        } else {
                            QuickDropSecondaryContainer.copy(alpha = 0.45f)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("profile_order_otp_${order.orderId}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (order.stageIndex >= 3) Icons.Filled.Verified else Icons.Filled.Pin,
                                contentDescription = null,
                                tint = if (order.stageIndex >= 3) VegGreen else QuickDropOnSecondaryFixed,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (order.stageIndex >= 3) {
                                    "OTP Verified (${order.deliveryOtp}) • Delivered"
                                } else {
                                    "Delivery OTP: ${order.deliveryOtp} • Give to rider at doorstep"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (order.stageIndex >= 3) VegGreen else QuickDropOnSecondaryFixed
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₹${order.totalPaid} • Saved ₹${order.totalSaved}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onOpenOrderTracking(order.orderId) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(if (order.stageIndex >= 3) "Details" else "Track Live")
                            }
                            if (order.reorderCartItems.isNotEmpty()) {
                                Button(
                                    onClick = { onReorder(order) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Reorder")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRefundInfoDialog) {
        AlertDialog(
            onDismissRequest = { showRefundInfoDialog = false },
            title = { Text("100% Cancellation & Refund Guarantee") },
            text = {
                Text(
                    "If you cancel before store packing or kitchen preparation starts, 100% of your payment is refunded instantly to your original UPI/card source. Damaged or missing items receive doorstep replacement or instant wallet credit."
                )
            },
            confirmButton = {
                TextButton(onClick = { showRefundInfoDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}
