package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

object FieldlyRepository {

    // Default company rules
    val companySettings = MutableStateFlow(
        CompanySettings(
            id = "c1",
            name = "Alpha Sales Corp",
            checkInRadiusMeters = 50,
            workStartTime = "09:00",
            workEndTime = "18:00",
            reportTime = "18:30"
        )
    )

    // Current active user
    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "u1",
            companyId = "c1",
            name = "Tanvir Ahmed",
            email = "tanvir.sales@alpha.com",
            phone = "+880 1711-234567",
            role = UserRole.REP,
            initials = "TA"
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Consents
    val hasConsented = MutableStateFlow(true)
    val hasViewedBatteryGuide = MutableStateFlow(false)

    // Current Rep Location (Dhaka Business District / Gulshan 1, lat: 23.7808, lng: 90.4152)
    val currentLat = MutableStateFlow(23.7808)
    val currentLng = MutableStateFlow(90.4152)
    val currentAccuracy = MutableStateFlow(12.0f) // meters
    val isMockGpsDetected = MutableStateFlow(false)
    val isNetworkOnline = MutableStateFlow(true)

    // Day Session
    private val _activeSession = MutableStateFlow<DaySession?>(
        DaySession(
            id = "s1",
            repId = "u1",
            startedAt = System.currentTimeMillis() - 3 * 3600 * 1000,
            totalDistanceMeters = 8450.0,
            isActive = true
        )
    )
    val activeSession: StateFlow<DaySession?> = _activeSession.asStateFlow()

    // Clients list
    private val _clients = MutableStateFlow<List<Client>>(
        listOf(
            Client(
                id = "cl-1",
                name = "Bengal Mart Ltd",
                clientType = "Superstore",
                phone = "+880 1819-112233",
                address = "Gulshan Circle 1, Dhaka",
                latitude = 23.7805,
                longitude = 90.4154
            ),
            Client(
                id = "cl-2",
                name = "Apex Mega Retail",
                clientType = "Fashion & Footwear",
                phone = "+880 1912-445566",
                address = "Road 11, Banani, Dhaka",
                latitude = 23.7937,
                longitude = 90.4066
            ),
            Client(
                id = "cl-3",
                name = "Padma Pharma Center",
                clientType = "Pharmacy Wholesale",
                phone = "+880 1715-778899",
                address = "Mohakhali Wireless Gate",
                latitude = 23.7772,
                longitude = 90.4005
            ),
            Client(
                id = "cl-4",
                name = "Shurjo Electronics",
                clientType = "Dealer",
                phone = "+880 1611-332211",
                address = "Badda Link Road",
                latitude = 23.7830,
                longitude = 90.4240
            ),
            Client(
                id = "cl-5",
                name = "Meghna Wholesale Depot",
                clientType = "Distributor",
                phone = "+880 1511-998877",
                address = "Tejgaon Commercial Area",
                latitude = 23.7640,
                longitude = 90.3950
            )
        )
    )
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    // Today's Plan Items
    private val _planItems = MutableStateFlow<List<PlanItem>>(
        listOf(
            PlanItem(
                id = "p-1",
                planId = "pl-today",
                client = _clients.value[0],
                sequenceOrder = 1,
                status = VisitStatus.DONE,
                targetTime = "10:00 AM",
                completedAt = "10:15 AM"
            ),
            PlanItem(
                id = "p-2",
                planId = "pl-today",
                client = _clients.value[1],
                sequenceOrder = 2,
                status = VisitStatus.DONE,
                targetTime = "11:30 AM",
                completedAt = "11:42 AM"
            ),
            PlanItem(
                id = "p-3",
                planId = "pl-today",
                client = _clients.value[2],
                sequenceOrder = 3,
                status = VisitStatus.PENDING,
                targetTime = "02:00 PM"
            ),
            PlanItem(
                id = "p-4",
                planId = "pl-today",
                client = _clients.value[3],
                sequenceOrder = 4,
                status = VisitStatus.PENDING,
                targetTime = "03:30 PM"
            ),
            PlanItem(
                id = "p-5",
                planId = "pl-today",
                client = _clients.value[4],
                sequenceOrder = 5,
                status = VisitStatus.PENDING,
                targetTime = "05:00 PM"
            )
        )
    )
    val planItems: StateFlow<List<PlanItem>> = _planItems.asStateFlow()

