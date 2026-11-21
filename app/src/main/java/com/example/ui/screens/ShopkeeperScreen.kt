package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.components.VegNonVegBadge
import com.example.ui.theme.*
import com.example.viewmodel.QuickDropUiState

private enum class ShopkeeperTab(val label: String) {
    APPLY_STATUS("Shop & Apply"),
    LISTINGS("My Listings"),
    ORDERS_OTP("Orders & OTP")
}

@Composable
fun ShopkeeperScreen(
    uiState: QuickDropUiState,
    onBack: () -> Unit,
    onSelectShop: (String) -> Unit,
    onSubmitApplication: (
        shopName: String,
        ownerName: String,
        phone: String,
        shopType: ShopType,
        area: String,
        fullAddress: String,
        etaMins: Int,
        gstNote: String,
        initialListings: List<ShopListingDraft>
    ) -> String?,
    onAddListing: (
        appId: String,
        title: String,
        categoryOrCuisine: String,
        unitOrPortion: String,
        price: Int,
        mrp: Int,
        isVeg: Boolean,
        description: String
    ) -> Unit,
    onUpdateListingPrice: (appId: String, listingId: String, price: Int, mrp: Int) -> Unit,
    onRemoveListing: (appId: String, listingId: String) -> Unit,
    onMarkOrderPacked: (appId: String, orderId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableStateOf(ShopkeeperTab.APPLY_STATUS) }
    var showNewShopDialog by remember { mutableStateOf(false) }
    var showAddListingDialog by remember { mutableStateOf(false) }
    var editingPriceListing by remember { mutableStateOf<ShopListingDraft?>(null) }

    val activeShop = uiState.activeShopApplication
    val shopOrders = remember(uiState.orders, activeShop?.id) {
        if (activeShop == null) emptyList()
        else uiState.orders.filter { ord ->
            ord.shopId == activeShop.id || ord.reorderCartItems.any { it.sellerShopId == activeShop.id }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("shopkeeper_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_navigate_back),
                                    tint = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = "QuickDrop Partner Hub",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = activeShop?.shopName ?: "Apply to sell groceries or food",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.86f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = { showNewShopDialog = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = QuickDropSecondaryContainer,
                                contentColor = QuickDropOnSecondaryFixed
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("shopkeeper_new_apply_btn")
                        ) {
                            Icon(Icons.Filled.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Apply Shop", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = Color.Transparent,
                        contentColor = QuickDropSecondaryContainer,
                        divider = {}
                    ) {
                        ShopkeeperTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            val title = when (tab) {
                                ShopkeeperTab.ORDERS_OTP -> "Orders & OTP (${shopOrders.size})"
                                ShopkeeperTab.LISTINGS -> "Listings (${activeShop?.listings?.size ?: 0})"
                                else -> tab.label
                            }
                            Tab(
                                selected = selected,
                                onClick = { selectedTab = tab },
                                modifier = Modifier.testTag("shopkeeper_tab_${tab.name.lowercase()}"),
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                                        ),
                                        color = if (selected) QuickDropSecondaryContainer else Color.White.copy(alpha = 0.78f),
                                        maxLines = 1
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == ShopkeeperTab.LISTINGS && activeShop != null) {
                FloatingActionButton(
                    onClick = { showAddListingDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("shopkeeper_add_listing_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Listing")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .navigationBarsPadding()
        ) {
            when (selectedTab) {
                ShopkeeperTab.APPLY_STATUS -> ShopkeeperStatusTab(
                    applications = uiState.shopApplications,
                    activeShop = activeShop,
                    onSelectShop = onSelectShop,
                    onOpenApplyDialog = { showNewShopDialog = true },
                    onJumpToListings = { selectedTab = ShopkeeperTab.LISTINGS },
                    onJumpToOrders = { selectedTab = ShopkeeperTab.ORDERS_OTP }
                )

                ShopkeeperTab.LISTINGS -> ShopkeeperListingsTab(
                    activeShop = activeShop,
                    onOpenAddListing = { showAddListingDialog = true },
                    onEditPrice = { editingPriceListing = it },
                    onRemoveListing = { item ->
                        activeShop?.let { onRemoveListing(it.id, item.id) }
                    }
                )

                ShopkeeperTab.ORDERS_OTP -> ShopkeeperOrdersOtpTab(
                    activeShop = activeShop,
                    orders = shopOrders,
                    onMarkPacked = { orderId ->
                        activeShop?.let { onMarkOrderPacked(it.id, orderId) }
                    }
                )
            }
        }
    }

    if (showNewShopDialog) {
        ApplyNewShopDialog(
            categories = uiState.categories,
            onDismiss = { showNewShopDialog = false },
            onSubmit = { shopName, ownerName, phone, shopType, area, address, eta, gst, listings ->
                val createdId = onSubmitApplication(
                    shopName, ownerName, phone, shopType, area, address, eta, gst, listings
                )
                if (createdId != null) {
                    showNewShopDialog = false
                }
            }
        )
    }

    if (showAddListingDialog && activeShop != null) {
        AddShopListingDialog(
            shop = activeShop,
            categories = uiState.categories,
            onDismiss = { showAddListingDialog = false },
            onAdd = { title, cat, unit, price, mrp, isVeg, desc ->
                onAddListing(activeShop.id, title, cat, unit, price, mrp, isVeg, desc)
                showAddListingDialog = false
            }
        )
    }

    editingPriceListing?.let { listing ->
        if (activeShop != null) {
            EditListingPriceDialog(
                listing = listing,
                onDismiss = { editingPriceListing = null },
                onSave = { price, mrp ->
                    onUpdateListingPrice(activeShop.id, listing.id, price, mrp)
                    editingPriceListing = null
                }
            )
        }
    }
}

@Composable
private fun ShopkeeperStatusTab(
    applications: List<ShopkeeperApplication>,
    activeShop: ShopkeeperApplication?,
    onSelectShop: (String) -> Unit,
    onOpenApplyDialog: () -> Unit,
    onJumpToListings: () -> Unit,
    onJumpToOrders: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (applications.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Registered Shop Accounts (${applications.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(applications, key = { it.id }) { app ->
                            val selected = activeShop?.id == app.id
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectShop(app.id) },
                                label = { Text("${app.shopName} (${app.status.label.substringBefore(" ")})") }
                            )
                        }
                    }
                }
            }
        }

        if (activeShop == null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(18.dp),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Open Your Shop on QuickDrop",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Apply with your shop details & initial product/menu listings. Once Admin reviews and approves, your store, live orders & Rider Pickup OTP system turn ON!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onOpenApplyDialog, shape = RoundedCornerShape(12.dp)) {
                            Text("Apply Now")
                        }
                    }
                }
            }
        } else {
            item {
                val (statusColor, statusBg, statusIcon, statusHeadline, statusSub) = when (activeShop.status) {
                    ShopApplicationStatus.PENDING_REVIEW -> listOf(
                        Color(0xFFB26A00),
                        Color(0xFFFFF4E0),
                        Icons.Filled.HourglassTop,
                        "Pending Admin Review",
                        "Admin is reviewing your shop & listings. Once approved, your items go live and Orders + Rider OTP turn ON."
                    )
                    ShopApplicationStatus.APPROVED -> listOf(
                        VegGreen,
                        VegGreen.copy(alpha = 0.12f),
                        Icons.Filled.Verified,
                        "Shop Approved & System LIVE!",
                        "Customers can see your listings and place orders. Use Orders & OTP tab to pack orders and share Pickup OTP with Riders."
                    )
                    ShopApplicationStatus.REJECTED -> listOf(
                        MaterialTheme.colorScheme.error,
                        MaterialTheme.colorScheme.errorContainer,
                        Icons.Filled.Cancel,
                        "Application Rejected",
                        activeShop.adminReviewNote ?: "Please check your details and re-apply."
                    )
                }

                Surface(
                    color = statusBg as Color,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, (statusColor as Color).copy(alpha = 0.35f)),
                    modifier = Modifier.testTag("shopkeeper_status_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = statusIcon as androidx.compose.ui.graphics.vector.ImageVector,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = statusHeadline as String,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = statusColor
                            )
                        }
                        Text(
                            text = statusSub as String,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!activeShop.adminReviewNote.isNullOrBlank() && activeShop.status != ShopApplicationStatus.REJECTED) {
                            Text(
                                text = "Admin Note: ${activeShop.adminReviewNote}",
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 1.dp
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeShop.shopName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                color = if (activeShop.shopType == ShopType.FOOD) ZomatoCrimson.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = activeShop.shopType.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (activeShop.shopType == ShopType.FOOD) ZomatoCrimson else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text("Owner: ${activeShop.ownerName} • Phone: ${activeShop.phone}", style = MaterialTheme.typography.bodyMedium)
                        Text("Address: ${activeShop.fullAddress} (${activeShop.area})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (activeShop.gstOrLicenseNote.isNotBlank()) {
                            Text("License/GST: ${activeShop.gstOrLicenseNote}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Estimated Delivery: ${activeShop.etaMins} mins • Submitted ${activeShop.submittedAtText}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onJumpToListings,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Manage Listings (${activeShop.listings.size})")
                            }
                            Button(
                                onClick = onJumpToOrders,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Orders & OTP")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopkeeperListingsTab(
    activeShop: ShopkeeperApplication?,
    onOpenAddListing: () -> Unit,
    onEditPrice: (ShopListingDraft) -> Unit,
    onRemoveListing: (ShopListingDraft) -> Unit
) {
    if (activeShop == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Apply for a shop first to add product listings.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(
                color = if (activeShop.status == ShopApplicationStatus.APPROVED) {
                    VegGreen.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (activeShop.status == ShopApplicationStatus.APPROVED) {
                            "Listings are LIVE to customers (${activeShop.listings.size})"
                        } else {
                            "Draft Listings (${activeShop.listings.size}) — Published after Admin approval"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (activeShop.status == ShopApplicationStatus.APPROVED) VegGreen else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onOpenAddListing) {
                        Text("+ Add Item")
                    }
                }
            }
        }

        if (activeShop.listings.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.AddShoppingCart, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("No listings yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Add items you sell so Admin can review and publish your store.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onOpenAddListing) { Text("Add First Item") }
                    }
                }
            }
        }

        items(activeShop.listings, key = { it.id }) { item ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VegNonVegBadge(isVeg = item.isVeg)
                        Column {
                            Text(item.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "${item.unitOrPortion} • ${item.categoryOrCuisine}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "₹${item.price}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (item.mrp > item.price) {
                                    Text(
                                        text = "₹${item.mrp}",
                                        style = MaterialTheme.typography.bodySmall,
                                        textDecoration = TextDecoration.LineThrough,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = if (item.publishedTargetId != null) VegGreen.copy(alpha = 0.14f)
                                    else Color(0xFFFFF4E0),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = if (item.publishedTargetId != null) "LIVE" else "DRAFT",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = if (item.publishedTargetId != null) VegGreen else Color(0xFFB26A00),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    Row {
                        IconButton(onClick = { onEditPrice(item) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Price", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onRemoveListing(item) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove Listing", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopkeeperOrdersOtpTab(
    activeShop: ShopkeeperApplication?,
    orders: List<ActiveOrder>,
    onMarkPacked: (String) -> Unit
) {
    if (activeShop == null || activeShop.status != ShopApplicationStatus.APPROVED) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.testTag("shopkeeper_orders_locked_notice")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LockClock,
                        contentDescription = null,
                        tint = Color(0xFFB26A00),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Orders & Rider OTP Locked",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (activeShop == null) {
                            "Submit a shop application first. Once Admin approves it, live customer orders & Rider Pickup OTPs will appear here."
                        } else {
                            "\"${activeShop.shopName}\" is currently ${activeShop.status.label}. Once Admin reviews and approves your application, your entire Order & Pickup OTP system activates here."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (orders.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(18.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = VegGreen,
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            text = "Store is LIVE & Ready for Orders!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "When a customer orders from ${activeShop.shopName}, the order and your 4-digit Rider Pickup OTP will show up right here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(orders, key = { it.orderId }) { order ->
            val stageText = when (order.stageIndex) {
                0 -> "New Order Received"
                1 -> "Packed & Ready for Rider"
                2 -> "Rider Picked Up (Out for Delivery)"
                else -> "Delivered to Customer"
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.testTag("shop_order_card_${order.orderId}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Order ${order.orderId}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                text = "${order.itemsCount} items • ₹${order.totalPaid}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            color = if (order.stageIndex >= 2) VegGreen.copy(alpha = 0.14f)
                            else QuickDropSecondaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = stageText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (order.stageIndex >= 2) VegGreen else QuickDropOnSecondaryFixed,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Text(
                        text = order.summaryTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Assigned Rider: ${order.riderName} (${order.vehicleLabel})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Shopkeeper -> Rider Pickup OTP Box
                    Surface(
                        color = if (order.isShopPickupOtpVerified) VegGreen.copy(alpha = 0.12f)
                        else QuickDropSecondaryContainer.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (order.isShopPickupOtpVerified) VegGreen.copy(alpha = 0.4f)
                            else QuickDropOnSecondaryFixed.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_pickup_otp_box_${order.orderId}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (order.isShopPickupOtpVerified) {
                                        "Rider Pickup OTP Verified ✓"
                                    } else {
                                        "Tell Rider This Pickup OTP"
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (order.isShopPickupOtpVerified) VegGreen else QuickDropOnSecondaryFixed
                                )
                                Text(
                                    text = if (order.isShopPickupOtpVerified) {
                                        "Handed over to ${order.riderName}"
                                    } else {
                                        "Share this code with ${order.riderName} when handing over the packed order."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = if (order.isShopPickupOtpVerified) VegGreen else MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = order.shopPickupOtp,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    if (order.stageIndex == 0) {
                        Button(
                            onClick = { onMarkPacked(order.orderId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shop_mark_packed_${order.orderId}"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Mark Packed & Ready for Rider")
                        }
                    }
                }
            }
        }
    }
}
