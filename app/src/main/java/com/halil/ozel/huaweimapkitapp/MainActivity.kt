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

        //View Binding
        binding.huaweiMapView.onCreate(mapViewBundle)
        binding.huaweiMapView.getMapAsync(this)
    }


    // if the map is ready
    override fun onMapReady(map: HuaweiMap) {

        //mapping
        huaweiMap = map

        //marker add
        marker = huaweiMap.addMarker(
            MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker()) //default marker
                .title("Huawei Turkey") // maker title
                .position(LatLng(41.031261, 29.117277)) //marker position

        )
        //camera position settings
        cameraPosition = CameraPosition.builder()
            .target(LatLng(41.031261, 29.117277))
            .zoom(10f)
            .bearing(2.0f)
            .tilt(2.5f).build()
        cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        huaweiMap.moveCamera(cameraUpdate)

    }

    companion object {
        private const val MAP_BUNDLE_KEY = "MapBundleKey"
    }
}