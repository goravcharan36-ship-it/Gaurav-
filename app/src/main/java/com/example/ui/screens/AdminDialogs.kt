package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.model.*
import com.example.ui.components.VegNonVegBadge
import com.example.ui.theme.VegGreen
import com.example.ui.theme.ZomatoCrimson

@Composable
fun AdminDeleteConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(title) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("admin_confirm_delete_btn")
            ) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminChangePasswordDialog(
    onDismiss: () -> Unit,
    onChangePassword: (current: String, newPass: String) -> Boolean
) {
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Admin Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = currentPass,
                    onValueChange = { currentPass = it; errorMsg = null },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it; errorMsg = null },
                    label = { Text("New Password (min 4 chars)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it; errorMsg = null },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        newPass.trim().length < 4 -> errorMsg = "New password must be at least 4 characters."
                        newPass != confirmPass -> errorMsg = "Passwords do not match."
                        !onChangePassword(currentPass, newPass) -> errorMsg = "Current password is incorrect."
                        else -> onDismiss()
                    }
                }
            ) {
                Text("Save Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminFeeConfigDialog(
    baseDeliveryFee: Int,
    handlingFee: Int,
    weatherSurgeFee: Int,
    freeThreshold: Int,
    onDismiss: () -> Unit,
    onSave: (baseFee: Int, handling: Int, surge: Int, threshold: Int) -> Unit
) {
    var baseFeeText by remember { mutableStateOf(baseDeliveryFee.toString()) }
    var handlingText by remember { mutableStateOf(handlingFee.toString()) }
    var surgeText by remember { mutableStateOf(weatherSurgeFee.toString()) }
    var thresholdText by remember { mutableStateOf(freeThreshold.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delivery & Handling Fees") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = baseFeeText,
                    onValueChange = { baseFeeText = it.filter { c -> c.isDigit() } },
                    label = { Text("Base Delivery Fee (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = handlingText,
                    onValueChange = { handlingText = it.filter { c -> c.isDigit() } },
                    label = { Text("Darkstore Handling Fee (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = surgeText,
                    onValueChange = { surgeText = it.filter { c -> c.isDigit() } },
                    label = { Text("Weather Surge Fee (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = { thresholdText = it.filter { c -> c.isDigit() } },
                    label = { Text("Free Delivery Threshold (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        baseFeeText.toIntOrNull() ?: baseDeliveryFee,
                        handlingText.toIntOrNull() ?: handlingFee,
                        surgeText.toIntOrNull() ?: weatherSurgeFee,
                        thresholdText.toIntOrNull() ?: freeThreshold
                    )
                },
                modifier = Modifier.testTag("admin_save_fees_btn")
            ) {
                Text("Apply Fees")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminEditOrderDialog(
    order: ActiveOrder,
    onDismiss: () -> Unit,
    onSave: (etaMinutes: Int, stageIndex: Int, deliveryAddress: String) -> Unit
) {
    var etaText by remember { mutableStateOf(order.etaMinutes.toString()) }
    var stageIndex by remember { mutableIntStateOf(order.stageIndex) }
    var address by remember { mutableStateOf(order.deliveryAddress) }
    val stages = listOf("Confirmed", if (order.isFoodOrder) "Preparing" else "Packing", "Out for Delivery", "Delivered")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Order ${order.orderId}") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Order Stage", style = MaterialTheme.typography.labelMedium)
                stages.forEachIndexed { idx, label ->
                    val isDeliveredLocked = idx == 3 && !order.isOtpVerified && order.stageIndex < 3
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = stageIndex == idx,
                            enabled = !isDeliveredLocked,
                            onClick = { stageIndex = idx }
                        )
                        Text(
                            text = if (isDeliveredLocked) "$label (Requires Customer OTP)" else label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDeliveredLocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                OutlinedTextField(
                    value = etaText,
                    onValueChange = { etaText = it.filter { c -> c.isDigit() } },
                    label = { Text("ETA (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery Address") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(etaText.toIntOrNull() ?: order.etaMinutes, stageIndex, address)
                }
            ) { Text("Update Order") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminAddProductDialog(
    categories: List<GroceryCategory>,
    onDismiss: () -> Unit,
    onConfirm: (
        brand: String,
        name: String,
        categoryId: String,
        subcategory: String,
        unitLabel: String,
        price: Int,
        mrp: Int,
        isVeg: Boolean,
        etaMinutes: Int,
        isBestseller: Boolean,
        badgeText: String?,
        isMissedQuickAdd: Boolean,
        extraVariants: List<ProductVariant>,
        keepOpen: Boolean
    ) -> Unit
) {
    var brand by remember { mutableStateOf("QuickDrop Fresh") }
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var catMenuExpanded by remember { mutableStateOf(false) }
    var etaText by remember { mutableStateOf("10") }
    var badgeText by remember { mutableStateOf("NEW") }
    var isVeg by remember { mutableStateOf(true) }
    var isBestseller by remember { mutableStateOf(true) }
    var isMissedQuickAdd by remember { mutableStateOf(false) }

    val variantStates = remember {
        mutableStateListOf(
            ProductVariant(
                id = "new_v1",
                label = "500 g",
                price = 79,
                mrp = 99
            )
        )
    }

    fun submitProduct(keepOpen: Boolean) {
        val sanitizedVariants = variantStates.mapIndexed { idx, v ->
            val safePrice = v.price.coerceAtLeast(1)
            v.copy(
                id = "new_v_${idx + 1}",
                label = v.label.trim().ifEmpty { "1 pack" },
                price = safePrice,
                mrp = v.mrp.coerceAtLeast(safePrice)
            )
        }
        val firstVar = sanitizedVariants.first()
        val eta = etaText.toIntOrNull() ?: 10
        val sub = selectedCategory.subcategories.getOrElse(1) { "All" }
        onConfirm(
            brand,
            name,
            selectedCategory.id,
            sub,
            firstVar.label,
            firstVar.price,
            firstVar.mrp,
            isVeg,
            eta,
            isBestseller,
            badgeText.trim().takeIf { it.isNotEmpty() },
            isMissedQuickAdd,
            sanitizedVariants,
            keepOpen
        )
        if (keepOpen) {
            name = ""
            variantStates.clear()
            variantStates.add(ProductVariant(id = "new_v1", label = "500 g", price = 79, mrp = 99))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .padding(vertical = 16.dp),
        title = { Text("Add New Grocery SKU") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Title *") },
                    placeholder = { Text("e.g. Organic Hass Avocado") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_product_name_input")
                )
                Box {
                    OutlinedButton(
                        onClick = { catMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Category: ${selectedCategory.name}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    DropdownMenu(
                        expanded = catMenuExpanded,
                        onDismissRequest = { catMenuExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat
                                    catMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = badgeText,
                        onValueChange = { badgeText = it },
                        label = { Text("Badge Tag") },
                        placeholder = { Text("NEW / HOT") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = etaText,
                        onValueChange = { etaText = it.filter { c -> c.isDigit() } },
                        label = { Text("ETA (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pack Variants & Prices",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(
                        onClick = {
                            val nextIdx = variantStates.size + 1
                            variantStates.add(
                                ProductVariant(
                                    id = "new_v_$nextIdx",
                                    label = "Pack $nextIdx",
                                    price = variantStates.lastOrNull()?.price ?: 99,
                                    mrp = variantStates.lastOrNull()?.mrp ?: 119
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Pack")
                    }
                }

                variantStates.forEachIndexed { idx, v ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pack ${idx + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (variantStates.size > 1) {
                                    IconButton(
                                        onClick = { variantStates.removeAt(idx) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove pack",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = v.label,
                                onValueChange = { variantStates[idx] = v.copy(label = it) },
                                label = { Text("Pack Size (e.g. 500 g)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = if (v.price == 0) "" else v.price.toString(),
                                    onValueChange = { raw ->
                                        val digits = raw.filter { it.isDigit() }
                                        variantStates[idx] = v.copy(price = digits.toIntOrNull() ?: 0)
                                    },
                                    label = { Text("Selling ₹") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = if (v.mrp == 0) "" else v.mrp.toString(),
                                    onValueChange = { raw ->
                                        val digits = raw.filter { it.isDigit() }
                                        variantStates[idx] = v.copy(mrp = digits.toIntOrNull() ?: 0)
                                    },
                                    label = { Text("MRP ₹") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pure Veg Item", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isVeg, onCheckedChange = { isVeg = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show in Home Bestsellers", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isBestseller, onCheckedChange = { isBestseller = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show in Cart 'Missed Something?'", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isMissedQuickAdd, onCheckedChange = { isMissedQuickAdd = it })
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { submitProduct(keepOpen = true) },
                    enabled = name.trim().isNotEmpty(),
                    modifier = Modifier.testTag("save_and_add_another_product_button")
                ) {
                    Text("+ Another")
                }
                Button(
                    onClick = { submitProduct(keepOpen = false) },
                    enabled = name.trim().isNotEmpty(),
                    modifier = Modifier.testTag("confirm_add_product_button")
                ) {
                    Text("Add SKU")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminEditProductFullDialog(
    product: GroceryProduct,
    categories: List<GroceryCategory>,
    onDismiss: () -> Unit,
    onConfirm: (
        brand: String,
        name: String,
        categoryId: String,
        subcategory: String,
        etaMinutes: Int,
        isVeg: Boolean,
        isInStock: Boolean,
        isBestseller: Boolean,
        isStealDeal: Boolean,
        badgeText: String?,
        variants: List<ProductVariant>
    ) -> Unit
) {
    var brand by remember { mutableStateOf(product.brand) }
    var name by remember { mutableStateOf(product.name) }
    var selectedCategory by remember {
        mutableStateOf(categories.firstOrNull { it.id == product.categoryId } ?: categories.first())
    }
    var catMenuExpanded by remember { mutableStateOf(false) }
    var subcategory by remember { mutableStateOf(product.subcategory) }
    var etaText by remember { mutableStateOf(product.etaMinutes.toString()) }
    var badgeText by remember { mutableStateOf(product.badgeText ?: "") }
    var isVeg by remember { mutableStateOf(product.isVeg) }
    var isInStock by remember { mutableStateOf(product.isInStock) }
    var isBestseller by remember { mutableStateOf(product.isBestseller) }
    var isStealDeal by remember { mutableStateOf(product.isStealDeal) }

    val variantStates = remember(product.id) {
        mutableStateListOf<ProductVariant>().apply { addAll(product.variants) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .padding(vertical = 16.dp),
        title = {
            Text(
                text = "Edit SKU Details & Prices",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_product_name_input")
                )
                Box {
                    OutlinedButton(
                        onClick = { catMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Category: ${selectedCategory.name}")
                    }
                    DropdownMenu(
                        expanded = catMenuExpanded,
                        onDismissRequest = { catMenuExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat
                                    subcategory = cat.subcategories.firstOrNull() ?: "All"
                                    catMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subcategory,
                        onValueChange = { subcategory = it },
                        label = { Text("Subcategory") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = etaText,
                        onValueChange = { etaText = it.filter { c -> c.isDigit() } },
                        label = { Text("ETA (mins)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.6f)
                    )
                }
                OutlinedTextField(
                    value = badgeText,
                    onValueChange = { badgeText = it },
                    label = { Text("Badge Tag (optional, e.g. BESTSELLER)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pack Variants & Editable Prices",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(
                        onClick = {
                            val nextIdx = variantStates.size + 1
                            variantStates.add(
                                ProductVariant(
                                    id = "${product.id}_v_${System.currentTimeMillis()}_$nextIdx",
                                    label = "Pack $nextIdx",
                                    price = variantStates.lastOrNull()?.price ?: 99,
                                    mrp = variantStates.lastOrNull()?.mrp ?: 119
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Pack")
                    }
                }

                variantStates.forEachIndexed { idx, v ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Variant ${idx + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (variantStates.size > 1) {
                                    IconButton(
                                        onClick = { variantStates.removeAt(idx) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove variant",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = v.label,
                                onValueChange = { variantStates[idx] = v.copy(label = it) },
                                label = { Text("Pack Label (e.g. 500 ml)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = if (v.price == 0) "" else v.price.toString(),
                                    onValueChange = { raw ->
                                        val digits = raw.filter { it.isDigit() }
                                        variantStates[idx] = v.copy(price = digits.toIntOrNull() ?: 0)
                                    },
                                    label = { Text("Selling ₹") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("edit_variant_price_$idx")
                                )
                                OutlinedTextField(
                                    value = if (v.mrp == 0) "" else v.mrp.toString(),
                                    onValueChange = { raw ->
                                        val digits = raw.filter { it.isDigit() }
                                        variantStates[idx] = v.copy(mrp = digits.toIntOrNull() ?: 0)
                                    },
                                    label = { Text("MRP ₹") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("In Stock", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isInStock, onCheckedChange = { isInStock = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pure Veg", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isVeg, onCheckedChange = { isVeg = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Featured Bestseller", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isBestseller, onCheckedChange = { isBestseller = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pin as Steal of the Day", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isStealDeal, onCheckedChange = { isStealDeal = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sanitized = variantStates.map { v ->
                        val safePrice = v.price.coerceAtLeast(1)
                        v.copy(
                            label = v.label.trim().ifEmpty { "1 pack" },
                            price = safePrice,
                            mrp = v.mrp.coerceAtLeast(safePrice)
                        )
                    }
                    onConfirm(
                        brand,
                        name,
                        selectedCategory.id,
                        subcategory,
                        etaText.toIntOrNull() ?: product.etaMinutes,
                        isVeg,
                        isInStock,
                        isBestseller,
                        isStealDeal,
                        badgeText,
                        sanitized
                    )
                },
                enabled = name.trim().isNotEmpty(),
                modifier = Modifier.testTag("confirm_edit_product_btn")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminCategoryDialog(
    existingCategory: GroceryCategory?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, hasSaleBadge: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(existingCategory?.name ?: "") }
    var hasSaleBadge by remember { mutableStateOf(existingCategory?.hasSaleBadge ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingCategory == null) "Add Grocery Category" else "Edit Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name *") },
                    placeholder = { Text("e.g. Organic & Gourmet") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show SALE badge on rail", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = hasSaleBadge, onCheckedChange = { hasSaleBadge = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, hasSaleBadge) },
                enabled = name.trim().isNotEmpty()
            ) {
                Text(if (existingCategory == null) "Add Category" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminAddRestaurantDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, Int, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var cuisines by remember { mutableStateOf("North Indian, Bowls") }
    var area by remember { mutableStateOf("Indiranagar") }
    var etaText by remember { mutableStateOf("22") }
    var costForTwoText by remember { mutableStateOf("450") }
    var offerBanner by remember { mutableStateOf("Flat ₹100 OFF above ₹299") }
    var isPureVeg by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Onboard Restaurant Partner") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Restaurant Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_restaurant_name_input")
                )
                OutlinedTextField(
                    value = cuisines,
                    onValueChange = { cuisines = it },
                    label = { Text("Cuisines") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Neighborhood / Area") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = etaText,
                        onValueChange = { etaText = it.filter { c -> c.isDigit() } },
                        label = { Text("ETA (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costForTwoText,
                        onValueChange = { costForTwoText = it.filter { c -> c.isDigit() } },
                        label = { Text("Cost for 2 (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = offerBanner,
                    onValueChange = { offerBanner = it },
                    label = { Text("Offer Banner") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pure Veg Kitchen", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isPureVeg, onCheckedChange = { isPureVeg = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name,
                        cuisines,
                        area,
                        etaText.toIntOrNull() ?: 22,
                        costForTwoText.toIntOrNull() ?: 450,
                        offerBanner,
                        isPureVeg
                    )
                },
                enabled = name.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = ZomatoCrimson)
            ) {
                Text("Onboard")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminEditRestaurantFullDialog(
    restaurant: Restaurant,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        cuisines: String,
        area: String,
        etaMins: Int,
        priceForTwo: Int,
        offerBanner: String,
        isPureVeg: Boolean,
        isAcceptingOrders: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(restaurant.name) }
    var cuisines by remember { mutableStateOf(restaurant.cuisines) }
    var area by remember { mutableStateOf(restaurant.area) }
    var etaText by remember { mutableStateOf(restaurant.deliveryTimeMins.toString()) }
    var costForTwoText by remember { mutableStateOf(restaurant.priceForTwo.toString()) }
    var offer by remember { mutableStateOf(restaurant.offerBanner) }
    var isPureVeg by remember { mutableStateOf(restaurant.isPureVeg) }
    var isAcceptingOrders by remember { mutableStateOf(restaurant.isAcceptingOrders) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Restaurant Details") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Restaurant Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cuisines,
                    onValueChange = { cuisines = it },
                    label = { Text("Cuisines") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = etaText,
                        onValueChange = { etaText = it.filter { c -> c.isDigit() } },
                        label = { Text("ETA (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costForTwoText,
                        onValueChange = { costForTwoText = it.filter { c -> c.isDigit() } },
                        label = { Text("Cost for 2 ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = offer,
                    onValueChange = { offer = it },
                    label = { Text("Offer Banner") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pure Veg Kitchen", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isPureVeg, onCheckedChange = { isPureVeg = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Accepting Orders", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isAcceptingOrders, onCheckedChange = { isAcceptingOrders = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name,
                        cuisines,
                        area,
                        etaText.toIntOrNull() ?: restaurant.deliveryTimeMins,
                        costForTwoText.toIntOrNull() ?: restaurant.priceForTwo,
                        offer,
                        isPureVeg,
                        isAcceptingOrders
                    )
                },
                enabled = name.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = ZomatoCrimson)
            ) { Text("Save Details") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminMenuDishesDialog(
    restaurant: Restaurant,
    onDismiss: () -> Unit,
    onAddDish: (name: String, desc: String, category: String, price: Int, mrp: Int, isVeg: Boolean, isBestseller: Boolean, isSpicy: Boolean) -> Unit,
    onUpdateDish: (dishId: String, name: String, desc: String, category: String, price: Int, mrp: Int, isVeg: Boolean, isBestseller: Boolean, isSpicy: Boolean) -> Unit,
    onDeleteDish: (dishId: String) -> Unit
) {
    var editingDish by remember { mutableStateOf<MenuItem?>(null) }
    var showAddDish by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .padding(vertical = 16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${restaurant.name} Menu",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${restaurant.menu.size} dishes • Edit prices or add/remove",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
                    onClick = { showAddDish = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Dish")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (restaurant.menu.isEmpty()) {
                    Text(
                        text = "No dishes on this menu yet. Tap Add Dish to create one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    restaurant.menu.forEach { dish ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        VegNonVegBadge(isVeg = dish.isVeg)
                                        Text(
                                            text = dish.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "₹${dish.price}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (dish.mrp > dish.price) {
                                            Text(
                                                text = "₹${dish.mrp}",
                                                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "• ${dish.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row {
                                    IconButton(onClick = { editingDish = dish }) {
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = "Edit Dish",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { onDeleteDish(dish.id) }) {
                                        Icon(
                                            Icons.Filled.DeleteOutline,
                                            contentDescription = "Remove Dish",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )

    if (showAddDish) {
        AdminDishFormDialog(
            existingDish = null,
            onDismiss = { showAddDish = false },
            onConfirm = { name, desc, cat, price, mrp, veg, bestseller, spicy ->
                onAddDish(name, desc, cat, price, mrp, veg, bestseller, spicy)
                showAddDish = false
            }
        )
    }

    editingDish?.let { dish ->
        AdminDishFormDialog(
            existingDish = dish,
            onDismiss = { editingDish = null },
            onConfirm = { name, desc, cat, price, mrp, veg, bestseller, spicy ->
                onUpdateDish(dish.id, name, desc, cat, price, mrp, veg, bestseller, spicy)
                editingDish = null
            }
        )
    }
}

@Composable
private fun AdminDishFormDialog(
    existingDish: MenuItem?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String, category: String, price: Int, mrp: Int, isVeg: Boolean, isBestseller: Boolean, isSpicy: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(existingDish?.name ?: "") }
    var desc by remember { mutableStateOf(existingDish?.description ?: "") }
    var category by remember { mutableStateOf(existingDish?.category ?: "Recommended") }
    var priceText by remember { mutableStateOf((existingDish?.price ?: 249).toString()) }
    var mrpText by remember { mutableStateOf((existingDish?.mrp ?: 299).toString()) }
    var isVeg by remember { mutableStateOf(existingDish?.isVeg ?: true) }
    var isBestseller by remember { mutableStateOf(existingDish?.isBestseller ?: true) }
    var isSpicy by remember { mutableStateOf(existingDish?.isSpicy ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingDish == null) "Add Menu Dish" else "Edit Dish & Price") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Menu Section (e.g. Recommended, Starters)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                        label = { Text("Selling ₹") },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pure Veg", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isVeg, onCheckedChange = { isVeg = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bestseller Badge", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isBestseller, onCheckedChange = { isBestseller = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Spicy Indicator", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isSpicy, onCheckedChange = { isSpicy = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceText.toIntOrNull() ?: 199
                    val m = mrpText.toIntOrNull() ?: p
                    onConfirm(name, desc, category, p, m, isVeg, isBestseller, isSpicy)
                },
                enabled = name.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = ZomatoCrimson)
            ) {
                Text(if (existingDish == null) "Add Dish" else "Save Dish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminCouponDialog(
    existingCoupon: Coupon?,
    onDismiss: () -> Unit,
    onConfirm: (code: String, title: String, desc: String, discount: Int, minOrder: Int, freeDel: Boolean, isActive: Boolean) -> Unit
) {
    var code by remember { mutableStateOf(existingCoupon?.code ?: "") }
    var title by remember { mutableStateOf(existingCoupon?.title ?: "") }
    var desc by remember { mutableStateOf(existingCoupon?.description ?: "") }
    var discountText by remember { mutableStateOf((existingCoupon?.discountAmount ?: 75).toString()) }
    var minOrderText by remember { mutableStateOf((existingCoupon?.minOrder ?: 299).toString()) }
    var freeDelivery by remember { mutableStateOf(existingCoupon?.freeDelivery ?: false) }
    var isActive by remember { mutableStateOf(existingCoupon?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingCoupon == null) "Create Promo Coupon" else "Edit Coupon ${existingCoupon.code}") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { if (existingCoupon == null) code = it.uppercase() },
                    enabled = existingCoupon == null,
                    label = { Text("Coupon Code *") },
                    placeholder = { Text("e.g. SAVE75") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_coupon_code_input")
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Offer Headline") },
                    placeholder = { Text("e.g. Flat ₹75 OFF") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { discountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Discount ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minOrderText,
                        onValueChange = { minOrderText = it.filter { c -> c.isDigit() } },
                        label = { Text("Min Cart ₹") },
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
                    Text("Unlocks Free Delivery", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = freeDelivery, onCheckedChange = { freeDelivery = it })
                }
                if (existingCoupon != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Coupon Active", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        code,
                        title,
                        desc,
                        discountText.toIntOrNull() ?: 75,
                        minOrderText.toIntOrNull() ?: 299,
                        freeDelivery,
                        isActive
                    )
                },
                enabled = code.trim().isNotEmpty(),
                modifier = Modifier.testTag("confirm_add_coupon_button")
            ) {
                Text(if (existingCoupon == null) "Publish" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminPromoBannerDialog(
    onDismiss: () -> Unit,
    onConfirm: (tag: String, title: String, subtitle: String, ctaText: String, isFoodPromo: Boolean) -> Unit
) {
    var tag by remember { mutableStateOf("FLASH SALE") }
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var cta by remember { mutableStateOf("Order Now") }
    var isFoodPromo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Home Promo Banner") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isFoodPromo,
                        onClick = { isFoodPromo = false },
                        label = { Text("Grocery Home") }
                    )
                    FilterChip(
                        selected = isFoodPromo,
                        onClick = { isFoodPromo = true },
                        label = { Text("Food Home") }
                    )
                }
                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Pill Tag") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Banner Headline *") },
                    placeholder = { Text("e.g. Weekend Mega Drop") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Subtitle") },
                    placeholder = { Text("e.g. Up to 40% off fresh essentials") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cta,
                    onValueChange = { cta = it },
                    label = { Text("CTA Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tag, title, subtitle, cta, isFoodPromo) },
                enabled = title.trim().isNotEmpty()
            ) { Text("Add Banner") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminRiderOtpDialog(
    order: ActiveOrder,
    onDismiss: () -> Unit,
    onVerifyOtp: (enteredOtp: String) -> Boolean
) {
    var otpInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Pin,
                contentDescription = null,
                tint = VegGreen
            )
        },
        title = { Text("Rider Delivery OTP Handover") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Order ${order.orderId} • Rider: ${order.riderName}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ask the customer for the 4-digit Delivery OTP displayed under their order in the QuickDrop app, then enter it below to finish the ride.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { raw ->
                        otpInput = raw.filter { it.isDigit() }.take(4)
                        errorMsg = null
                    },
                    label = { Text("Enter 4-Digit Customer OTP") },
                    placeholder = { Text("e.g. 4829") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMsg != null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_rider_otp_input")
                )
                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ok = onVerifyOtp(otpInput)
                    if (ok) {
                        onDismiss()
                    } else {
                        errorMsg = "Incorrect OTP! Ask customer for the 4-digit OTP shown under their order."
                    }
                },
                enabled = otpInput.length == 4,
                colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                modifier = Modifier.testTag("admin_confirm_otp_end_ride_btn")
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Verify & End Ride")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminShopPickupOtpDialog(
    order: ActiveOrder,
    onDismiss: () -> Unit,
    onVerifyPickupOtp: (enteredOtp: String) -> Boolean
) {
    var otpInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Storefront,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Shop Pickup OTP Verification") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Order ${order.orderId} • ${order.hubOrKitchen}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Rider ${order.riderName} must ask the Shopkeeper (Dukaandar) for their 4-digit Pickup OTP shown in their Partner Portal before leaving with the order.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { raw ->
                        otpInput = raw.filter { it.isDigit() }.take(4)
                        errorMsg = null
                    },
                    label = { Text("Enter 4-Digit Shop Pickup OTP") },
                    placeholder = { Text("e.g. 6194") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMsg != null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_shop_pickup_otp_input")
                )
                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ok = onVerifyPickupOtp(otpInput)
                    if (ok) {
                        onDismiss()
                    } else {
                        errorMsg = "Incorrect Shop Pickup OTP! Ask the Shopkeeper for the 4-digit Pickup OTP."
                    }
                },
                enabled = otpInput.length == 4,
                modifier = Modifier.testTag("admin_confirm_shop_pickup_otp_btn")
            ) {
                Icon(Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Verify & Start Ride")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminRejectShopDialog(
    application: ShopkeeperApplication,
    onDismiss: () -> Unit,
    onReject: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.GppBad,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Reject ${application.shopName}?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Add a clear reason so the shopkeeper knows what to fix before re-applying:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason / Note") },
                    placeholder = { Text("e.g. Please attach valid GST / FSSAI number") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onReject(reason) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("admin_confirm_reject_shop_btn")
            ) {
                Text("Reject Shop")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


