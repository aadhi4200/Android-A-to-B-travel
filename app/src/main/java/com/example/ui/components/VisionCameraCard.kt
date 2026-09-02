package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ArucoDetectionState
import com.example.data.model.VisionStatus
import com.example.ui.theme.*

@Composable
fun VisionCameraCard(
  visionStatus: VisionStatus,
  modifier: Modifier = Modifier
) {
  val isMarkerDetected = visionStatus.arucoState != ArucoDetectionState.SEARCHING

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(
        width = 1.dp,
        color = if (isMarkerDetected) BorderGreen else BorderSubtle,
        shape = RoundedCornerShape(14.dp)
      )
      .testTag("vision_camera_card"),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = null,
            tint = PrimaryBlueLight,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "FPV VISION & ARUCO",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlueLight,
            letterSpacing = 1.sp,
            fontSize = 11.sp
          )
        }

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = StatusGreenDim,
          border = BorderStroke(1.dp, BorderGreen)
        ) {
          Text(
            text = "ONLINE • ${visionStatus.fps} FPS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = StatusGreenLight,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // FPV Camera Viewport with Synthetic HUD overlay
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(120.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF07080D))
          .border(1.dp, BorderMedium, RoundedCornerShape(8.dp))
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val cx = w / 2f
          val cy = h / 2f

          // Artificial Horizon Line
          drawLine(
            color = Color(0x663B82F6),
            start = Offset(cx - 50f, cy),
            end = Offset(cx - 15f, cy),
            strokeWidth = 1.5f
          )
          drawLine(
            color = Color(0x663B82F6),
            start = Offset(cx + 15f, cy),
            end = Offset(cx + 50f, cy),
            strokeWidth = 1.5f
          )
          drawCircle(color = PrimaryBlue, radius = 3f, center = Offset(cx, cy))

          // Pitch Ladder
          drawLine(
            color = Color(0x333B82F6),
            start = Offset(cx - 25f, cy - 25f),
            end = Offset(cx + 25f, cy - 25f),
            strokeWidth = 1f
          )
          drawLine(
            color = Color(0x333B82F6),
            start = Offset(cx - 25f, cy + 25f),
            end = Offset(cx + 25f, cy + 25f),
            strokeWidth = 1f
          )

          // If ArUco landing marker detected: Draw targeted tracking bounding box
          if (isMarkerDetected) {
            val boxSize = 54f
            val boxLeft = cx - boxSize / 2f
            val boxTop = cy - boxSize / 2f + 5f

            // Bounding box corners
            val cornerLen = 12f
            val boxColor = if (visionStatus.arucoState == ArucoDetectionState.LOCKED) StatusGreenLight else StatusAmberLight

            // Top-left
            drawLine(color = boxColor, start = Offset(boxLeft, boxTop), end = Offset(boxLeft + cornerLen, boxTop), strokeWidth = 2f)
            drawLine(color = boxColor, start = Offset(boxLeft, boxTop), end = Offset(boxLeft, boxTop + cornerLen), strokeWidth = 2f)
            // Top-right
            drawLine(color = boxColor, start = Offset(boxLeft + boxSize, boxTop), end = Offset(boxLeft + boxSize - cornerLen, boxTop), strokeWidth = 2f)
            drawLine(color = boxColor, start = Offset(boxLeft + boxSize, boxTop), end = Offset(boxLeft + boxSize, boxTop + cornerLen), strokeWidth = 2f)
            // Bottom-left
            drawLine(color = boxColor, start = Offset(boxLeft, boxTop + boxSize), end = Offset(boxLeft + cornerLen, boxTop + boxSize), strokeWidth = 2f)
            drawLine(color = boxColor, start = Offset(boxLeft, boxTop + boxSize), end = Offset(boxLeft, boxTop + boxSize - cornerLen), strokeWidth = 2f)
            // Bottom-right
            drawLine(color = boxColor, start = Offset(boxLeft + boxSize, boxTop + boxSize), end = Offset(boxLeft + boxSize - cornerLen, boxTop + boxSize), strokeWidth = 2f)
            drawLine(color = boxColor, start = Offset(boxLeft + boxSize, boxTop + boxSize), end = Offset(boxLeft + boxSize, boxTop + boxSize - cornerLen), strokeWidth = 2f)

            // Inner target crosshair
            drawLine(
              color = boxColor.copy(alpha = 0.6f),
              start = Offset(cx - 8f, cy + 5f),
              end = Offset(cx + 8f, cy + 5f),
              strokeWidth = 1f
            )
            drawLine(
              color = boxColor.copy(alpha = 0.6f),
              start = Offset(cx, cy - 3f),
              end = Offset(cx, cy + 13f),
              strokeWidth = 1f
            )
          }
        }

        // FPV Top Overlay
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            "REC ● 1080P",
            style = MaterialTheme.typography.labelSmall,
            color = StatusRedLight,
            fontSize = 9.sp
          )
          Text(
            "VISION: ACTIVE",
            style = MaterialTheme.typography.labelSmall,
            color = StatusGreenLight,
            fontSize = 9.sp
          )
        }

        // FPV Bottom Overlay
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(8.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            "ARUCO: ${visionStatus.arucoState.label}",
            style = MaterialTheme.typography.labelSmall,
            color = if (isMarkerDetected) StatusGreenLight else StatusAmberLight,
            fontSize = 9.sp
          )
          if (isMarkerDetected) {
            Text(
              "ID: #${visionStatus.markerId} • ${(visionStatus.landingMarkerDistance * 10).toInt() / 10.0}m",
              style = MaterialTheme.typography.labelSmall,
              color = PrimaryBlueLight,
              fontSize = 9.sp
            )
          }
        }
      }

      // Marker Detected Information Banner
      if (isMarkerDetected) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          color = StatusGreenDim,
          border = BorderStroke(1.dp, BorderGreen)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("LANDING MARKER", style = MaterialTheme.typography.labelSmall, color = Slate400, fontSize = 9.sp)
              Text("DETECTED (ARUCO #${visionStatus.markerId})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = StatusGreenLight)
            }

            Column(horizontalAlignment = Alignment.End) {
              Text("DISTANCE / ALIGNMENT", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp)
              Text(
                "${visionStatus.landingMarkerDistance} m • ${visionStatus.landingAlignment}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Slate100
              )
            }
          }
        }
      }
    }
  }
}
