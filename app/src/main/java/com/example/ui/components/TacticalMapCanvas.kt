package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeoPoint
import com.example.data.model.MapVisualType
import com.example.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun TacticalMapCanvas(
  modifier: Modifier = Modifier,
  droneLat: Double,
  droneLon: Double,
  droneHeading: Float,
  droneAltitude: Double,
  isArmed: Boolean,
  pointA: GeoPoint?,
  pointB: GeoPoint?,
  flownPath: List<GeoPoint> = emptyList(),
  avoidancePath: List<GeoPoint> = emptyList(),
  mapType: MapVisualType = MapVisualType.TACTICAL_GRID,
  onMapTap: ((GeoPoint) -> Unit)? = null,
  isSelectingPointA: Boolean = false,
  isSelectingPointB: Boolean = false,
  onSelectPointAMode: (() -> Unit)? = null,
  onSelectPointBMode: (() -> Unit)? = null,
  cruiseAltitudeMeters: Double = 32.4
) {
  var zoomLevel by remember { mutableFloatStateOf(1.0f) }
  var panOffsetX by remember { mutableFloatStateOf(0f) }
  var panOffsetY by remember { mutableFloatStateOf(0f) }
  var autoFollowDrone by remember { mutableStateOf(false) }

  val textMeasurer = rememberTextMeasurer()

  // Base reference coordinate (centered around Point A or Point B or default location)
  val baseCenterLat = pointA?.latitude ?: pointB?.latitude ?: 10.123456
  val baseCenterLon = pointA?.longitude ?: pointB?.longitude ?: 76.123456

  // Calculate distance between A and B
  val calculatedDistanceKm = remember(pointA, pointB) {
    if (pointA != null && pointB != null) {
      val lat1 = Math.toRadians(pointA.latitude)
      val lon1 = Math.toRadians(pointA.longitude)
      val lat2 = Math.toRadians(pointB.latitude)
      val lon2 = Math.toRadians(pointB.longitude)
      val dlat = lat2 - lat1
      val dlon = lon2 - lon1
      val sinLat = sin(dlat / 2.0)
      val sinLon = sin(dlon / 2.0)
      val comp = sinLat * sinLat + cos(lat1) * cos(lat2) * sinLon * sinLon
      val c = 2.0 * atan2(sqrt(comp), sqrt(1.0 - comp))
      ((6371.0 * c) * 100).roundToInt() / 100.0
    } else 0.0
  }

  val estimatedFlightSeconds = remember(calculatedDistanceKm) {
    if (calculatedDistanceKm > 0.0) ((calculatedDistanceKm * 1000.0) / 7.8).toInt() + 60 else 0
  }
  val estimatedFlightMinutes = estimatedFlightSeconds / 60
  val estimatedFlightRemainingSec = estimatedFlightSeconds % 60
  val estimatedBatteryPercent = remember(calculatedDistanceKm) {
    if (calculatedDistanceKm > 0.0) (calculatedDistanceKm * 7.5).toInt().coerceIn(10, 45) else 0
  }

  // Function to center and fit route in view
  fun fitRouteInView(canvasWidth: Float, canvasHeight: Float) {
    if (pointA != null && pointB != null) {
      val midLat = (pointA.latitude + pointB.latitude) / 2.0
      val midLon = (pointA.longitude + pointB.longitude) / 2.0

      val dyMidMeters = (midLat - baseCenterLat) * 111000.0
      val dxMidMeters = (midLon - baseCenterLon) * (111000.0 * cos(Math.toRadians(baseCenterLat)))

      val distMeters = calculatedDistanceKm * 1000.0
      val requiredSpan = (distMeters * 1.5).coerceAtLeast(300.0)
      val targetZoom = ((min(canvasWidth, canvasHeight) * 10.0f) / requiredSpan).toFloat().coerceIn(0.5f, 3.0f)

      zoomLevel = targetZoom
      val pixelsPerMeter = zoomLevel / 10.0f
      panOffsetX = -(dxMidMeters * pixelsPerMeter).toFloat()
      panOffsetY = (dyMidMeters * pixelsPerMeter).toFloat()
      autoFollowDrone = false
    } else {
      panOffsetX = 0f
      panOffsetY = 0f
      zoomLevel = 1.0f
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        when (mapType) {
          MapVisualType.TACTICAL_GRID -> SpecialistBg
          MapVisualType.SATELLITE -> Color(0xFF090D14)
          MapVisualType.DARK_VECTOR -> Color(0xFF05070B)
        }
      )
      // Multi-touch gestures: Pinch zoom + multi-finger pan
      .pointerInput(Unit) {
        detectTransformGestures(panZoomLock = false) { _, pan, zoom, _ ->
          zoomLevel = (zoomLevel * zoom).coerceIn(0.4f, 4.5f)
          panOffsetX += pan.x
          panOffsetY += pan.y
          autoFollowDrone = false
        }
      }
      // Tap gesture: Place Point A or Point B
      .pointerInput(isSelectingPointA, isSelectingPointB, zoomLevel, panOffsetX, panOffsetY, autoFollowDrone) {
        detectTapGestures { tapOffset ->
          if (onMapTap != null) {
            val cx = size.width / 2f + (if (autoFollowDrone) 0f else panOffsetX)
            val cy = size.height / 2f + (if (autoFollowDrone) 0f else panOffsetY)
            val metersPerPixel = 10.0 / zoomLevel
            val dxMeters = (tapOffset.x - cx) * metersPerPixel
            val dyMeters = (cy - tapOffset.y) * metersPerPixel

            val newLat = baseCenterLat + (dyMeters / 111000.0)
            val newLon = baseCenterLon + (dxMeters / (111000.0 * cos(Math.toRadians(baseCenterLat))))
            val formattedLat = (newLat * 1000000).roundToInt() / 1000000.0
            val formattedLon = (newLon * 1000000).roundToInt() / 1000000.0

            val waypointLabel = if (isSelectingPointA) "Point A" else if (isSelectingPointB) "Point B" else "Selected Pin"
            onMapTap(GeoPoint(formattedLat, formattedLon, waypointLabel))
          }
        }
      }
      .testTag("tactical_map_canvas")
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvasWidth = size.width
      val canvasHeight = size.height

      val centerX = canvasWidth / 2f + (if (autoFollowDrone) 0f else panOffsetX)
      val centerY = canvasHeight / 2f + (if (autoFollowDrone) 0f else panOffsetY)

      // 1. Draw Grid & Radar Rings
      drawTacticalGrid(
        centerX = centerX,
        centerY = centerY,
        width = canvasWidth,
        height = canvasHeight,
        zoom = zoomLevel,
        mapType = mapType,
        textMeasurer = textMeasurer
      )

      // Coordinate converter helper
      fun geoToScreen(lat: Double, lon: Double): Offset {
        val dyMeters = (lat - baseCenterLat) * 111000.0
        val dxMeters = (lon - baseCenterLon) * (111000.0 * cos(Math.toRadians(baseCenterLat)))
        val pixelsPerMeter = zoomLevel / 10.0f
        val sx = centerX + (dxMeters * pixelsPerMeter).toFloat()
        val sy = centerY - (dyMeters * pixelsPerMeter).toFloat()
        return Offset(sx, sy)
      }

      // 2. Draw Geofence Boundary (2500m radius around Home/Point A)
      if (pointA != null) {
        val centerScreen = geoToScreen(pointA.latitude, pointA.longitude)
        val geofenceRadiusPixels = (2500.0 * (zoomLevel / 10.0f)).toFloat()
        drawCircle(
          color = Color(0x103B82F6),
          radius = geofenceRadiusPixels,
          center = centerScreen
        )
        drawCircle(
          color = Color(0x403B82F6),
          radius = geofenceRadiusPixels,
          center = centerScreen,
          style = Stroke(
            width = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
          )
        )
      }

      // 3. Draw Planned Route Line (Point A -> Point B)
      if (pointA != null && pointB != null) {
        val startOffset = geoToScreen(pointA.latitude, pointA.longitude)
        val endOffset = geoToScreen(pointB.latitude, pointB.longitude)

        // Outer ambient glow line
        drawLine(
          color = PrimaryBlue.copy(alpha = 0.35f),
          start = startOffset,
          end = endOffset,
          strokeWidth = 10f,
          cap = StrokeCap.Round
        )

        // Dashed tactical planned route
        drawLine(
          color = PrimaryBlueLight,
          start = startOffset,
          end = endOffset,
          strokeWidth = 3.5f,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f),
          cap = StrokeCap.Round
        )

        // Directional flight arrows along the trajectory from Point A to Point B
        val dx = endOffset.x - startOffset.x
        val dy = endOffset.y - startOffset.y
        val angleRad = atan2(dy, dx)
        val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()

        listOf(0.30f, 0.50f, 0.70f).forEach { fraction ->
          val arrowCenter = Offset(
            startOffset.x + dx * fraction,
            startOffset.y + dy * fraction
          )
          rotate(degrees = angleDeg, pivot = arrowCenter) {
            val arrowPath = Path().apply {
              moveTo(arrowCenter.x + 8f, arrowCenter.y)
              lineTo(arrowCenter.x - 6f, arrowCenter.y - 6f)
              lineTo(arrowCenter.x - 2f, arrowCenter.y)
              lineTo(arrowCenter.x - 6f, arrowCenter.y + 6f)
              close()
            }
            drawPath(path = arrowPath, color = PrimaryBlueLight)
          }
        }

        // Distance & Bearing Midpoint Tag
        val midPoint = Offset((startOffset.x + endOffset.x) / 2f, (startOffset.y + endOffset.y) / 2f)
        val routeBadgeText = "$calculatedDistanceKm km"
        val badgeMeasure = textMeasurer.measure(
          AnnotatedString(routeBadgeText),
          style = TextStyle(
            color = Slate100,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        )
        val badgeW = badgeMeasure.size.width + 16f
        val badgeH = badgeMeasure.size.height + 8f
        val badgeTopLeft = Offset(midPoint.x - badgeW / 2f, midPoint.y - badgeH / 2f - 16f)

        drawRoundRect(
          color = SpecialistCardBg.copy(alpha = 0.92f),
          topLeft = badgeTopLeft,
          size = androidx.compose.ui.geometry.Size(badgeW, badgeH),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        drawRoundRect(
          color = BorderBlue,
          topLeft = badgeTopLeft,
          size = androidx.compose.ui.geometry.Size(badgeW, badgeH),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
          style = Stroke(width = 1f)
        )
        drawText(
          badgeMeasure,
          topLeft = Offset(badgeTopLeft.x + 8f, badgeTopLeft.y + 4f)
        )
      }

      // 4. Draw Flown Breadcrumbs Path (if any)
      if (flownPath.size > 1) {
        val path = Path()
        val first = geoToScreen(flownPath.first().latitude, flownPath.first().longitude)
        path.moveTo(first.x, first.y)
        for (i in 1 until flownPath.size) {
          val pt = geoToScreen(flownPath[i].latitude, flownPath[i].longitude)
          path.lineTo(pt.x, pt.y)
        }
        drawPath(
          path = path,
          color = StatusGreenLight,
          style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
      }

      // 5. Draw Obstacle Avoidance Trajectory Spline (if active)
      if (avoidancePath.size >= 2) {
        val avoidPath = Path()
        val p0 = geoToScreen(avoidancePath[0].latitude, avoidancePath[0].longitude)
        avoidPath.moveTo(p0.x, p0.y)
        for (i in 1 until avoidancePath.size) {
          val pt = geoToScreen(avoidancePath[i].latitude, avoidancePath[i].longitude)
          avoidPath.lineTo(pt.x, pt.y)
        }
        drawPath(
          path = avoidPath,
          color = StatusAmberDim,
          style = Stroke(width = 10f, cap = StrokeCap.Round)
        )
        drawPath(
          path = avoidPath,
          color = StatusAmberLight,
          style = Stroke(
            width = 3.5f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
          )
        )

        // Draw Obstacle Warning Marker icon at mid-point
        if (avoidancePath.size >= 2) {
          val midPt = geoToScreen(avoidancePath[1].latitude, avoidancePath[1].longitude)
          drawCircle(color = StatusRedLight, radius = 10f, center = midPt)
          drawCircle(
            color = Color.White,
            radius = 16f,
            center = midPt,
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f))
          )
        }
      }

      // 6. Draw Point A Marker
      if (pointA != null) {
        val aPos = geoToScreen(pointA.latitude, pointA.longitude)
        drawWaypointMarker(
          scope = this,
          pos = aPos,
          label = "POINT A (START)",
          badgeColor = PrimaryBlueLight,
          textMeasurer = textMeasurer
        )
      }

      // 7. Draw Point B Marker (Landing Zone)
      if (pointB != null) {
        val bPos = geoToScreen(pointB.latitude, pointB.longitude)
        drawLandingZoneMarker(
          scope = this,
          pos = bPos,
          label = "POINT B (LANDING)",
          badgeColor = StatusGreenLight,
          textMeasurer = textMeasurer
        )
      }

      // 8. Draw Real-Time Rotating Drone Marker
      val dronePos = geoToScreen(droneLat, droneLon)
      drawTacticalDrone(
        scope = this,
        center = dronePos,
        heading = droneHeading,
        altitude = droneAltitude,
        isArmed = isArmed,
        textMeasurer = textMeasurer
      )
    }

    // Interactive Mode Header & Floating Tap Indicator (Top Center)
    if (isSelectingPointA || isSelectingPointB) {
      Surface(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 12.dp)
          .testTag("banner_map_tap_instruction"),
        shape = RoundedCornerShape(20.dp),
        color = SpecialistCardBg.copy(alpha = 0.95f),
        border = BorderStroke(1.5.dp, if (isSelectingPointA) PrimaryBlueLight else StatusGreenLight)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isSelectingPointA) Icons.Default.Place else Icons.Default.MyLocation,
            contentDescription = null,
            tint = if (isSelectingPointA) PrimaryBlueLight else StatusGreenLight,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isSelectingPointA) "TAP MAP TO SET POINT A" else "TAP MAP TO SET POINT B",
            color = Slate100,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }
      }
    }

    // Top-Left Quick Selection Mode Switcher Chips
    Row(
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(top = 12.dp, start = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      if (onSelectPointAMode != null) {
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelectPointAMode() }
            .testTag("btn_map_mode_point_a"),
          shape = RoundedCornerShape(8.dp),
          color = if (isSelectingPointA) PrimaryBlue else SpecialistCardBg.copy(alpha = 0.88f),
          border = BorderStroke(1.dp, if (isSelectingPointA) PrimaryBlueLight else BorderSubtle)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isSelectingPointA) Color.White else PrimaryBlueLight)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "SET A",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = if (isSelectingPointA) Color.White else Slate200,
              fontSize = 10.sp
            )
          }
        }
      }

      if (onSelectPointBMode != null) {
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelectPointBMode() }
            .testTag("btn_map_mode_point_b"),
          shape = RoundedCornerShape(8.dp),
          color = if (isSelectingPointB) PrimaryBlue else SpecialistCardBg.copy(alpha = 0.88f),
          border = BorderStroke(1.dp, if (isSelectingPointB) StatusGreenLight else BorderSubtle)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isSelectingPointB) Color.White else StatusGreenLight)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "SET B",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = if (isSelectingPointB) Color.White else Slate200,
              fontSize = 10.sp
            )
          }
        }
      }
    }

    // Floating Map Controls (Zoom +, Zoom -, Fit Route, Center on Drone)
    Column(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 12.dp, end = 12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      FilledIconButton(
        onClick = { zoomLevel = (zoomLevel * 1.3f).coerceAtMost(4.5f) },
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = SpecialistCardBg.copy(alpha = 0.92f),
          contentColor = Slate100
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .size(36.dp)
          .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
          .testTag("map_zoom_in")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
      }

      FilledIconButton(
        onClick = { zoomLevel = (zoomLevel / 1.3f).coerceAtLeast(0.4f) },
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = SpecialistCardBg.copy(alpha = 0.92f),
          contentColor = Slate100
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .size(36.dp)
          .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
          .testTag("map_zoom_out")
      ) {
        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
      }

      FilledIconButton(
        onClick = {
          fitRouteInView(600f, 400f)
        },
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = SpecialistCardBg.copy(alpha = 0.92f),
          contentColor = PrimaryBlueLight
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .size(36.dp)
          .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
          .testTag("map_fit_route")
      ) {
        Icon(Icons.Default.CropFree, contentDescription = "Fit Route", modifier = Modifier.size(18.dp))
      }

      FilledIconButton(
        onClick = {
          panOffsetX = 0f
          panOffsetY = 0f
          autoFollowDrone = true
        },
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = if (autoFollowDrone) PrimaryBlue else SpecialistCardBg.copy(alpha = 0.92f),
          contentColor = if (autoFollowDrone) Color.White else Slate100
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .size(36.dp)
          .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
          .testTag("map_center_drone")
      ) {
        Icon(Icons.Default.MyLocation, contentDescription = "Center on Drone", modifier = Modifier.size(18.dp))
      }
    }

    // Scale and coordinates badge bottom-left
    Surface(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 12.dp, bottom = 12.dp),
      shape = RoundedCornerShape(6.dp),
      color = SpecialistBg.copy(alpha = 0.88f),
      border = BorderStroke(1.dp, BorderSubtle)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .width(26.dp)
            .height(2.dp)
            .background(PrimaryBlueLight)
        )
        Spacer(modifier = Modifier.width(6.dp))
        val scaleMeters = (300 / zoomLevel).toInt()
        Text(
          text = "${scaleMeters}m | GEOFENCE 2.5KM",
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          color = Slate400
        )
      }
    }

    // Floating Route Estimations HUD Banner at Bottom Right of Map Canvas
    if (pointA != null && pointB != null && calculatedDistanceKm > 0) {
      Surface(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 12.dp, bottom = 12.dp)
          .testTag("map_route_hud_pill"),
        shape = RoundedCornerShape(8.dp),
        color = SpecialistCardBg.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, BorderBlue)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Route, contentDescription = null, tint = PrimaryBlueLight, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              "$calculatedDistanceKm km",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = PrimaryBlueLight,
              fontSize = 10.sp
            )
          }

          Text("•", color = Slate500, fontSize = 10.sp)

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = Slate300, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              "${estimatedFlightMinutes}m ${estimatedFlightRemainingSec}s",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = Slate100,
              fontSize = 10.sp
            )
          }

          Text("•", color = Slate500, fontSize = 10.sp)

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = StatusGreenLight, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              "$estimatedBatteryPercent%",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = StatusGreenLight,
              fontSize = 10.sp
            )
          }
        }
      }
    }
  }
}

