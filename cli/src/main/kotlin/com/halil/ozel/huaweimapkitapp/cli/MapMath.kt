package com.halil.ozel.huaweimapkitapp.cli

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Marker and camera used by the Android sample in MainActivity.
 */
data class RouteStop(val title: String, val position: LatLng)

object SampleMap {
    const val TITLE = "Huawei Turkey"
    const val LATITUDE = 41.031261
    const val LONGITUDE = 29.117277
    const val ZOOM = 10.0
    const val BEARING = 2.0
    const val TILT = 2.5
    const val CIRCLE_RADIUS_METERS = 800.0

    val route = listOf(
        RouteStop("Huawei Turkey", LatLng(LATITUDE, LONGITUDE)),
        RouteStop("Üsküdar", LatLng(41.0267, 29.0158)),
        RouteStop("Kadıköy", LatLng(40.9927, 29.0233)),
    )
}

data class LatLng(val latitude: Double, val longitude: Double)

data class CameraPosition(
    val target: LatLng,
    val zoom: Double,
    val bearing: Double,
    val tilt: Double,
)

object MapMath {
    const val EARTH_RADIUS_METERS = 6_371_000.0
    const val MIN_LATITUDE = -90.0
    const val MAX_LATITUDE = 90.0
    const val MIN_LONGITUDE = -180.0
    const val MAX_LONGITUDE = 180.0
    const val MIN_ZOOM = 0.0
    const val MAX_ZOOM = 20.0
    const val MIN_BEARING = 0.0
    const val MAX_BEARING = 360.0
    const val MIN_TILT = 0.0
    const val MAX_TILT = 75.0

    fun sampleCamera(): CameraPosition = CameraPosition(
        target = LatLng(SampleMap.LATITUDE, SampleMap.LONGITUDE),
        zoom = SampleMap.ZOOM,
        bearing = SampleMap.BEARING,
        tilt = SampleMap.TILT,
    )

    fun routeLengthMeters(points: List<LatLng>): Double {
        if (points.size < 2) {
            throw IllegalArgumentException("route needs at least two points")
        }
        return points.zipWithNext().sumOf { (from, to) -> distanceMeters(from, to) }
    }

    fun circleAreaSquareMeters(radiusMeters: Double): Double {
        if (radiusMeters.isNaN() || radiusMeters <= 0.0) {
            throw IllegalArgumentException("radius must be greater than 0")
        }
        return Math.PI * radiusMeters * radiusMeters
    }

    fun distanceMeters(from: LatLng, to: LatLng): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    fun validateLatLng(latitude: Double, longitude: Double) {
        requireRange("latitude", latitude, MIN_LATITUDE, MAX_LATITUDE)
        requireRange("longitude", longitude, MIN_LONGITUDE, MAX_LONGITUDE)
    }

    fun validateCamera(camera: CameraPosition) {
        validateLatLng(camera.target.latitude, camera.target.longitude)
        requireRange("zoom", camera.zoom, MIN_ZOOM, MAX_ZOOM)
        requireRange("bearing", camera.bearing, MIN_BEARING, MAX_BEARING)
        requireRange("tilt", camera.tilt, MIN_TILT, MAX_TILT)
    }

    private fun requireRange(name: String, value: Double, min: Double, max: Double) {
        if (value.isNaN() || value < min || value > max) {
            throw IllegalArgumentException("$name must be between $min and $max")
        }
    }
}
