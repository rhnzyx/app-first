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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FieldlyRepository
import com.example.localization.AppLanguage
import com.example.localization.FieldlyStrings
import com.example.localization.LocalAppLanguage
import com.example.model.RepLivePosition
import com.example.model.RepStatus
import com.example.model.SecurityFlag
import com.example.ui.components.BentoMetricTile
import com.example.ui.components.FieldlyCard
import com.example.ui.components.RepStatusRing
import com.example.ui.theme.*

@Composable
fun ManagerDashboardScreen(
    onNavigateToLiveMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val liveReps by FieldlyRepository.liveReps.collectAsState()
    val flags by FieldlyRepository.flags.collectAsState()
    val visits by FieldlyRepository.visits.collectAsState()

    var selectedRepForDetail by remember { mutableStateOf<RepLivePosition?>(null) }

    val activeRepsCount = liveReps.count { it.status == RepStatus.ACTIVE }
    val totalRepsCount = liveReps.size
    val visitsDoneCount = liveReps.sumOf { it.visitsDone }
    val totalVisitsTarget = liveReps.sumOf { it.visitsTotal }
    val visitsRemaining = (totalVisitsTarget - visitsDoneCount).coerceAtLeast(0)
    val needsReviewCount = visits.count { it.status == com.example.model.VisitStatus.NEEDS_REVIEW }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Dashboard Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (lang == AppLanguage.ENGLISH) "Manager Control Center" else "ম্যানেজার কন্ট্রোল সেন্টার",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Alpha Sales Corp • Live Operations",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onNavigateToLiveMap,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FieldlyAccent)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Live Map", fontSize = 12.sp)
                }
            }
        }

        // Bento Grid: 2x2 + 1 Wide
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BentoMetricTile(
                        title = "Active Reps",
                        value = "$activeRepsCount / $totalRepsCount",
                        subtitle = "Moving in territory",
                        icon = Icons.Default.DirectionsWalk,
                        iconTint = StatusActive,
                        modifier = Modifier.weight(1f)
                    )
                    BentoMetricTile(
                        title = "Visits Done",
                        value = "$visitsDoneCount",
                        subtitle = "Verified completions",
                        icon = Icons.Default.CheckCircle,
                        iconTint = FieldlySuccess,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BentoMetricTile(
                        title = "Remaining",
                        value = "$visitsRemaining",
                        subtitle = "Visits pending today",
                        icon = Icons.Default.HourglassBottom,
                        iconTint = FieldlyAccent,
                        modifier = Modifier.weight(1f)
                    )
                    BentoMetricTile(
                        title = "Needs Review",
                        value = "$needsReviewCount",
                        subtitle = "Accuracy drift photo",
                        icon = Icons.Default.CameraAlt,
                        iconTint = FieldlyWarning,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Wide Bento Tile for Security Flags
                BentoMetricTile(
                    title = "Suspicious Telemetry & Flags",
                    value = "${flags.size} Detected",
                    subtitle = "Mock GPS, root detection & speed jumps",
                    icon = Icons.Default.Shield,
                    iconTint = FieldlyError,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Team Reps Execution List Header
        item {
            Text(
                text = if (lang == AppLanguage.ENGLISH) "Rep Progress Breakdown" else "প্রতিনিধিদের অগ্রগতির তালিকা",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // List of Reps
        items(liveReps) { repPos ->
            FieldlyCard(
                modifier = Modifier.clickable { selectedRepForDetail = repPos }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RepStatusRing(
                            status = repPos.status,
                            initials = repPos.rep.initials,
                            size = 40.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = repPos.rep.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${repPos.rep.phone} • Battery ${repPos.batteryLevel}%",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${repPos.visitsDone} of ${repPos.visitsTotal} done",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = FieldlyAccent
                        )
                        val repProgress = if (repPos.visitsTotal > 0) repPos.visitsDone.toFloat() / repPos.visitsTotal else 0f
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { repProgress },
                            modifier = Modifier
                                .width(80.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = FieldlySuccess,
                            trackColor = FieldlyBorderLight
                        )
                    }
                }
            }
        }

        // Security Alerts Section
        if (flags.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (lang == AppLanguage.ENGLISH) "Active Security Flags" else "নিরাপত্তা সতর্কতা",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FieldlyError
                )
            }

            items(flags) { flag ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FieldlyError.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = FieldlyError,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = flag.repName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = flag.flagType.replace("_", " ").uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FieldlyError
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = flag.message,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail dialog for rep
    selectedRepForDetail?.let { rep ->
        AlertDialog(
            onDismissRequest = { selectedRepForDetail = null },
            confirmButton = {
                TextButton(onClick = { selectedRepForDetail = null }) {
                    Text("Close")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RepStatusRing(status = rep.status, initials = rep.rep.initials, size = 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(rep.rep.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Role: Sales Representative")
                    Text("Status: ${rep.status.name} (Speed: ${rep.speedKmh} km/h)")
                    Text("Battery Level: ${rep.batteryLevel}%")
                    Text("Completed Visits: ${rep.visitsDone} / ${rep.visitsTotal}")
                    Text("Last GPS Ping: 2 minutes ago")
                    Text("GPS Accuracy: ±${rep.accuracy.toInt()}m")
                }
            }
        )
    }
}
