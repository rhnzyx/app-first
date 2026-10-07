package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.localization.FieldlyStrings
import com.example.localization.LocalAppLanguage
import com.example.ui.components.FieldlyCard
import com.example.ui.components.PrimaryFieldlyButton
import com.example.ui.theme.*

@Composable
fun ConsentScreen(
    onConsentAccepted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    PrimaryFieldlyButton(
                        text = FieldlyStrings.consentAgree(lang),
                        onClick = onConsentAccepted,
                        backgroundColor = FieldlyAccent
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FieldlyAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = FieldlyAccent,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = FieldlyStrings.consentTitle(lang),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (lang == AppLanguage.ENGLISH)
                    "Fieldly values your privacy. We are fully transparent about what data is collected and how it is used during your field work."
                else
                    "ফিল্ডলি আপনার গোপনীয়তাকে অগ্রাধিকার দেয়। আপনার ফিল্ড ওয়ার্কের সময় কী তথ্য সংগৃহীত হয় তা নিচে পরিষ্কারভাবে উল্লেখ করা হলো।",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            ConsentBulletItem(
                icon = Icons.Default.MyLocation,
                iconColor = FieldlyAccent,
                title = if (lang == AppLanguage.ENGLISH) "Location Tracking Window" else "লোকেশন ট্র্যাকিং সময়সীমা",
                description = if (lang == AppLanguage.ENGLISH)
                    "GPS coordinates are ONLY recorded between 'Start Day' and 'End Day'. Tracking turns off completely outside shift hours."
                else
                    "জিপিএস লোকেশন শুধুমাত্র 'দিন শুরু' এবং 'দিন শেষ' করার মধ্যবর্তী সময়ে রেকর্ড করা হয়। কাজ শেষে ট্র্যাকিং সম্পূর্ণরূপে বন্ধ থাকে।"
            )

            ConsentBulletItem(
                icon = Icons.Default.Lock,
                iconColor = FieldlySuccess,
                title = if (lang == AppLanguage.ENGLISH) "PostGIS Verified Check-In" else "যাচাইকৃত চেক-ইন তথ্য",
                description = if (lang == AppLanguage.ENGLISH)
                    "When checking in at a client, we verify your proximity within the company boundary (default 50 meters) using GPS coordinates and accuracy."
                else
                    "গ্রাহকের দোকানে চেক-ইন করার সময় কোম্পানির নির্ধারিত দূরত্বে (সাধারণত ৫০ মিটার) আপনি আছেন কিনা তা যাচাই করা হয়।"
            )

            ConsentBulletItem(
                icon = Icons.Default.BatteryAlert,
                iconColor = FieldlyWarning,
                title = if (lang == AppLanguage.ENGLISH) "Foreground Notification & Battery" else "ফোরগ্রাউন্ড নোটিফিকেশন ও ব্যাটারি",
                description = if (lang == AppLanguage.ENGLISH)
                    "A persistent Android status notification shows you whenever tracking is running. Smart motion filtering uses less than 10% battery per day."
                else
                    "ট্র্যাকিং চলাকালীন ফোনের নোটিফিকেশন বারে স্পষ্ট নোটিফিকেশন দেখা যাবে। স্মার্ট ফিল্টারিংয়ের মাধ্যমে দিনে ১০% এরও কম ব্যাটারি খরচ হয়।"
            )

            ConsentBulletItem(
                icon = Icons.Default.CheckCircle,
                iconColor = FieldlyPrimary,
                title = if (lang == AppLanguage.ENGLISH) "Zero Personal Data Collection" else "কোনো ব্যক্তিগত ডেটা নয়",
                description = if (lang == AppLanguage.ENGLISH)
                    "Fieldly never accesses your personal calls, SMS, browsing history, or private media files. All business data is encrypted."
                else
                    "ফিল্ডলি আপনার ব্যক্তিগত কল, বার্তা বা ফাইল অ্যাক্সেস করে না। সকল তথ্য নিরাপদভাবে সংরক্ষিত।"
            )
        }
    }
}

@Composable
private fun ConsentBulletItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    FieldlyCard {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
