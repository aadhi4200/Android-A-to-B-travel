package com.example.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SpecialistBg)
      .verticalScroll(scrollState)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Specialist Header
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
          Text(
            text = "GROUND CONTROL",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlueLight,
            fontSize = 10.sp,
            letterSpacing = 2.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "DRONE A → B",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Slate100,
            letterSpacing = (-0.2).sp
          )
        }

        // Connection Status Pill
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (telemetry.connected) StatusGreenDim else StatusRedDim,
          border = BorderStroke(1.dp, if (telemetry.connected) BorderGreen else BorderRed)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (telemetry.connected) StatusGreen else StatusRed)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (telemetry.connected) "CONNECTED" else "OFFLINE",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = if (telemetry.connected) StatusGreenLight else StatusRedLight,
              fontSize = 11.sp,
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
          .border(1.dp, BlueTintBorder, RoundedCornerShape(14.dp)),
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
              "MISSION STATE",
              style = MaterialTheme.typography.labelSmall,
              color = PrimaryBlueLight,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
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
            Text("LIVE HUD", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Section Title
    Text(
      text = "SYSTEM TELEMETRY",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = Slate400,
      letterSpacing = 1.5.sp
    )

    // Full Telemetry Cards Grid
    TelemetryGridSection(telemetry = telemetry)

    // Pre-Flight Readiness & Quick Navigation Actions
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
      color = SpecialistCardBg
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "SPECIALIST SUBSYSTEMS",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = PrimaryBlueLight,
          letterSpacing = 1.sp
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          SubsystemStatusItem("PX4 Autopilot", "ONLINE", StatusGreenLight)
          SubsystemStatusItem("MAVLink v2.0", "SYNCED", StatusGreenLight)
          SubsystemStatusItem("SLAM Vision", "TRACKING", StatusGreenLight)
          SubsystemStatusItem("ArUco Target", "READY", PrimaryBlueLight)
        }

        Spacer(modifier = Modifier.height(2.dp))

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
    Text(status, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color, fontSize = 11.sp)
  }
}

