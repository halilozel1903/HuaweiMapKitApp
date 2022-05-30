# Huawei Map Kit App 🗺 📍 🧭

![Screenshot](https://github.com/halilozel1903/HuaweiMapKitApp/blob/master/photos/huawei.png)

## What is Huawei Map Kit ⁉️
Map Kit is an SDK for map development. It covers map data of more than 200 countries and regions, and supports hundreds of languages. With this SDK, you can easily integrate map-based functions into your apps.

HUAWEI Map Kit uses the WGS 84 GPS coordinate system, which meets most map development requirements outside China, including:
Map display: Displays buildings, roads, water systems, and Points of Interest (POIs).
Map interaction: Controls the interaction gestures and buttons on the map.
Map drawing: Adds location markers, map layers, overlays, and various shapes. <br>

## How to use Huawei Map Kit 🧐

- First, you need to register a Huawei Developer account. You can register it for free from the link below.

![Screenshot](https://github.com/halilozel1903/HuaweiMapKitApp/blob/master/photos/huaweiDeveloperAccount.png)<br><br>

https://id5.cloud.huawei.com/CAS/portal/userRegister/regbyemail.html

- After opening the developer account, we will create a project

![Screenshot](https://github.com/halilozel1903/HuaweiMapKitApp/blob/master/photos/huaweiProject.png)<br><br>

- And open an application in the project.

![Screenshot](https://github.com/halilozel1903/HuaweiMapKitApp/blob/master/photos/huaweiApp.png)<br><br>

- Need to adjust the settings of the application. Since it is a sample project, I share all the information with you.

![Screenshot](https://github.com/halilozel1903/HuaweiMapKitApp/blob/master/photos/huaweiSettings.png)<br><br>

- Need to produce SHA-256 certificate fingerprints. We click on the Gradle area on the right in Android Studio.

![Screenshot](https://github.com/halilozel1903/HuaweiMapKitApp/blob/master/photos/gradleSign.png)<br><br>

- Clicking the signingReport button under Tasks, we generate sha 256 code. We add it to the relevant field in the project settings.

![Screenshot](https://github.com/halilozel1903/HuaweiMapKitApp/blob/master/photos/sha256.png)<br><br>

- Need to activate MapKit from the Manage API section.

![Screenshot](https://github.com/halilozel1903/HuaweiMapKitApp/blob/master/photos/huaweiMapKit.png)<br><br>

- Download the `agconnect-services.json` file. Then we add it to the app folder of our project.

![Screenshot](https://github.com/halilozel1903/HuaweiMapKitApp/blob/master/photos/agconnect.png)<br><br>

- Define the necessary permissions in the `AndroidManifest.xml` file.

```kotlin
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="com.huawei.appmarket.service.commondata.permission.GET_COMMON_DATA" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
``` 
<br>

- In the `build.gradle(HuaweiMapKitApp)` field, we add the necessary codes for Huawei Map Kit.

```kotlin
buildscript {
    ext.kotlin_version = "1.6.21"
    repositories {
        google()
        mavenCentral()
        maven { url 'https://developer.huawei.com/repo/' }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.2.1'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath 'com.huawei.agconnect:agcp:1.6.0.300'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url 'https://developer.huawei.com/repo/'
        }
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```
<br>

- Add the plugin in `build.gradle(:app)`

```kotlin
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'com.huawei.agconnect'
}
```
<br>

- Add the Map Kit dependency in `build.gradle(:app)`

```kotlin
dependencies {
    implementation 'androidx.core:core-ktx:1.7.0'
    implementation 'androidx.appcompat:appcompat:1.4.1'
    implementation 'com.google.android.material:material:1.6.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'com.huawei.hms:maps:6.4.1.300'
}
```
<br>

- Now can add map to our layout file (`activity_main.xml`).

```kotlin
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.huawei.hms.maps.MapView
        xmlns:map="http://schemas.android.com/apk/res-auto"
        android:id="@+id/huaweiMapView"
        map:uiCompass="true"
        map:uiZoomControls="true"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```
<br>

- Make map settings in the `MainActivity.kt`. Add a static location information and marker to the map.

```kotlin
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var huaweiMap: HuaweiMap
    private lateinit var marker: Marker
    private lateinit var cameraUpdate: CameraUpdate
    private lateinit var cameraPosition: CameraPosition
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // View Binding Settings
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_BUNDLE_KEY)
        }
        binding.huaweiMapView.onCreate(mapViewBundle)
        binding.huaweiMapView.getMapAsync(this)
    }


    // if the map is ready
    override fun onMapReady(map: HuaweiMap) {

        // mapping
        huaweiMap = map

        // marker add
        marker = huaweiMap.addMarker(
            MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker()) // default marker
                .title("Huawei Turkey") // maker title
                .position(LatLng(41.031261, 29.117277)) // marker position

        )
        // camera position settings
        cameraPosition = CameraPosition.builder()
            .target(LatLng(41.031261, 29.117277))
            .zoom(10f)
            .bearing(2.0f)
            .tilt(2.5f).build()
        cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        huaweiMap.moveCamera(cameraUpdate)

    }
    // constant
    companion object {
        private const val MAP_BUNDLE_KEY = "MapBundleKey"
    }
}
```
- Have done all the necessary actions. Congratulations! 🥳 You have developed the first Huawei Maps application.

## Screenshots 📱

<img src="https://github.com/halilozel1903/HuaweiMapKitApp/blob/master/photos/huaweiMapKitScreen1.png" width="300" /> <img src="https://github.com/halilozel1903/HuaweiMapKitApp/blob/master/photos/huaweiMapKitScreen2.png" width="300" />


## Resources 📚
- https://developer.huawei.com/consumer/en/hms/huawei-MapKit/
- https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-sdk-introduction-0000001050158633
- https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/hms-map-v4-faq
- https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-sdk-version-change-history-0000001050156688

<br>

## License ℹ️
```
MIT License

Copyright (c) 2022 Halil OZEL

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
