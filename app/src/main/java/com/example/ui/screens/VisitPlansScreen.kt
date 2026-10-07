package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Tune
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
import com.example.model.UserRole
import com.example.ui.components.FieldlyCard
import com.example.ui.components.PrimaryFieldlyButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun VisitPlansScreen(
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val currentUser by FieldlyRepository.currentUser.collectAsState()
    val planItems by FieldlyRepository.planItems.collectAsState()
    val companySettings by FieldlyRepository.companySettings.collectAsState()

    var showAdminSettingsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (currentUser.role == UserRole.REP) "My Assigned Route" else "Team Visit Plans",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Territory: Gulshan - Banani Commercial Route",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.MANAGER) {
                IconButton(
                    onClick = { showAdminSettingsDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(FieldlyAccent.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Rules", tint = FieldlyAccent)
                }
            }
        }

        // Summary Card
        FieldlyCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Current Verification Radius", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${companySettings.checkInRadiusMeters} meters", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FieldlyPrimary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FieldlySuccess.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Auto EOD: ${companySettings.reportTime}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FieldlySuccess)
                }
            }
        }

        // Ordered Route List
        Text("Sequential Route (${planItems.size} Stops)", fontSize = 15.sp, fontWeight = FontWeight.Bold)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(planItems) { item ->
                FieldlyCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FieldlyAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${item.sequenceOrder}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = item.client.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${item.client.clientType} • ${item.client.address}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            StatusBadge(status = item.status)
                            if (item.completedAt != null) {
                                Text(
                                    text = "Done: ${item.completedAt}",
                                    fontSize = 11.sp,
                                    color = FieldlySuccess
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Admin Company Rules Dialog
    if (showAdminSettingsDialog) {
        var currentRadius by remember { mutableFloatStateOf(companySettings.checkInRadiusMeters.toFloat()) }

        AlertDialog(
            onDismissRequest = { showAdminSettingsDialog = false },
            title = { Text("Company Rules Configuration", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Check-In Radius: ${currentRadius.toInt()} meters",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = currentRadius,
                        onValueChange = { currentRadius = it },
                        valueRange = 30f..150f,
                        steps = 11,
                        colors = SliderDefaults.colors(thumbColor = FieldlyAccent, activeTrackColor = FieldlyAccent)
                    )
                    Text(
                        text = "Radius boundary rule: GPS proximity must be within 30m - 150m (Default: 50m). Check-in beyond this distance is rejected on server.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("Work Hours: ${companySettings.workStartTime} - ${companySettings.workEndTime}")
                    Text("Automated EOD Report Time: ${companySettings.reportTime}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        FieldlyRepository.updateCheckInRadius(currentRadius.toInt())
                        showAdminSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FieldlyAccent)
                ) {
                    Text("Save Rules")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
