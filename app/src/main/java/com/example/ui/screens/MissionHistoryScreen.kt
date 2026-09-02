package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.MissionHistoryItem
import com.example.data.model.MissionHistoryStatus
import com.example.ui.components.EventTimelineView
import com.example.ui.theme.*
import com.example.ui.viewmodel.DroneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionHistoryScreen(
  viewModel: DroneViewModel,
  modifier: Modifier = Modifier
) {
  val historyList by viewModel.historyList.collectAsState()
  val selectedItem by viewModel.selectedHistoryItem.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SpecialistBg)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Title
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "MISSION HISTORY",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Slate100
        )
        Text(
          text = "Logs & Telemetry Archive",
          style = MaterialTheme.typography.bodySmall,
          color = Slate400
        )
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = StatusBlueDim,
        border = BorderStroke(1.dp, BorderBlue)
      ) {
        Text(
          text = "${historyList.size} RECORDED",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = PrimaryBlueLight,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          fontSize = 11.sp
        )
      }
    }

    // List of Mission Cards
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(historyList, key = { it.id }) { item ->
        MissionHistoryCard(
          item = item,
          onClick = { viewModel.openHistoryItem(item) }
        )
      }
    }
  }

  // Mission Details Inspector Modal Sheet
  if (selectedItem != null) {
    val item = selectedItem!!
    ModalBottomSheet(
      onDismissRequest = { viewModel.openHistoryItem(null) },
      containerColor = SpecialistCardBg,
      shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
      dragHandle = {
        Box(
          modifier = Modifier
            .padding(vertical = 10.dp)
            .width(40.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(BorderMedium)
        )
      }
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Modal Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = item.id,
              style = MaterialTheme.typography.labelMedium,
              fontFamily = FontFamily.Monospace,
              color = PrimaryBlueLight
            )
            Text(
              text = "Mission Details: A → B",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Slate100
            )
          }

          StatusBadge(status = item.status)
        }

        // Key Metric Summary Grid
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
          color = SpecialistSurfaceVariant
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              MetricItem("Distance", "${item.distanceKm} km", PrimaryBlueLight)
              MetricItem("Duration", item.durationFormatted, Slate100)
              MetricItem("Battery Used", "${item.batteryConsumedPercent}%", StatusAmberLight)
              MetricItem("Landing", item.landingStatus, StatusGreenLight)
            }

            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              MetricItem("Point A", item.pointA.name.ifEmpty { "Start" }, Slate400)
              MetricItem("Point B", item.pointB.name.ifEmpty { "Destination" }, Slate400)
              MetricItem("Obstacles", "${item.obstacleEventsCount} Avoided", if (item.obstacleEventsCount > 0) StatusAmberLight else Slate400)
            }
          }
        }

        // Full Chronological Event Timeline
        EventTimelineView(events = item.events)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = { viewModel.openHistoryItem(null) },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          colors = ButtonDefaults.buttonColors(containerColor = SpecialistSurfaceVariant, contentColor = Slate200),
          border = BorderStroke(1.dp, BorderMedium),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("CLOSE LOGS", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun MissionHistoryCard(
  item: MissionHistoryItem,
  onClick: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .testTag("history_card_${item.id}"),
    color = SpecialistCardBg
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "A → B",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlueLight
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "• ${item.dateFormatted}",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
          )
        }

        StatusBadge(status = item.status)
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          Text(
            text = "${item.distanceKm} km",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Slate100
          )
          Text(
            text = item.durationFormatted,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400
          )
          Text(
            text = "Bat: ${item.batteryConsumedPercent}%",
            style = MaterialTheme.typography.bodyMedium,
            color = StatusAmberLight
          )
        }

        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = Slate500,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
private fun StatusBadge(status: MissionHistoryStatus) {
  val (bgColor, textColor, borderColor) = when (status) {
    MissionHistoryStatus.COMPLETED -> Triple(StatusGreenDim, StatusGreenLight, BorderGreen)
    MissionHistoryStatus.ABORTED -> Triple(StatusRedDim, StatusRedLight, BorderRed)
    MissionHistoryStatus.FAILED -> Triple(StatusRedDim, StatusRedLight, BorderRed)
    MissionHistoryStatus.RTL -> Triple(StatusAmberDim, StatusAmberLight, BorderAmber)
    MissionHistoryStatus.EMERGENCY -> Triple(StatusRedDim, StatusRedLight, BorderRed)
  }

  Surface(
    shape = RoundedCornerShape(4.dp),
    color = bgColor,
    border = BorderStroke(1.dp, borderColor)
  ) {
    Text(
      text = status.label,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = textColor,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
    )
  }
}

@Composable
private fun MetricItem(label: String, value: String, color: Color) {
  Column {
    Text(label, style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp)
    Text(
      value,
      style = MaterialTheme.typography.bodySmall,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      color = color
    )
  }
}

