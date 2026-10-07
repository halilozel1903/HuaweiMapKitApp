package com.halil.ozel.huaweimapkitapp.cli

import java.util.Locale
import kotlin.system.exitProcess

class CliException(val exitCode: Int, message: String) : RuntimeException(message)

private const val HELP = """
Huawei Map Kit CLI

Commands:
  sample
      Print the marker and camera used by the Android sample.
  distance <fromLat> <fromLng> <toLat> <toLng>
      Great-circle distance between two WGS 84 points, in meters and kilometers.
  camera [--lat N] [--lng N] [--zoom N] [--bearing N] [--tilt N]
      Check a Map Kit camera. Omitted values use the sample camera.
  route [lat lng lat lng ...]
      Length of the sample route, or of the points you pass. At least two points.
  circle [--radius N] [--lat N] [--lng N]
      Area of a circle. Defaults to the 800 m circle around Huawei Turkey.

Camera limits: latitude -90..90, longitude -180..180, zoom 0..20, bearing 0..360, tilt 0..75.
"""

fun main(args: Array<String>) {
    try {
        println(execute(args.toList()).trimEnd())
    } catch (error: CliException) {
        System.err.println(error.message?.trimEnd())
        exitProcess(error.exitCode)
    }
}

fun execute(args: List<String>): String {
    if (args.isEmpty() || args.first() in setOf("help", "-h", "--help")) {
        return HELP
    }
    return when (val command = args.first()) {
        "sample" -> {
            if (args.size != 1) {
                throw CliException(2, "sample takes no arguments\n$HELP")
            }
            formatSample()
        }
        "distance" -> formatDistance(parseDistance(args.drop(1)))
        "camera" -> formatCamera(parseCamera(args.drop(1)))
        "route" -> formatRoute(parseRoute(args.drop(1)))
        "circle" -> formatCircle(parseCircle(args.drop(1)))
        else -> throw CliException(2, "Unknown command '$command'\n$HELP")
    }
}

private fun formatSample(): String {
    val camera = MapMath.sampleCamera()
    return """
        Marker
          title: ${SampleMap.TITLE}
          latitude: ${formatCoordinate(SampleMap.LATITUDE)}
          longitude: ${formatCoordinate(SampleMap.LONGITUDE)}
        Camera
          latitude: ${formatCoordinate(camera.target.latitude)}
          longitude: ${formatCoordinate(camera.target.longitude)}
          zoom: ${formatNumber(camera.zoom)}
          bearing: ${formatNumber(camera.bearing)}
          tilt: ${formatNumber(camera.tilt)}
    """.trimIndent()
}

private fun parseDistance(args: List<String>): Pair<LatLng, LatLng> {
    if (args.size != 4) {
        throw CliException(2, "distance expects fromLat fromLng toLat toLng\n$HELP")
    }
    val numbers = args.map { token ->
        token.toDoubleOrNull() ?: throw CliException(2, "Not a number: $token")
    }
    val from = LatLng(numbers[0], numbers[1])
    val to = LatLng(numbers[2], numbers[3])
    try {
        MapMath.validateLatLng(from.latitude, from.longitude)
        MapMath.validateLatLng(to.latitude, to.longitude)
    } catch (error: IllegalArgumentException) {
        throw CliException(2, error.message ?: "Invalid coordinates")
    }
    return from to to
}

private fun formatDistance(points: Pair<LatLng, LatLng>): String {
    val meters = MapMath.distanceMeters(points.first, points.second)
    val kilometers = meters / 1000.0
    return """
        Distance
          meters: ${formatDecimal(meters, 1)}
          kilometers: ${formatDecimal(kilometers, 3)}
    """.trimIndent()
}

private fun parseCamera(args: List<String>): CameraPosition {
    val values = linkedMapOf<String, Double>()
    var index = 0
    while (index < args.size) {
        val flag = args[index]
        if (!flag.startsWith("--") || index + 1 >= args.size) {
            throw CliException(2, "Expected --name value pairs\n$HELP")
        }
        val name = flag.removePrefix("--")
        if (name !in setOf("lat", "lng", "zoom", "bearing", "tilt")) {
            throw CliException(2, "Unknown camera option '$flag'\n$HELP")
        }
        val raw = args[index + 1]
        values[name] = raw.toDoubleOrNull() ?: throw CliException(2, "Not a number: $raw")
        index += 2
    }
    val camera = CameraPosition(
        target = LatLng(
            values["lat"] ?: SampleMap.LATITUDE,
            values["lng"] ?: SampleMap.LONGITUDE,
        ),
        zoom = values["zoom"] ?: SampleMap.ZOOM,
        bearing = values["bearing"] ?: SampleMap.BEARING,
        tilt = values["tilt"] ?: SampleMap.TILT,
    )
    try {
        MapMath.validateCamera(camera)
    } catch (error: IllegalArgumentException) {
        throw CliException(2, error.message ?: "Invalid camera")
    }
    return camera
}