private fun DrawScope.drawTacticalGrid(
  centerX: Float,
  centerY: Float,
  width: Float,
  height: Float,
  zoom: Float,
  mapType: MapVisualType,
  textMeasurer: TextMeasurer
) {
  val spacing = 70f * zoom

  // Grid Lines
  var x = centerX % spacing
  while (x < width) {
    drawLine(
      color = GridLine,
      start = Offset(x, 0f),
      end = Offset(x, height),
      strokeWidth = 1f
    )
    x += spacing
  }

  var y = centerY % spacing
  while (y < height) {
    drawLine(
      color = GridLine,
      start = Offset(0f, y),
      end = Offset(width, y),
      strokeWidth = 1f
    )
    y += spacing
  }

  // Tactical concentric radar range rings around center
  val ringSteps = listOf(150f, 300f, 450f)
  for (r in ringSteps) {
    val scaledR = r * zoom
    drawCircle(
      color = Color(0x0A3B82F6),
      radius = scaledR,
      center = Offset(centerX, centerY),
      style = Stroke(width = 1f)
    )
  }

  // Crosshair center mark
  drawLine(
    color = GridCrosshair,
    start = Offset(centerX - 12f, centerY),
    end = Offset(centerX + 12f, centerY),
    strokeWidth = 1.5f
  )
  drawLine(
    color = GridCrosshair,
    start = Offset(centerX, centerY - 12f),
    end = Offset(centerX, centerY + 12f),
    strokeWidth = 1.5f
  )
}

