package com.example.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage {
    ENGLISH,
    BANGLA
}

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

object FieldlyStrings {
    fun appName(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Fieldly"
        AppLanguage.BANGLA -> "ফিল্ডলি"
    }

    fun tagLine(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Smart Field Sales Coordination"
        AppLanguage.BANGLA -> "স্মার্ট ফিল্ড সেলস সমন্বয়"
    }

    // Roles
    fun roleRep(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Sales Representative"
        AppLanguage.BANGLA -> "বিক্রয় প্রতিনিধি (রিপ)"
    }

    fun roleManager(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Field Manager"
        AppLanguage.BANGLA -> "ফিল্ড ম্যানেজার"
    }

    fun roleAdmin(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Company Admin"
        AppLanguage.BANGLA -> "কোম্পানি অ্যাডমিন"
    }

    // Navigation
    fun navHome(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Today"
        AppLanguage.BANGLA -> "আজকের কাজ"
    }

    fun navLiveMap(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Live Map"
        AppLanguage.BANGLA -> "লাইভ ম্যাপ"
    }

    fun navDashboard(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Dashboard"
        AppLanguage.BANGLA -> "ড্যাশবোর্ড"
    }

    fun navClients(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Clients"
        AppLanguage.BANGLA -> "গ্রাহক তালিকা"
    }

    fun navPlans(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Plans"
        AppLanguage.BANGLA -> "ভিজিট প্ল্যান"
    }

    fun navReport(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "EOD Report"
        AppLanguage.BANGLA -> "দিনের রিপোর্ট"
    }

    // Rep Actions
    fun startDay(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Start Day"
        AppLanguage.BANGLA -> "দিন শুরু করুন"
    }

    fun endDay(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "End Day"
        AppLanguage.BANGLA -> "দিন শেষ করুন"
    }

    fun dayActive(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Day Active • Tracking On"
        AppLanguage.BANGLA -> "দিন চালু • ট্র্যাকিং সক্রিয়"
    }

    fun dayNotStarted(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Day Not Started • Tracking Off"
        AppLanguage.BANGLA -> "দিন শুরু হয়নি • ট্র্যাকিং বন্ধ"
    }

    fun checkIn(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Check-In"
        AppLanguage.BANGLA -> "চেক-ইন"
    }

    fun checkInVerified(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Verified Check-In"
        AppLanguage.BANGLA -> "যাচাইকৃত চেক-ইন"
    }

    fun checkInNeedsReview(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Needs Review (GPS Drift)"
        AppLanguage.BANGLA -> "পর্যালোচনা প্রয়োজন (জিপিএস ত্রুটি)"
    }

    fun checkInRejected(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Check-In Rejected (Too Far)"
        AppLanguage.BANGLA -> "চেক-ইন প্রত্যাখ্যাত (অতিরিক্ত দূরত্ব)"
    }

    // Statuses
    fun statusPending(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Pending"
        AppLanguage.BANGLA -> "বাকি আছে"
    }

    fun statusDone(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Completed"
        AppLanguage.BANGLA -> "সম্পন্ন"
    }

    fun statusNeedsReview(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Needs Review"
        AppLanguage.BANGLA -> "পর্যালোচনাধীন"
    }

    fun statusActive(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Active (Moving)"
        AppLanguage.BANGLA -> "সক্রিয় (চলমান)"
    }

    fun statusIdle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Idle (Stationary)"
        AppLanguage.BANGLA -> "স্থির (অপেক্ষারত)"
    }

    fun statusOffline(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Offline"
        AppLanguage.BANGLA -> "অফলাইন"
    }

    // Clients
    fun addClient(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Add Client"
        AppLanguage.BANGLA -> "নতুন গ্রাহক যোগ"
    }

    fun saveCurrentLocation(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Save My Current Location"
        AppLanguage.BANGLA -> "আমার বর্তমান অবস্থান সংরক্ষণ করুন"
    }

    fun searchClients(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Search client name or phone..."
        AppLanguage.BANGLA -> "গ্রাহকের নাম বা ফোন নম্বর খুঁজুন..."
    }

    // Battery guide
    fun batteryGuideTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Background Tracking Setup"
        AppLanguage.BANGLA -> "ব্যাকগ্রাউন্ড ট্র্যাকিং সেটআপ"
    }

    fun batteryGuideSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Exempt Fieldly from battery optimization to prevent OEM system kill."
        AppLanguage.BANGLA -> "স্মার্টফোনের ব্যাটারি অপটিমাইজেশন বন্ধ রাখুন যেন অ্যাপ বন্ধ না হয়।"
    }

    // Privacy & Consent
    fun consentTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "What Fieldly Collects"
        AppLanguage.BANGLA -> "ফিল্ডলি কী তথ্য সংগ্রহ করে"
    }

    fun consentAgree(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "I Understand & Consent"
        AppLanguage.BANGLA -> "আমি বুঝেছি ও সম্মতি জানাচ্ছি"
    }

    fun offlineQueueNotice(lang: AppLanguage, count: Int) = when (lang) {
        AppLanguage.ENGLISH -> "$count check-ins saved offline. Will auto-sync when online."
        AppLanguage.BANGLA -> "$count টি চেক-ইন অফলাইনে সংরক্ষিত। নেটওয়ার্ক পেলে সিন্ক হবে।"
    }

    fun syncNow(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Sync Now"
        AppLanguage.BANGLA -> "এখনই সিন্ক করুন"
    }
}
