package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.localization.LocalAppLanguage
import com.example.ui.components.FieldlyCard
import com.example.ui.components.PrimaryFieldlyButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun EodReportScreen(
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val context = LocalContext.current
    val currentUser by FieldlyRepository.currentUser.collectAsState()
    val visits by FieldlyRepository.visits.collectAsState()
    val activeSession by FieldlyRepository.activeSession.collectAsState()
    val companySettings by FieldlyRepository.companySettings.collectAsState()
    val flags by FieldlyRepository.flags.collectAsState()

    val totalVisits = visits.size
    val verifiedVisits = visits.count { it.status == com.example.model.VisitStatus.DONE }
    val flaggedVisits = visits.count { it.status == com.example.model.VisitStatus.NEEDS_REVIEW }
    val distanceKm = String.format("%.2f", (activeSession?.totalDistanceMeters ?: 0.0) / 1000.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Report Document Card
        FieldlyCard(
            borderColor = FieldlyAccent.copy(alpha = 0.5f)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(FieldlyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "End of Day Report",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Generated at ${companySettings.reportTime} UTC+6",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FieldlySuccess.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Auto Synced", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FieldlySuccess)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = FieldlyBorderLight)
            Spacer(modifier = Modifier.height(14.dp))

            // Metadata info
            Text("Representative: ${currentUser.name} (${currentUser.phone})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Company: ${companySettings.name}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Target Territory: Gulshan-1 Commercial Hub", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(14.dp))

            // Summary metrics grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Total Visits", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$totalVisits", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FieldlyPrimary)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Verified", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$verifiedVisits", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FieldlySuccess)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Distance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${distanceKm} km", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FieldlyAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visits audit list
            Text("Completed Check-Ins Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            visits.forEach { v ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(v.clientName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        StatusBadge(status = v.status)
                    }
                    Text(
                        text = "Distance: ${v.distanceMeters}m | Accuracy: ±${v.accuracyMeters.toInt()}m",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (v.notes.isNotBlank()) {
                        Text(
                            text = "Note: \"${v.notes}\"",
                            fontSize = 11.sp,
                            color = FieldlyTextTertiaryLight
                        )
                    }
                }
                HorizontalDivider(color = FieldlyBorderLight.copy(alpha = 0.5f))
            }
        }

        // Action Buttons: Share & Email
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    Toast.makeText(context, "PDF Report exported successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FieldlyPrimary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export PDF", fontSize = 13.sp)
            }

            Button(
                onClick = {
                    Toast.makeText(context, "EOD Report emailed to manager & rep!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FieldlyAccent)
            ) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Email Report", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