private fun parseRoute(args: List<String>): List<RouteStop> {
    if (args.isEmpty()) return SampleMap.route
    if (args.size < 4 || args.size % 2 != 0) {
        throw CliException(2, "route expects lat lng pairs, at least two points\n$HELP")
    }
    return args.chunked(2).mapIndexed { index, pair ->
        val latitude = pair[0].toDoubleOrNull() ?: throw CliException(2, "Not a number: ${pair[0]}")
        val longitude = pair[1].toDoubleOrNull() ?: throw CliException(2, "Not a number: ${pair[1]}")
        try {
            MapMath.validateLatLng(latitude, longitude)
        } catch (error: IllegalArgumentException) {
            throw CliException(2, error.message ?: "Invalid coordinates")
        }
        RouteStop("Point ${index + 1}", LatLng(latitude, longitude))
    }
}

private fun formatRoute(stops: List<RouteStop>): String {
    val lines = stops.joinToString("\n") { stop ->
        "  ${stop.title}: ${formatCoordinate(stop.position.latitude)}, ${formatCoordinate(stop.position.longitude)}"
    }
    val meters = try {
        MapMath.routeLengthMeters(stops.map { it.position })
    } catch (error: IllegalArgumentException) {
        throw CliException(2, error.message ?: "Invalid route")
    }
    return """
        Route
        $lines
          legs: ${stops.size - 1}
          meters: ${formatDecimal(meters, 1)}
          kilometers: ${formatDecimal(meters / 1000.0, 3)}
    """.trimIndent()
}

private fun parseCircle(args: List<String>): Pair<LatLng, Double> {
    val values = linkedMapOf<String, Double>()
    var index = 0
    while (index < args.size) {
        val flag = args[index]
        if (!flag.startsWith("--") || index + 1 >= args.size) {
            throw CliException(2, "Expected --name value pairs\n$HELP")
        }
        val name = flag.removePrefix("--")
        if (name !in setOf("radius", "lat", "lng")) {
            throw CliException(2, "Unknown circle option '$flag'\n$HELP")
        }
        val raw = args[index + 1]
        values[name] = raw.toDoubleOrNull() ?: throw CliException(2, "Not a number: $raw")
        index += 2
    }
    val center = LatLng(
        values["lat"] ?: SampleMap.LATITUDE,
        values["lng"] ?: SampleMap.LONGITUDE,
    )
    val radius = values["radius"] ?: SampleMap.CIRCLE_RADIUS_METERS
    try {
        MapMath.validateLatLng(center.latitude, center.longitude)
        MapMath.circleAreaSquareMeters(radius)
    } catch (error: IllegalArgumentException) {
        throw CliException(2, error.message ?: "Invalid circle")
    }
    return center to radius
}

private fun formatCircle(circle: Pair<LatLng, Double>): String {
    val (center, radius) = circle
    val area = MapMath.circleAreaSquareMeters(radius)
    return """
        Circle
          latitude: ${formatCoordinate(center.latitude)}
          longitude: ${formatCoordinate(center.longitude)}
          radiusMeters: ${formatNumber(radius)}
          areaSquareMeters: ${formatDecimal(area, 1)}
          areaSquareKilometers: ${formatDecimal(area / 1_000_000.0, 3)}
    """.trimIndent()
}

private fun formatCamera(camera: CameraPosition): String = """
    Camera
      latitude: ${formatCoordinate(camera.target.latitude)}
      longitude: ${formatCoordinate(camera.target.longitude)}
      zoom: ${formatNumber(camera.zoom)}
      bearing: ${formatNumber(camera.bearing)}
      tilt: ${formatNumber(camera.tilt)}
""".trimIndent()

private fun formatCoordinate(value: Double): String = formatDecimal(value, 6).trimEnd('0').trimEnd('.')

private fun formatNumber(value: Double): String = formatDecimal(value, 4).trimEnd('0').trimEnd('.')

private fun formatDecimal(value: Double, decimals: Int): String =
    String.format(Locale.US, "%.${decimals}f", value)
