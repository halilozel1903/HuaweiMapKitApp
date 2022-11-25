package com.halil.ozel.huaweimapkitapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_BUNDLE_KEY)
        }

        // View Binding
        binding.huaweiMapView.apply {
            onCreate(mapViewBundle)
            getMapAsync(this@MainActivity)
        }
    }


    // If the map is ready
    override fun onMapReady(map: HuaweiMap) {

        // Mapping
        huaweiMap = map

        // Marker add
        marker = huaweiMap.addMarker(
            MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker()) // Default marker icon
                .title(getString(R.string.location_name)) // Marker title
                .position(LatLng(LATITUDE, LONGITUDE)) // Marker position

        )
        // Camera position settings
        cameraPosition = CameraPosition.builder()
            .target(LatLng(LATITUDE, LONGITUDE))
            .zoom(ZOOM)
            .bearing(BEARING)
            .tilt(TILT).build()
        cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        huaweiMap.moveCamera(cameraUpdate)

    }

    companion object {
        private const val MAP_BUNDLE_KEY = "MapBundleKey"
        private const val LATITUDE: Double = 41.031261
        private const val LONGITUDE: Double = 29.117277
        private const val ZOOM: Float = 10f
        private const val BEARING: Float = 2.0f
        private const val TILT: Float = 2.5f
    }
}