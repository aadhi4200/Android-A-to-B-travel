package com.example.data.service

import com.example.data.model.*
import kotlinx.coroutines.flow.StateFlow

interface DroneService {
  val telemetry: StateFlow<DroneTelemetry>
  val missionState: StateFlow<MissionState>
  val slamStatus: StateFlow<SlamStatus>
  val obstacleStatus: StateFlow<ObstacleAvoidanceStatus>
  val visionStatus: StateFlow<VisionStatus>
  val landingSequence: StateFlow<LandingSequenceState>
  val missionProgress: StateFlow<Float> // 0..100
  val currentMission: StateFlow<MissionPlan?>
  val flownPath: StateFlow<List<GeoPoint>>
  val avoidancePath: StateFlow<List<GeoPoint>>
  val missionTimeline: StateFlow<List<MissionEvent>>
  val lastCompletedMission: StateFlow<MissionCompleteSummary?>

  suspend fun getTelemetry(): DroneTelemetry
  fun setMissionPlan(plan: MissionPlan)
  fun startMission()
  fun pauseMission()
  fun resumeMission()
  fun abortMission()
  fun returnToHome()
  fun resetToIdle()
  fun setConnection(connected: Boolean)
  fun dismissMissionComplete()
}
