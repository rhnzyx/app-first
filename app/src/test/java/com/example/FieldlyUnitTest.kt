package com.example

import com.example.data.FieldlyRepository
import com.example.model.Client
import com.example.model.VisitStatus
import org.junit.Assert.*
import org.junit.Test

class FieldlyUnitTest {

    @Test
    fun testHaversineDistanceAccuracy() {
        // Bengal Mart Ltd coords
        val lat1 = 23.7808
        val lon1 = 90.4152
        val lat2 = 23.7805
        val lon2 = 90.4154

        val distance = FieldlyRepository.calculateDistanceMeters(lat1, lon1, lat2, lon2)
        // Distance should be approximately 38 meters
        assertTrue("Distance should be within 20m - 60m range", distance in 20..60)
    }

    @Test
    fun testCheckInVerificationWithinRadius() {
        val client = Client(
            id = "c_test",
            name = "Test Retailer",
            clientType = "Retailer",
            phone = "12345",
            address = "Test Road",
            latitude = 23.7808,
            longitude = 90.4152
        )

        // Exact match location, accuracy 5m
        val result = FieldlyRepository.processCheckIn(
            client = client,
            userLat = 23.7808,
            userLng = 90.4152,
            accuracy = 5.0f,
            isMock = false,
            notes = "Test Note",
            photoUri = null
        )

        assertTrue(result.isSuccess)
        assertFalse(result.isRejected)
        assertFalse(result.isNeedsReview)
    }

    @Test
    fun testCheckInRejectionTooFar() {
        val client = Client(
            id = "c_test_far",
            name = "Far Retailer",
            clientType = "Retailer",
            phone = "12345",
            address = "Far Road",
            latitude = 23.8500, // kilometers away
            longitude = 90.4500
        )

        val result = FieldlyRepository.processCheckIn(
            client = client,
            userLat = 23.7808,
            userLng = 90.4152,
            accuracy = 10.0f,
            isMock = false,
            notes = "",
            photoUri = null
        )

        assertTrue(result.isRejected)
        assertFalse(result.isSuccess)
    }
}