    // Completed Check-Ins history
    private val _visits = MutableStateFlow<List<VisitCheckIn>>(
        listOf(
            VisitCheckIn(
                id = "v-1",
                clientId = "cl-1",
                clientName = "Bengal Mart Ltd",
                timestamp = System.currentTimeMillis() - 2 * 3600 * 1000,
                latitude = 23.7806,
                longitude = 90.4153,
                distanceMeters = 15,
                accuracyMeters = 8.0f,
                isMock = false,
                status = VisitStatus.DONE,
                notes = "Weekly stock replenishment order signed. Manager was present.",
                isOfflineQueued = false
            ),
            VisitCheckIn(
                id = "v-2",
                clientId = "cl-2",
                clientName = "Apex Mega Retail",
                timestamp = System.currentTimeMillis() - 1 * 3600 * 1000,
                latitude = 23.7938,
                longitude = 90.4065,
                distanceMeters = 22,
                accuracyMeters = 14.0f,
                isMock = false,
                status = VisitStatus.DONE,
                notes = "Promotional display banners installed at entrance.",
                isOfflineQueued = false
            )
        )
    )
    val visits: StateFlow<List<VisitCheckIn>> = _visits.asStateFlow()

    // Offline Queued Check-Ins
    private val _offlineQueue = MutableStateFlow<List<VisitCheckIn>>(emptyList())
    val offlineQueue: StateFlow<List<VisitCheckIn>> = _offlineQueue.asStateFlow()

    // Security Flags
    private val _flags = MutableStateFlow<List<SecurityFlag>>(
        listOf(
            SecurityFlag(
                id = "f-1",
                repName = "Tanvir Ahmed",
                flagType = "accuracy_drift",
                severity = "medium",
                message = "GPS accuracy drifted to 62m at Mohakhali underpass",
                timestampMillis = System.currentTimeMillis() - 40 * 60 * 1000
            )
        )
    )
    val flags: StateFlow<List<SecurityFlag>> = _flags.asStateFlow()

    // Reps Live Positions (for Manager Live Map & Dashboard)
    private val _liveReps = MutableStateFlow<List<RepLivePosition>>(
        listOf(
            RepLivePosition(
                rep = UserProfile("u1", "c1", "Tanvir Ahmed", "tanvir.sales@alpha.com", "+880 1711-234567", UserRole.REP, "TA"),
                latitude = 23.7808,
                longitude = 90.4152,
                accuracy = 12.0f,
                status = RepStatus.ACTIVE,
                speedKmh = 14.5f,
                batteryLevel = 84,
                lastUpdatedMillis = System.currentTimeMillis() - 2 * 60 * 1000,
                visitsDone = 2,
                visitsTotal = 5
            ),
            RepLivePosition(
                rep = UserProfile("u2", "c1", "Sadia Karim", "sadia.k@alpha.com", "+880 1822-334455", UserRole.REP, "SK"),
                latitude = 23.7940,
                longitude = 90.4045,
                accuracy = 18.0f,
                status = RepStatus.ACTIVE,
                speedKmh = 19.2f,
                batteryLevel = 67,
                lastUpdatedMillis = System.currentTimeMillis() - 4 * 60 * 1000,
                visitsDone = 4,
                visitsTotal = 6
            ),
            RepLivePosition(
                rep = UserProfile("u3", "c1", "Rahim Chowdhury", "rahim.c@alpha.com", "+880 1933-556677", UserRole.REP, "RC"),
                latitude = 23.7710,
                longitude = 90.3980,
                accuracy = 25.0f,
                status = RepStatus.IDLE,
                speedKmh = 0.0f,
                batteryLevel = 52,
                lastUpdatedMillis = System.currentTimeMillis() - 15 * 60 * 1000,
                visitsDone = 1,
                visitsTotal = 4
            ),
            RepLivePosition(
                rep = UserProfile("u4", "c1", "Mehnaz Parveen", "mehnaz.p@alpha.com", "+880 1744-889900", UserRole.REP, "MP"),
                latitude = 23.7620,
                longitude = 90.3890,
                accuracy = 30.0f,
                status = RepStatus.OFFLINE,
                speedKmh = 0.0f,
                batteryLevel = 19,
                lastUpdatedMillis = System.currentTimeMillis() - 75 * 60 * 1000,
                visitsDone = 0,
                visitsTotal = 5
            )
        )
    )
    val liveReps: StateFlow<List<RepLivePosition>> = _liveReps.asStateFlow()

    // Notification Feed
    private val _notifications = MutableStateFlow<List<NotificationItem>>(
        listOf(
            NotificationItem(
                id = "n-1",
                title = "New Visit Plan Assigned",
                message = "Manager assigned 5 visits for Gulshan area today.",
                timeFormatted = "09:00 AM",
                isRead = true
            ),
            NotificationItem(
                id = "n-2",
                title = "Check-in Review Approved",
                message = "Your check-in at Apex Mega Retail was verified with photo proof.",
                timeFormatted = "11:45 AM",
                isRead = false
            ),
            NotificationItem(
                id = "n-3",
                title = "Rep Idle Notice (45m)",
                message = "Rahim Chowdhury has been stationary at Mohakhali for 45 minutes.",
                timeFormatted = "01:10 PM",
                isRead = false
            )
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Switch Role
    fun switchRole(newRole: UserRole) {
        val current = _currentUser.value
        _currentUser.value = when (newRole) {
            UserRole.REP -> UserProfile("u1", "c1", "Tanvir Ahmed", "tanvir.sales@alpha.com", "+880 1711-234567", UserRole.REP, "TA")
            UserRole.MANAGER -> UserProfile("m1", "c1", "Kamrul Hassan", "kamrul.manager@alpha.com", "+880 1711-998877", UserRole.MANAGER, "KH")
            UserRole.ADMIN -> UserProfile("a1", "c1", "Alpha Admin", "admin@alpha.com", "+880 1700-000001", UserRole.ADMIN, "AA")
        }
    }

    // Toggle Start Day / End Day
    fun toggleDaySession() {
        val current = _activeSession.value
        if (current == null || !current.isActive) {
            // Start day
            _activeSession.value = DaySession(
                id = "s-${System.currentTimeMillis()}",
                repId = _currentUser.value.id,
                startedAt = System.currentTimeMillis(),
                totalDistanceMeters = 0.0,
                isActive = true
            )
        } else {
            // End day
            _activeSession.value = current.copy(
                endedAt = System.currentTimeMillis(),
                isActive = false
            )
        }
    }

    // PostGIS Haversine distance calculator
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).roundToInt()
    }

