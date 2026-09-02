package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MissionState
import com.example.ui.theme.*

@Composable
fun SafetyControls(
  missionState: MissionState,
  onPause: () -> Unit,
  onResume: () -> Unit,
  onRtl: () -> Unit,
  onAbort: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showAbortDialog by remember { mutableStateOf(false) }
  var showRtlDialog by remember { mutableStateOf(false) }

  val isNavigating = missionState.isNavigating
  val isPaused = missionState == MissionState.PAUSED
  val isIdleOrLanded = missionState == MissionState.IDLE || missionState == MissionState.LANDED

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Pause / Resume Button
      if (isPaused) {
        Button(
          onClick = onResume,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("btn_resume_mission"),
          colors = ButtonDefaults.buttonColors(
            containerColor = StatusGreen,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("RESUME", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
      } else {
        OutlinedButton(
          onClick = onPause,
          enabled = isNavigating,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("btn_pause_mission"),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isNavigating) StatusAmberLight else Slate500
          ),
          border = BorderStroke(1.dp, if (isNavigating) BorderAmber else BorderSubtle),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("PAUSE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
      }

      // Return to Home (RTL) Button
      OutlinedButton(
        onClick = { showRtlDialog = true },
        enabled = isNavigating || isPaused,
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .testTag("btn_rtl_mission"),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = if (isNavigating || isPaused) PrimaryBlueLight else Slate500
        ),
        border = BorderStroke(1.dp, if (isNavigating || isPaused) PrimaryBlueLight else BorderSubtle),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("RETURN HOME", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
      }
    }

    // Full Width Abort Button
    Button(
      onClick = { showAbortDialog = true },
      enabled = !isIdleOrLanded,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("btn_abort_mission"),
      colors = ButtonDefaults.buttonColors(
        containerColor = if (!isIdleOrLanded) StatusRed else SpecialistSurfaceVariant,
        contentColor = if (!isIdleOrLanded) Color.White else Slate500
      ),
      shape = RoundedCornerShape(10.dp)
    ) {
      Icon(Icons.Default.Dangerous, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        "ABORT MISSION",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
    }
  }

  // Abort Confirmation Dialog
  if (showAbortDialog) {
    AlertDialog(
      onDismissRequest = { showAbortDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRedLight)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Abort autonomous mission?", color = Slate100)
        }
      },
      text = {
        Text(
          "The drone will execute the configured failsafe procedure (immediate emergency hover and controlled descent). Flight-critical safety is executed onboard.",
          color = Slate400,
          style = MaterialTheme.typography.bodyMedium
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showAbortDialog = false
            onAbort()
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = Color.White),
          modifier = Modifier.testTag("btn_confirm_abort")
        ) {
          Text("ABORT", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { showAbortDialog = false },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
          border = BorderStroke(1.dp, BorderMedium),
          modifier = Modifier.testTag("btn_cancel_abort")
        ) {
          Text("CANCEL")
        }
      },
      containerColor = SpecialistCardBg,
      shape = RoundedCornerShape(16.dp)
    )
  }

  // RTL Confirmation Dialog
  if (showRtlDialog) {
    AlertDialog(
      onDismissRequest = { showRtlDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Home, contentDescription = null, tint = PrimaryBlueLight)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Return to Home (RTL)?", color = Slate100)
        }
      },
      text = {
        Text(
          "The drone will cancel the current mission plan and autonomously navigate back to the launch vertiport (Point A) at cruising altitude.",
          color = Slate400,
          style = MaterialTheme.typography.bodyMedium
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showRtlDialog = false
            onRtl()
          },
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color.White),
          modifier = Modifier.testTag("btn_confirm_rtl")
        ) {
          Text("CONFIRM RTL", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { showRtlDialog = false },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
          border = BorderStroke(1.dp, BorderMedium)
        ) {
          Text("CANCEL")
        }
      },
      containerColor = SpecialistCardBg,
      shape = RoundedCornerShape(16.dp)
    )
  }
}

