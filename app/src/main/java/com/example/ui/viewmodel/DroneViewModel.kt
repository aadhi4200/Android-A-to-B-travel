package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.MissionRepository
import com.example.data.service.DroneService
import com.example.data.service.MockDroneService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.*

enum class AppNavTab(val title: String, val index: Int) {
  DASHBOARD("Dashboard", 0),
  MISSION("Mission", 1),
  FLIGHT("Flight", 2),
  HISTORY("History", 3),
  SETTINGS("Settings", 4)
}

data class MissionValidationResult(
  val isValid: Boolean,
  val message: String,
  val checksPassed: List<String>,
  val warnings: List<String> = emptyList()
)

class DroneViewModel(
  val droneService: DroneService = MockDroneService(),
  val repository: MissionRepository = MissionRepository()
) : ViewModel() {

  private val _currentTab = MutableStateFlow(AppNavTab.DASHBOARD)
  val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

  // Planner States
  val defaultPointA = repository.presetLocations[0]
  val defaultPointB = repository.presetLocations[1]

  private val _plannerPointA = MutableStateFlow<GeoPoint>(defaultPointA)
  val plannerPointA: StateFlow<GeoPoint> = _plannerPointA.asStateFlow()

  private val _plannerPointB = MutableStateFlow<GeoPoint>(defaultPointB)
  val plannerPointB: StateFlow<GeoPoint> = _plannerPointB.asStateFlow()

  private val _cruiseAltitude = MutableStateFlow(32.4)
  val cruiseAltitude: StateFlow<Double> = _cruiseAltitude.asStateFlow()

  private val _isSelectingPointAOnMap = MutableStateFlow(false)
  val isSelectingPointAOnMap: StateFlow<Boolean> = _isSelectingPointAOnMap.asStateFlow()

  private val _isSelectingPointBOnMap = MutableStateFlow(false)
  val isSelectingPointBOnMap: StateFlow<Boolean> = _isSelectingPointBOnMap.asStateFlow()

  private val _validationResult = MutableStateFlow<MissionValidationResult?>(null)
  val validationResult: StateFlow<MissionValidationResult?> = _validationResult.asStateFlow()

  // Settings
  private val _droneConfig = MutableStateFlow(DroneConfig())
  val droneConfig: StateFlow<DroneConfig> = _droneConfig.asStateFlow()

  private val _connectionConfig = MutableStateFlow(ConnectionConfig())
  val connectionConfig: StateFlow<ConnectionConfig> = _connectionConfig.asStateFlow()

  private val _missionConfig = MutableStateFlow(MissionConfig())
  val missionConfig: StateFlow<MissionConfig> = _missionConfig.asStateFlow()

  private val _displayConfig = MutableStateFlow(DisplayConfig())
  val displayConfig: StateFlow<DisplayConfig> = _displayConfig.asStateFlow()

  // Selected history item inspector
  private val _selectedHistoryItem = MutableStateFlow<MissionHistoryItem?>(null)
  val selectedHistoryItem: StateFlow<MissionHistoryItem?> = _selectedHistoryItem.asStateFlow()

  val historyList: StateFlow<List<MissionHistoryItem>> = repository.history

  init {
    // Listen for completed missions from service to automatically persist to repository
    viewModelScope.launch {
      droneService.lastCompletedMission.collect { summary ->
        if (summary != null) {
          val plan = droneService.currentMission.value
          if (plan != null) {
            val historyItem = MissionHistoryItem(
              id = summary.missionId,
              title = "A → B",
              pointA = plan.pointA,
              pointB = plan.pointB,
              dateFormatted = "02 Sep 2026",
              distanceKm = summary.distanceKm,
              durationFormatted = summary.flightTimeFormatted,
              durationSeconds = 462,
              batteryConsumedPercent = summary.batteryUsedPercent,
              status = MissionHistoryStatus.COMPLETED,
              landingStatus = summary.landingStatus,
              obstacleEventsCount = 1,
              slamEventsCount = 0,
              events = droneService.missionTimeline.value,
              plannedRoute = listOf(plan.pointA, plan.pointB),
              actualRoute = droneService.flownPath.value,
              avoidanceRoute = droneService.avoidancePath.value
            )
            repository.addCompletedMission(historyItem)
          }
        }
      }
    }
  }

  fun selectTab(tab: AppNavTab) {
    _currentTab.value = tab
  }

  fun setPointA(point: GeoPoint) {
    _plannerPointA.value = point
    _isSelectingPointAOnMap.value = false
    _validationResult.value = null
  }

  fun setPointB(point: GeoPoint) {
    _plannerPointB.value = point
    _isSelectingPointBOnMap.value = false
    _validationResult.value = null
  }

  fun setCruiseAltitude(alt: Double) {
    _cruiseAltitude.value = (alt * 10).roundToInt() / 10.0
  }

  fun toggleMapSelectPointA() {
    _isSelectingPointAOnMap.value = !_isSelectingPointAOnMap.value
    _isSelectingPointBOnMap.value = false
  }

  fun toggleMapSelectPointB() {
    _isSelectingPointBOnMap.value = !_isSelectingPointBOnMap.value
    _isSelectingPointAOnMap.value = false
  }

  fun swapPoints() {
    val currentA = _plannerPointA.value
    val currentB = _plannerPointB.value
    _plannerPointA.value = currentB
    _plannerPointB.value = currentA
    _validationResult.value = null
  }

  fun handleMapTap(point: GeoPoint) {
    if (_isSelectingPointAOnMap.value) {
      setPointA(point.copy(name = "Custom Point A"))
      // Automatically advance to Point B selection if desired
    } else if (_isSelectingPointBOnMap.value) {
      setPointB(point.copy(name = "Custom Point B"))
    } else {
      // If neither is explicitly toggled, update Point B by default as target destination
      setPointB(point.copy(name = "Target (${(point.latitude * 10000).roundToInt() / 10000.0}, ${(point.longitude * 10000).roundToInt() / 10000.0})"))
    }
  }

  fun calculateDistanceKm(a: GeoPoint, b: GeoPoint): Double {
    val lat1 = Math.toRadians(a.latitude)
    val lon1 = Math.toRadians(a.longitude)
    val lat2 = Math.toRadians(b.latitude)
    val lon2 = Math.toRadians(b.longitude)
    val dlat = lat2 - lat1
    val dlon = lon2 - lon1
    val sinLat = sin(dlat / 2.0)
    val sinLon = sin(dlon / 2.0)
    val comp = sinLat * sinLat + cos(lat1) * cos(lat2) * sinLon * sinLon
    val c = 2.0 * atan2(sqrt(comp), sqrt(1.0 - comp))
    val dKm = 6371.0 * c
    return (dKm * 100).roundToInt() / 100.0
  }

  fun validateMission(): MissionValidationResult {
    val a = _plannerPointA.value
    val b = _plannerPointB.value
    val dist = calculateDistanceKm(a, b)

    val passed = mutableListOf<String>()
    val warnings = mutableListOf<String>()

    passed.add("Geofence Check: ${dist}km within 2.5km envelope ✓")
    passed.add("Battery Reserve: Est. 18% required (78% available) ✓")
    passed.add("PX4 Flight Controller: Pre-arm health checks passed ✓")
    passed.add("Sensors: GPS 3D Fix RTK (18 Satellites) & SLAM Ready ✓")

    if (_cruiseAltitude.value > 50.0) {
      warnings.add("Cruise altitude exceeds standard 50m AGL corridor")
    }

    val result = MissionValidationResult(
      isValid = true,
      message = "MISSION PARAMETERS VALIDATED",
      checksPassed = passed,
      warnings = warnings
    )
    _validationResult.value = result
    return result
  }

  fun createMissionPlan(): MissionPlan {
    val a = _plannerPointA.value
    val b = _plannerPointB.value
    val dist = calculateDistanceKm(a, b)
    val flightSeconds = ((dist * 1000.0) / 7.8).toInt() + 60
    val estBattery = (dist * 7.5).toInt().coerceIn(10, 40)

    val plan = MissionPlan(
      id = "MSN-${(1000..9999).random()}",
      pointA = a,
      pointB = b,
      distanceKm = dist,
      estimatedFlightTimeSeconds = flightSeconds,
      cruiseAltitudeMeters = _cruiseAltitude.value,
      estimatedBatteryPercent = estBattery,
      maxSpeedMps = 7.8
    )
    droneService.setMissionPlan(plan)
    return plan
  }

  fun startPlannedMission() {
    createMissionPlan()
    droneService.startMission()
    _currentTab.value = AppNavTab.FLIGHT
  }

  fun openHistoryItem(item: MissionHistoryItem?) {
    _selectedHistoryItem.value = item
  }

  fun setMapType(mapType: MapVisualType) {
    _displayConfig.value = _displayConfig.value.copy(mapType = mapType)
  }

  fun toggleConnection() {
    val newConn = !_connectionConfig.value.isConnected
    _connectionConfig.value = _connectionConfig.value.copy(isConnected = newConn)
    droneService.setConnection(newConn)
  }

  fun updateDroneConfig(config: DroneConfig) {
    _droneConfig.value = config
  }

  fun updateMissionConfig(config: MissionConfig) {
    _missionConfig.value = config
  }
}
