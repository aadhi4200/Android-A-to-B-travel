package com.example.data.model

data class DroneTelemetry(
  val latitude: Double = 10.123456,
  val longitude: Double = 76.123456,
  val altitude: Double = 32.4,
  val relativeAltitude: Double = 32.4,
  val groundSpeed: Double = 7.8,
  val verticalSpeed: Double = 0.2,
  val heading: Float = 124f,
  val batteryPercentage: Int = 78,
  val batteryVoltage: Double = 15.6,
  val satellites: Int = 18,
  val hdop: Double = 0.7,
  val flightMode: String = "AUTO",
  val armed: Boolean = true,
  val connected: Boolean = true,
  val signalStrengthDbm: Int = -64,
  val linkQuality: String = "Excellent",
  val timestamp: String = "12:00:00"
)

enum class FlightModeType(val label: String) {
  IDLE("IDLE"),
  ARMED("ARMED"),
  TAKEOFF("TAKEOFF"),
  AUTO("AUTO"),
  GUIDED("GUIDED"),
  LOITER("LOITER"),
  RTL("RTL"),
  LAND("LAND")
}
