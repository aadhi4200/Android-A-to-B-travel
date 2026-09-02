package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.model.MapVisualType
import com.example.ui.theme.*
import com.example.ui.viewmodel.DroneViewModel

@Composable
fun SettingsScreen(
  viewModel: DroneViewModel,
  modifier: Modifier = Modifier
) {
  val droneConfig by viewModel.droneConfig.collectAsState()
  val connectionConfig by viewModel.connectionConfig.collectAsState()
  val missionConfig by viewModel.missionConfig.collectAsState()
  val displayConfig by viewModel.displayConfig.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SpecialistBg)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    Column {
      Text(
        text = "SETTINGS & SYSTEM CONFIG",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Slate100
      )
      Text(
        text = "PX4 Autopilot & Ground Control Parameters",
        style = MaterialTheme.typography.bodySmall,
        color = Slate400
      )
    }

    // 1. DRONE CONFIGURATION
    SettingsSectionCard(title = "DRONE HARDWARE & VEHICLE", icon = Icons.Default.Flight) {
      SettingReadonlyRow("Drone ID", droneConfig.droneId, isMonospace = true)
      SettingReadonlyRow("Vehicle Type", droneConfig.vehicleType)
      SettingReadonlyRow("Firmware Version", droneConfig.firmwareVersion, isMonospace = true)
      SettingReadonlyRow("Home Position", "${droneConfig.homeLatitude}, ${droneConfig.homeLongitude}", isMonospace = true)
    }

    // 2. CONNECTION CONFIGURATION
    SettingsSectionCard(title = "TELEMETRY & NETWORK LINKS", icon = Icons.Default.Sensors) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Connection Status", style = MaterialTheme.typography.bodyMedium, color = Slate200)
          Text(
            if (connectionConfig.isConnected) "Connected • ${connectionConfig.pingMs}ms latency" else "Disconnected",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = if (connectionConfig.isConnected) StatusGreenLight else StatusRedLight
          )
        }
        Switch(
          checked = connectionConfig.isConnected,
          onCheckedChange = { viewModel.toggleConnection() },
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = PrimaryBlue,
            uncheckedThumbColor = Slate500,
            uncheckedTrackColor = SpecialistSurfaceVariant
          ),
          modifier = Modifier.testTag("switch_connection")
        )
      }

      HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

      SettingReadonlyRow("REST Backend URL", connectionConfig.backendUrl, isMonospace = true)
      SettingReadonlyRow("WebSocket Telemetry", connectionConfig.websocketUrl, isMonospace = true)
      SettingReadonlyRow("MQTT Telemetry Broker", connectionConfig.mqttBroker, isMonospace = true)
      SettingReadonlyRow("Protocol Standard", connectionConfig.mavlinkProtocol)
    }

    // 3. MISSION CONFIGURATION
    SettingsSectionCard(title = "AUTONOMOUS MISSION DEFAULTS", icon = Icons.Default.Speed) {
      SettingReadonlyRow("Default Cruise Altitude", "${missionConfig.defaultAltitudeMeters} m AGL", isMonospace = true)
      SettingReadonlyRow("Maximum Ground Speed", "${missionConfig.maxSpeedMps} m/s (~28 km/h)", isMonospace = true)
      SettingReadonlyRow("Geofence Radius Limit", "${missionConfig.geofenceRadiusMeters} m", isMonospace = true)
      SettingReadonlyRow("Low Battery Failsafe", missionConfig.failsafeAction)
    }

    // 4. DISPLAY & INTERFACE
    SettingsSectionCard(title = "DISPLAY & MAP PREFERENCES", icon = Icons.Default.Layers) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Map Visual Layer Style", style = MaterialTheme.typography.bodyMedium, color = Slate200)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          MapVisualType.values().forEach { mapType ->
            val isSelected = displayConfig.mapType == mapType
            Surface(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, if (isSelected) PrimaryBlueLight else BorderSubtle, RoundedCornerShape(8.dp)),
              color = if (isSelected) PrimaryBlue else SpecialistSurfaceVariant,
              onClick = { viewModel.setMapType(mapType) }
            ) {
              Box(
                modifier = Modifier.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = mapType.label,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else Slate300,
                  fontSize = 10.sp
                )
              }
            }
          }
        }
      }

      HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

      SettingReadonlyRow("Display Theme", if (displayConfig.isDarkMode) "Hardware / Specialist Dark" else "Standard Light")
      SettingReadonlyRow("Measurement Units", if (displayConfig.useMetricUnits) "Metric (meters, m/s)" else "Imperial (feet, kts)")
    }

    // Reset Simulation Button
    Button(
      onClick = { viewModel.droneService.resetToIdle() },
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("btn_reset_simulation"),
      colors = ButtonDefaults.buttonColors(containerColor = SpecialistSurfaceVariant, contentColor = StatusAmberLight),
      shape = RoundedCornerShape(10.dp),
      border = BorderStroke(1.dp, BorderAmber)
    ) {
      Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("RESET SIMULATION TO POINT A", fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
private fun SettingsSectionCard(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  content: @Composable ColumnScope.() -> Unit
) {
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
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = PrimaryBlueLight, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = PrimaryBlueLight,
          letterSpacing = 1.sp,
          fontSize = 11.sp
        )
      }

      content()
    }
  }
}

@Composable
private fun SettingReadonlyRow(label: String, value: String, isMonospace: Boolean = false) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = Slate400)
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
      fontWeight = FontWeight.Bold,
      color = Slate100
    )
  }
}

