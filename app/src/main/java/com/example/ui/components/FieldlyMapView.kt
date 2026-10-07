package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.Client
import com.example.model.RepLivePosition
import com.example.model.RepStatus
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun FieldlyMapView(
    centerLat: Double,
    centerLng: Double,
    zoomLevel: Int,
    reps: List<RepLivePosition>,
    clients: List<Client>,
    selectedRep: RepLivePosition?,
    onRepSelected: (RepLivePosition) -> Unit,
    onClientSelected: ((Client) -> Unit)? = null,
    onMapCenterChanged: ((Double, Double) -> Unit)? = null,
    modifier: Modifier = Modifier,
    tileUrlTemplate: String = "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
) {
    var currentLat by remember(centerLat) { mutableStateOf(centerLat) }
    var currentLng by remember(centerLng) { mutableStateOf(centerLng) }
    var zoom by remember(zoomLevel) { mutableStateOf(zoomLevel.coerceIn(13, 18)) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8ECEF))
            .pointerInput(zoom) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Drag translation in Mercator coordinates
                    val latDegreePerPixel = 0.000008 * 2.0.pow((18 - zoom).toDouble())
                    val lngDegreePerPixel = 0.000008 * 2.0.pow((18 - zoom).toDouble())
                    currentLat += dragAmount.y * latDegreePerPixel
                    currentLng -= dragAmount.x * lngDegreePerPixel
                    onMapCenterChanged?.invoke(currentLat, currentLng)
                }
            }
    ) {
        val density = LocalDensity.current.density
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val centerX = widthPx / 2f
        val centerY = heightPx / 2f

        // 1. OSM Tile layer grid calculation
        val centerTileX = lon2tile(currentLng, zoom)
        val centerTileY = lat2tile(currentLat, zoom)

        // Render 3x3 tile grid around center tile
        Box(modifier = Modifier.fillMaxSize()) {
            val tilePixelSize = 256f * (density / 2.75f)
            val fracX = (lon2tileDouble(currentLng, zoom) - centerTileX).toFloat()
            val fracY = (lat2tileDouble(currentLat, zoom) - centerTileY).toFloat()

            for (dx in -1..1) {
                for (dy in -1..1) {
                    val tileX = centerTileX + dx
                    val tileY = centerTileY + dy
                    val url = tileUrlTemplate
                        .replace("{z}", zoom.toString())
                        .replace("{x}", tileX.toString())
                        .replace("{y}", tileY.toString())

                    val xOffset = centerX + (dx - fracX) * tilePixelSize - (tilePixelSize / 2f)
                    val yOffset = centerY + (dy - fracY) * tilePixelSize - (tilePixelSize / 2f)

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = "OSM Tile",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .offset(
                                x = (xOffset / density).dp,
                                y = (yOffset / density).dp
                            )
                            .size((tilePixelSize / density).dp)
                    )
                }
            }
        }

        // 2. Vector canvas overlay for radius circles and paths
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw 50m check-in radius around selected rep or center
            val selected = selectedRep ?: reps.firstOrNull()
            if (selected != null) {
                val (sx, sy) = projectToScreen(
                    selected.latitude, selected.longitude,
                    currentLat, currentLng, zoom, centerX, centerY
                )
                // 50m radius ring
                val radiusPx = metersToPixels(50.0, selected.latitude, zoom)
                drawCircle(
                    color = FieldlyAccent.copy(alpha = 0.15f),
                    radius = radiusPx,
                    center = Offset(sx, sy)
                )
                drawCircle(
                    color = FieldlyAccent.copy(alpha = 0.6f),
                    radius = radiusPx,
                    center = Offset(sx, sy),
                    style = Stroke(width = 2f)
                )
            }
        }

        // 3. Client Pins on Map
        clients.forEach { client ->
            val (px, py) = projectToScreen(
                client.latitude, client.longitude,
                currentLat, currentLng, zoom, centerX, centerY
            )
            if (px in -60f..(widthPx + 60f) && py in -60f..(heightPx + 60f)) {
                Box(
                    modifier = Modifier
                        .offset(x = ((px - 18f) / density).dp, y = ((py - 36f) / density).dp)
                        .clickable { onClientSelected?.invoke(client) }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FieldlyPrimary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = client.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = client.name,
                            tint = FieldlyPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // 4. Rep Markers with Status Rings and Initials
        reps.forEach { repPos ->
            val (rx, ry) = projectToScreen(
                repPos.latitude, repPos.longitude,
                currentLat, currentLng, zoom, centerX, centerY
            )
            if (rx in -50f..(widthPx + 50f) && ry in -50f..(heightPx + 50f)) {
                val isSelected = selectedRep?.rep?.id == repPos.rep.id
                val ringColor = when (repPos.status) {
                    RepStatus.ACTIVE -> StatusActive
                    RepStatus.IDLE -> StatusIdle
                    RepStatus.OFFLINE -> StatusOffline
                }

                Box(
                    modifier = Modifier
                        .offset(x = ((rx - 24f) / density).dp, y = ((ry - 24f) / density).dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ringColor)
                        .padding(if (isSelected) 4.dp else 3.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) FieldlyPrimary else Color.White)
                        .clickable { onRepSelected(repPos) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = repPos.rep.initials,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSelected) Color.White else FieldlyPrimary
                    )
                }
            }
        }

        // 5. Map Controls (Zoom +, Zoom -, Center)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { if (zoom < 18) zoom += 1 },
                modifier = Modifier.size(44.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }

            FloatingActionButton(
                onClick = { if (zoom > 13) zoom -= 1 },
                modifier = Modifier.size(44.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }

            FloatingActionButton(
                onClick = {
                    currentLat = centerLat
                    currentLng = centerLng
                },
                modifier = Modifier.size(44.dp),
                containerColor = FieldlyAccent,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter")
            }
        }
    }
}

// Spherical Mercator Conversion Helpers
private fun lon2tile(lon: Double, zoom: Int): Int =
    floor((lon + 180.0) / 360.0 * (1 shl zoom)).toInt()

private fun lat2tile(lat: Double, zoom: Int): Int =
    floor((1.0 - asinh(tan(Math.toRadians(lat))) / Math.PI) / 2.0 * (1 shl zoom)).toInt()

private fun lon2tileDouble(lon: Double, zoom: Int): Double =
    (lon + 180.0) / 360.0 * (1 shl zoom)

private fun lat2tileDouble(lat: Double, zoom: Int): Double =
    (1.0 - asinh(tan(Math.toRadians(lat))) / Math.PI) / 2.0 * (1 shl zoom)

private fun projectToScreen(
    lat: Double, lng: Double,
    centerLat: Double, centerLng: Double,
    zoom: Int,
    centerX: Float, centerY: Float
): Pair<Float, Float> {
    val scale = 256.0 * (1 shl zoom)
    val worldX = (lng + 180.0) / 360.0 * scale
    val worldY = (1.0 - asinh(tan(Math.toRadians(lat))) / Math.PI) / 2.0 * scale

    val centerWorldX = (centerLng + 180.0) / 360.0 * scale
    val centerWorldY = (1.0 - asinh(tan(Math.toRadians(centerLat))) / Math.PI) / 2.0 * scale

    val screenX = (centerX + (worldX - centerWorldX)).toFloat()
    val screenY = (centerY + (worldY - centerWorldY)).toFloat()
    return Pair(screenX, screenY)
}

private fun metersToPixels(meters: Double, lat: Double, zoom: Int): Float {
    val metersPerPixel = (156543.03392 * cos(Math.toRadians(lat))) / (1 shl zoom)
    return (meters / metersPerPixel).toFloat()
}