private fun drawWaypointMarker(
  scope: DrawScope,
  pos: Offset,
  label: String,
  badgeColor: Color,
  textMeasurer: TextMeasurer
) {
  scope.apply {
    // Pulse outer halo
    drawCircle(color = badgeColor.copy(alpha = 0.2f), radius = 22f, center = pos)
    drawCircle(color = badgeColor.copy(alpha = 0.6f), radius = 14f, center = pos)
    drawCircle(color = SpecialistBg, radius = 9f, center = pos)
    drawCircle(color = badgeColor, radius = 5f, center = pos)

    // Tag Card label above
    val labelResult = textMeasurer.measure(
      AnnotatedString(label),
      style = TextStyle(
        color = badgeColor,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
      )
    )
    val boxWidth = labelResult.size.width + 12f
    val boxHeight = labelResult.size.height + 6f
    val boxTopLeft = Offset(pos.x - boxWidth / 2f, pos.y - 38f)

    drawRoundRect(
      color = SpecialistCardBg.copy(alpha = 0.9f),
      topLeft = boxTopLeft,
      size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawRoundRect(
      color = badgeColor.copy(alpha = 0.8f),
      topLeft = boxTopLeft,
      size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
      style = Stroke(width = 1f)
    )
    drawText(labelResult, topLeft = Offset(pos.x - labelResult.size.width / 2f, pos.y - 35f))
  }
}

private fun drawLandingZoneMarker(
  scope: DrawScope,
  pos: Offset,
  label: String,
  badgeColor: Color,
  textMeasurer: TextMeasurer
) {
  scope.apply {
    // ArUco Landing Pad Target Bullseye
    drawCircle(
      color = badgeColor.copy(alpha = 0.2f),
      radius = 28f,
      center = pos,
      style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
    )
    drawCircle(color = badgeColor.copy(alpha = 0.4f), radius = 18f, center = pos)
    drawCircle(color = SpecialistBg, radius = 12f, center = pos)
    drawCircle(color = badgeColor, radius = 6f, center = pos)

    // Target crosshair corners
    drawLine(color = badgeColor, start = Offset(pos.x - 24f, pos.y), end = Offset(pos.x - 14f, pos.y), strokeWidth = 2f)
    drawLine(color = badgeColor, start = Offset(pos.x + 14f, pos.y), end = Offset(pos.x + 24f, pos.y), strokeWidth = 2f)
    drawLine(color = badgeColor, start = Offset(pos.x, pos.y - 24f), end = Offset(pos.x, pos.y - 14f), strokeWidth = 2f)
    drawLine(color = badgeColor, start = Offset(pos.x, pos.y + 14f), end = Offset(pos.x, pos.y + 24f), strokeWidth = 2f)

    val labelResult = textMeasurer.measure(
      AnnotatedString(label),
      style = TextStyle(
        color = badgeColor,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
      )
    )
    val boxWidth = labelResult.size.width + 12f
    val boxHeight = labelResult.size.height + 6f
    val boxTopLeft = Offset(pos.x - boxWidth / 2f, pos.y - 44f)

    drawRoundRect(
      color = SpecialistCardBg.copy(alpha = 0.9f),
      topLeft = boxTopLeft,
      size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawRoundRect(
      color = badgeColor.copy(alpha = 0.8f),
      topLeft = boxTopLeft,
      size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
      style = Stroke(width = 1f)
    )
    drawText(labelResult, topLeft = Offset(pos.x - labelResult.size.width / 2f, pos.y - 41f))
  }
}

private fun drawTacticalDrone(
  scope: DrawScope,
  center: Offset,
  heading: Float,
  altitude: Double,
  isArmed: Boolean,
  textMeasurer: TextMeasurer
) {
  scope.apply {
    // Rotating group for heading
    rotate(degrees = heading, pivot = center) {
      // Camera FoV Cone
      val conePath = Path().apply {
        moveTo(center.x, center.y)
        lineTo(center.x - 35f, center.y - 75f)
        lineTo(center.x + 35f, center.y - 75f)
        close()
      }
      drawPath(
        path = conePath,
        brush = Brush.verticalGradient(
          colors = listOf(PrimaryBlue.copy(alpha = 0.25f), Color.Transparent),
          startY = center.y - 75f,
          endY = center.y
        )
      )

      // Drone Quadcopter Body
      // Arms (diagonal X)
      drawLine(
        color = Color(0xFF64748B),
        start = Offset(center.x - 18f, center.y - 18f),
        end = Offset(center.x + 18f, center.y + 18f),
        strokeWidth = 3f,
        cap = StrokeCap.Round
      )
      drawLine(
        color = Color(0xFF64748B),
        start = Offset(center.x - 18f, center.y + 18f),
        end = Offset(center.x + 18f, center.y - 18f),
        strokeWidth = 3f,
        cap = StrokeCap.Round
      )

      // 4 Rotors
      val rotorRadius = 7f
      // Front Left & Right (Blue)
      drawCircle(color = PrimaryBlueLight, radius = rotorRadius, center = Offset(center.x - 18f, center.y - 18f))
      drawCircle(color = PrimaryBlueLight, radius = rotorRadius, center = Offset(center.x + 18f, center.y - 18f))
      // Back Left & Right (Amber/Orange)
      drawCircle(color = StatusAmberLight, radius = rotorRadius, center = Offset(center.x - 18f, center.y + 18f))
      drawCircle(color = StatusAmberLight, radius = rotorRadius, center = Offset(center.x + 18f, center.y + 18f))

      // Center Fuselage
      drawCircle(color = SpecialistCardBg, radius = 10f, center = center)
      drawCircle(
        color = if (isArmed) PrimaryBlueLight else Color.Gray,
        radius = 10f,
        center = center,
        style = Stroke(width = 2f)
      )

      // Forward Heading Arrow (Triangle)
      val arrowPath = Path().apply {
        moveTo(center.x, center.y - 12f)
        lineTo(center.x - 4f, center.y - 4f)
        lineTo(center.x + 4f, center.y - 4f)
        close()
      }
      drawPath(path = arrowPath, color = if (isArmed) PrimaryBlueLight else Color.White)
    }

    // Altitude Badge non-rotated below drone
    val altText = "${(altitude * 10).roundToInt() / 10.0}m"
    val altMeasure = textMeasurer.measure(
      AnnotatedString(altText),
      style = TextStyle(
        color = Slate100,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
      )
    )
    drawRoundRect(
      color = SpecialistBg.copy(alpha = 0.88f),
      topLeft = Offset(center.x - (altMeasure.size.width / 2f) - 4f, center.y + 24f),
      size = androidx.compose.ui.geometry.Size(altMeasure.size.width + 8f, altMeasure.size.height + 4f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawText(
      altMeasure,
      topLeft = Offset(center.x - (altMeasure.size.width / 2f), center.y + 26f)
    )
  }
}
