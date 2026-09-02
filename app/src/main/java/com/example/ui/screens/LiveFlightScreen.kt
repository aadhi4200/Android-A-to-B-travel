package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DroneViewModel
import kotlin.math.roundToInt

@Composable
fun LiveFlightScreen(
  viewModel: DroneViewModel,
  modifier: Modifier = Modifier
) {
  val telemetry by viewModel.droneService.telemetry.collectAsState()
  val missionState by viewModel.droneService.missionState.collectAsState()
  val missionProgress by viewModel.droneService.missionProgress.collectAsState()
  val slamStatus by viewModel.droneService.slamStatus.collectAsState()
  val obstacleStatus by viewModel.droneService.obstacleStatus.collectAsState()
  val visionStatus by viewModel.droneService.visionStatus.collectAsState()
  val landingSequence by viewModel.droneService.landingSequence.collectAsState()
  val currentMission by viewModel.droneService.currentMission.collectAsState()
  val flownPath by viewModel.droneService.flownPath.collectAsState()
  val avoidancePath by viewModel.droneService.avoidancePath.collectAsState()
  val displayConfig by viewModel.displayConfig.collectAsState()

  var isTelemetryPanelExpanded by remember { mutableStateOf(true) }
  var selectedHudTab by remember { mutableIntStateOf(0) } // 0: SLAM, 1: Obstacles, 2: Vision/Camera, 3: Safety

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SpecialistBg)
  ) {
    // TOP STATUS STRIP (MISSION STATE & PROGRESS A -> B)
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("live_flight_top_bar"),
      color = BlueTintBg,
      border = BorderStroke(1.dp, BlueTintBorder)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "MISSION STATE",
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryBlueLight,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = missionState.label,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = when (missionState) {
              MissionState.OBSTACLE_DETECTED, MissionState.AVOIDING -> StatusAmberLight
              MissionState.ABORTED -> StatusRedLight
              else -> Slate100
            }
          )
        }

        // Mission Progress Percentage & Progress Bar (A -> B)
        Column(
          horizontalAlignment = Alignment.End,
          modifier = Modifier.width(140.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "A",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = PrimaryBlueLight,
              fontSize = 10.sp
            )
            Text(
              text = "${missionProgress.roundToInt()}%",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = PrimaryBlueLight,
              fontSize = 10.sp
            )
            Text(
              text = "B",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = PrimaryBlueLight,
              fontSize = 10.sp
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Mission Progress Bar
          LinearProgressIndicator(
            progress = { (missionProgress / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = when (missionState) {
              MissionState.OBSTACLE_DETECTED, MissionState.AVOIDING -> StatusAmber
              MissionState.ABORTED -> StatusRed
              else -> PrimaryBlue
            },
            trackColor = Color(0x33FFFFFF)
          )
        }
      }
    }

    // MAIN INTERACTIVE TACTICAL MAP (Takes prominent dynamic space)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1.2f)
        .background(SpecialistCanvasBg)
    ) {
      TacticalMapCanvas(
        droneLat = telemetry.latitude,
        droneLon = telemetry.longitude,
        droneHeading = telemetry.heading,
        droneAltitude = telemetry.altitude,
        isArmed = telemetry.armed,
        pointA = currentMission?.pointA,
        pointB = currentMission?.pointB,
        flownPath = flownPath,
        avoidancePath = avoidancePath,
        mapType = displayConfig.mapType
      )

      // Floating Left HUD Overlay: SLAM & Obstacle badges
      Column(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Floating SLAM Pill
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xCC0A0B10),
          border = BorderStroke(1.dp, BorderMedium),
          modifier = Modifier.width(135.dp)
        ) {
          Column(modifier = Modifier.padding(8.dp)) {
            Text(
              text = "SLAM STATUS",
              style = MaterialTheme.typography.labelSmall,
              color = Slate400,
              fontWeight = FontWeight.Bold,
              fontSize = 8.sp,
              letterSpacing = 0.5.sp
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = slamStatus.status.label,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = StatusGreenLight,
                fontSize = 11.sp
              )
              Text(
                text = "${slamStatus.confidence}%",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Slate300,
                fontSize = 10.sp
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(3.dp)
                  .clip(CircleShape)
                  .background(StatusGreen.copy(alpha = 0.8f))
              )
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(3.dp)
                  .clip(CircleShape)
                  .background(StatusGreen.copy(alpha = 0.8f))
              )
            }
          }
        }

        // Floating Obstacle Alert Pill (if obstacle detected)
        if (obstacleStatus.status != ObstacleState.CLEAR) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = StatusAmberDim,
            border = BorderStroke(1.dp, BorderAmber),
            modifier = Modifier.width(135.dp)
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Text(
                text = "AVOIDANCE",
                style = MaterialTheme.typography.labelSmall,
                color = StatusAmberLight,
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
                letterSpacing = 0.5.sp
              )
              Text(
                text = obstacleStatus.status.label,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = StatusAmberLight,
                fontSize = 10.sp
              )
              Text(
                text = "Front: ${obstacleStatus.frontDistance}m",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = StatusAmberLight.copy(alpha = 0.8f),
                fontSize = 9.sp
              )
            }
          }
        }
      }

      // Floating Right HUD Overlay: Mini Camera PiP
      Surface(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(12.dp)
          .size(width = 100.dp, height = 66.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xCC000000),
        border = BorderStroke(1.dp, BorderProminent)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          // Label Top-Left
          Surface(
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(4.dp),
            shape = RoundedCornerShape(4.dp),
            color = Color(0x99000000)
          ) {
            Text(
              text = "CAM 01",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 7.sp,
              color = Slate300,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
          }

          // Center Reticle
          Box(
            modifier = Modifier
              .size(16.dp)
              .align(Alignment.Center)
              .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(2.dp))
          )

          // 30 FPS Bottom-Right
          Text(
            text = "30 FPS",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontSize = 7.sp,
            color = StatusGreenLight,
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(4.dp)
          )
        }
      }

      // If in Landing phase, show Landing Overlay Banner on Map
      if (missionState == MissionState.LANDING || missionState == MissionState.LANDED) {
        Surface(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 12.dp),
          shape = RoundedCornerShape(20.dp),
          color = SpecialistBg.copy(alpha = 0.92f),
          border = BorderStroke(1.dp, BorderGreen)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.FlightLand, contentDescription = null, tint = StatusGreenLight, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (missionState == MissionState.LANDED) "TOUCHDOWN COMPLETE" else "AUTONOMOUS LANDING DESCENT",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = StatusGreenLight
            )
          }
        }
      }
    }

    // BOTTOM SPECIALIST HUD INSTRUMENT & SAFETY PANEL
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1.05f),
      color = SpecialistCardBg,
      border = BorderStroke(1.dp, BorderSubtle),
      shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 14.dp, vertical = 10.dp)
      ) {
        // High-Density 4-Metric Grid (ALT, SPEED, BATT, GPS)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          SpecialistInstrumentMetric("ALT", "${(telemetry.altitude * 10).roundToInt() / 10.0}", "m")
          SpecialistInstrumentMetric("SPEED", "${(telemetry.groundSpeed * 10).roundToInt() / 10.0}", "m/s")
          SpecialistInstrumentMetric("BATT", "${telemetry.batteryPercentage}", "%")
          SpecialistInstrumentMetric("GPS", "${telemetry.satellites}", "sat")

          IconButton(
            onClick = { isTelemetryPanelExpanded = !isTelemetryPanelExpanded },
            modifier = Modifier.size(30.dp)
          ) {
            Icon(
              imageVector = if (isTelemetryPanelExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
              contentDescription = "Toggle HUD Panel",
              tint = PrimaryBlueLight
            )
          }
        }

        // Direct Quick Hardware Safety Controls (ABORT & PAUSE / RESUME)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // ABORT BUTTON
          Button(
            onClick = { viewModel.droneService.abortMission() },
            modifier = Modifier
              .weight(1f)
              .height(42.dp)
              .testTag("btn_quick_abort"),
            colors = ButtonDefaults.buttonColors(
              containerColor = StatusRedDim,
              contentColor = StatusRedLight
            ),
            border = BorderStroke(1.dp, BorderRed),
            shape = RoundedCornerShape(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(StatusRed)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("ABORT", fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
          }

          // PAUSE / RESUME BUTTON
          if (missionState == MissionState.PAUSED) {
            Button(
              onClick = { viewModel.droneService.resumeMission() },
              modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .testTag("btn_quick_resume"),
              colors = ButtonDefaults.buttonColors(
                containerColor = StatusGreenDim,
                contentColor = StatusGreenLight
              ),
              border = BorderStroke(1.dp, BorderGreen),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("RESUME", fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            }
          } else {
            Button(
              onClick = { viewModel.droneService.pauseMission() },
              modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .testTag("btn_quick_pause"),
              colors = ButtonDefaults.buttonColors(
                containerColor = Slate700.copy(alpha = 0.5f),
                contentColor = Slate100
              ),
              border = BorderStroke(1.dp, BorderMedium),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("PAUSE", fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            }
          }
        }

        if (isTelemetryPanelExpanded) {
          // Subsystem Selector Tabs (SLAM, Obstacles, Camera, Safety)
          TabRow(
            selectedTabIndex = selectedHudTab,
            containerColor = SpecialistSurfaceVariant,
            contentColor = PrimaryBlueLight,
            indicator = { tabPositions ->
              if (selectedHudTab < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                  modifier = Modifier.tabIndicatorOffset(tabPositions[selectedHudTab]),
                  color = PrimaryBlueLight
                )
              }
            },
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .height(34.dp)
          ) {
            Tab(
              selected = selectedHudTab == 0,
              onClick = { selectedHudTab = 0 },
              text = { Text("SLAM", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp) },
              modifier = Modifier.testTag("tab_slam")
            )
            Tab(
              selected = selectedHudTab == 1,
              onClick = { selectedHudTab = 1 },
              text = { Text("OBSTACLES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp) },
              modifier = Modifier.testTag("tab_obstacles")
            )
            Tab(
              selected = selectedHudTab == 2,
              onClick = { selectedHudTab = 2 },
              text = { Text("CAMERA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp) },
              modifier = Modifier.testTag("tab_camera")
            )
            Tab(
              selected = selectedHudTab == 3,
              onClick = { selectedHudTab = 3 },
              text = { Text("SAFETY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp) },
              modifier = Modifier.testTag("tab_safety")
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Scrollable Card Content
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            when (selectedHudTab) {
              0 -> {
                SlamStatusCard(slamStatus = slamStatus)
              }
              1 -> {
                ObstacleAvoidanceCard(obstacleStatus = obstacleStatus)
              }
              2 -> {
                VisionCameraCard(visionStatus = visionStatus)
              }
              3 -> {
                SafetyControls(
                  missionState = missionState,
                  onPause = { viewModel.droneService.pauseMission() },
                  onResume = { viewModel.droneService.resumeMission() },
                  onRtl = { viewModel.droneService.returnToHome() },
                  onAbort = { viewModel.droneService.abortMission() }
                )
              }
            }

            // If landing sequence is active, show landing sequence card regardless
            if (missionState == MissionState.LANDING || missionState == MissionState.LANDED) {
              LandingSequenceCard(landingSequence = landingSequence)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SpecialistInstrumentMetric(label: String, value: String, unit: String) {
  Column(horizontalAlignment = Alignment.Start) {
    Text(
      text = label.uppercase(),
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = Slate500,
      fontSize = 9.sp,
      letterSpacing = 0.5.sp
    )
    Row(verticalAlignment = Alignment.Bottom) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = Slate100,
        fontSize = 16.sp
      )
      Spacer(modifier = Modifier.width(2.dp))
      Text(
        text = unit,
        style = MaterialTheme.typography.labelSmall,
        color = Slate400,
        fontSize = 10.sp,
        modifier = Modifier.padding(bottom = 1.dp)
      )
    }
  }
}

