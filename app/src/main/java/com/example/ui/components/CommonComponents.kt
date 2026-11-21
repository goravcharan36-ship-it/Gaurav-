package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun QuickDropTopHeader(
    selectedAddress: DeliveryAddress,
    appMode: AppMode,
    isDetectingGpsLocation: Boolean = false,
    onLocationClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pulseTransition = rememberInfiniteTransition(label = "live_dot")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse_alpha"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_quickdrop_bag),
                    contentDescription = stringResource(R.string.cd_brand_logo),
                    modifier = Modifier.size(36.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val badgeColor = if (appMode == AppMode.GROCERIES) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            ZomatoCrimson
                        }
                        val badgeTextColor = if (appMode == AppMode.GROCERIES) {
                            QuickDropOnSecondaryFixed
                        } else {
                            Color.White
                        }
                        Surface(
                            color = badgeColor,
                            shape = CircleShape
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = if (appMode == AppMode.GROCERIES) Icons.Filled.Bolt else Icons.Filled.Restaurant,
                                    contentDescription = null,
                                    tint = badgeTextColor,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = if (appMode == AppMode.GROCERIES) "10 MINS" else "22 MINS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = badgeTextColor
                                )
                            }
                        }
                        Text(
                            text = if (isDetectingGpsLocation) "Locating GPS..." else "QuickDrop",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDetectingGpsLocation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onLocationClick)
                            .padding(vertical = 2.dp)
                            .testTag("location_selector_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDetectingGpsLocation) "Detecting current location..." else selectedAddress.areaShort,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.cd_location_dropdown),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .alpha(pulseAlpha)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Text(
                            text = "Live",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                        .clickable(onClick = onProfileClick)
                        .testTag("header_profile_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = stringResource(R.string.cd_profile_avatar),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingCartSummaryBar(
    itemCount: Int,
    subtotal: Int,
    totalSavings: Int,
    onViewCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onViewCart)
            .testTag("floating_cart_bar"),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingBag,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "$itemCount ${if (itemCount == 1) "ITEM" else "ITEMS"} • ₹$subtotal",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (totalSavings > 0) {
                        Text(
                            text = "₹$totalSavings saved on this cart",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "View Cart",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
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

@Composable
fun QuickDropBottomNavBar(
    currentTab: BottomTab,
    cartItemCount: Int,
    onSelectTab: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 4.dp
    ) {
        BottomTab.entries.forEach { tab ->
            val selected = currentTab == tab
            val icon = when (tab) {
                BottomTab.HOME -> if (selected) Icons.Filled.Storefront else Icons.Outlined.Storefront
                BottomTab.CATEGORIES -> if (selected) Icons.Filled.GridView else Icons.Outlined.GridView
                BottomTab.SEARCH -> if (selected) Icons.Filled.Search else Icons.Outlined.Search
                BottomTab.CART -> if (selected) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart
                BottomTab.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
                icon = {
                    if (tab == BottomTab.CART && cartItemCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(text = cartItemCount.toString())
                                }
                            }
                        ) {
                            Icon(imageVector = icon, contentDescription = tab.label)
                        }
                    } else {
                        Icon(imageVector = icon, contentDescription = tab.label)
                    }
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun AddOrStepperButton(
    quantity: Int,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    compact: Boolean = false
) {
    AnimatedContent(
        targetState = quantity > 0,
        label = "add_stepper_anim",
        modifier = modifier
    ) { hasItems ->
        if (!hasItems) {
            Surface(
                modifier = Modifier
                    .height(if (compact) 32.dp else 36.dp)
                    .widthIn(min = if (compact) 64.dp else 72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAdd),
                color = accentColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text(
                        text = "ADD",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = accentColor
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.height(if (compact) 32.dp else 36.dp),
                color = accentColor,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier.size(if (compact) 28.dp else 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = stringResource(R.string.cd_decrement_qty),
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier.size(if (compact) 28.dp else 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.cd_increment_qty),
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductArtwork(
    imageUrl: String,
    categoryId: String,
    title: String,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(tintColor),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotBlank()) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                loading = {
                    CategoryIconFallback(categoryId = categoryId, tint = MaterialTheme.colorScheme.primary)
                },
                error = {
                    CategoryIconFallback(categoryId = categoryId, tint = MaterialTheme.colorScheme.primary)
                }
            )
        } else {
            CategoryIconFallback(categoryId = categoryId, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun CategoryIconFallback(
    categoryId: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val icon = iconForCategory(categoryId)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(tint.copy(alpha = 0.14f), Color.Transparent)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
fun VegNonVegBadge(isVeg: Boolean, modifier: Modifier = Modifier) {
    val color = if (isVeg) VegGreen else NonVegRed
    Box(
        modifier = modifier
            .size(14.dp)
            .border(1.3.dp, color, RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
    }
}

fun iconForCategory(key: String): ImageVector {
    return when (key) {
        "dairy", "breakfast_dining" -> Icons.Filled.BreakfastDining
        "fruits_veg", "nutrition" -> Icons.Filled.Eco
        "atta_rice", "grain" -> Icons.Filled.Grain
        "snacks", "cookie" -> Icons.Filled.Cookie
        "drinks", "local_bar" -> Icons.Filled.LocalBar
        "sweets", "icecream" -> Icons.Filled.Icecream
        "instant", "ramen_dining" -> Icons.Filled.RamenDining
        "household", "cleaning_services" -> Icons.Filled.CleaningServices
        "c_biryani", "rice_bowl" -> Icons.Filled.RiceBowl
        "c_dosa", "bakery_dining" -> Icons.Filled.BakeryDining
        "c_pizza", "local_pizza" -> Icons.Filled.LocalPizza
        "c_burger", "lunch_dining" -> Icons.Filled.LunchDining
        "c_dessert" -> Icons.Filled.Cake
        "c_chai", "local_cafe" -> Icons.Filled.LocalCafe
        "doorbell" -> Icons.Filled.Doorbell
        "phone_disabled" -> Icons.Filled.PhoneDisabled
        "meeting_room" -> Icons.Filled.MeetingRoom
        "security" -> Icons.Filled.Security
        else -> Icons.Filled.ShoppingBasket
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressPickerBottomSheet(
    addresses: List<DeliveryAddress>,
    selectedAddress: DeliveryAddress,
    onSelectAddress: (DeliveryAddress) -> Unit,
    onAddCustomAddress: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var customLabel by remember { mutableStateOf("Home 2") }
    var customFull by remember { mutableStateOf("") }
    var customArea by remember { mutableStateOf("Bengaluru") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Delivery Location",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            InteractiveAddressMapCard(
                selectedAreaLabel = customArea,
                onMapPinPicked = { area, hint ->
                    customArea = area
                    customFull = hint
                    showAddForm = true
                }
            )

            addresses.forEach { addr ->
                val isSelected = addr.id == selectedAddress.id
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            onSelectAddress(addr)
                            onDismiss()
                        },
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(14.dp),
                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = when (addr.label.lowercase()) {
                                "home" -> Icons.Filled.Home
                                "work" -> Icons.Filled.Work
                                else -> Icons.Filled.LocationOn
                            },
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = addr.label,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "• ${addr.distanceKm} km from ${addr.hubName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = addr.fullAddress,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (!showAddForm) {
                OutlinedButton(
                    onClick = { showAddForm = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.AddLocationAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Address")
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customLabel,
                        onValueChange = { customLabel = it },
                        label = { Text("Label (e.g. Home, Friend's Flat)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customFull,
                        onValueChange = { customFull = it },
                        label = { Text("Flat / Street / Landmark") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customArea,
                        onValueChange = { customArea = it },
                        label = { Text("Area & City") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (customFull.isNotBlank()) {
                                onAddCustomAddress(customLabel, customFull, customArea)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save & Deliver Here")
                    }
                }
            }
        }
    }
}
