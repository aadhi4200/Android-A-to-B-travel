package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LandingSequenceState
import com.example.ui.theme.*

@Composable
fun LandingSequenceCard(
  landingSequence: LandingSequenceState,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .border(1.5.dp, StatusSuccess, RoundedCornerShape(12.dp))
      .testTag("landing_sequence_card"),
    color = AerospaceCardBg
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "DESTINATION REACHED",
            style = MaterialTheme.typography.labelSmall,
            color = StatusSuccess,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "LANDING SEQUENCE",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }

        Surface(
          shape = RoundedCornerShape(4.dp),
          color = StatusSuccessDim
        ) {
          Text(
            text = if (landingSequence.isTouchdownDone) "TOUCHDOWN ✓" else "AUTONOMOUS LANDING",
            style = MaterialTheme.typography.labelSmall,
            color = StatusSuccess,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 5 Steps
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        LandingStepRow(
          title = "Positioning",
          isDone = landingSequence.isPositioningDone,
          isActive = landingSequence.activeStepIndex == 0
        )
        LandingStepRow(
          title = "Marker Detection",
          isDone = landingSequence.isMarkerDetectedDone,
          isActive = landingSequence.activeStepIndex == 1
        )
        LandingStepRow(
          title = "Alignment",
          isDone = landingSequence.isAlignmentDone,
          isActive = landingSequence.activeStepIndex == 2
        )
        LandingStepRow(
          title = "Descent",
          isDone = landingSequence.isTouchdownDone,
          isActive = landingSequence.isDescentInProgress
        )
        LandingStepRow(
          title = "Touchdown",
          isDone = landingSequence.isTouchdownDone,
          isActive = landingSequence.activeStepIndex == 4 && !landingSequence.isTouchdownDone
        )
      }
    }
  }
}

@Composable
private fun LandingStepRow(
  title: String,
  isDone: Boolean,
  isActive: Boolean
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (isDone) {
      Box(
        modifier = Modifier
          .size(18.dp)
          .clip(CircleShape)
          .background(StatusSuccess),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = AerospaceBg,
          modifier = Modifier.size(12.dp)
        )
      }
    } else if (isActive) {
      Box(
        modifier = Modifier
          .size(18.dp)
          .clip(CircleShape)
          .background(CyanNeon),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(AerospaceBg)
        )
      }
    } else {
      Box(
        modifier = Modifier
          .size(18.dp)
          .clip(CircleShape)
          .border(1.5.dp, TextMuted, CircleShape)
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    Text(
      text = title,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
      color = when {
        isDone -> StatusSuccess
        isActive -> CyanNeon
        else -> TextSecondary
      }
    )

    if (isActive) {
      Spacer(modifier = Modifier.weight(1f))
      Text(
        text = "IN PROGRESS...",
        style = MaterialTheme.typography.labelSmall,
        color = CyanNeon,
        fontSize = 9.sp
      )
    }
  }
}
