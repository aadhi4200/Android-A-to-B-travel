package com.example.data.model

data class DroneConfig(
  val droneId: String = "PX4-VTOL-X8",
  val vehicleType: String = "Hexacopter VTOL",
  val homeLatitude: Double = 10.123456,
  val homeLongitude: Double = 76.123456,
  val firmwareVersion: String = "PX4 v1.14.3-ros2"
)

data class ConnectionConfig(
  val backendUrl: String = "http://192.168.1.100:8080/api/v1",
  val websocketUrl: String = "ws://192.168.1.100:8080/ws/telemetry",
  val mqttBroker: String = "mqtt://broker.hivemq.com:1883",
  val isConnected: Boolean = true,
  val pingMs: Int = 24,
  val mavlinkProtocol: String = "MAVLink v2.0"
)

data class MissionConfig(
  val defaultAltitudeMeters: Double = 32.4,
  val maxSpeedMps: Double = 8.0,
  val geofenceRadiusMeters: Double = 2500.0,
  val autoRtlOnLowBattery: Boolean = true,
  val failsafeAction: String = "RETURN TO HOME"
)

enum class MapVisualType(val label: String) {
  TACTICAL_GRID("Tactical Grid"),
  SATELLITE("Satellite"),
  DARK_VECTOR("Dark Vector")
}

data class DisplayConfig(
  val isDarkMode: Boolean = true,
  val mapType: MapVisualType = MapVisualType.TACTICAL_GRID,
  val useMetricUnits: Boolean = true,
  val showCameraPip: Boolean = true,
  val showSlamDebug: Boolean = true
)
