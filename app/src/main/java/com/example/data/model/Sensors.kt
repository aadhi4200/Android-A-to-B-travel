package com.example.data.model

enum class SlamTrackingState(val label: String) {
  TRACKING("TRACKING"),
  INITIALIZING("INITIALIZING"),
  DEGRADED("DEGRADED"),
  LOST("LOST")
}

data class SlamStatus(
  val status: SlamTrackingState = SlamTrackingState.TRACKING,
  val localization: String = "GOOD",
  val confidence: Int = 94,
  val mapActive: Boolean = true,
  val poseState: String = "STABLE",
  val featureCount: Int = 512,
  val loopClosures: Int = 14
)

enum class ObstacleState(val label: String) {
  CLEAR("CLEAR"),
  DETECTED("OBSTACLE DETECTED"),
  AVOIDING("AVOIDING")
}

data class ObstacleAvoidanceStatus(
  val status: ObstacleState = ObstacleState.CLEAR,
  val frontDistance: Double = 18.4,
  val leftDistance: Double = 12.2,
  val rightDistance: Double = 14.7,
  val upDistance: Double = 8.5,
  val downDistance: Double = 6.2,
  val detectedSector: String = "FRONT",
  val detectedDistance: Double = 4.2,
  val action: String = "CLEAR"
)

enum class ArucoDetectionState(val label: String) {
  SEARCHING("SEARCHING"),
  DETECTED("DETECTED"),
  LOCKED("LOCKED")
}

data class VisionStatus(
  val cameraOnline: Boolean = true,
  val fps: Int = 30,
  val visionActive: Boolean = true,
  val arucoState: ArucoDetectionState = ArucoDetectionState.SEARCHING,
  val landingMarkerDistance: Double = 8.4,
  val landingAlignment: String = "GOOD",
  val markerId: Int = 42
)

data class LandingSequenceState(
  val isPositioningDone: Boolean = false,
  val isMarkerDetectedDone: Boolean = false,
  val isAlignmentDone: Boolean = false,
  val isDescentInProgress: Boolean = false,
  val isTouchdownDone: Boolean = false,
  val activeStepIndex: Int = 0 // 0..4
)
