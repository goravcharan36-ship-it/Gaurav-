package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.model.*
import com.example.ui.theme.QuickDropOnSecondaryFixed
import com.example.ui.theme.QuickDropSecondaryContainer
import com.example.ui.theme.VegGreen
import com.example.ui.theme.ZomatoCrimson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.roundToInt

@Composable
fun LiveDeliveryRouteMap(
    order: ActiveOrder,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = if (order.isFoodOrder) ZomatoCrimson else MaterialTheme.colorScheme.primary

    var zoom by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val targetProgress = when (order.stageIndex) {
        0 -> 0.06f
        1 -> 0.22f
        2 -> 0.68f
        else -> 1.0f
    }
    val riderProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 800),
        label = "rider_route_progress"
    )

    val pulseTransition = rememberInfiniteTransition(label = "rider_pulse")
    val pulseRadiusScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.9f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "pulse_scale"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(236.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(Color(0xFFEAF0EC))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoomChange, _ ->
                            zoom = (zoom * zoomChange).coerceIn(1f, 2.4f)
                            val maxPan = 120f * zoom
                            panOffset = Offset(
                                (panOffset.x + pan.x).coerceIn(-maxPan, maxPan),
                                (panOffset.y + pan.y).coerceIn(-maxPan, maxPan)
                            )
                        }
                    }
                    .testTag("live_delivery_map")
            ) {
                val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                val heightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

                val origin = Offset(widthPx * 0.16f, heightPx * 0.72f)
                val control = Offset(widthPx * 0.46f, heightPx * 0.16f)
                val dest = Offset(widthPx * 0.84f, heightPx * 0.34f)

                fun quadPoint(t: Float): Offset {
                    val u = 1f - t
                    return Offset(
                        x = u * u * origin.x + 2f * u * t * control.x + t * t * dest.x,
                        y = u * u * origin.y + 2f * u * t * control.y + t * t * dest.y
                    )
                }

                val riderPt = quadPoint(riderProgress)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = zoom
                            scaleY = zoom
                            translationX = panOffset.x
                            translationY = panOffset.y
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Park areas
                        drawRoundRect(
                            color = Color(0xFFD2EBD6),
                            topLeft = Offset(size.width * 0.05f, size.height * 0.12f),
                            size = Size(size.width * 0.24f, size.height * 0.28f),
                            cornerRadius = CornerRadius(18f, 18f)
                        )
                        drawRoundRect(
                            color = Color(0xFFD2EBD6),
                            topLeft = Offset(size.width * 0.62f, size.height * 0.60f),
                            size = Size(size.width * 0.30f, size.height * 0.26f),
                            cornerRadius = CornerRadius(18f, 18f)
                        )
                        // Lake feature
                        drawRoundRect(
                            color = Color(0xFFC6E2F9),
                            topLeft = Offset(size.width * 0.36f, size.height * 0.66f),
                            size = Size(size.width * 0.20f, size.height * 0.24f),
                            cornerRadius = CornerRadius(32f, 32f)
                        )

                        // Building blocks
                        val blockColor = Color(0xFFE0E6E2)
                        listOf(
                            Offset(0.34f, 0.12f) to Size(0.14f, 0.16f),
                            Offset(0.54f, 0.10f) to Size(0.16f, 0.15f),
                            Offset(0.10f, 0.48f) to Size(0.18f, 0.14f),
                            Offset(0.42f, 0.42f) to Size(0.15f, 0.14f),
                            Offset(0.76f, 0.12f) to Size(0.16f, 0.15f)
                        ).forEach { (pos, sz) ->
                            drawRoundRect(
                                color = blockColor,
                                topLeft = Offset(size.width * pos.x, size.height * pos.y),
                                size = Size(size.width * sz.width, size.height * sz.height),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                        }

                        // Secondary streets
                        val minorRoad = Color(0xFFF9FBFA)
                        for (i in 1..4) {
                            val y = size.height * (i * 0.20f)
                            drawLine(
                                color = minorRoad,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 10f
                            )
                        }
                        for (i in 1..4) {
                            val x = size.width * (i * 0.22f)
                            drawLine(
                                color = minorRoad,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 10f
                            )
                        }

                        // Major arterial roads
                        drawLine(
                            color = Color(0xFFFFFFFF),
                            start = Offset(0f, size.height * 0.78f),
                            end = Offset(size.width, size.height * 0.26f),
                            strokeWidth = 18f
                        )
                        drawLine(
                            color = Color(0xFFFFF4D2),
                            start = Offset(0f, size.height * 0.34f),
                            end = Offset(size.width, size.height * 0.56f),
                            strokeWidth = 16f
                        )

                        // Remaining route dashed curve
                        val fullRoute = Path().apply {
                            moveTo(origin.x, origin.y)
                            quadraticTo(control.x, control.y, dest.x, dest.y)
                        }
                        drawPath(
                            path = fullRoute,
                            color = accentColor.copy(alpha = 0.28f),
                            style = Stroke(
                                width = 10f,
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
                            )
                        )

                        // Active completed segment from origin to rider
                        val completedPath = Path().apply {
                            moveTo(origin.x, origin.y)
                            val steps = 20
                            for (s in 1..steps) {
                                val pt = quadPoint(riderProgress * (s.toFloat() / steps))
                                lineTo(pt.x, pt.y)
                            }
                        }
                        drawPath(
                            path = completedPath,
                            color = accentColor,
                            style = Stroke(width = 11f, cap = StrokeCap.Round)
                        )

                        // Pulsing halo around rider
                        if (order.stageIndex in 1..2) {
                            drawCircle(
                                color = accentColor.copy(alpha = 0.22f),
                                radius = 26f * pulseRadiusScale,
                                center = riderPt
                            )
                        }
                    }

                    // Origin Hub/Kitchen Marker
                    Box(
                        modifier = Modifier
                            .offset { IntOffset((origin.x - 16.dp.toPx()).roundToInt(), (origin.y - 16.dp.toPx()).roundToInt()) }
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = Color(0xFF1A2229),
                            border = BorderStroke(2.dp, Color.White),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (order.isFoodOrder) Icons.Filled.Restaurant else Icons.Filled.Storefront,
                                    contentDescription = "Origin Hub",
                                    tint = QuickDropSecondaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Destination Home Marker
                    Box(
                        modifier = Modifier
                            .offset { IntOffset((dest.x - 17.dp.toPx()).roundToInt(), (dest.y - 30.dp.toPx()).roundToInt()) }
                    ) {
                        Surface(
                            modifier = Modifier.size(34.dp),
                            shape = CircleShape,
                            color = VegGreen,
                            border = BorderStroke(2.dp, Color.White),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = "Delivery Destination",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Live Rider Scooter Pin
                    Box(
                        modifier = Modifier
                            .offset { IntOffset((riderPt.x - 20.dp.toPx()).roundToInt(), (riderPt.y - 20.dp.toPx()).roundToInt()) }
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = accentColor,
                            border = BorderStroke(2.5.dp, Color.White),
                            shadowElevation = 6.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.ElectricMoped,
                                    contentDescription = "Live Delivery Partner",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Top-left Live Route Badge
                Surface(
                    color = Color(0xFF1A2229).copy(alpha = 0.9f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Navigation,
                            contentDescription = null,
                            tint = QuickDropSecondaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (order.stageIndex >= 3) "Delivered at Doorstep"
                            else "${order.hubOrKitchen} → You (${order.etaMinutes}m)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Bottom-right Map Controls (Zoom + Open External Map)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = { zoom = (zoom + 0.35f).coerceAtMost(2.4f) },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Zoom in map", modifier = Modifier.size(18.dp))
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            zoom = 1f
                            panOffset = Offset.Zero
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.MyLocation, contentDescription = "Recenter map", modifier = Modifier.size(18.dp))
                    }
                }

                // Bottom-left Open in Google Maps chip
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = CircleShape,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(CircleShape)
                        .clickable {
                            val query = Uri.encode(order.deliveryAddress)
                            val uri = Uri.parse("geo:12.9716,77.5946?q=$query")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            runCatching { context.startActivity(intent) }
                        }
                        .testTag("open_external_maps_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = "Open in Maps",
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Open Map",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Legend Footer Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF1A2229), CircleShape))
                    Text("Hub", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Box(modifier = Modifier.size(8.dp).background(accentColor, CircleShape))
                    Text(order.riderName, style = MaterialTheme.typography.labelSmall, color = accentColor)
                    Spacer(Modifier.width(6.dp))
                    Box(modifier = Modifier.size(8.dp).background(VegGreen, CircleShape))
                    Text("Home", style = MaterialTheme.typography.labelSmall, color = VegGreen)
                }
                Text(
                    text = "Pinch or tap + to zoom",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InteractiveAddressMapCard(
    selectedAreaLabel: String,
    onMapPinPicked: (areaShort: String, fullAddressHint: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pinNormalized by remember { mutableStateOf(Offset(0.55f, 0.42f)) }
    var gpsStatus by remember { mutableStateOf<String?>(null) }
    var isLocating by remember { mutableStateOf(false) }

    val hubPoints = remember {
        listOf(
            Triple("Indiranagar, Bengaluru", "100 Feet Road, Indiranagar", Offset(0.28f, 0.28f)),
            Triple("Koramangala, Bengaluru", "80 Feet Road, Koramangala 4th Block", Offset(0.45f, 0.68f)),
            Triple("HSR Layout, Bengaluru", "27th Main Road, Sector 1, HSR", Offset(0.76f, 0.72f)),
            Triple("Bellandur, Bengaluru", "Green Glen Layout, Bellandur", Offset(0.78f, 0.32f))
        )
    }

    fun resolveLocationFromDevice() {
        scope.launch {
            isLocating = true
            gpsStatus = "Detecting GPS coordinates..."
            val result = fetchDeviceLocationAddress(context)
            isLocating = false
            if (result != null) {
                pinNormalized = Offset(0.52f, 0.48f)
                gpsStatus = "GPS locked: ${result.areaShort}"
                onMapPinPicked(result.areaShort, result.fullAddress)
            } else {
                gpsStatus = "GPS unavailable here — tap any pin on the map"
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            resolveLocationFromDevice()
        } else {
            gpsStatus = "Permission denied — tap any pin on the map instead"
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFE9F0EB))
                    .pointerInput(Unit) {
                        detectTapGestures { tap ->
                            val nx = (tap.x / size.width.toFloat()).coerceIn(0.08f, 0.92f)
                            val ny = (tap.y / size.height.toFloat()).coerceIn(0.12f, 0.88f)
                            pinNormalized = Offset(nx, ny)
                            val nearest = hubPoints.minByOrNull { (_, _, pt) ->
                                val dx = pt.x - nx
                                val dy = pt.y - ny
                                dx * dx + dy * dy
                            } ?: hubPoints.first()
                            onMapPinPicked(nearest.first, nearest.second)
                            gpsStatus = "Pin dropped near ${nearest.first}"
                        }
                    }
                    .testTag("address_picker_map")
            ) {
                val w = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                val h = constraints.maxHeight.toFloat().coerceAtLeast(1f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = Color(0xFFD0EAD4),
                        topLeft = Offset(size.width * 0.08f, size.height * 0.14f),
                        size = Size(size.width * 0.22f, size.height * 0.32f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        color = Color(0xFFC5E1F8),
                        topLeft = Offset(size.width * 0.52f, size.height * 0.56f),
                        size = Size(size.width * 0.22f, size.height * 0.28f),
                        cornerRadius = CornerRadius(24f, 24f)
                    )
                    for (i in 1..3) {
                        drawLine(
                            color = Color.White,
                            start = Offset(0f, size.height * (i * 0.25f)),
                            end = Offset(size.width, size.height * (i * 0.25f)),
                            strokeWidth = 10f
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(size.width * (i * 0.25f), 0f),
                            end = Offset(size.width * (i * 0.25f), size.height),
                            strokeWidth = 10f
                        )
                    }
                    hubPoints.forEach { (_, _, pt) ->
                        drawCircle(
                            color = Color(0xFF0C831F).copy(alpha = 0.22f),
                            radius = 18f,
                            center = Offset(size.width * pt.x, size.height * pt.y)
                        )
                        drawCircle(
                            color = Color(0xFF0C831F),
                            radius = 7f,
                            center = Offset(size.width * pt.x, size.height * pt.y)
                        )
                    }
                }

                // Draggable/tappable delivery pin
                Box(
                    modifier = Modifier.offset {
                        IntOffset(
                            (pinNormalized.x * w - 16.dp.toPx()).roundToInt(),
                            (pinNormalized.y * h - 30.dp.toPx()).roundToInt()
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Selected Map Pin",
                        tint = ZomatoCrimson,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.94f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Tap map to pin: $selectedAreaLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !isLocating) {
                            val hasFine = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasFine) {
                                resolveLocationFromDevice()
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                        .testTag("detect_gps_location_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = "Detect GPS Location",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isLocating) "Locating..." else "Use GPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            if (gpsStatus != null) {
                Text(
                    text = gpsStatus!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

data class GpsDetectedAddress(
    val areaShort: String,
    val fullAddress: String,
    val latitude: Double,
    val longitude: Double
)

@SuppressLint("MissingPermission")
suspend fun fetchDeviceLocationAddress(context: Context): GpsDetectedAddress? {
    val location = findBestOrCurrentLocation(context) ?: return null
    return withContext(Dispatchers.IO) {
        runCatching {
            @Suppress("DEPRECATION")
            val addrList = Geocoder(context, Locale.getDefault())
                .getFromLocation(location.latitude, location.longitude, 1)
            val first = addrList?.firstOrNull()
            val subLoc = first?.subLocality?.takeIf { it.isNotBlank() }
            val locality = first?.locality?.takeIf { it.isNotBlank() }
            val area = when {
                subLoc != null && locality != null -> "$subLoc, $locality"
                subLoc != null -> subLoc
                locality != null -> locality
                else -> "GPS (${String.format(Locale.US, "%.3f", location.latitude)}, ${String.format(Locale.US, "%.3f", location.longitude)})"
            }
            val full = first?.getAddressLine(0)?.takeIf { it.isNotBlank() }
                ?: "GPS Location (${String.format(Locale.US, "%.4f", location.latitude)}, ${String.format(Locale.US, "%.4f", location.longitude)})"
            GpsDetectedAddress(
                areaShort = area,
                fullAddress = full,
                latitude = location.latitude,
                longitude = location.longitude
            )
        }.getOrElse {
            val coordsArea = "GPS (${String.format(Locale.US, "%.3f", location.latitude)}, ${String.format(Locale.US, "%.3f", location.longitude)})"
            val coordsFull = "Lat ${String.format(Locale.US, "%.4f", location.latitude)}, Lng ${String.format(Locale.US, "%.4f", location.longitude)}"
            GpsDetectedAddress(
                areaShort = coordsArea,
                fullAddress = coordsFull,
                latitude = location.latitude,
                longitude = location.longitude
            )
        }
    }
}

fun isDeviceLocationEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
    return runCatching { LocationManagerCompat.isLocationEnabled(lm) }.getOrDefault(false)
}

@SuppressLint("MissingPermission")
private suspend fun findBestOrCurrentLocation(context: Context): Location? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val enabledProviders = runCatching { lm.getProviders(true) }.getOrDefault(emptyList())

    val candidateProviders = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && enabledProviders.contains(LocationManager.FUSED_PROVIDER)) {
            add(LocationManager.FUSED_PROVIDER)
        }
        if (enabledProviders.contains(LocationManager.NETWORK_PROVIDER)) {
            add(LocationManager.NETWORK_PROVIDER)
        }
        if (enabledProviders.contains(LocationManager.GPS_PROVIDER)) {
            add(LocationManager.GPS_PROVIDER)
        }
        enabledProviders.filterNot { it in this }.forEach { add(it) }
    }

    for (provider in candidateProviders) {
        val timeout = if (provider == LocationManager.GPS_PROVIDER) 3500L else 2500L
        val fresh = withTimeoutOrNull(timeout) {
            requestSingleDeviceLocation(context, lm, provider)
        }
        if (fresh != null) return fresh
    }

    // Fallback to freshest/most-accurate last-known location across all providers
    var bestLoc: Location? = null
    val allProviders = runCatching { lm.allProviders }.getOrDefault(enabledProviders)
    for (p in allProviders) {
        val l = runCatching { lm.getLastKnownLocation(p) }.getOrNull() ?: continue
        if (bestLoc == null || l.time > bestLoc.time + 60_000L || l.accuracy < bestLoc.accuracy) {
            bestLoc = l
        }
    }
    return bestLoc
}

@SuppressLint("MissingPermission")
private suspend fun requestSingleDeviceLocation(
    context: Context,
    lm: LocationManager,
    provider: String
): Location? = suspendCancellableCoroutine { cont ->
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val signal = CancellationSignal()
        cont.invokeOnCancellation { signal.cancel() }
        runCatching {
            lm.getCurrentLocation(provider, signal, context.mainExecutor) { loc ->
                if (cont.isActive) cont.resume(loc)
            }
        }.onFailure {
            if (cont.isActive) cont.resume(null)
        }
    } else {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                runCatching { lm.removeUpdates(this) }
                if (cont.isActive) cont.resume(location)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
        }
        cont.invokeOnCancellation { runCatching { lm.removeUpdates(listener) } }
        runCatching {
            @Suppress("DEPRECATION")
            lm.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        }.onFailure {
            if (cont.isActive) cont.resume(null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExtraItemsBottomSheet(
    title: String,
    subtitle: String,
    products: List<GroceryProduct>,
    restaurants: List<Restaurant>,
    allowCustomItem: Boolean,
    onAddProduct: (GroceryProduct) -> Unit,
    onAddDish: (MenuItem) -> Unit,
    onAddCustomExtra: (name: String, price: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterTab by remember { mutableIntStateOf(0) } // 0=All, 1=Groceries, 2=Food, 3=Custom
    var customName by remember { mutableStateOf("") }
    var customPriceText by remember { mutableStateOf("40") }

    val matchingProducts = remember(products, searchQuery) {
        products.filter { prod ->
            prod.isInStock && (
                searchQuery.isBlank() ||
                    prod.name.contains(searchQuery, ignoreCase = true) ||
                    prod.brand.contains(searchQuery, ignoreCase = true)
                )
        }
    }

    val matchingDishes = remember(restaurants, searchQuery) {
        restaurants.filter { it.isAcceptingOrders }.flatMap { it.menu }.filter { dish ->
            searchQuery.isBlank() ||
                dish.name.contains(searchQuery, ignoreCase = true) ||
                dish.restaurantName.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 540.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onDismiss) { Text("Done") }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search extra items (milk, chips, dosa...)") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("extra_items_search_input")
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val tabs = buildList {
                    add(0 to "All")
                    add(1 to "Groceries")
                    add(2 to "Food")
                    if (allowCustomItem) add(3 to "+ Custom Saman")
                }
                items(tabs) { (id, label) ->
                    FilterChip(
                        selected = filterTab == id,
                        onClick = { filterTab = id },
                        label = { Text(label) }
                    )
                }
            }

            if (filterTab == 3 && allowCustomItem) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Add Any Extra Custom Item",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Item Name (e.g. Coriander, Ice Cubes)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_extra_name_input")
                        )
                        OutlinedTextField(
                            value = customPriceText,
                            onValueChange = { customPriceText = it.filter { c -> c.isDigit() } },
                            label = { Text("Estimated Price (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val price = customPriceText.toIntOrNull() ?: 40
                                onAddCustomExtra(customName, price)
                                customName = ""
                            },
                            enabled = customName.trim().isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_custom_extra_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add Extra Item")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filterTab == 0 || filterTab == 1) {
                        items(matchingProducts.take(20), key = { "extra_g_${it.id}" }) { prod ->
                            val safeIdx = prod.defaultVariantIndex.coerceIn(0, prod.variants.lastIndex)
                            val variant = prod.variants[safeIdx]
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
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        VegNonVegBadge(isVeg = prod.isVeg)
                                        Column {
                                            Text(
                                                text = prod.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${variant.label} • ₹${variant.price}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    FilledTonalButton(
                                        onClick = { onAddProduct(prod) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("extra_add_prod_${prod.id}")
                                    ) {
                                        Text("+ ADD")
                                    }
                                }
                            }
                        }
                    }

                    if (filterTab == 0 || filterTab == 2) {
                        items(matchingDishes.take(20), key = { "extra_f_${it.id}" }) { dish ->
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
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        VegNonVegBadge(isVeg = dish.isVeg)
                                        Column {
                                            Text(
                                                text = dish.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${dish.restaurantName} • ₹${dish.price}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    FilledTonalButton(
                                        onClick = { onAddDish(dish) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = ZomatoCrimson.copy(alpha = 0.14f),
                                            contentColor = ZomatoCrimson
                                        )
                                    ) {
                                        Text("+ ADD")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
