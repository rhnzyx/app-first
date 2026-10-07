package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FieldlyRepository
import com.example.localization.AppLanguage
import com.example.localization.FieldlyStrings
import com.example.localization.LocalAppLanguage
import com.example.model.RepLivePosition
import com.example.model.RepStatus
import com.example.ui.components.FieldlyCard
import com.example.ui.components.FieldlyMapView
import com.example.ui.components.RepStatusRing
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerLiveMapScreen(
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val context = LocalContext.current

    val liveReps by FieldlyRepository.liveReps.collectAsState()
    val clients by FieldlyRepository.clients.collectAsState()

    var selectedRep by remember { mutableStateOf<RepLivePosition?>(liveReps.firstOrNull()) }
    var centerLat by remember { mutableStateOf(23.7808) }
    var centerLng by remember { mutableStateOf(90.4152) }
    var zoomLevel by remember { mutableIntStateOf(14) }

    // Bottom sheet state
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 160.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Header of sheet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (lang == AppLanguage.ENGLISH) "Field Team (${liveReps.size})" else "ফিল্ড টিম (${liveReps.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Realtime GPS • OSM Tiles Free Tier",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Active count chip
                    val activeCount = liveReps.count { it.status == RepStatus.ACTIVE }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(StatusActive.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$activeCount Moving",
                            color = StatusActive,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rep list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(liveReps) { repPos ->
                        val isSelected = selectedRep?.rep?.id == repPos.rep.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedRep = repPos
                                    centerLat = repPos.latitude
                                    centerLng = repPos.longitude
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) FieldlyAccent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RepStatusRing(
                                        status = repPos.status,
                                        initials = repPos.rep.initials,
                                        size = 42.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = repPos.rep.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val statusText = when (repPos.status) {
                                                RepStatus.ACTIVE -> "${repPos.speedKmh.toInt()} km/h"
                                                RepStatus.IDLE -> "Stationary"
                                                RepStatus.OFFLINE -> "Offline"
                                            }
                                            Text(
                                                text = "$statusText • Battery ${repPos.batteryLevel}%",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${repPos.visitsDone}/${repPos.visitsTotal} Visits",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = FieldlyAccent
                                    )
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${repPos.rep.phone}"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = "Call Rep",
                                            tint = FieldlyAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fullscreen OpenStreetMap
            FieldlyMapView(
                centerLat = centerLat,
                centerLng = centerLng,
                zoomLevel = zoomLevel,
                reps = liveReps,
                clients = clients,
                selectedRep = selectedRep,
                onRepSelected = { rep ->
                    selectedRep = rep
                    centerLat = rep.latitude
                    centerLng = rep.longitude
                },
                onClientSelected = { client ->
                    centerLat = client.latitude
                    centerLng = client.longitude
                }
            )

            // Top Quick Status Overlay
            selectedRep?.let { rep ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RepStatusRing(status = rep.status, initials = rep.rep.initials, size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(rep.rep.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("50m Check-in Zone Highlighted", fontSize = 11.sp, color = FieldlyAccent)
                            }
                        }

                        Button(
                            onClick = {
                                centerLat = rep.latitude
                                centerLng = rep.longitude
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FieldlyAccent)
                        ) {
                            Text("Center", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
