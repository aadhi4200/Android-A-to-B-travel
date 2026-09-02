package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ObstacleAvoidanceStatus
import com.example.data.model.ObstacleState
import com.example.ui.theme.*

@Composable
fun ObstacleAvoidanceCard(
  obstacleStatus: ObstacleAvoidanceStatus,
  modifier: Modifier = Modifier
) {
  val isAlert = obstacleStatus.status != ObstacleState.CLEAR

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(
        width = 1.dp,
        color = if (isAlert) BorderAmber else BorderSubtle,
        shape = RoundedCornerShape(14.dp)
      )
      .testTag("obstacle_avoidance_card"),
    color = SpecialistCardBg
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Top Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "OBSTACLE AVOIDANCE",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = PrimaryBlueLight,
          letterSpacing = 1.sp,
          fontSize = 11.sp
        )

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (isAlert) StatusAmberDim else StatusGreenDim,
          border = BorderStroke(
            1.dp,
            if (isAlert) BorderAmber else BorderGreen
          )
        ) {
          Text(
            text = obstacleStatus.status.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isAlert) StatusAmberLight else StatusGreenLight,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp
          )
        }
      }

      // If Obstacle is detected: prominent warning banner
      if (isAlert) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          color = StatusAmberDim,
          border = BorderStroke(1.dp, BorderAmber)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = StatusAmberLight,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "⚠ OBSTACLE DETECTED",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = StatusAmberLight
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  obstacleStatus.detectedSector,
                  style = MaterialTheme.typography.labelSmall,
                  color = Slate400,
                  fontSize = 10.sp
                )
                Text(
                  "${obstacleStatus.detectedDistance} m",
                  style = MaterialTheme.typography.displayMedium,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = Slate100
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  "ACTION",
                  style = MaterialTheme.typography.labelSmall,
                  color = Slate400,
                  fontSize = 10.sp
                )
                Text(
                  obstacleStatus.action,
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = StatusAmberLight
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 5-direction LiDAR/Sonar Readouts
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        SonarBar(label = "FRONT", distance = obstacleStatus.frontDistance, maxDistance = 20.0, isFront = true)
        SonarBar(label = "LEFT", distance = obstacleStatus.leftDistance, maxDistance = 20.0)
        SonarBar(label = "RIGHT", distance = obstacleStatus.rightDistance, maxDistance = 20.0)
        SonarBar(label = "UP", distance = obstacleStatus.upDistance, maxDistance = 15.0)
        SonarBar(label = "DOWN", distance = obstacleStatus.downDistance, maxDistance = 10.0)
      }
    }
  }
}

@Composable
private fun SonarBar(
  label: String,
  distance: Double,
  maxDistance: Double,
  isFront: Boolean = false
) {
  val fraction = (distance / maxDistance).coerceIn(0.0, 1.0).toFloat()
  val barColor = when {
    distance < 5.0 -> StatusRedLight
    distance < 10.0 -> StatusAmberLight
    else -> PrimaryBlueLight
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label.padEnd(6, ' '),
      style = MaterialTheme.typography.labelSmall,
      fontFamily = FontFamily.Monospace,
      color = Slate400,
      fontSize = 10.sp,
      modifier = Modifier.width(52.dp)
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp))
        .background(SpecialistSurfaceVariant)
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(fraction)
          .clip(RoundedCornerShape(3.dp))
          .background(barColor)
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    Text(
      text = String.format("%.1f m", distance),
      style = MaterialTheme.typography.labelMedium,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      color = if (distance < 5.0) StatusRedLight else Slate100,
      fontSize = 11.sp,
      modifier = Modifier.width(55.dp)
    )
  }
}

