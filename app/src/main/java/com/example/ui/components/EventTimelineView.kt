package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.EventType
import com.example.data.model.MissionEvent
import com.example.ui.theme.*

@Composable
fun EventTimelineView(
  events: List<MissionEvent>,
  modifier: Modifier = Modifier,
  maxItems: Int? = null
) {
  val displayEvents = if (maxItems != null) events.take(maxItems) else events

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
      .testTag("event_timeline_view"),
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
          text = "MISSION TIMELINE",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = PrimaryBlueLight,
          letterSpacing = 1.sp,
          fontSize = 11.sp
        )
        Text(
          text = "${events.size} EVENTS",
          style = MaterialTheme.typography.labelSmall,
          color = Slate500,
          fontSize = 10.sp
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (displayEvents.isEmpty()) {
        Text(
          text = "No events recorded yet. Ready for mission start.",
          style = MaterialTheme.typography.bodySmall,
          color = Slate500,
          modifier = Modifier.padding(vertical = 12.dp)
        )
      } else {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          displayEvents.forEachIndexed { index, event ->
            EventTimelineItem(
              event = event,
              isFirst = index == 0,
              isLast = index == displayEvents.size - 1
            )
          }
        }
      }
    }
  }
}

@Composable
private fun EventTimelineItem(
  event: MissionEvent,
  isFirst: Boolean,
  isLast: Boolean
) {
  val (dotColor, textColor) = when (event.type) {
    EventType.INFO -> Pair(PrimaryBlueLight, Slate100)
    EventType.ACTION -> Pair(PrimaryBlue, Slate100)
    EventType.WARNING -> Pair(StatusAmberLight, StatusAmberLight)
    EventType.SUCCESS -> Pair(StatusGreenLight, Slate100)
    EventType.ERROR -> Pair(StatusRedLight, StatusRedLight)
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Timestamp
    Text(
      text = event.timestamp,
      style = MaterialTheme.typography.labelSmall,
      fontFamily = FontFamily.Monospace,
      color = Slate500,
      fontSize = 11.sp,
      modifier = Modifier.width(62.dp)
    )

    // Dot indicator
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(dotColor)
    )

    Spacer(modifier = Modifier.width(10.dp))

    // Event description
    Text(
      text = event.message,
      style = MaterialTheme.typography.bodyMedium,
      color = textColor,
      fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Normal,
      modifier = Modifier.weight(1f)
    )
  }
}

