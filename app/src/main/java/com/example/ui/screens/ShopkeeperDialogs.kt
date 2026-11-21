package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.model.*

@Composable
fun ApplyNewShopDialog(
    categories: List<GroceryCategory>,
    onDismiss: () -> Unit,
    onSubmit: (
        shopName: String,
        ownerName: String,
        phone: String,
        shopType: ShopType,
        area: String,
        fullAddress: String,
        etaMins: Int,
        gstNote: String,
        initialListings: List<ShopListingDraft>
    ) -> Unit
) {
    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var shopType by remember { mutableStateOf(ShopType.GROCERY) }
    var area by remember { mutableStateOf("Indiranagar") }
    var fullAddress by remember { mutableStateOf("") }
    var etaText by remember { mutableStateOf("15") }
    var gstNote by remember { mutableStateOf("") }

    // Inline initial listing fields
    var itemTitle by remember { mutableStateOf("") }
    var itemUnit by remember { mutableStateOf("500 g") }
    var itemPriceText by remember { mutableStateOf("99") }
    var itemMrpText by remember { mutableStateOf("120") }
    var itemIsVeg by remember { mutableStateOf(true) }
    val draftItems = remember { mutableStateListOf<ShopListingDraft>() }

    fun addDraftItemFromFields() {
        val cleanTitle = itemTitle.trim()
        if (cleanTitle.isEmpty()) return
        val p = (itemPriceText.toIntOrNull() ?: 50).coerceAtLeast(1)
        val m = (itemMrpText.toIntOrNull() ?: p).coerceAtLeast(p)
        val catOrCuisine = if (shopType == ShopType.GROCERY) {
            categories.firstOrNull()?.id ?: "dairy"
        } else {
            "Recommended"
        }
        draftItems.add(
            ShopListingDraft(
                id = "draft_${draftItems.size + 1}_${System.currentTimeMillis()}",
                title = cleanTitle,
                categoryOrCuisine = catOrCuisine,
                unitOrPortion = itemUnit.trim().ifEmpty { if (shopType == ShopType.GROCERY) "1 pack" else "1 plate" },
                price = p,
                mrp = m,
                isVeg = itemIsVeg
            )
        )
        itemTitle = ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .widthIn(max = 520.dp),
        icon = { Icon(Icons.Filled.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Apply to Open Your Shop") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Submit your shop details and starter items. Admin will review & turn your store ON.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShopType.entries.forEach { type ->
                        FilterChip(
                            selected = shopType == type,
                            onClick = { shopType = type },
                            label = { Text(type.label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop / Kitchen Name *") },
                    placeholder = { Text("e.g. Gupta Kirana Mart") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apply_shop_name_input")
                )
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Owner Full Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apply_owner_name_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it.filter { ch -> ch.isDigit() }.take(12) },
                        label = { Text("Phone *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("apply_phone_input")
                    )
                    OutlinedTextField(
                        value = etaText,
                        onValueChange = { etaText = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("ETA (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area / Locality") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fullAddress,
                    onValueChange = { fullAddress = it },
                    label = { Text("Shop Address") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = gstNote,
                    onValueChange = { gstNote = it },
                    label = { Text("GST / FSSAI / License Info (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Add Starter Listings (${draftItems.size} added)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                draftItems.forEach { d ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${d.title} (${d.unitOrPortion}) • ₹${d.price}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { draftItems.remove(d) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove item", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = itemTitle,
                    onValueChange = { itemTitle = it },
                    label = { Text(if (shopType == ShopType.GROCERY) "Product Name" else "Dish Name") },
                    placeholder = { Text(if (shopType == ShopType.GROCERY) "e.g. Organic Honey 500g" else "e.g. Paneer Butter Masala") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = itemUnit,
                        onValueChange = { itemUnit = it },
                        label = { Text("Unit/Pack") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = itemPriceText,
                        onValueChange = { itemPriceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Price ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = itemMrpText,
                        onValueChange = { itemMrpText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("MRP ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = itemIsVeg, onCheckedChange = { itemIsVeg = it })
                        Text("Pure Veg Item", style = MaterialTheme.typography.bodySmall)
                    }
                    FilledTonalButton(
                        onClick = { addDraftItemFromFields() },
                        enabled = itemTitle.trim().isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Item")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemTitle.trim().isNotEmpty()) {
                        addDraftItemFromFields()
                    }
                    onSubmit(
                        shopName,
                        ownerName,
                        phone,
                        shopType,
                        area,
                        fullAddress,
                        etaText.toIntOrNull() ?: 15,
                        gstNote,
                        draftItems.toList()
                    )
                },
                enabled = shopName.trim().isNotEmpty() && ownerName.trim().isNotEmpty() && phone.trim().isNotEmpty(),
                modifier = Modifier.testTag("submit_shop_application_btn")
            ) {
                Text("Submit for Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddShopListingDialog(
    shop: ShopkeeperApplication,
    categories: List<GroceryCategory>,
    onDismiss: () -> Unit,
    onAdd: (
        title: String,
        categoryOrCuisine: String,
        unitOrPortion: String,
        price: Int,
        mrp: Int,
        isVeg: Boolean,
        description: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var unitOrPortion by remember {
        mutableStateOf(if (shop.shopType == ShopType.GROCERY) "1 pack" else "1 portion")
    }
    var priceText by remember { mutableStateOf("") }
    var mrpText by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf(
            if (shop.shopType == ShopType.GROCERY) {
                categories.firstOrNull()?.id ?: "dairy"
            } else {
                "Recommended"
            }
        )
    }
    var description by remember { mutableStateOf("") }
    var isVeg by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Listing to ${shop.shopName}") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (shop.shopType == ShopType.GROCERY) "Product Name *" else "Dish Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unitOrPortion,
                    onValueChange = { unitOrPortion = it },
                    label = { Text(if (shop.shopType == ShopType.GROCERY) "Pack / Weight" else "Portion Size") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (shop.shopType == ShopType.GROCERY && categories.isNotEmpty()) {
                    var catExpanded by remember { mutableStateOf(false) }
                    val catName = categories.firstOrNull { it.id == selectedCategory }?.name ?: selectedCategory
                    Box {
                        OutlinedButton(
                            onClick = { catExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Category: $catName")
                        }
                        DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCategory = cat.id
                                        catExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { selectedCategory = it },
                        label = { Text("Menu Section / Cuisine") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                        label = { Text("Selling Price ₹ *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = mrpText,
                        onValueChange = { mrpText = it.filter { c -> c.isDigit() } },
                        label = { Text("MRP ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isVeg, onCheckedChange = { isVeg = it })
                    Text("Vegetarian Item", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = (priceText.toIntOrNull() ?: 1).coerceAtLeast(1)
                    val m = (mrpText.toIntOrNull() ?: p).coerceAtLeast(p)
                    onAdd(title, selectedCategory, unitOrPortion, p, m, isVeg, description)
                },
                enabled = title.trim().isNotEmpty() && (priceText.toIntOrNull() ?: 0) > 0
            ) {
                Text(if (shop.status == ShopApplicationStatus.APPROVED) "Publish Live" else "Save Draft")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditListingPriceDialog(
    listing: ShopListingDraft,
    onDismiss: () -> Unit,
    onSave: (price: Int, mrp: Int) -> Unit
) {
    var priceText by remember { mutableStateOf(listing.price.toString()) }
    var mrpText by remember { mutableStateOf(listing.mrp.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Price • ${listing.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                    label = { Text("Selling Price ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mrpText,
                    onValueChange = { mrpText = it.filter { c -> c.isDigit() } },
                    label = { Text("MRP ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = (priceText.toIntOrNull() ?: listing.price).coerceAtLeast(1)
                    val m = (mrpText.toIntOrNull() ?: p).coerceAtLeast(p)
                    onSave(p, m)
                },
                enabled = (priceText.toIntOrNull() ?: 0) > 0
            ) { Text("Save Price") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
