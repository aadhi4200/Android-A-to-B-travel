package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
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
  badgeText: String? = null,
  badgeColor: Color = PrimaryBlueLight,
  progressBarFraction: Float? = null,
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
        .padding(13.dp)
    ) {
      // Title Row + Icon & Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(accentColor)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Slate400,
            fontWeight = FontWeight.Bold,
            fontSize = 9.5.sp,
            letterSpacing = 1.sp
          )
        }

        if (badgeText != null) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = badgeColor.copy(alpha = 0.15f),
            border = BorderStroke(0.5.dp, badgeColor.copy(alpha = 0.4f))
          ) {
            Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = badgeColor,
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
          }
        } else {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Primary Numeric / Status Value
      Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start
      ) {
        Text(
          text = value,
          style = MaterialTheme.typography.titleLarge,
          fontFamily = FontFamily.Monospace,
          color = Slate100,
          fontSize = 19.sp,
          fontWeight = FontWeight.Bold
        )
        if (unit.isNotEmpty()) {
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = Slate400,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 2.dp)
          )
        }
      }

      // Optional Linear Level Indicator
      if (progressBarFraction != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(SpecialistSurfaceVariant)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(progressBarFraction.coerceIn(0f, 1f))
              .fillMaxHeight()
              .clip(RoundedCornerShape(2.dp))
              .background(accentColor)
          )
        }
      }

      // Subtext / Metadata Line
      if (subtext != null) {
        Spacer(modifier = Modifier.height(5.dp))
        Text(
          text = subtext,
          style = MaterialTheme.typography.bodySmall,
          fontFamily = FontFamily.Monospace,
          color = Slate500,
          fontSize = 9.5.sp
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
    // 1. Row 1: Battery & GPS
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Battery Card
      val batFraction = (telemetry.batteryPercentage / 100f).coerceIn(0f, 1f)
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
        badgeText = "${telemetry.batteryVoltage}V",
        badgeColor = batColor,
        progressBarFraction = batFraction,
        subtext = "4S LiPo • Cell ~${(telemetry.batteryVoltage / 4.0 * 100).roundToInt() / 100.0}V",
        testTag = "card_battery"
      )

      // GPS Card
      TelemetryMetricCard(
        modifier = Modifier.weight(1f),
        title = "GPS",
        value = "${telemetry.satellites}",
        unit = "SAT",
        icon = Icons.Default.GpsFixed,
        accentColor = PrimaryBlueLight,
        badgeText = "3D FIX",
        badgeColor = PrimaryBlueLight,
        progressBarFraction = (telemetry.satellites / 24f).coerceIn(0f, 1f),
        subtext = "HDOP: ${telemetry.hdop} • RTK Lock",
        testTag = "card_gps"
      )
    }

    // 2. Row 2: Altitude & Speed
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Altitude Card
      TelemetryMetricCard(
        modifier = Modifier.weight(1f),
        title = "Altitude",
        value = "${(telemetry.altitude * 10).roundToInt() / 10.0}",
        unit = "m AGL",
        icon = Icons.Default.Height,
        accentColor = PrimaryBlueLight,
        badgeText = "BARO",
        badgeColor = PrimaryBlueLight,
        subtext = "V.Spd: ${(telemetry.verticalSpeed * 10).roundToInt() / 10.0} m/s",
        testTag = "card_altitude"
      )

      // Speed Card
      val speedKmh = ((telemetry.groundSpeed * 3.6) * 10).roundToInt() / 10.0
      TelemetryMetricCard(
        modifier = Modifier.weight(1f),
        title = "Speed",
        value = "${(telemetry.groundSpeed * 10).roundToInt() / 10.0}",
        unit = "m/s",
        icon = Icons.Default.Speed,
        accentColor = PrimaryBlue,
        badgeText = "$speedKmh km/h",
        badgeColor = PrimaryBlueLight,
        subtext = "Ground Vector Vxy",
        testTag = "card_speed"
      )
    }

    // 3. Row 3: Heading & Flight Mode
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Heading Card
      val cardinal = getHeadingCompassCardinal(telemetry.heading)
      TelemetryMetricCard(
        modifier = Modifier.weight(1f),
        title = "Heading",
        value = "${telemetry.heading.roundToInt()}°",
        unit = cardinal,
        icon = Icons.Default.Explore,
        accentColor = StatusAmberLight,
        badgeText = "MAG 1",
        badgeColor = StatusAmberLight,
        subtext = "Yaw Angle Ref North",
        testTag = "card_heading"
      )

      // Flight Mode Card
      val isAuto = telemetry.flightMode.contains("AUTO", ignoreCase = true) || telemetry.flightMode.contains("MISSION", ignoreCase = true)
      TelemetryMetricCard(
        modifier = Modifier.weight(1f),
        title = "Flight Mode",
        value = telemetry.flightMode,
        icon = Icons.Default.FlightTakeoff,
        accentColor = if (isAuto) StatusGreenLight else PrimaryBlueLight,
        badgeText = if (telemetry.armed) "ARMED" else "DISARM",
        badgeColor = if (telemetry.armed) StatusGreenLight else Slate400,
        subtext = if (telemetry.armed) "Motors Active • PX4" else "Motors Locked",
        testTag = "card_flight_mode"
      )
    }

    // 4. Row 4: Signal Card (MAVLink Telemetry & RF Link)
    val signalPercent = telemetry.signalStrengthDbm.let { dbm ->
      ((dbm + 100) / 50f * 100).toInt().coerceIn(10, 100)
    }
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
        .testTag("card_signal"),
      color = SpecialistCardBg
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(StatusGreenDim)
              .border(1.dp, BorderGreen, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CellTower,
              contentDescription = null,
              tint = StatusGreenLight,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "SIGNAL & TELEMETRY LINK",
                style = MaterialTheme.typography.labelSmall,
                color = Slate400,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                letterSpacing = 1.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = StatusGreenDim,
                border = BorderStroke(0.5.dp, BorderGreen)
              ) {
                Text(
                  text = telemetry.linkQuality,
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  color = StatusGreenLight,
                  fontSize = 8.5.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = "${telemetry.signalStrengthDbm} dBm",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                color = Slate100,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "• Link: $signalPercent% • 915MHz MAVLink",
                style = MaterialTheme.typography.bodySmall,
                color = Slate400,
                fontSize = 11.sp
              )
            }
          }
        }

        // Live Link Bars
        Row(
          horizontalArrangement = Arrangement.spacedBy(3.dp),
          verticalAlignment = Alignment.Bottom,
          modifier = Modifier.height(20.dp)
        ) {
          listOf(0.35f, 0.55f, 0.75f, 1.0f).forEachIndexed { index, heightFrac ->
            val isActive = signalPercent >= (index + 1) * 22
            Box(
              modifier = Modifier
                .width(4.dp)
                .fillMaxHeight(heightFrac)
                .clip(RoundedCornerShape(1.dp))
                .background(if (isActive) StatusGreenLight else SpecialistSurfaceVariant)
            )
          }
        }
      }
    }

    // 5. Tactical Sub-Grid: Real-time Coordinates & Latency Banner
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
        .testTag("card_coordinates_banner"),
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
          Text("CURRENT COORDINATES", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp, letterSpacing = 0.5.sp)
          Text(
            "${telemetry.latitude}, ${telemetry.longitude}",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryBlueLight
          )
        }
        Column(horizontalAlignment = Alignment.End) {
          Text("DOWNLINK LATENCY", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp, letterSpacing = 0.5.sp)
          Text(
            "14 ms • 0.0% Loss",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
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
