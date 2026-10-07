package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.model.NotificationItem
import com.example.model.UserRole
import com.example.ui.components.FieldlyCard
import com.example.ui.screens.*
import com.example.ui.theme.FieldlyAccent
import com.example.ui.theme.FieldlyPrimary
import com.example.ui.theme.FieldlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }

            CompositionLocalProvider(LocalAppLanguage provides currentLanguage) {
                FieldlyTheme {
                    FieldlyMainApp(
                        currentLanguage = currentLanguage,
                        onLanguageToggle = {
                            currentLanguage = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.BANGLA else AppLanguage.ENGLISH
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldlyMainApp(
    currentLanguage: AppLanguage,
    onLanguageToggle: () -> Unit
) {
    val currentUser by FieldlyRepository.currentUser.collectAsState()
    val hasConsented by FieldlyRepository.hasConsented.collectAsState()
    val notifications by FieldlyRepository.notifications.collectAsState()

    var currentScreen by remember { mutableStateOf("home") }
    var showRoleDialog by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showConsentSheet by remember { mutableStateOf(!hasConsented) }

    // If consent hasn't been agreed to yet, show consent screen
    if (showConsentSheet) {
        ConsentScreen(
            onConsentAccepted = {
                FieldlyRepository.hasConsented.value = true
                showConsentSheet = false
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FieldlyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "F",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = FieldlyStrings.appName(currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${currentUser.name} (${currentUser.role.name})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Role switcher button (Rep / Manager / Admin)
                    IconButton(onClick = { showRoleDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.SwitchAccount,
                            contentDescription = "Switch Role",
                            tint = FieldlyAccent
                        )
                    }

                    // Language toggle button (EN / বাং)
                    TextButton(onClick = onLanguageToggle) {
                        Text(
                            text = if (currentLanguage == AppLanguage.ENGLISH) "বাং" else "EN",
                            fontWeight = FontWeight.Bold,
                            color = FieldlyAccent
                        )
                    }

                    // Notification bell with unread badge
                    val unreadCount = notifications.count { !it.isRead }
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text("$unreadCount")
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { showNotificationsSheet = true }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                // Screen items based on Role
                if (currentUser.role == UserRole.REP) {
                    NavigationBarItem(
                        selected = currentScreen == "home",
                        onClick = { currentScreen = "home" },
                        icon = { Icon(Icons.Default.Today, contentDescription = "Today") },
                        label = { Text(FieldlyStrings.navHome(currentLanguage)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "clients",
                        onClick = { currentScreen = "clients" },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = "Clients") },
                        label = { Text(FieldlyStrings.navClients(currentLanguage)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "battery",
                        onClick = { currentScreen = "battery" },
                        icon = { Icon(Icons.Default.BatteryChargingFull, contentDescription = "Battery") },
                        label = { Text("Battery") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "report",
                        onClick = { currentScreen = "report" },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Report") },
                        label = { Text(FieldlyStrings.navReport(currentLanguage)) }
                    )
                } else {
                    // Manager / Admin navigation
                    NavigationBarItem(
                        selected = currentScreen == "live_map",
                        onClick = { currentScreen = "live_map" },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Live Map") },
                        label = { Text(FieldlyStrings.navLiveMap(currentLanguage)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "dashboard",
                        onClick = { currentScreen = "dashboard" },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text(FieldlyStrings.navDashboard(currentLanguage)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "plans",
                        onClick = { currentScreen = "plans" },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = "Plans") },
                        label = { Text(FieldlyStrings.navPlans(currentLanguage)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "clients",
                        onClick = { currentScreen = "clients" },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = "Clients") },
                        label = { Text(FieldlyStrings.navClients(currentLanguage)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "report",
                        onClick = { currentScreen = "report" },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Report") },
                        label = { Text(FieldlyStrings.navReport(currentLanguage)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "home" -> RepHomeScreen()
                "live_map" -> ManagerLiveMapScreen()
                "dashboard" -> ManagerDashboardScreen(onNavigateToLiveMap = { currentScreen = "live_map" })
                "clients" -> ClientsScreen()
                "plans" -> VisitPlansScreen()
                "battery" -> BatteryGuideScreen()
                "report" -> EodReportScreen()
                else -> RepHomeScreen()
            }
        }
    }

    // Role Switcher Dialog
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Switch Demo Role", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select a role to preview the customized UX:", fontSize = 13.sp)

                    RoleSelectCard(
                        title = "Sales Rep (Tanvir)",
                        subtitle = "Start Day, Visit targets, GPS check-in",
                        isSelected = currentUser.role == UserRole.REP,
                        onClick = {
                            FieldlyRepository.switchRole(UserRole.REP)
                            currentScreen = "home"
                            showRoleDialog = false
                        }
                    )

                    RoleSelectCard(
                        title = "Field Manager (Kamrul)",
                        subtitle = "Live Map, Bento Dashboard, Team status",
                        isSelected = currentUser.role == UserRole.MANAGER,
                        onClick = {
                            FieldlyRepository.switchRole(UserRole.MANAGER)
                            currentScreen = "live_map"
                            showRoleDialog = false
                        }
                    )

                    RoleSelectCard(
                        title = "Company Admin",
                        subtitle = "Company check-in radius rule (30-150m)",
                        isSelected = currentUser.role == UserRole.ADMIN,
                        onClick = {
                            FieldlyRepository.switchRole(UserRole.ADMIN)
                            currentScreen = "dashboard"
                            showRoleDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Notifications Bottom Sheet (Push simulation)
    if (showNotificationsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationsSheet = false },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Notification Feed (FCM Push)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(notifications) { notif ->
                        FieldlyCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(FieldlyAccent.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = FieldlyAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(notif.timeFormatted, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(notif.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RoleSelectCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) FieldlyAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, FieldlyAccent) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FieldlyAccent)
            }
        }
    }
}
