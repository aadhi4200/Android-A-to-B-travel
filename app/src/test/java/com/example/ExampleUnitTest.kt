package com.example

import com.example.data.model.GeoPoint
import com.example.ui.viewmodel.DroneViewModel
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testDistanceCalculation() {
    val viewModel = DroneViewModel()
    val pointA = GeoPoint(10.123456, 76.123456, "Point A")
    val pointB = GeoPoint(10.138456, 76.133456, "Point B")
    val distance = viewModel.calculateDistanceKm(pointA, pointB)
    assertTrue("Distance should be greater than 0", distance > 0.0)
    assertTrue("Distance should be around 1.98km", distance in 1.5..2.5)
  }

  @Test
  fun testSwapPoints() {
    val viewModel = DroneViewModel()
    val origA = viewModel.plannerPointA.value
    val origB = viewModel.plannerPointB.value

    viewModel.swapPoints()

    assertEquals(origB.latitude, viewModel.plannerPointA.value.latitude, 0.0001)
    assertEquals(origA.latitude, viewModel.plannerPointB.value.latitude, 0.0001)
  }

  @Test
  fun testMapTapPointSelection() {
    val viewModel = DroneViewModel()
    viewModel.toggleMapSelectPointA()
    assertTrue(viewModel.isSelectingPointAOnMap.value)

    val tapped = GeoPoint(10.200000, 76.200000, "Tapped")
    viewModel.handleMapTap(tapped)

    assertEquals(10.200000, viewModel.plannerPointA.value.latitude, 0.0001)
    assertFalse(viewModel.isSelectingPointAOnMap.value)
  }
}
