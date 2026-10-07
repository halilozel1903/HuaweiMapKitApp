package com.halil.ozel.huaweimapkitapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.halil.ozel.huaweimapkitapp.databinding.ActivityMainBinding
import com.huawei.hms.maps.*
import com.huawei.hms.maps.model.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var huaweiMap: HuaweiMap
    private lateinit var marker: Marker
    private lateinit var cameraUpdate: CameraUpdate
    private lateinit var cameraPosition: CameraPosition
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setView()

        var mapViewBundle: Bundle? = null
        savedInstanceState?.getBundle(MAP_BUNDLE_KEY)?.also { mapViewBundle = it }

        binding.huaweiMapView.apply {
            onCreate(mapViewBundle)
            getMapAsync(this@MainActivity)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.huaweiMapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.huaweiMapView.onResume()
    }

    override fun onPause() {
        binding.huaweiMapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.huaweiMapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        binding.huaweiMapView.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_BUNDLE_KEY, mapViewBundle)
        }
        binding.huaweiMapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.huaweiMapView.onLowMemory()
    }

    private fun addStopMarker(position: LatLng, title: String, snippet: String, hue: Float): Marker {
        return huaweiMap.addMarker(
            MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(hue))
                .title(title)
                .snippet(snippet)
                .position(position),
        )
    }

    private fun setView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.mapTypeButton.text = getString(
            R.string.map_type_button,
            getString(R.string.map_type_normal),
        )
        binding.mapTypeButton.setOnClickListener { showNextMapType() }
        binding.trafficButton.setOnClickListener { toggleTraffic() }
    }

    private fun toggleTraffic() {
        if (!::huaweiMap.isInitialized) return
        val enabled = !huaweiMap.isTrafficEnabled
        huaweiMap.isTrafficEnabled = enabled
        binding.trafficButton.setText(if (enabled) R.string.traffic_on else R.string.traffic_off)
    }

    private fun showNextMapType() {
        if (!::huaweiMap.isInitialized) return
        val index = MAP_TYPES.indexOf(huaweiMap.mapType).let { if (it < 0) 0 else it }
        val next = MAP_TYPES[(index + 1) % MAP_TYPES.size]
        huaweiMap.mapType = next
        binding.mapTypeButton.text = getString(R.string.map_type_button, mapTypeLabel(next))
    }

    private fun mapTypeLabel(type: Int): String = when (type) {
        HuaweiMap.MAP_TYPE_SATELLITE -> getString(R.string.map_type_satellite)
        HuaweiMap.MAP_TYPE_TERRAIN -> getString(R.string.map_type_terrain)
        else -> getString(R.string.map_type_normal)
    }

    // If the map is ready
    override fun onMapReady(map: HuaweiMap) {

        // Mapping
        huaweiMap = map

        marker = addStopMarker(
            position = OFFICE,
            title = getString(R.string.location_name),
            snippet = getString(R.string.snippet_office),
            hue = BitmapDescriptorFactory.HUE_RED,
        )
        addStopMarker(
            position = USKUDAR,
            title = getString(R.string.stop_uskudar),
            snippet = getString(R.string.snippet_stop),
            hue = BitmapDescriptorFactory.HUE_AZURE,
        )
        addStopMarker(
            position = KADIKOY,
            title = getString(R.string.stop_kadikoy),
            snippet = getString(R.string.snippet_stop),
            hue = BitmapDescriptorFactory.HUE_ORANGE,
        )
        huaweiMap.addPolyline(
            PolylineOptions()
                .add(OFFICE, USKUDAR, KADIKOY)
                .color(ContextCompat.getColor(this, R.color.route_stroke))
                .width(ROUTE_WIDTH)
                .geodesic(true),
        )
        huaweiMap.addCircle(
            CircleOptions()
                .center(OFFICE)
                .radius(CIRCLE_RADIUS_METERS)
                .strokeColor(ContextCompat.getColor(this, R.color.circle_stroke))
                .fillColor(ContextCompat.getColor(this, R.color.circle_fill))
                .strokeWidth(CIRCLE_STROKE_WIDTH),
        )
        huaweiMap.setOnMarkerClickListener { clicked ->
            huaweiMap.animateCamera(CameraUpdateFactory.newLatLng(clicked.position))
            false
        }
        // Camera position settings
        cameraPosition = CameraPosition.builder()
            .target(LatLng(LATITUDE, LONGITUDE))
            .zoom(ZOOM)
            .bearing(BEARING)
            .tilt(TILT).build()
        cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        huaweiMap.moveCamera(cameraUpdate)
        binding.mapControls.post {
            huaweiMap.setPadding(0, 0, 0, binding.mapControls.height)
        }
    }

    companion object {
        private const val MAP_BUNDLE_KEY = "MapBundleKey"
        private const val LATITUDE: Double = 41.031261
        private const val LONGITUDE: Double = 29.117277
        private const val ZOOM: Float = 10f
        private const val BEARING: Float = 2.0f
        private const val TILT: Float = 2.5f
        private val OFFICE = LatLng(LATITUDE, LONGITUDE)
        private val USKUDAR = LatLng(41.0267, 29.0158)
        private val KADIKOY = LatLng(40.9927, 29.0233)
        private const val CIRCLE_RADIUS_METERS = 800.0
        private const val ROUTE_WIDTH = 12f
        private const val CIRCLE_STROKE_WIDTH = 4f
        private val MAP_TYPES = intArrayOf(
            HuaweiMap.MAP_TYPE_NORMAL,
            HuaweiMap.MAP_TYPE_SATELLITE,
            HuaweiMap.MAP_TYPE_TERRAIN,
        )
    }
}