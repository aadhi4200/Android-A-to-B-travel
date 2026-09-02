package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DroneTelemetry
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun TelemetryMetricCard(
  modifier: Modifier = Modifier,
  title: String,
  value: String,
  unit: String = "",
  icon: ImageVector,
  accentColor: Color = PrimaryBlueLight,
  subtext: String? = null,
  testTag: String = "telemetry_card"
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
      .testTag(testTag),
    color = SpecialistCardBg
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = Slate500,
          fontWeight = FontWeight.Bold,
          fontSize = 9.sp,
          letterSpacing = 1.sp
        )
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(16.dp)
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start
      ) {
        Text(
          text = value,
          style = MaterialTheme.typography.titleLarge,
          fontFamily = FontFamily.Monospace,
          color = Slate100,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold
        )
        if (unit.isNotEmpty()) {
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = Slate400,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 2.dp)
          )
        }
      }

      if (subtext != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = subtext,
          style = MaterialTheme.typography.bodySmall,
          color = Slate500,
          fontSize = 10.sp
        )
      }
    }
  }
}

@Composable
fun TelemetryGridSection(
  telemetry: DroneTelemetry,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Row 1: Battery & GPS
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Battery
      val batColor = when {
        telemetry.batteryPercentage > 50 -> StatusGreenLight
        telemetry.batteryPercentage > 25 -> StatusAmberLight
        else -> StatusRedLight
      }
      TelemetryMetricCard(
        modifier = Modifier.weight(1f),
        title = "Battery",
        value = "${telemetry.batteryPercentage}%",
        unit = "${telemetry.batteryVoltage}V",
        icon = Icons.Default.BatteryChargingFull,
        accentColor = batColor,
        subtext = "4S LiPo • Healthy",
        testTag = "card_battery"
      )

      // GPS
      TelemetryMetricCard(
        modifier = Modifier.weight(1f),
        title = "GPS",
        value = "${telemetry.satellites}",
        unit = "SAT",
        icon = Icons.Default.GpsFixed,
        accentColor = PrimaryBlueLight,
        subtext = "HDOP: ${telemetry.hdop} • 3D RTK",
        testTag = "card_gps"
      )
    }

    // Row 2: Altitude & Speed
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      TelemetryMetricCard(
        modifier = Modifier.weight(1f),
        title = "Altitude",
        value = "${(telemetry.altitude * 10).roundToInt() / 10.0}",
        unit = "m",
        icon = Icons.Default.Height,
        accentColor = PrimaryBlueLight,
        subtext = "Vert: ${(telemetry.verticalSpeed * 10).roundToInt() / 10.0} m/s",
        testTag = "card_altitude"
      )

      TelemetryMetricCard(
        modifier = Modifier.weight(1f),
        title = "Speed",
        value = "${(telemetry.groundSpeed * 10).roundToInt() / 10.0}",
        unit = "m/s",
        icon = Icons.Default.Speed,
        accentColor = PrimaryBlue,
        subtext = "Ground Velocity",
        testTag = "card_speed"
      )
    }

    // Row 3: Heading & Flight Mode
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      TelemetryMetricCard(
        modifier = Modifier.weight(1f),
        title = "Heading",
        value = "${telemetry.heading.roundToInt()}°",
        unit = getHeadingCompassCardinal(telemetry.heading),
        icon = Icons.Default.Explore,
        accentColor = StatusAmberLight,
        subtext = "Mag Compass",
        testTag = "card_heading"
      )

      TelemetryMetricCard(
        modifier = Modifier.weight(1f),
        title = "Flight Mode",
        value = telemetry.flightMode,
        icon = Icons.Default.FlightTakeoff,
        accentColor = if (telemetry.flightMode == "AUTO") StatusGreenLight else PrimaryBlueLight,
        subtext = if (telemetry.armed) "ARMED • Ready" else "DISARMED",
        testTag = "card_flight_mode"
      )
    }

    // Additional Detail Coordinates Bar
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
      color = SpecialistSurfaceVariant
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("COORDINATES", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp)
          Text(
            "${telemetry.latitude}, ${telemetry.longitude}",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = PrimaryBlueLight
          )
        }
        Column(horizontalAlignment = Alignment.End) {
          Text("SIGNAL", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp)
          Text(
            "${telemetry.linkQuality} (${telemetry.signalStrengthDbm} dBm)",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = StatusGreenLight
          )
        }
      }
    }
  }
}

private fun getHeadingCompassCardinal(heading: Float): String {
  val normalized = (heading % 360 + 360) % 360
  return when {
    normalized >= 337.5 || normalized < 22.5 -> "N"
    normalized >= 22.5 && normalized < 67.5 -> "NE"
    normalized >= 67.5 && normalized < 112.5 -> "E"
    normalized >= 112.5 && normalized < 157.5 -> "SE"
    normalized >= 157.5 && normalized < 202.5 -> "S"
    normalized >= 202.5 && normalized < 247.5 -> "SW"
    normalized >= 247.5 && normalized < 292.5 -> "W"
    else -> "NW"
  }
}

