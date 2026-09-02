package com.example.data.model

data class GeoPoint(
  val latitude: Double,
  val longitude: Double,
  val name: String = ""
)

enum class MissionState(val label: String, val isNavigating: Boolean = false) {
  IDLE("IDLE"),
  CONNECTING("CONNECTING"),
  CONNECTED("CONNECTED"),
  ARMING("ARMING"),
  TAKEOFF("TAKEOFF", true),
  ASCENDING("ASCENDING", true),
  NAVIGATING("NAVIGATING", true),
  WAYPOINT_REACHED("WAYPOINT_REACHED", true),
  OBSTACLE_DETECTED("OBSTACLE_DETECTED", true),
  AVOIDING("AVOIDING", true),
  ROUTE_REPLANNING("ROUTE_REPLANNING", true),
  ARRIVING("ARRIVING", true),
  LANDING("LANDING", true),
  LANDED("LANDED"),
  PAUSED("PAUSED"),
  ABORTED("ABORTED"),
  RTL("RTL", true),
  ERROR("ERROR")
}

data class MissionPlan(
  val id: String,
  val pointA: GeoPoint,
  val pointB: GeoPoint,
  val distanceKm: Double,
  val estimatedFlightTimeSeconds: Int,
  val cruiseAltitudeMeters: Double = 32.4,
  val estimatedBatteryPercent: Int = 18,
  val maxSpeedMps: Double = 8.0
)

data class MissionEvent(
  val timestamp: String,
  val message: String,
  val type: EventType = EventType.INFO
)

enum class EventType {
  INFO,
  ACTION,
  WARNING,
  SUCCESS,
  ERROR
}

data class MissionHistoryItem(
  val id: String,
  val title: String = "A → B",
  val pointA: GeoPoint,
  val pointB: GeoPoint,
  val dateFormatted: String,
  val distanceKm: Double,
  val durationFormatted: String,
  val durationSeconds: Int,
  val batteryConsumedPercent: Int,
  val status: MissionHistoryStatus,
  val landingStatus: String = "SUCCESS",
  val obstacleEventsCount: Int = 1,
  val slamEventsCount: Int = 0,
  val events: List<MissionEvent> = emptyList(),
  val plannedRoute: List<GeoPoint> = emptyList(),
  val actualRoute: List<GeoPoint> = emptyList(),
  val avoidanceRoute: List<GeoPoint> = emptyList()
)

enum class MissionHistoryStatus(val label: String) {
  COMPLETED("COMPLETED"),
  ABORTED("ABORTED"),
  FAILED("FAILED"),
  RTL("RTL"),
  EMERGENCY("EMERGENCY")
}

data class MissionCompleteSummary(
  val missionId: String,
  val distanceKm: Double,
  val flightTimeFormatted: String,
  val batteryUsedPercent: Int,
  val landingStatus: String = "SUCCESS"
)
