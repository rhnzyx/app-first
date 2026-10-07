package com.example.model

enum class UserRole {
    REP,
    MANAGER,
    ADMIN
}

enum class VisitStatus {
    PENDING,
    DONE,
    NEEDS_REVIEW
}

enum class RepStatus {
    ACTIVE,   // Moving (green ring)
    IDLE,     // Stationary (orange ring)
    OFFLINE   // Inactive or ended day (gray ring)
}

data class UserProfile(
    val id: String,
    val companyId: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val initials: String
)

data class CompanySettings(
    val id: String,
    val name: String,
    val checkInRadiusMeters: Int = 50,
    val workStartTime: String = "09:00",
    val workEndTime: String = "18:00",
    val reportTime: String = "18:30"
)

data class Client(
    val id: String,
    val name: String,
    val clientType: String,
    val phone: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isArchived: Boolean = false
)

data class PlanItem(
    val id: String,
    val planId: String,
    val client: Client,
    val sequenceOrder: Int,
    val status: VisitStatus,
    val targetTime: String? = null,
    val completedAt: String? = null
)

data class DaySession(
    val id: String,
    val repId: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val totalDistanceMeters: Double = 0.0,
    val isActive: Boolean = true
)

data class VisitCheckIn(
    val id: String,
    val clientId: String,
    val clientName: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Int,
    val accuracyMeters: Float,
    val isMock: Boolean,
    val status: VisitStatus,
    val notes: String = "",
    val photoUri: String? = null,
    val isOfflineQueued: Boolean = false
)

data class RepLivePosition(
    val rep: UserProfile,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val status: RepStatus,
    val speedKmh: Float,
    val batteryLevel: Int,
    val lastUpdatedMillis: Long,
    val visitsDone: Int,
    val visitsTotal: Int
)

data class SecurityFlag(
    val id: String,
    val repName: String,
    val flagType: String,
    val severity: String,
    val message: String,
    val timestampMillis: Long
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeFormatted: String,
    val isRead: Boolean = false,
    val type: String = "info"
)
