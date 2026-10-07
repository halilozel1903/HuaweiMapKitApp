package com.halil.ozel.huaweimapkitapp.cli

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MapKitCliTest {

    @Test
    fun distanceBetweenTheSamePointIsZero() {
        val point = LatLng(SampleMap.LATITUDE, SampleMap.LONGITUDE)
        assertEquals(0.0, MapMath.distanceMeters(point, point), 0.001)
    }

    @Test
    fun distanceIsSymmetric() {
        val istanbul = LatLng(41.0082, 28.9784)
        val ankara = LatLng(39.9334, 32.8597)
        val forward = MapMath.distanceMeters(istanbul, ankara)
        val backward = MapMath.distanceMeters(ankara, istanbul)
        assertEquals(forward, backward, 0.001)
        assertTrue(forward > 300_000.0)
        assertTrue(forward < 500_000.0)
    }

    @Test
    fun oneDegreeOfLongitudeAtTheEquatorIsAbout111Kilometers() {
        val meters = MapMath.distanceMeters(LatLng(0.0, 0.0), LatLng(0.0, 1.0))
        assertTrue(abs(meters - 111_194.9) < 1.0)
    }

    @Test
    fun sampleCommandPrintsTheAndroidMarker() {
        val output = execute(listOf("sample"))
        assertTrue(output.contains("Huawei Turkey"))
        assertTrue(output.contains("41.031261"))
        assertTrue(output.contains("29.117277"))
        assertTrue(output.contains("zoom: 10"))
        assertTrue(output.contains("bearing: 2"))
        assertTrue(output.contains("tilt: 2.5"))
    }

    @Test
    fun cameraRejectsALatitudeOutsideTheMapRange() {
        val error = assertFailsWith<CliException> {
            execute(listOf("camera", "--lat", "95", "--lng", "29"))
        }
        assertEquals(2, error.exitCode)
        assertTrue(error.message!!.contains("latitude"))
    }

    @Test
    fun distanceRejectsTheWrongNumberOfArguments() {
        val error = assertFailsWith<CliException> {
            execute(listOf("distance", "41", "29"))
        }
        assertEquals(2, error.exitCode)
    }

    @Test
    fun sampleRouteLengthIsTheSumOfItsLegs() {
        val points = SampleMap.route.map { it.position }
        val expected = MapMath.distanceMeters(points[0], points[1]) +
            MapMath.distanceMeters(points[1], points[2])
        assertEquals(expected, MapMath.routeLengthMeters(points), 0.001)
        val output = execute(listOf("route"))
        assertTrue(output.contains("Huawei Turkey"))
        assertTrue(output.contains("Üsküdar"))
        assertTrue(output.contains("Kadıköy"))
        assertTrue(output.contains("legs: 2"))
    }

    @Test
    fun circleAreaUsesTheSampleRadius() {
        val area = MapMath.circleAreaSquareMeters(SampleMap.CIRCLE_RADIUS_METERS)
        assertEquals(Math.PI * 800.0 * 800.0, area, 0.001)
        val output = execute(listOf("circle"))
        assertTrue(output.contains("radiusMeters: 800"))
        assertTrue(output.contains("41.031261"))
    }

    @Test
    fun circleRejectsANegativeRadius() {
        val error = assertFailsWith<CliException> {
            execute(listOf("circle", "--radius", "-10"))
        }
        assertEquals(2, error.exitCode)
    }
}
