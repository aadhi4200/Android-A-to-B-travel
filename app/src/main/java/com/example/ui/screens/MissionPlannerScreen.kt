package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.GeoPoint
import com.example.ui.components.TacticalMapCanvas
import com.example.ui.theme.*
import com.example.ui.viewmodel.DroneViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionPlannerScreen(
  viewModel: DroneViewModel,
  modifier: Modifier = Modifier
) {
  val telemetry by viewModel.droneService.telemetry.collectAsState()
  val pointA by viewModel.plannerPointA.collectAsState()
  val pointB by viewModel.plannerPointB.collectAsState()
  val cruiseAlt by viewModel.cruiseAltitude.collectAsState()
  val isSelectingPointA by viewModel.isSelectingPointAOnMap.collectAsState()
  val isSelectingPointB by viewModel.isSelectingPointBOnMap.collectAsState()
  val validationResult by viewModel.validationResult.collectAsState()
  val displayConfig by viewModel.displayConfig.collectAsState()

  var isMapExpanded by remember { mutableStateOf(false) }
  var showPointACoordDialog by remember { mutableStateOf(false) }
  var showPointBCoordDialog by remember { mutableStateOf(false) }
  var showLocationSearchDialog by remember { mutableStateOf(false) }
  var missionCreatedSnackbar by remember { mutableStateOf(false) }

  val distanceKm = viewModel.calculateDistanceKm(pointA, pointB)
  val estimatedFlightSeconds = ((distanceKm * 1000.0) / 7.8).toInt() + 60
  val estimatedFlightMinutes = estimatedFlightSeconds / 60
  val estimatedFlightRemainingSec = estimatedFlightSeconds % 60
  val estimatedBatteryUsage = (distanceKm * 7.5).toInt().coerceIn(10, 45)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SpecialistBg)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "MISSION PLANNER",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Slate100
        )
        Text(
          text = "Point A → Point B Trajectory Planning",
          style = MaterialTheme.typography.bodySmall,
          color = Slate400
        )
      }

      Surface(
        shape = RoundedCornerShape(20.dp),
        color = StatusBlueDim,
        border = BorderStroke(1.dp, BorderBlue)
      ) {
        Text(
          text = "PX4 Autonomous",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = PrimaryBlueLight,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          fontSize = 11.sp
        )
      }
    }

    // Tactical Map Planner Canvas Section
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Map, contentDescription = null, tint = PrimaryBlueLight, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "TACTICAL ROUTE MAP",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Slate200,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp
          )
        }

        // Map Expansion Toggle Button
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { isMapExpanded = !isMapExpanded }
            .testTag("btn_toggle_map_expand"),
          color = SpecialistSurfaceVariant,
          border = BorderStroke(1.dp, BorderSubtle)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isMapExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
              contentDescription = null,
              tint = Slate300,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isMapExpanded) "COLLAPSE" else "EXPAND MAP",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              color = Slate300
            )
          }
        }
      }

      // Interactive Map Container
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(if (isMapExpanded) 380.dp else 260.dp)
          .animateContentSize()
          .clip(RoundedCornerShape(14.dp))
          .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
      ) {
        TacticalMapCanvas(
          droneLat = telemetry.latitude,
          droneLon = telemetry.longitude,
          droneHeading = telemetry.heading,
          droneAltitude = telemetry.altitude,
          isArmed = telemetry.armed,
          pointA = pointA,
          pointB = pointB,
          flownPath = emptyList(),
          avoidancePath = emptyList(),
          mapType = displayConfig.mapType,
          onMapTap = { point -> viewModel.handleMapTap(point) },
          isSelectingPointA = isSelectingPointA,
          isSelectingPointB = isSelectingPointB,
          onSelectPointAMode = { viewModel.toggleMapSelectPointA() },
          onSelectPointBMode = { viewModel.toggleMapSelectPointB() },
          cruiseAltitudeMeters = cruiseAlt
        )
      }

      // Quick Preset Destination Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "PRESETS:",
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          color = Slate500
        )
        viewModel.repository.presetLocations.forEach { preset ->
          val isSelected = pointB.name == preset.name
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .clickable { viewModel.setPointB(preset) }
              .testTag("preset_${preset.name.replace(" ", "_")}"),
            color = if (isSelected) StatusBlueDim else SpecialistSurfaceVariant,
            border = BorderStroke(1.dp, if (isSelected) BorderBlue else BorderSubtle)
          ) {
            Text(
              text = preset.name,
              style = MaterialTheme.typography.labelSmall,
              color = if (isSelected) PrimaryBlueLight else Slate300,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    // POINT A & POINT B SELECTORS & ROUTE SUMMARY
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // POINT A SECTION
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(PrimaryBlueLight)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "POINT A (START / LAUNCH)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlueLight
              )
            }
            Text(
              pointA.name.ifEmpty { "Start Location" },
              style = MaterialTheme.typography.bodySmall,
              color = Slate400
            )
          }

          Text(
            text = "${pointA.latitude}, ${pointA.longitude}",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Slate100
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Point A Source Options Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            PlannerOptionChip(
              label = "Drone Pos",
              icon = Icons.Default.Flight,
              selected = false,
              onClick = {
                viewModel.setPointA(GeoPoint(telemetry.latitude, telemetry.longitude, "Current Drone Pos"))
              },
              modifier = Modifier.weight(1f),
              testTag = "chip_point_a_drone"
            )
            PlannerOptionChip(
              label = "Phone GPS",
              icon = Icons.Default.Smartphone,
              selected = false,
              onClick = {
                viewModel.setPointA(GeoPoint(10.123456, 76.123456, "Phone GPS"))
              },
              modifier = Modifier.weight(1f),
              testTag = "chip_point_a_phone"
            )
            PlannerOptionChip(
              label = "Map Tap",
              icon = Icons.Default.TouchApp,
              selected = isSelectingPointA,
              onClick = { viewModel.toggleMapSelectPointA() },
              modifier = Modifier.weight(1f),
              testTag = "chip_point_a_tap"
            )
            PlannerOptionChip(
              label = "Coords",
              icon = Icons.Default.EditLocation,
              selected = false,
              onClick = { showPointACoordDialog = true },
              modifier = Modifier.weight(1f),
              testTag = "chip_point_a_coords"
            )
          }
        }

        // Distance & Quick Swap Transition
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .height(1.dp)
              .background(BorderSubtle)
          )
          Surface(
            modifier = Modifier
              .padding(horizontal = 8.dp)
              .clip(RoundedCornerShape(12.dp))
              .clickable { viewModel.swapPoints() }
              .testTag("btn_swap_points"),
            shape = RoundedCornerShape(12.dp),
            color = SpecialistSurfaceVariant,
            border = BorderStroke(1.dp, BorderBlue)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.SwapVert,
                contentDescription = "Swap Points",
                tint = PrimaryBlueLight,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "DISTANCE: $distanceKm km",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Slate100
              )
            }
          }
          Box(
            modifier = Modifier
              .weight(1f)
              .height(1.dp)
              .background(BorderSubtle)
          )
        }

        // POINT B SECTION
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(StatusGreenLight)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "POINT B (DESTINATION / LANDING)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = StatusGreenLight
              )
            }
            Text(
              pointB.name.ifEmpty { "Target Location" },
              style = MaterialTheme.typography.bodySmall,
              color = Slate400
            )
          }

          Text(
            text = "${pointB.latitude}, ${pointB.longitude}",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Slate100
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Point B Source Options Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            PlannerOptionChip(
              label = "Map Tap",
              icon = Icons.Default.TouchApp,
              selected = isSelectingPointB,
              onClick = { viewModel.toggleMapSelectPointB() },
              modifier = Modifier.weight(1f),
              testTag = "chip_point_b_tap"
            )
            PlannerOptionChip(
              label = "Search Preset",
              icon = Icons.Default.Search,
              selected = false,
              onClick = { showLocationSearchDialog = true },
              modifier = Modifier.weight(1.2f),
              testTag = "chip_point_b_search"
            )
            PlannerOptionChip(
              label = "Coords",
              icon = Icons.Default.EditLocation,
              selected = false,
              onClick = { showPointBCoordDialog = true },
              modifier = Modifier.weight(1f),
              testTag = "chip_point_b_coords"
            )
          }
        }
      }
    }

    // MISSION ESTIMATIONS CARD (Distance, Est Time, Cruise Alt, Est Battery)
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
        .testTag("card_mission_estimations"),
      color = SpecialistCardBg
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            "MISSION ESTIMATIONS & TELEMETRY PROFILE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlueLight,
            letterSpacing = 1.sp,
            fontSize = 11.sp
          )
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = StatusGreenDim,
            border = BorderStroke(1.dp, BorderGreen)
          ) {
            Text(
              "GEOFENCE SAFE",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = StatusGreenLight,
              fontSize = 9.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          EstimationMetric("Distance", "$distanceKm km", PrimaryBlueLight)
          EstimationMetric("Flight Time", "${estimatedFlightMinutes}m ${estimatedFlightRemainingSec}s", Slate100)
          EstimationMetric("Cruise Altitude", "$cruiseAlt m", StatusAmberLight)
          EstimationMetric("Battery Usage", "$estimatedBatteryUsage%", StatusGreenLight)
        }

        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

        // Cruise Altitude Slider
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Adjust Cruise Altitude:", style = MaterialTheme.typography.bodySmall, color = Slate400)
            Text("${cruiseAlt}m AGL", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = PrimaryBlueLight)
          }
          Slider(
            value = cruiseAlt.toFloat(),
            onValueChange = { viewModel.setCruiseAltitude(it.toDouble()) },
            valueRange = 10f..60f,
            steps = 9,
            colors = SliderDefaults.colors(
              thumbColor = PrimaryBlue,
              activeTrackColor = PrimaryBlue,
              inactiveTrackColor = SpecialistSurfaceVariant
            )
          )
        }
      }
    }

    // VALIDATION RESULT BANNER (if validated)
    if (validationResult != null) {
      val res = validationResult!!
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = StatusGreenDim,
        border = BorderStroke(1.dp, BorderGreen)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreenLight, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(res.message, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StatusGreenLight)
          }
          Spacer(modifier = Modifier.height(6.dp))
          res.checksPassed.forEach { check ->
            Text("• $check", style = MaterialTheme.typography.bodySmall, color = Slate300)
          }
        }
      }
    }

    // ACTION BUTTONS (VALIDATE MISSION, CREATE MISSION, START MISSION)
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Validate Mission Button
        OutlinedButton(
          onClick = { viewModel.validateMission() },
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("btn_validate_mission"),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlueLight),
          border = BorderStroke(1.dp, BorderBlue),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("VALIDATE", fontWeight = FontWeight.Bold)
        }

        // Create Mission Button
        OutlinedButton(
          onClick = {
            viewModel.createMissionPlan()
            missionCreatedSnackbar = true
          },
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("btn_create_mission"),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate200),
          border = BorderStroke(1.dp, BorderMedium),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("CREATE PLAN", fontWeight = FontWeight.Bold)
        }
      }

      // START MISSION Button
      Button(
        onClick = { viewModel.startPlannedMission() },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_start_mission"),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color.White),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          "START MISSION",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }
    }
  }

  // Preset Location Search Dialog
  if (showLocationSearchDialog) {
    AlertDialog(
      onDismissRequest = { showLocationSearchDialog = false },
      title = { Text("Select Destination Preset", color = Slate100) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          viewModel.repository.presetLocations.forEach { preset ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  viewModel.setPointB(preset)
                  showLocationSearchDialog = false
                },
              color = SpecialistSurfaceVariant
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(preset.name, style = MaterialTheme.typography.titleSmall, color = Slate100)
                Text("${preset.latitude}, ${preset.longitude}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = PrimaryBlueLight)
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showLocationSearchDialog = false }) {
          Text("CLOSE", color = PrimaryBlueLight)
        }
      },
      containerColor = SpecialistCardBg,
      shape = RoundedCornerShape(16.dp)
    )
  }

  // Manual Coordinate Input Dialog for Point A
  if (showPointACoordDialog) {
    CoordinateInputDialog(
      title = "Set Point A Coordinates",
      initialLat = pointA.latitude,
      initialLon = pointA.longitude,
      onConfirm = { lat, lon ->
        viewModel.setPointA(GeoPoint(lat, lon, "Manual Point A"))
        showPointACoordDialog = false
      },
      onDismiss = { showPointACoordDialog = false }
    )
  }

  // Manual Coordinate Input Dialog for Point B
  if (showPointBCoordDialog) {
    CoordinateInputDialog(
      title = "Set Point B Coordinates",
      initialLat = pointB.latitude,
      initialLon = pointB.longitude,
      onConfirm = { lat, lon ->
        viewModel.setPointB(GeoPoint(lat, lon, "Manual Point B"))
        showPointBCoordDialog = false
      },
      onDismiss = { showPointBCoordDialog = false }
    )
  }
}

