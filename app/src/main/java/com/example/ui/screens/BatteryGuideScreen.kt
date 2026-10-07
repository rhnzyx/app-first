package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.localization.FieldlyStrings
import com.example.localization.LocalAppLanguage
import com.example.ui.components.FieldlyCard
import com.example.ui.theme.*

@Composable
fun BatteryGuideScreen(
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Xiaomi / MIUI", "Samsung OneUI", "Oppo ColorOS", "Realme UI")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(FieldlyWarning.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryAlert,
                    contentDescription = null,
                    tint = FieldlyWarning,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = FieldlyStrings.batteryGuideTitle(lang),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = FieldlyStrings.batteryGuideSubtitle(lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Brand Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = FieldlyAccent
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                )
            }
        }

        // Steps Content
        val steps = when (selectedTab) {
            0 -> listOf(
                "Open Phone Settings -> Apps -> Manage Apps -> Fieldly",
                "Turn ON 'Autostart' toggle",
                "Scroll down to 'Battery Saver' and select 'No Restrictions'",
                "Go to App Permissions -> Location -> Choose 'Allow all the time'",
                "Lock Fieldly in Recent Apps tray by pulling down on the card"
            )
            1 -> listOf(
                "Open Settings -> Battery and device care -> Battery",
                "Tap 'Background usage limits' -> 'Never sleeping apps'",
                "Tap the '+' icon and add Fieldly to the list",
                "Open App Info -> Battery -> Select 'Unrestricted'",
                "Turn off 'Put unused apps to sleep' for field work"
            )
            2 -> listOf(
                "Open Settings -> App Management -> App List -> Fieldly",
                "Tap 'Battery usage' -> Enable 'Allow background activity'",
                "Tap 'Auto launch' -> Enable background launch",
                "Go to Settings -> Battery -> More settings -> Optimize battery use",
                "Find Fieldly and choose 'Don't optimize'"
            )
            else -> listOf(
                "Open Settings -> Battery -> More battery settings",
                "Tap 'Optimize battery use' -> Fieldly -> Choose 'Don't optimize'",
                "Tap 'App Quick Freeze' and turn OFF for Fieldly",
                "Settings -> App Management -> Fieldly -> Enable 'Auto-launch'",
                "Ensure Foreground Service permission is permitted"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            steps.forEachIndexed { idx, step ->
                FieldlyCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(FieldlyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = step,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
