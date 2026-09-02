package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MissionRepository {

  val presetLocations = listOf(
    GeoPoint(10.123456, 76.123456, "Vertiport Alpha (Dock 1)"),
    GeoPoint(10.145678, 76.156789, "Logistics Hub Beta"),
    GeoPoint(10.118920, 76.134510, "Metro Rooftop Helipad 3"),
    GeoPoint(10.156200, 76.172400, "Medical Dispatch Depot"),
    GeoPoint(10.138400, 76.112900, "Coastal Sensor Station")
  )

  private val _history = MutableStateFlow(
    listOf(
      MissionHistoryItem(
        id = "MSN-8842",
        title = "A → B",
        pointA = GeoPoint(10.123456, 76.123456, "Vertiport Alpha"),
        pointB = GeoPoint(10.145678, 76.156789, "Logistics Hub Beta"),
        dateFormatted = "02 Sep 2026",
        distanceKm = 2.43,
        durationFormatted = "7m 42s",
        durationSeconds = 462,
        batteryConsumedPercent = 18,
        status = MissionHistoryStatus.COMPLETED,
        landingStatus = "SUCCESS",
        obstacleEventsCount = 1,
        slamEventsCount = 0,
        events = listOf(
          MissionEvent("09:42:10", "Mission started", EventType.INFO),
          MissionEvent("09:42:24", "Drone armed", EventType.ACTION),
          MissionEvent("09:42:31", "Takeoff complete", EventType.SUCCESS),
          MissionEvent("09:43:12", "Navigation started", EventType.ACTION),
          MissionEvent("09:45:28", "Obstacle detected", EventType.WARNING),
          MissionEvent("09:45:29", "Route replanning", EventType.ACTION),
          MissionEvent("09:45:36", "Avoidance complete", EventType.SUCCESS),
          MissionEvent("09:48:41", "Destination reached", EventType.SUCCESS),
          MissionEvent("09:48:48", "Landing marker detected", EventType.ACTION),
          MissionEvent("09:49:02", "Landing started", EventType.ACTION),
          MissionEvent("09:49:21", "Touchdown", EventType.SUCCESS),
          MissionEvent("09:49:23", "Mission completed", EventType.SUCCESS)
        )
      ),
      MissionHistoryItem(
        id = "MSN-8839",
        title = "A → B",
        pointA = GeoPoint(10.118920, 76.134510, "Metro Helipad 3"),
        pointB = GeoPoint(10.156200, 76.172400, "Medical Dispatch"),
        dateFormatted = "01 Sep 2026",
        distanceKm = 4.12,
        durationFormatted = "11m 15s",
        durationSeconds = 675,
        batteryConsumedPercent = 29,
        status = MissionHistoryStatus.COMPLETED,
        landingStatus = "SUCCESS",
        obstacleEventsCount = 0,
        slamEventsCount = 0,
        events = listOf(
          MissionEvent("14:10:02", "Mission started", EventType.INFO),
          MissionEvent("14:10:18", "Takeoff complete", EventType.SUCCESS),
          MissionEvent("14:21:10", "Touchdown", EventType.SUCCESS),
          MissionEvent("14:21:17", "Mission completed", EventType.SUCCESS)
        )
      ),
      MissionHistoryItem(
        id = "MSN-8824",
        title = "A → B",
        pointA = GeoPoint(10.138400, 76.112900, "Coastal Sensor Station"),
        pointB = GeoPoint(10.123456, 76.123456, "Vertiport Alpha"),
        dateFormatted = "30 Aug 2026",
        distanceKm = 1.85,
        durationFormatted = "4m 50s",
        durationSeconds = 290,
        batteryConsumedPercent = 12,
        status = MissionHistoryStatus.RTL,
        landingStatus = "HOME_LANDED",
        obstacleEventsCount = 2,
        slamEventsCount = 1,
        events = listOf(
          MissionEvent("17:05:00", "Mission started", EventType.INFO),
          MissionEvent("17:07:12", "Gust wind warning - SLAM degraded", EventType.WARNING),
          MissionEvent("17:07:45", "RTL engaged by operator", EventType.WARNING),
          MissionEvent("17:09:50", "Home Touchdown completed", EventType.SUCCESS)
        )
      )
    )
  )
  val history: StateFlow<List<MissionHistoryItem>> = _history.asStateFlow()

  fun addCompletedMission(item: MissionHistoryItem) {
    _history.value = listOf(item) + _history.value
  }
}
