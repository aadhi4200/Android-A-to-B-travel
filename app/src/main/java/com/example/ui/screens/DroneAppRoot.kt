package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.MissionCompleteSummary
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.DroneViewModel

@Composable
fun DroneAppRoot(
  viewModel: DroneViewModel = viewModel()
) {
  val currentTab by viewModel.currentTab.collectAsState()
  val lastCompletedMission by viewModel.droneService.lastCompletedMission.collectAsState()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = SpecialistBg,
    bottomBar = {
      SpecialistBottomNavigationBar(
        currentTab = currentTab,
        onTabSelected = { tab -> viewModel.selectTab(tab) }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentTab) {
        AppNavTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
        AppNavTab.MISSION -> MissionPlannerScreen(viewModel = viewModel)
        AppNavTab.FLIGHT -> LiveFlightScreen(viewModel = viewModel)
        AppNavTab.HISTORY -> MissionHistoryScreen(viewModel = viewModel)
        AppNavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
      }

      // Mission Complete Modal Dialog
      if (lastCompletedMission != null) {
        MissionCompleteDialog(
          summary = lastCompletedMission!!,
          onDismiss = { viewModel.droneService.dismissMissionComplete() },
          onViewHistory = {
            viewModel.droneService.dismissMissionComplete()
            viewModel.selectTab(AppNavTab.HISTORY)
          }
        )
      }
    }
  }
}

@Composable
private fun SpecialistBottomNavigationBar(
  currentTab: AppNavTab,
  onTabSelected: (AppNavTab) -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("drone_bottom_navigation"),
    color = SpecialistHeaderBg,
    border = BorderStroke(1.dp, BorderSubtle)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(72.dp)
        .padding(horizontal = 12.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. DASH
      SpecialistNavTabItem(
        label = "DASH",
        icon = Icons.Default.Dashboard,
        selected = currentTab == AppNavTab.DASHBOARD,
        onClick = { onTabSelected(AppNavTab.DASHBOARD) },
        testTag = "nav_tab_dashboard"
      )

      // 2. MISSION
      SpecialistNavTabItem(
        label = "MISSION",
        icon = Icons.Default.Route,
        selected = currentTab == AppNavTab.MISSION,
        onClick = { onTabSelected(AppNavTab.MISSION) },
        testTag = "nav_tab_mission"
      )

      // 3. PROMINENT LIVE CENTER ACTION
      Box(
        modifier = Modifier
          .padding(bottom = 6.dp)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) { onTabSelected(AppNavTab.FLIGHT) }
          .testTag("nav_tab_flight"),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .shadow(
                elevation = if (currentTab == AppNavTab.FLIGHT) 12.dp else 4.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = PrimaryBlue
              )
              .clip(RoundedCornerShape(14.dp))
              .background(if (currentTab == AppNavTab.FLIGHT) PrimaryBlue else SpecialistSurfaceVariant)
              .border(
                1.dp,
                if (currentTab == AppNavTab.FLIGHT) PrimaryBlueLight else BorderMedium,
                RoundedCornerShape(14.dp)
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Radar,
              contentDescription = "Live Flight",
              tint = if (currentTab == AppNavTab.FLIGHT) Slate100 else Slate400,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp,
            color = if (currentTab == AppNavTab.FLIGHT) PrimaryBlueLight else Slate400
          )
        }
      }

      // 4. HISTORY
      SpecialistNavTabItem(
        label = "HISTORY",
        icon = Icons.Default.History,
        selected = currentTab == AppNavTab.HISTORY,
        onClick = { onTabSelected(AppNavTab.HISTORY) },
        testTag = "nav_tab_history"
      )

      // 5. SETUP
      SpecialistNavTabItem(
        label = "SETUP",
        icon = Icons.Default.Settings,
        selected = currentTab == AppNavTab.SETTINGS,
        onClick = { onTabSelected(AppNavTab.SETTINGS) },
        testTag = "nav_tab_settings"
      )
    }
  }
}

@Composable
private fun SpecialistNavTabItem(
  label: String,
  icon: ImageVector,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) { onClick() }
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (selected) PrimaryBlueLight else Slate500,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 9.sp,
        letterSpacing = 0.5.sp,
        color = if (selected) PrimaryBlueLight else Slate500
      )
    }
  }
}

@Composable
private fun MissionCompleteDialog(
  summary: MissionCompleteSummary,
  onDismiss: () -> Unit,
  onViewHistory: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(StatusGreenDim)
            .border(1.dp, BorderGreen, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreenLight, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "MISSION COMPLETE",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = StatusGreenLight
        )
        Text(
          text = "Autonomous Travel: A → B",
          style = MaterialTheme.typography.bodySmall,
          color = Slate400
        )
      }
    },
    text = {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .border(1.dp, BorderMedium, RoundedCornerShape(12.dp)),
        color = SpecialistSurfaceVariant
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          DebriefRow("Distance", "${summary.distanceKm} km")
          DebriefRow("Flight Time", summary.flightTimeFormatted)
          DebriefRow("Battery Used", "${summary.batteryUsedPercent}%")
          DebriefRow("Landing", summary.landingStatus, StatusGreenLight)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onViewHistory,
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Slate100),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("btn_complete_view_history")
      ) {
        Text("VIEW HISTORY & LOGS", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate400),
        border = BorderStroke(1.dp, BorderMedium),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("DISMISS")
      }
    },
    containerColor = SpecialistCardBg,
    shape = RoundedCornerShape(16.dp)
  )
}

@Composable
private fun DebriefRow(label: String, value: String, valueColor: Color = Slate100) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = Slate400)
    Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
  }
}

