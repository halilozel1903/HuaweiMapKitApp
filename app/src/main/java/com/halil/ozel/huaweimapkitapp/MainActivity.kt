package com.halil.ozel.huaweimapkitapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    private val locationManager by lazy { getSystemService(LocationManager::class.java) }
    private var deviceLocationListener: LocationListener? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            enableMyLocation(moveCamera = true)
        } else {
            Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

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
        deviceLocationListener?.let { listener ->
            if (hasLocationPermission()) {
                locationManager.removeUpdates(listener)
            }
        }
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
        binding.fitRouteButton.setOnClickListener { fitRoute() }
        binding.resetCameraButton.setOnClickListener { resetCamera() }
        binding.myLocationButton.setOnClickListener { onMyLocationClicked() }
    }

    private fun onMyLocationClicked() {
        if (hasLocationPermission()) {
            enableMyLocation(moveCamera = true)
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    private fun enableMyLocation(moveCamera: Boolean) {
        if (!::huaweiMap.isInitialized || !hasLocationPermission()) return
        try {
            huaweiMap.isMyLocationEnabled = true
            huaweiMap.uiSettings.isMyLocationButtonEnabled = true
            if (moveCamera) {
                moveCameraToDeviceLocation()
            }
        } catch (_: SecurityException) {
            Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveCameraToDeviceLocation() {
        val provider = locationProvider() ?: run {
            Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                deviceLocationListener = null
                if (hasLocationPermission()) {
                    locationManager.removeUpdates(this)
                }
                if (!::huaweiMap.isInitialized) return
                huaweiMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        MY_LOCATION_ZOOM,
                    ),
                )
            }
        }
        try {
            val last = locationManager.getLastKnownLocation(provider)
            if (last != null) {
                listener.onLocationChanged(last)
            } else {
                deviceLocationListener = listener
                locationManager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            }
        } catch (_: SecurityException) {
            Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun locationProvider(): String? = when {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> null
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    private fun fitRoute() {
        if (!::huaweiMap.isInitialized) return
        val bounds = LatLngBounds.builder()
            .include(OFFICE)
            .include(USKUDAR)
            .include(KADIKOY)
            .build()
        huaweiMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, ROUTE_PADDING))
    }

    private fun resetCamera() {
        if (!::huaweiMap.isInitialized) return
        huaweiMap.animateCamera(CameraUpdateFactory.newCameraPosition(sampleCamera()))
    }

    private fun sampleCamera(): CameraPosition = CameraPosition.builder()
        .target(OFFICE)
        .zoom(ZOOM)
        .bearing(BEARING)
        .tilt(TILT)
        .build()

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
        if (hasLocationPermission()) {
            enableMyLocation(moveCamera = false)
        }
        // Camera position settings
        cameraPosition = sampleCamera()
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
        private const val ROUTE_PADDING = 120
        private const val MY_LOCATION_ZOOM = 15f
        private val MAP_TYPES = intArrayOf(
            HuaweiMap.MAP_TYPE_NORMAL,
            HuaweiMap.MAP_TYPE_SATELLITE,
            HuaweiMap.MAP_TYPE_TERRAIN,
        )
    }
}