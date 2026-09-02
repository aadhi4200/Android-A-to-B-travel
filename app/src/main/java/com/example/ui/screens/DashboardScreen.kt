package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.example.data.model.MissionState
import com.example.ui.components.TelemetryGridSection
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.DroneViewModel

@Composable
fun DashboardScreen(
  viewModel: DroneViewModel,
  modifier: Modifier = Modifier
) {
  val telemetry by viewModel.droneService.telemetry.collectAsState()
  val missionState by viewModel.droneService.missionState.collectAsState()
  val connectionConfig by viewModel.connectionConfig.collectAsState()
  val scrollState = rememberScrollState()

  // Pulsing animation for the active connection indicator
  val infiniteTransition = rememberInfiniteTransition(label = "connectionPulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SpecialistBg)
      .verticalScroll(scrollState)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Specialist Header: "DRONE A → B" + Connection Indicator
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
        .testTag("dashboard_header_card"),
      color = SpecialistHeaderBg
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(PrimaryBlueLight)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "TACTICAL GROUND CONTROL",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = PrimaryBlueLight,
              fontSize = 9.5.sp,
              letterSpacing = 1.5.sp
            )
          }
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "Drone A → B",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Slate100,
            fontSize = 22.sp,
            letterSpacing = (-0.3).sp
          )
        }

        // Aerospace Connection Indicator Badge
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (telemetry.connected) StatusGreenDim else StatusRedDim,
          border = BorderStroke(1.dp, if (telemetry.connected) BorderGreen else BorderRed),
          modifier = Modifier.testTag("connection_indicator_badge")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                  if (telemetry.connected) StatusGreen.copy(alpha = pulseAlpha)
                  else StatusRed
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (telemetry.connected) "CONNECTED" else "OFFLINE",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = if (telemetry.connected) StatusGreenLight else StatusRedLight,
              fontSize = 10.5.sp,
              letterSpacing = 0.5.sp
            )
          }
        }
      }
    }

    // Active Mission State Banner (if in flight/active)
    if (missionState != MissionState.IDLE) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .border(1.dp, BlueTintBorder, RoundedCornerShape(14.dp))
          .testTag("dashboard_active_mission_banner"),
        color = BlueTintBg
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              "CURRENT MISSION FLIGHT PHASE",
              style = MaterialTheme.typography.labelSmall,
              color = PrimaryBlueLight,
              fontSize = 9.5.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              missionState.label,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = Slate100
            )
          }

          Button(
            onClick = { viewModel.selectTab(AppNavTab.FLIGHT) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Slate100),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("btn_view_live_flight")
          ) {
            Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("LIVE HUD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }

    // Section Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Analytics, contentDescription = null, tint = PrimaryBlueLight, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "TELEMETRY METRICS",
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = Slate300,
          letterSpacing = 1.sp,
          fontSize = 11.sp
        )
      }

      Text(
        text = "MAVLink v2.0 • 20 Hz",
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        color = Slate500,
        fontSize = 10.sp
      )
    }

    // Grid-based Telemetry Cards for Battery, GPS, Altitude, Speed, Heading, Flight Mode, and Signal
    TelemetryGridSection(
      telemetry = telemetry,
      modifier = Modifier.testTag("section_telemetry_grid")
    )

    // Pre-Flight Readiness & Quick Navigation Actions
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
        .testTag("card_subsystems_status"),
      color = SpecialistCardBg
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "AVIONICS & SUBSYSTEMS STATUS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlueLight,
            letterSpacing = 1.sp,
            fontSize = 10.sp
          )

          Surface(
            shape = RoundedCornerShape(4.dp),
            color = StatusGreenDim,
            border = BorderStroke(0.5.dp, BorderGreen)
          ) {
            Text(
              "ALL SYSTEMS NOMINAL",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = StatusGreenLight,
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          SubsystemStatusItem("PX4 Autopilot", "ONLINE", StatusGreenLight)
          SubsystemStatusItem("MAVLink v2.0", "SYNCED", StatusGreenLight)
          SubsystemStatusItem("SLAM Vision", "TRACKING", StatusGreenLight)
          SubsystemStatusItem("ArUco Pad", "READY", PrimaryBlueLight)
        }

        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

        // Quick Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = { viewModel.selectTab(AppNavTab.MISSION) },
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("btn_dashboard_plan_mission"),
            colors = ButtonDefaults.buttonColors(
              containerColor = PrimaryBlue,
              contentColor = Slate100
            ),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("PLAN MISSION", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }

          OutlinedButton(
            onClick = { viewModel.selectTab(AppNavTab.FLIGHT) },
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("btn_dashboard_live_flight"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate100),
            border = BorderStroke(1.dp, BorderMedium),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryBlueLight)
            Spacer(modifier = Modifier.width(6.dp))
            Text("LIVE FLIGHT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun SubsystemStatusItem(
  name: String,
  status: String,
  color: Color
) {
  Column(horizontalAlignment = Alignment.Start) {
    Text(name, style = MaterialTheme.typography.bodySmall, color = Slate500, fontSize = 10.sp)
    Spacer(modifier = Modifier.height(2.dp))
    Text(status, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = color, fontSize = 11.sp)
  }
}