@Composable
private fun PlannerOptionChip(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "chip_option"
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .testTag(testTag),
    color = if (selected) PrimaryBlue else SpecialistSurfaceVariant,
    border = BorderStroke(1.dp, if (selected) PrimaryBlueLight else BorderSubtle)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) Color.White else Slate300,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(3.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = if (selected) Color.White else Slate300
      )
    }
  }
}

@Composable
private fun EstimationMetric(
  label: String,
  value: String,
  color: Color
) {
  Column(horizontalAlignment = Alignment.Start) {
    Text(label, style = MaterialTheme.typography.bodySmall, color = Slate500, fontSize = 10.sp)
    Text(
      value,
      style = MaterialTheme.typography.labelMedium,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      color = color
    )
  }
}

@Composable
private fun CoordinateInputDialog(
  title: String,
  initialLat: Double,
  initialLon: Double,
  onConfirm: (Double, Double) -> Unit,
  onDismiss: () -> Unit
) {
  var latText by remember { mutableStateOf(initialLat.toString()) }
  var lonText by remember { mutableStateOf(initialLon.toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title, color = Slate100) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = latText,
          onValueChange = { latText = it },
          label = { Text("Latitude") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_latitude")
        )
        OutlinedTextField(
          value = lonText,
          onValueChange = { lonText = it },
          label = { Text("Longitude") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_longitude")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val lat = latText.toDoubleOrNull() ?: initialLat
          val lon = lonText.toDoubleOrNull() ?: initialLon
          onConfirm(lat, lon)
        },
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color.White)
      ) {
        Text("SET COORDINATES", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("CANCEL", color = Slate400)
      }
    },
    containerColor = SpecialistCardBg,
    shape = RoundedCornerShape(16.dp)
  )
}
