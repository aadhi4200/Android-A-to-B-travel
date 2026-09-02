package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
  isSelectingPointB: Boolean = false
) {
  var zoomLevel by remember { mutableFloatStateOf(1.0f) }
  var panOffsetX by remember { mutableFloatStateOf(0f) }
  var panOffsetY by remember { mutableFloatStateOf(0f) }
  var autoFollowDrone by remember { mutableStateOf(true) }

  val textMeasurer = rememberTextMeasurer()

  // Base reference coordinate (centered around default Vertiport Alpha)
  val baseCenterLat = pointA?.latitude ?: 10.123456
  val baseCenterLon = pointA?.longitude ?: 76.123456

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        when (mapType) {
          MapVisualType.TACTICAL_GRID -> AerospaceBg
          MapVisualType.SATELLITE -> Color(0xFF0A121A)
          MapVisualType.DARK_VECTOR -> Color(0xFF05080E)
        }
      )
      .pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
          change.consume()
          panOffsetX += dragAmount.x
          panOffsetY += dragAmount.y
          autoFollowDrone = false
        }
      }
      .pointerInput(isSelectingPointA, isSelectingPointB) {
        detectTapGestures { tapOffset ->
          if (onMapTap != null) {
            // Convert screen tap offset to approx geo coordinates
            val cx = size.width / 2f + panOffsetX
            val cy = size.height / 2f + panOffsetY
            val metersPerPixel = 10.0 / zoomLevel
            val dxMeters = (tapOffset.x - cx) * metersPerPixel
            val dyMeters = (cy - tapOffset.y) * metersPerPixel

            val newLat = baseCenterLat + (dyMeters / 111000.0)
            val newLon = baseCenterLon + (dxMeters / (111000.0 * cos(Math.toRadians(baseCenterLat))))
            onMapTap(GeoPoint(newLat, newLon, "Map Pin"))
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
          color = Color(0x1500E5FF),
          radius = geofenceRadiusPixels,
          center = centerScreen
        )
        drawCircle(
          color = Color(0x6600E5FF),
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

        // Glow line
        drawLine(
          color = CyanGlow,
          start = startOffset,
          end = endOffset,
          strokeWidth = 8f,
          cap = StrokeCap.Round
        )
        // Dashed tactical planned route
        drawLine(
          color = CyanNeon,
          start = startOffset,
          end = endOffset,
          strokeWidth = 3f,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
          cap = StrokeCap.Round
        )
      }

      // 4. Draw Flown Breadcrumbs Path
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
          color = StatusSuccess,
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
        // Glow warning
        drawPath(
          path = avoidPath,
          color = StatusWarningDim,
          style = Stroke(width = 10f, cap = StrokeCap.Round)
        )
        drawPath(
          path = avoidPath,
          color = StatusWarning,
          style = Stroke(
            width = 3.5f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
          )
        )

        // Draw Obstacle Warning Marker icon at mid-point
        if (avoidancePath.size >= 2) {
          val midPt = geoToScreen(avoidancePath[1].latitude, avoidancePath[1].longitude)
          drawCircle(color = StatusError, radius = 10f, center = midPt)
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
          label = "POINT A",
          sublabel = "START / HOME",
          badgeColor = CyanNeon,
          textMeasurer = textMeasurer
        )
      }

      // 7. Draw Point B Marker (Landing Zone)
      if (pointB != null) {
        val bPos = geoToScreen(pointB.latitude, pointB.longitude)
        drawLandingZoneMarker(
          scope = this,
          pos = bPos,
          label = "POINT B",
          sublabel = "DESTINATION",
          badgeColor = StatusSuccess,
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

    // Selection mode banner
    if (isSelectingPointA || isSelectingPointB) {
      Surface(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = AerospaceCardBg.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isSelectingPointA) CyanNeon else StatusSuccess)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = if (isSelectingPointA) CyanNeon else StatusSuccess,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isSelectingPointA) "TAP MAP TO SET POINT A" else "TAP MAP TO SET POINT B",
            color = TextPrimary,
            style = MaterialTheme.typography.labelMedium
          )
        }
      }
    }

    // Floating Map Controls (Zoom +, Zoom -, Center on Drone)
    Column(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 16.dp, end = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilledIconButton(
        onClick = { zoomLevel = (zoomLevel * 1.3f).coerceAtMost(4.0f) },
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = AerospaceCardBg.copy(alpha = 0.9f),
          contentColor = TextPrimary
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(40.dp).testTag("map_zoom_in")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
      }

      FilledIconButton(
        onClick = { zoomLevel = (zoomLevel / 1.3f).coerceAtLeast(0.4f) },
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = AerospaceCardBg.copy(alpha = 0.9f),
          contentColor = TextPrimary
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(40.dp).testTag("map_zoom_out")
      ) {
        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
      }

      FilledIconButton(
        onClick = {
          panOffsetX = 0f
          panOffsetY = 0f
          autoFollowDrone = true
        },
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = if (autoFollowDrone) CyanNeon else AerospaceCardBg.copy(alpha = 0.9f),
          contentColor = if (autoFollowDrone) AerospaceBg else TextPrimary
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(40.dp).testTag("map_center_drone")
      ) {
        Icon(Icons.Default.MyLocation, contentDescription = "Center on Drone", modifier = Modifier.size(20.dp))
      }
    }

    // Scale and coordinates badge bottom-left
    Surface(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 12.dp, bottom = 12.dp),
      shape = RoundedCornerShape(6.dp),
      color = AerospaceBg.copy(alpha = 0.85f),
      border = androidx.compose.foundation.BorderStroke(1.dp, AerospaceBorder)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .width(30.dp)
            .height(2.dp)
            .background(CyanNeon)
        )
        Spacer(modifier = Modifier.width(6.dp))
        val scaleMeters = (300 / zoomLevel).toInt()
        Text(
          text = "${scaleMeters}m | GEOFENCE 2.5KM",
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondary
        )
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
      color = Color(0x0F00E5FF),
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
  sublabel: String,
  badgeColor: Color,
  textMeasurer: TextMeasurer
) {
  scope.apply {
    // Pulse outer halo
    drawCircle(color = badgeColor.copy(alpha = 0.2f), radius = 22f, center = pos)
    drawCircle(color = badgeColor.copy(alpha = 0.6f), radius = 14f, center = pos)
    drawCircle(color = AerospaceBg, radius = 9f, center = pos)
    drawCircle(color = badgeColor, radius = 5f, center = pos)

    // Tag Card label above
    val labelResult = textMeasurer.measure(
      AnnotatedString(label),
      style = TextStyle(color = badgeColor, fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    )
    drawText(labelResult, topLeft = Offset(pos.x - labelResult.size.width / 2f, pos.y - 36f))
  }
}

private fun drawLandingZoneMarker(
  scope: DrawScope,
  pos: Offset,
  label: String,
  sublabel: String,
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
    drawCircle(color = AerospaceBg, radius = 12f, center = pos)
    drawCircle(color = badgeColor, radius = 6f, center = pos)

    // Target crosshair corners
    drawLine(color = badgeColor, start = Offset(pos.x - 24f, pos.y), end = Offset(pos.x - 14f, pos.y), strokeWidth = 2f)
    drawLine(color = badgeColor, start = Offset(pos.x + 14f, pos.y), end = Offset(pos.x + 24f, pos.y), strokeWidth = 2f)
    drawLine(color = badgeColor, start = Offset(pos.x, pos.y - 24f), end = Offset(pos.x, pos.y - 14f), strokeWidth = 2f)
    drawLine(color = badgeColor, start = Offset(pos.x, pos.y + 14f), end = Offset(pos.x, pos.y + 24f), strokeWidth = 2f)

    val labelResult = textMeasurer.measure(
      AnnotatedString(label),
      style = TextStyle(color = badgeColor, fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    )
    drawText(labelResult, topLeft = Offset(pos.x - labelResult.size.width / 2f, pos.y - 42f))
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
          colors = listOf(CyanNeon.copy(alpha = 0.25f), Color.Transparent),
          startY = center.y - 75f,
          endY = center.y
        )
      )

      // Drone Quadcopter Body
      // Arms (diagonal X)
      drawLine(
        color = Color(0xFF94A3B8),
        start = Offset(center.x - 18f, center.y - 18f),
        end = Offset(center.x + 18f, center.y + 18f),
        strokeWidth = 3f,
        cap = StrokeCap.Round
      )
      drawLine(
        color = Color(0xFF94A3B8),
        start = Offset(center.x - 18f, center.y + 18f),
        end = Offset(center.x + 18f, center.y - 18f),
        strokeWidth = 3f,
        cap = StrokeCap.Round
      )

      // 4 Rotors
      val rotorRadius = 7f
      // Front Left & Right (Cyan)
      drawCircle(color = CyanNeon, radius = rotorRadius, center = Offset(center.x - 18f, center.y - 18f))
      drawCircle(color = CyanNeon, radius = rotorRadius, center = Offset(center.x + 18f, center.y - 18f))
      // Back Left & Right (Amber/Orange)
      drawCircle(color = StatusWarning, radius = rotorRadius, center = Offset(center.x - 18f, center.y + 18f))
      drawCircle(color = StatusWarning, radius = rotorRadius, center = Offset(center.x + 18f, center.y + 18f))

      // Center Fuselage
      drawCircle(color = AerospaceCardBg, radius = 10f, center = center)
      drawCircle(
        color = if (isArmed) CyanNeon else Color.Gray,
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
      drawPath(path = arrowPath, color = if (isArmed) CyanNeon else Color.White)
    }

    // Altitude Badge non-rotated below drone
    val altText = "${(altitude * 10).roundToInt() / 10.0}m"
    val altMeasure = textMeasurer.measure(
      AnnotatedString(altText),
      style = TextStyle(color = TextPrimary, fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    )
    drawRect(
      color = AerospaceBg.copy(alpha = 0.85f),
      topLeft = Offset(center.x - (altMeasure.size.width / 2f) - 4f, center.y + 24f),
      size = androidx.compose.ui.geometry.Size(altMeasure.size.width + 8f, altMeasure.size.height + 4f)
    )
    drawText(
      altMeasure,
      topLeft = Offset(center.x - (altMeasure.size.width / 2f), center.y + 26f)
    )
  }
}
