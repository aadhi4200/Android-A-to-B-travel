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
import com.example.data.model.SlamStatus
import com.example.data.model.SlamTrackingState
import com.example.ui.theme.*

@Composable
fun SlamStatusCard(
  slamStatus: SlamStatus,
  modifier: Modifier = Modifier
) {
  val isDegraded = slamStatus.status == SlamTrackingState.DEGRADED || slamStatus.confidence < 70

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(
        width = 1.dp,
        color = if (isDegraded) BorderAmber else BorderSubtle,
        shape = RoundedCornerShape(14.dp)
      )
      .testTag("slam_status_card"),
    color = SpecialistCardBg
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "SLAM LOCALIZATION",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = PrimaryBlueLight,
          letterSpacing = 1.sp,
          fontSize = 11.sp
        )

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (isDegraded) StatusAmberDim else StatusGreenDim,
          border = BorderStroke(
            1.dp,
            if (isDegraded) BorderAmber else BorderGreen
          )
        ) {
          Text(
            text = slamStatus.status.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isDegraded) StatusAmberLight else StatusGreenLight,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp
          )
        }
      }

      // Warning Banner if degraded
      if (isDegraded) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          color = StatusAmberDim,
          border = BorderStroke(1.dp, BorderAmber)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = StatusAmberLight,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "⚠ SLAM LOCALIZATION DEGRADED",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = StatusAmberLight
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4 Metrics Grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Status
        Column(modifier = Modifier.weight(1f)) {
          Text("STATUS", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp)
          Text(
            slamStatus.status.label,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Slate100
          )
        }

        // Localization
        Column(modifier = Modifier.weight(1f)) {
          Text("LOCALIZATION", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp)
          Text(
            slamStatus.localization,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (slamStatus.localization == "GOOD") StatusGreenLight else StatusAmberLight
          )
        }

        // Confidence
        Column(modifier = Modifier.weight(1f)) {
          Text("CONFIDENCE", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp)
          Text(
            "${slamStatus.confidence}%",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlueLight
          )
        }

        // Map / Pose
        Column(modifier = Modifier.weight(1f)) {
          Text("MAP / POSE", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp)
          Text(
            if (slamStatus.mapActive) "ACTIVE / ${slamStatus.poseState}" else "INACTIVE",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Slate100
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Confidence progress bar
      LinearProgressIndicator(
        progress = { (slamStatus.confidence / 100f).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp)),
        color = if (isDegraded) StatusAmber else PrimaryBlue,
        trackColor = SpecialistSurfaceVariant
      )
    }
  }
}