    // Verification Result structure
    data class VerificationResult(
        val isSuccess: Boolean,
        val isNeedsReview: Boolean,
        val isRejected: Boolean,
        val distanceMeters: Int,
        val allowedRadius: Int,
        val message: String
    )

    // Validate Check-In Logic
    fun processCheckIn(
        client: Client,
        userLat: Double,
        userLng: Double,
        accuracy: Float,
        isMock: Boolean,
        notes: String,
        photoUri: String?
    ): VerificationResult {
        val radius = companySettings.value.checkInRadiusMeters
        val distance = calculateDistanceMeters(userLat, userLng, client.latitude, client.longitude)

        // Mock GPS detection
        if (isMock) {
            _flags.value = listOf(
                SecurityFlag(
                    id = "flag-${System.currentTimeMillis()}",
                    repName = _currentUser.value.name,
                    flagType = "mock_gps",
                    severity = "critical",
                    message = "Mock location attempt detected at ${client.name}!",
                    timestampMillis = System.currentTimeMillis()
                )
            ) + _flags.value
        }

        // PostGIS formula: (distance minus accuracy)
        val effectiveDist = max(0, distance - accuracy.toInt())

        if (effectiveDist > radius) {
            return VerificationResult(
                isSuccess = false,
                isNeedsReview = false,
                isRejected = true,
                distanceMeters = distance,
                allowedRadius = radius,
                message = "Too far from ${client.name}. Current distance: ${distance}m (Allowed: ${radius}m)."
            )
        }

        val status = if (accuracy > 50.0f) VisitStatus.NEEDS_REVIEW else VisitStatus.DONE

        val newVisit = VisitCheckIn(
            id = "v-${System.currentTimeMillis()}",
            clientId = client.id,
            clientName = client.name,
            timestamp = System.currentTimeMillis(),
            latitude = userLat,
            longitude = userLng,
            distanceMeters = distance,
            accuracyMeters = accuracy,
            isMock = isMock,
            status = status,
            notes = notes,
            photoUri = photoUri,
            isOfflineQueued = !_isNetworkOnline()
        )

        if (!_isNetworkOnline()) {
            _offlineQueue.value = _offlineQueue.value + newVisit
        } else {
            _visits.value = listOf(newVisit) + _visits.value
        }

        // Update corresponding plan item
        _planItems.value = _planItems.value.map { item ->
            if (item.client.id == client.id) {
                item.copy(
                    status = status,
                    completedAt = "Just now"
                )
            } else item
        }

        return VerificationResult(
            isSuccess = status == VisitStatus.DONE,
            isNeedsReview = status == VisitStatus.NEEDS_REVIEW,
            isRejected = false,
            distanceMeters = distance,
            allowedRadius = radius,
            message = if (status == VisitStatus.DONE) "Check-in verified successfully!" else "Accuracy > 50m. Submitted for manager review with photo proof."
        )
    }

    private fun _isNetworkOnline() = isNetworkOnline.value

    fun toggleNetworkOnline() {
        isNetworkOnline.value = !isNetworkOnline.value
    }

    fun syncOfflineQueue(): Int {
        val queued = _offlineQueue.value
        val count = queued.size
        _visits.value = queued.map { it.copy(isOfflineQueued = false) } + _visits.value
        _offlineQueue.value = emptyList()
        return count
    }

    fun addClient(name: String, type: String, phone: String, address: String, lat: Double, lng: Double) {
        val newC = Client(
            id = "cl-${System.currentTimeMillis()}",
            name = name,
            clientType = type,
            phone = phone,
            address = address,
            latitude = lat,
            longitude = lng
        )
        _clients.value = listOf(newC) + _clients.value
    }

    fun updateCheckInRadius(newRadius: Int) {
        companySettings.value = companySettings.value.copy(
            checkInRadiusMeters = newRadius.coerceIn(30, 150)
        )
    }
}
