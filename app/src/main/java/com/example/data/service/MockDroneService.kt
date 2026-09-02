package com.example.data.service

import com.example.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

class MockDroneService(
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : DroneService {

  private val defaultPointA = GeoPoint(10.123456, 76.123456, "Vertiport Alpha")
  private val defaultPointB = GeoPoint(10.145678, 76.156789, "Logistics Hub Beta")

  private val _telemetry = MutableStateFlow(
    DroneTelemetry(
      latitude = defaultPointA.latitude,
      longitude = defaultPointA.longitude,
      altitude = 0.0,
      relativeAltitude = 0.0,
      groundSpeed = 0.0,
      verticalSpeed = 0.0,
      heading = 124f,
      batteryPercentage = 78,
      batteryVoltage = 15.6,
      satellites = 18,
      hdop = 0.7,
      flightMode = "IDLE",
      armed = false,
      connected = true,
      timestamp = getCurrentTime()
    )
  )
  override val telemetry: StateFlow<DroneTelemetry> = _telemetry.asStateFlow()

  private val _missionState = MutableStateFlow(MissionState.IDLE)
  override val missionState: StateFlow<MissionState> = _missionState.asStateFlow()

  private val _slamStatus = MutableStateFlow(
    SlamStatus(
      status = SlamTrackingState.TRACKING,
      localization = "GOOD",
      confidence = 94,
      mapActive = true,
      poseState = "STABLE"
    )
  )
  override val slamStatus: StateFlow<SlamStatus> = _slamStatus.asStateFlow()

  private val _obstacleStatus = MutableStateFlow(
    ObstacleAvoidanceStatus(
      status = ObstacleState.CLEAR,
      frontDistance = 18.4,
      leftDistance = 12.2,
      rightDistance = 14.7,
      upDistance = 8.5,
      downDistance = 6.2,
      action = "CLEAR"
    )
  )
  override val obstacleStatus: StateFlow<ObstacleAvoidanceStatus> = _obstacleStatus.asStateFlow()

  private val _visionStatus = MutableStateFlow(
    VisionStatus(
      cameraOnline = true,
      fps = 30,
      visionActive = true,
      arucoState = ArucoDetectionState.SEARCHING,
      landingMarkerDistance = 8.4,
      landingAlignment = "GOOD",
      markerId = 42
    )
  )
  override val visionStatus: StateFlow<VisionStatus> = _visionStatus.asStateFlow()

  private val _landingSequence = MutableStateFlow(LandingSequenceState())
  override val landingSequence: StateFlow<LandingSequenceState> = _landingSequence.asStateFlow()

  private val _missionProgress = MutableStateFlow(0f)
  override val missionProgress: StateFlow<Float> = _missionProgress.asStateFlow()

  private val _currentMission = MutableStateFlow<MissionPlan?>(
    MissionPlan(
      id = "MSN-${System.currentTimeMillis() % 10000}",
      pointA = defaultPointA,
      pointB = defaultPointB,
      distanceKm = 2.43,
      estimatedFlightTimeSeconds = 462,
      cruiseAltitudeMeters = 32.4,
      estimatedBatteryPercent = 18,
      maxSpeedMps = 7.8
    )
  )
  override val currentMission: StateFlow<MissionPlan?> = _currentMission.asStateFlow()

  private val _flownPath = MutableStateFlow<List<GeoPoint>>(listOf(defaultPointA))
  override val flownPath: StateFlow<List<GeoPoint>> = _flownPath.asStateFlow()

  private val _avoidancePath = MutableStateFlow<List<GeoPoint>>(emptyList())
  override val avoidancePath: StateFlow<List<GeoPoint>> = _avoidancePath.asStateFlow()

  private val _missionTimeline = MutableStateFlow<List<MissionEvent>>(emptyList())
  override val missionTimeline: StateFlow<List<MissionEvent>> = _missionTimeline.asStateFlow()

  private val _lastCompletedMission = MutableStateFlow<MissionCompleteSummary?>(null)
  override val lastCompletedMission: StateFlow<MissionCompleteSummary?> = _lastCompletedMission.asStateFlow()

  private var simulationJob: Job? = null
  private var idleTickerJob: Job? = null
  private var isPaused = false
  private var missionStartTimestamp: Long = 0

  init {
    startIdleTelemetryTicker()
  }

  private fun startIdleTelemetryTicker() {
    idleTickerJob?.cancel()
    idleTickerJob = scope.launch {
      while (isActive) {
        if (_missionState.value == MissionState.IDLE || _missionState.value == MissionState.LANDED) {
          val current = _telemetry.value
          // Small micro-variations for realism
          val microSpeed = if (current.armed) 0.1 else 0.0
          val microSats = (18 + ((-1..1).random())).coerceIn(16, 22)
          val microHdop = (0.7 + ((-1..1).random() * 0.02)).coerceIn(0.5, 0.9)
          _telemetry.value = current.copy(
            satellites = microSats,
            hdop = (microHdop * 10).roundToInt() / 10.0,
            timestamp = getCurrentTime()
          )
        }
        delay(1000)
      }
    }
  }

  override suspend fun getTelemetry(): DroneTelemetry = _telemetry.value

  override fun setMissionPlan(plan: MissionPlan) {
    _currentMission.value = plan
    _flownPath.value = listOf(plan.pointA)
    _avoidancePath.value = emptyList()
    _missionProgress.value = 0f
    _telemetry.value = _telemetry.value.copy(
      latitude = plan.pointA.latitude,
      longitude = plan.pointA.longitude,
      altitude = 0.0,
      relativeAltitude = 0.0,
      groundSpeed = 0.0,
      verticalSpeed = 0.0
    )
  }

  override fun startMission() {
    val plan = _currentMission.value ?: return
    simulationJob?.cancel()
    isPaused = false
    _missionTimeline.value = emptyList()
    _flownPath.value = listOf(plan.pointA)
    _avoidancePath.value = emptyList()
    _lastCompletedMission.value = null
    missionStartTimestamp = System.currentTimeMillis()

    simulationJob = scope.launch {
      runAutonomousSimulation(plan)
    }
  }

  private suspend fun runAutonomousSimulation(plan: MissionPlan) {
    val a = plan.pointA
    val b = plan.pointB
    val targetHeading = calculateBearing(a.latitude, a.longitude, b.latitude, b.longitude)
    val cruiseAlt = plan.cruiseAltitudeMeters

    addEvent("Mission started", EventType.INFO)

    // 1. ARMING
    _missionState.value = MissionState.ARMING
    _telemetry.value = _telemetry.value.copy(
      armed = true,
      flightMode = "ARMED",
      heading = targetHeading.toFloat()
    )
    delay(1500)
    addEvent("Drone armed", EventType.ACTION)

    // 2. TAKEOFF
    _missionState.value = MissionState.TAKEOFF
    _telemetry.value = _telemetry.value.copy(flightMode = "AUTO")
    for (step in 1..5) {
      val alt = step * 3.0
      _telemetry.value = _telemetry.value.copy(
        altitude = alt,
        relativeAltitude = alt,
        verticalSpeed = 1.5,
        groundSpeed = 0.5
      )
      delay(300)
    }
    addEvent("Takeoff complete", EventType.SUCCESS)

    // 3. ASCENDING TO CRUISE ALTITUDE
    _missionState.value = MissionState.ASCENDING
    for (step in 1..4) {
      val alt = 15.0 + step * ((cruiseAlt - 15.0) / 4.0)
      _telemetry.value = _telemetry.value.copy(
        altitude = (alt * 10).roundToInt() / 10.0,
        relativeAltitude = (alt * 10).roundToInt() / 10.0,
        verticalSpeed = 1.2,
        groundSpeed = 3.5
      )
      delay(300)
    }

    // 4. NAVIGATING
    _missionState.value = MissionState.NAVIGATING
    _telemetry.value = _telemetry.value.copy(
      altitude = cruiseAlt,
      relativeAltitude = cruiseAlt,
      verticalSpeed = 0.0,
      groundSpeed = plan.maxSpeedMps
    )
    addEvent("Navigation started", EventType.ACTION)

    val totalNavSteps = 80
    var obstacleTriggered = false
    var obstacleResolved = false

    var currentLat = a.latitude
    var currentLon = a.longitude
    val initialBattery = _telemetry.value.batteryPercentage

    for (step in 1..totalNavSteps) {
      while (isPaused) {
        delay(200)
      }

      val progress = (step.toFloat() / totalNavSteps.toFloat()) * 100f
      _missionProgress.value = (progress * 10).roundToInt() / 10f

      val progressFraction = step.toDouble() / totalNavSteps.toDouble()

      // Calculate base interpolated point along route
      var targetLat = a.latitude + (b.latitude - a.latitude) * progressFraction
      var targetLon = a.longitude + (b.longitude - a.longitude) * progressFraction

      // OBSTACLE SIMULATION at 35% - 55%
      if (progress in 35f..55f && !obstacleTriggered) {
        obstacleTriggered = true
        _missionState.value = MissionState.OBSTACLE_DETECTED
        _obstacleStatus.value = _obstacleStatus.value.copy(
          status = ObstacleState.DETECTED,
          frontDistance = 4.2,
          detectedSector = "FRONT",
          detectedDistance = 4.2,
          action = "REPLANNING ROUTE"
        )
        addEvent("Obstacle detected (Front 4.2m)", EventType.WARNING)
        delay(1200)

        _missionState.value = MissionState.ROUTE_REPLANNING
        addEvent("Route replanning", EventType.ACTION)
        delay(1000)

        _missionState.value = MissionState.AVOIDING
        _obstacleStatus.value = _obstacleStatus.value.copy(
          status = ObstacleState.AVOIDING,
          action = "AVOIDING PERIMETER"
        )
        addEvent("Executing avoidance maneuver", EventType.ACTION)

        // Generate avoidance trajectory arc
        val midLat = (a.latitude + b.latitude) / 2.0
        val midLon = (a.longitude + b.longitude) / 2.0
        // Lateral offset perpendicular to line
        val perpOffsetLat = -(b.longitude - a.longitude) * 0.25
        val perpOffsetLon = (b.latitude - a.latitude) * 0.25
        val avoidPoint = GeoPoint(midLat + perpOffsetLat, midLon + perpOffsetLon, "Avoidance Point")
        _avoidancePath.value = listOf(
          GeoPoint(currentLat, currentLon),
          avoidPoint,
          GeoPoint(targetLat, targetLon)
        )
      }

      if (progress > 55f && obstacleTriggered && !obstacleResolved) {
        obstacleResolved = true
        _missionState.value = MissionState.NAVIGATING
        _obstacleStatus.value = ObstacleAvoidanceStatus(
          status = ObstacleState.CLEAR,
          frontDistance = 18.4,
          leftDistance = 14.1,
          rightDistance = 15.6,
          upDistance = 8.5,
          downDistance = 6.2,
          action = "CLEAR"
        )
        addEvent("Avoidance complete", EventType.SUCCESS)
      }

      // If avoiding, bend the trajectory slightly
      if (_missionState.value == MissionState.AVOIDING) {
        val arcFactor = sin((progress - 35f) / 20f * Math.PI)
        val perpOffsetLat = -(b.longitude - a.longitude) * 0.25 * arcFactor
        val perpOffsetLon = (b.latitude - a.latitude) * 0.25 * arcFactor
        targetLat += perpOffsetLat
        targetLon += perpOffsetLon
      }

      currentLat = targetLat
      currentLon = targetLon

      val currentFlown = _flownPath.value.toMutableList()
      currentFlown.add(GeoPoint(currentLat, currentLon))
      _flownPath.value = currentFlown

      // Battery depletion
      val batteryDrop = ((progress / 100f) * plan.estimatedBatteryPercent).toInt()
      val currentBat = (initialBattery - batteryDrop).coerceAtLeast(10)

      // SLAM micro-fluctuations
      val slamConf = if (progress in 35f..55f) (88..92).random() else (94..98).random()
      _slamStatus.value = _slamStatus.value.copy(
        confidence = slamConf,
        featureCount = (480..540).random()
      )

      _telemetry.value = _telemetry.value.copy(
        latitude = currentLat,
        longitude = currentLon,
        groundSpeed = plan.maxSpeedMps + ((-3..3).random() * 0.1),
        verticalSpeed = ((-1..1).random() * 0.05),
        batteryPercentage = currentBat,
        batteryVoltage = 14.8 + (currentBat / 100.0) * 1.6,
        timestamp = getCurrentTime()
      )

      delay(180)
    }

    // 5. ARRIVING
    _missionState.value = MissionState.ARRIVING
    _telemetry.value = _telemetry.value.copy(
      groundSpeed = 2.1,
      latitude = b.latitude,
      longitude = b.longitude
    )
    addEvent("Destination reached", EventType.SUCCESS)
    delay(1000)

    // 6. VISION LANDING MARKER DETECTION
    _visionStatus.value = _visionStatus.value.copy(
      arucoState = ArucoDetectionState.DETECTED,
      landingMarkerDistance = 8.4,
      landingAlignment = "GOOD"
    )
    addEvent("Landing marker detected (ArUco #42)", EventType.ACTION)
    delay(1200)

    _visionStatus.value = _visionStatus.value.copy(
      arucoState = ArucoDetectionState.LOCKED,
      landingMarkerDistance = 7.9,
      landingAlignment = "EXCELLENT"
    )

    // 7. LANDING SEQUENCE
    _missionState.value = MissionState.LANDING
    addEvent("Landing sequence initiated", EventType.ACTION)

    _landingSequence.value = LandingSequenceState(isPositioningDone = true, activeStepIndex = 1)
    delay(800)

    _landingSequence.value = _landingSequence.value.copy(isMarkerDetectedDone = true, activeStepIndex = 2)
    delay(800)

    _landingSequence.value = _landingSequence.value.copy(isAlignmentDone = true, activeStepIndex = 3)
    delay(800)

    _landingSequence.value = _landingSequence.value.copy(isDescentInProgress = true, activeStepIndex = 3)
    addEvent("Autonomous descent started", EventType.ACTION)

    val descentAltSteps = 10
    for (i in 1..descentAltSteps) {
      val alt = cruiseAlt * (1f - (i.toFloat() / descentAltSteps.toFloat()))
      _telemetry.value = _telemetry.value.copy(
        altitude = (alt * 10).roundToInt() / 10.0,
        relativeAltitude = (alt * 10).roundToInt() / 10.0,
        verticalSpeed = -1.2,
        groundSpeed = 0.4
      )
      _visionStatus.value = _visionStatus.value.copy(
        landingMarkerDistance = max(0.5, (alt * 0.25 * 10).roundToInt() / 10.0)
      )
      delay(250)
    }

    // 8. TOUCHDOWN
    _landingSequence.value = _landingSequence.value.copy(
      isDescentInProgress = false,
      isTouchdownDone = true,
      activeStepIndex = 4
    )
    _missionState.value = MissionState.LANDED
    _telemetry.value = _telemetry.value.copy(
      altitude = 0.0,
      relativeAltitude = 0.0,
      verticalSpeed = 0.0,
      groundSpeed = 0.0,
      armed = false,
      flightMode = "LANDED"
    )
    addEvent("Touchdown", EventType.SUCCESS)
    delay(1000)

    // 9. MISSION COMPLETE
    addEvent("Mission completed", EventType.SUCCESS)
    val totalFlightSeconds = ((System.currentTimeMillis() - missionStartTimestamp) / 1000).toInt()
    val formattedDuration = "${totalFlightSeconds / 60}m ${totalFlightSeconds % 60}s"
    val batteryUsed = initialBattery - _telemetry.value.batteryPercentage

    _lastCompletedMission.value = MissionCompleteSummary(
      missionId = plan.id,
      distanceKm = plan.distanceKm,
      flightTimeFormatted = formattedDuration,
      batteryUsedPercent = batteryUsed,
      landingStatus = "SUCCESS"
    )
  }

  override fun pauseMission() {
    if (_missionState.value.isNavigating) {
      isPaused = true
      _missionState.value = MissionState.PAUSED
      _telemetry.value = _telemetry.value.copy(
        flightMode = "LOITER",
        groundSpeed = 0.0,
        verticalSpeed = 0.0
      )
      addEvent("Mission paused (Hover in place)", EventType.WARNING)
    }
  }

  override fun resumeMission() {
    if (_missionState.value == MissionState.PAUSED) {
      isPaused = false
      _missionState.value = MissionState.NAVIGATING
      _telemetry.value = _telemetry.value.copy(
        flightMode = "AUTO",
        groundSpeed = _currentMission.value?.maxSpeedMps ?: 7.8
      )
      addEvent("Mission resumed", EventType.INFO)
    }
  }

  override fun abortMission() {
    simulationJob?.cancel()
    isPaused = false
    _missionState.value = MissionState.ABORTED
    _telemetry.value = _telemetry.value.copy(
      flightMode = "ABORT",
      groundSpeed = 0.0,
      verticalSpeed = -1.8
    )
    addEvent("MISSION ABORTED - Failsafe descent triggered", EventType.ERROR)

    scope.launch {
      delay(1500)
      _telemetry.value = _telemetry.value.copy(
        altitude = 0.0,
        relativeAltitude = 0.0,
        verticalSpeed = 0.0,
        armed = false,
        flightMode = "LANDED"
      )
      _missionState.value = MissionState.LANDED
      addEvent("Drone secured on ground", EventType.INFO)
    }
  }

  override fun returnToHome() {
    val plan = _currentMission.value ?: return
    simulationJob?.cancel()
    isPaused = false
    _missionState.value = MissionState.RTL
    _telemetry.value = _telemetry.value.copy(
      flightMode = "RTL",
      groundSpeed = 6.5,
      verticalSpeed = 0.0
    )
    addEvent("Return-to-Home (RTL) activated", EventType.WARNING)

    simulationJob = scope.launch {
      val currentPos = GeoPoint(_telemetry.value.latitude, _telemetry.value.longitude)
      val homePos = plan.pointA
      val bearing = calculateBearing(currentPos.latitude, currentPos.longitude, homePos.latitude, homePos.longitude)
      _telemetry.value = _telemetry.value.copy(heading = bearing.toFloat())

      val rtlSteps = 25
      for (i in 1..rtlSteps) {
        val frac = i.toDouble() / rtlSteps.toDouble()
        val lat = currentPos.latitude + (homePos.latitude - currentPos.latitude) * frac
        val lon = currentPos.longitude + (homePos.longitude - currentPos.longitude) * frac
        _telemetry.value = _telemetry.value.copy(latitude = lat, longitude = lon)
        val currentFlown = _flownPath.value.toMutableList()
        currentFlown.add(GeoPoint(lat, lon))
        _flownPath.value = currentFlown
        delay(200)
      }

      addEvent("Arrived at Home point", EventType.SUCCESS)
      _missionState.value = MissionState.LANDING
      addEvent("Landing at Home location", EventType.ACTION)
      delay(2000)
      _telemetry.value = _telemetry.value.copy(altitude = 0.0, armed = false, flightMode = "LANDED")
      _missionState.value = MissionState.LANDED
      addEvent("RTL Touchdown completed", EventType.SUCCESS)
    }
  }

  override fun resetToIdle() {
    simulationJob?.cancel()
    isPaused = false
    val plan = _currentMission.value
    val startPos = plan?.pointA ?: defaultPointA
    _missionState.value = MissionState.IDLE
    _missionProgress.value = 0f
    _landingSequence.value = LandingSequenceState()
    _flownPath.value = listOf(startPos)
    _avoidancePath.value = emptyList()
    _missionTimeline.value = emptyList()
    _lastCompletedMission.value = null
    _visionStatus.value = VisionStatus()
    _obstacleStatus.value = ObstacleAvoidanceStatus()
    _telemetry.value = _telemetry.value.copy(
      latitude = startPos.latitude,
      longitude = startPos.longitude,
      altitude = 0.0,
      relativeAltitude = 0.0,
      groundSpeed = 0.0,
      verticalSpeed = 0.0,
      armed = false,
      flightMode = "IDLE",
      batteryPercentage = 78
    )
  }

  override fun setConnection(connected: Boolean) {
    _telemetry.value = _telemetry.value.copy(connected = connected)
    if (!connected) {
      addEvent("Telemetry connection lost", EventType.ERROR)
    } else {
      addEvent("Telemetry reconnected (PX4 MAVLink v2.0)", EventType.SUCCESS)
    }
  }

  override fun dismissMissionComplete() {
    _lastCompletedMission.value = null
  }

  private fun addEvent(message: String, type: EventType) {
    val event = MissionEvent(timestamp = getCurrentTime(), message = message, type = type)
    _missionTimeline.value = listOf(event) + _missionTimeline.value
  }

  private fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
  }

  private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val phi1 = Math.toRadians(lat1)
    val phi2 = Math.toRadians(lat2)
    val deltaLambda = Math.toRadians(lon2 - lon1)
    val y = sin(deltaLambda) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
    val theta = atan2(y, x)
    return (Math.toDegrees(theta) + 360.0) % 360.0
  }
}
