package com.example.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FieldlyRepository
import com.example.localization.AppLanguage
import com.example.localization.FieldlyStrings
import com.example.localization.LocalAppLanguage
import com.example.model.Client
import com.example.model.PlanItem
import com.example.model.VisitStatus
import com.example.ui.components.FieldlyCard
import com.example.ui.components.PrimaryFieldlyButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepHomeScreen(
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }

    val activeSession by FieldlyRepository.activeSession.collectAsState()
    val planItems by FieldlyRepository.planItems.collectAsState()
    val offlineQueue by FieldlyRepository.offlineQueue.collectAsState()
    val companySettings by FieldlyRepository.companySettings.collectAsState()
    val userLat by FieldlyRepository.currentLat.collectAsState()
    val userLng by FieldlyRepository.currentLng.collectAsState()
    val userAccuracy by FieldlyRepository.currentAccuracy.collectAsState()
    val isMockGps by FieldlyRepository.isMockGpsDetected.collectAsState()
    val isOnline by FieldlyRepository.isNetworkOnline.collectAsState()

    var showCheckInSheet by remember { mutableStateOf(false) }
    var selectedClientForCheckIn by remember { mutableStateOf<Client?>(null) }
    var checkInResult by remember { mutableStateOf<FieldlyRepository.VerificationResult?>(null) }

    val completedCount = planItems.count { it.status == VisitStatus.DONE || it.status == VisitStatus.NEEDS_REVIEW }
    val totalCount = planItems.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (activeSession?.isActive == true) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val nextPending = planItems.firstOrNull { it.status == VisitStatus.PENDING }?.client
                            ?: planItems.firstOrNull()?.client
                        selectedClientForCheckIn = nextPending
                        showCheckInSheet = true
                    },
                    containerColor = FieldlyAccent,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .padding(horizontal = 8.dp)
                        .testTag("big_check_in_button")
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = FieldlyStrings.checkIn(lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // 1. Day Session Card (Start Day / End Day)
            item {
                FieldlyCard(
                    backgroundColor = if (activeSession?.isActive == true) FieldlyPrimary else MaterialTheme.colorScheme.surface
                ) {
                    val isDayActive = activeSession?.isActive == true
                    val contentColor = if (isDayActive) Color.White else MaterialTheme.colorScheme.onSurface

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isDayActive) FieldlySuccess else FieldlyWarning)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isDayActive) FieldlyStrings.dayActive(lang) else FieldlyStrings.dayNotStarted(lang),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDayActive) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isDayActive) "Shift running • 8.4 km tracked" else "Ready to start your sales shift?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                        }

                        Button(
                            onClick = { FieldlyRepository.toggleDaySession() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDayActive) FieldlyError else FieldlyAccent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isDayActive) FieldlyStrings.endDay(lang) else FieldlyStrings.startDay(lang),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Offline Queue Alert Banner
            if (offlineQueue.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FieldlyWarning.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CloudOff, contentDescription = null, tint = FieldlyWarning)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = FieldlyStrings.offlineQueueNotice(lang, offlineQueue.size),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            TextButton(
                                onClick = { FieldlyRepository.syncOfflineQueue() }
                            ) {
                                Text(
                                    text = FieldlyStrings.syncNow(lang),
                                    fontWeight = FontWeight.Bold,
                                    color = FieldlyAccent
                                )
                            }
                        }
                    }
                }
            }

            // 3. Progress Card
            item {
                FieldlyCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == AppLanguage.ENGLISH) "Today's Visit Target" else "আজকের ভিজিট লক্ষ্য",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$completedCount / $totalCount",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = FieldlyAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = FieldlySuccess,
                        trackColor = FieldlyBorderLight
                    )
                }
            }

            // 4. Visit Plans Header
            item {
                Text(
                    text = if (lang == AppLanguage.ENGLISH) "Assigned Visits (${planItems.size})" else "নির্ধারিত ভিজিট তালিকা (${planItems.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 5. Plan Items List
            items(planItems) { item ->
                PlanItemRow(
                    item = item,
                    onCheckInClick = {
                        selectedClientForCheckIn = item.client
                        showCheckInSheet = true
                    }
                )
            }
        }
    }

    // Check-In Modal Sheet
    if (showCheckInSheet && selectedClientForCheckIn != null) {
        val client = selectedClientForCheckIn!!
        val currentDist = FieldlyRepository.calculateDistanceMeters(
            userLat, userLng, client.latitude, client.longitude
        )
        val radius = companySettings.checkInRadiusMeters

        var notes by remember { mutableStateOf("") }
        var photoTaken by remember { mutableStateOf(false) }
        var isSimulatedMock by remember { mutableStateOf(isMockGps) }

        ModalBottomSheet(
            onDismissRequest = {
                showCheckInSheet = false
                checkInResult = null
            },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Success / Reject banner if checked in
                if (checkInResult != null) {
                    val res = checkInResult!!
                    val (resBg, resIcon, resTitle) = when {
                        res.isSuccess -> Triple(FieldlySuccess, Icons.Default.CheckCircle, "Check-In Verified!")
                        res.isNeedsReview -> Triple(FieldlyWarning, Icons.Default.Warning, "Needs Manager Review")
                        else -> Triple(FieldlyError, Icons.Default.Error, "Check-In Rejected")
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(resBg.copy(alpha = 0.12f))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(resIcon, contentDescription = null, tint = resBg, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(resTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = resBg)
                                Text(res.message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    PrimaryFieldlyButton(
                        text = "Done",
                        onClick = {
                            showCheckInSheet = false
                            checkInResult = null
                        }
                    )
                } else {
                    // Pre-checkin sheet
                    Text(
                        text = "GPS Verified Check-In",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Client details card
                    FieldlyCard {
                        Text(client.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${client.clientType} • ${client.address}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current GPS Distance:", fontSize = 13.sp)
                            Text(
                                "${currentDist}m",
                                fontWeight = FontWeight.Bold,
                                color = if (currentDist <= radius) FieldlySuccess else FieldlyError
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Allowed Radius:", fontSize = 13.sp)
                            Text("${radius}m", fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("GPS Accuracy:", fontSize = 13.sp)
                            Text("±${userAccuracy.toInt()}m", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Photo proof capture button
                    OutlinedButton(
                        onClick = { photoTaken = !photoTaken },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (photoTaken) Icons.Default.Check else Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (photoTaken) "Photo Proof Attached ✓" else "Attach Storefront Photo Proof")
                    }

                    // Visit Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Visit Notes (orders, stock, feedback)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Mock GPS detector toggle (Simulate security check)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Simulate Mock Location (Security Test)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Switch(
                            checked = isSimulatedMock,
                            onCheckedChange = { isSimulatedMock = it }
                        )
                    }

                    // Submit Check-In Button
                    PrimaryFieldlyButton(
                        text = "Verify Proximity & Check-In",
                        onClick = {
                            // Haptic trigger
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                            }
                            val result = FieldlyRepository.processCheckIn(
                                client = client,
                                userLat = userLat,
                                userLng = userLng,
                                accuracy = userAccuracy,
                                isMock = isSimulatedMock,
                                notes = notes,
                                photoUri = if (photoTaken) "photo_proof_123.jpg" else null
                            )
                            checkInResult = result
                        },
                        backgroundColor = FieldlyAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanItemRow(
    item: PlanItem,
    onCheckInClick: () -> Unit
) {
    FieldlyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sequence badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FieldlyPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${item.sequenceOrder}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FieldlyPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.client.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.client.clientType} • ${item.client.address}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    if (item.targetTime != null) {
                        Text(
                            text = "Target: ${item.targetTime}",
                            fontSize = 11.sp,
                            color = FieldlyTextTertiaryLight
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = item.status)
                if (item.status == VisitStatus.PENDING) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onCheckInClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Check-In", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FieldlyAccent)
                    }
                }
            }
        }
    }
}
