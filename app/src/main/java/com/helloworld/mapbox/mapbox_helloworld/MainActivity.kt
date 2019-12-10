package com.helloworld.mapbox.mapbox_helloworld

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.building.BuildingPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.OnCameraTrackingChangedListener
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import com.navisens.motiondnaapi.MotionDna
import com.navisens.motiondnaapi.MotionDnaApplication
import timber.log.Timber
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var latLan: LatLng? = null
    lateinit var mapboxMap: MapboxMap
    lateinit var buildingPlugin: BuildingPlugin
    var motionDnaRuntimeSource: MotionDnaDataSource? = null

    // Your Navisens developer key
    val navisensDevKey = "s1Oz2c0TCwZYvjvBeOUeq8dGlzkAM6OaE10e2YKsFpULyxAqxDNqbV2Mz3K1Li9I"

    // Your mapbox token
    val mapBoxToken = "pk.eyJ1IjoiZG9ueW51cmFuc3lhaCIsImEiOiJjajNwMTBtdTYwMHAwMnduNGxybzdid2Z4In0.tjLzArV7DvhF9Nr1jxX4nQ"


    override fun onMapReady(mapboxMap: MapboxMap?) {
        this.mapboxMap = mapboxMap!!
        // Request Navisens MotionDna permissions
        ActivityCompat.requestPermissions(this, MotionDnaApplication.needsRequestingPermissions(), REQUEST_MDNA_PERMISSIONS)
        // Enable 3D buildings, why? Because it's cool.
        buildingPlugin = BuildingPlugin(mapView, this.mapboxMap)
        buildingPlugin.setVisibility(true)


        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    latLan?.let { motionDnaRuntimeSource?.locationChange(it) }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                }
            }
        }
        this.mapboxMap.addOnMapLongClickListener {
            latLan = it
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
            builder.setMessage("Sync your location to this position ?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show()
        }

        initNavi()
    }

    companion object {
        private val REQUEST_MDNA_PERMISSIONS = 1
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // Method called when permissions have been confirmed
        // Instantiating MotionDnaDataSource, passing in context, packagemanager and Navisens devkey
//        initNavi()
        Toast.makeText(applicationContext, "woW", Toast.LENGTH_LONG).show()
    }

    private fun initNavi() {
        try {
            motionDnaRuntimeSource = MotionDnaDataSource(applicationContext, packageManager, navisensDevKey, object : MotionDnaDataSource.logListener {
                override fun log(log: MotionDna) {
                    val logs = "Type : ${log.motion.motionType}\n" +
                            "Step : ${log.motion.stepFrequency}\n" +
                            "Magnetic : ${log.gpsLocation.magneticHeading}\n" +
                            "Calibration : ${log.calibrationStatus}\n" +
                            "lats : ${log.map.lats}\n" +
                            "langs : ${log.map.lngs}\n" +
                            "lats Local: ${log.location.globalLocation.latitude}\n" +
                            "langs Local: ${log.location.globalLocation.longitude}\n" +
                            "lats Global: ${log.gpsLocation.globalLocation.latitude}\n" +
                            "langs Global: ${log.gpsLocation.globalLocation.longitude}\n" +
                            "x1 : ${log.location.localLocation.x}\n" +
                            "y1 : ${log.location.localLocation.y}\n" +
                            "z1 : ${log.location.localLocation.z}\n" +
                            "x2 : ${(log.location.localLocation.x * 1e5).roundToInt() / 1e5}\n" +
                            "y2 : ${(log.location.localLocation.y * 1e5).roundToInt() / 1e5}\n" +
                            "z3 : ${log.gpsLocation.localLocation.z}\n" +
                            "vertical motion :${log.location.verticalMotionStatus.name}\n" +
                            "heading : ${log.location.heading}\n" +
                            "selected lat : ${latLan?.latitude} \n" +
                            "selected lon : ${latLan?.longitude}"
                    findViewById<TextView>(R.id.logCat).text = logs
                }
            })
            // Overridding internal data source with MotionDna data source.
            LocationLayerPlugin(mapView, this.mapboxMap, motionDnaRuntimeSource).also {
                //Follow positioning
                it.cameraMode = CameraMode.TRACKING
                // Renders position only not heading.
                it.renderMode = RenderMode.GPS
                lifecycle.addObserver(it)
                it.addOnLocationLongClickListener {
                    //sync
                    motionDnaRuntimeSource?.syncWithGPS()
                    Toast.makeText(this@MainActivity, "Long Click", Toast.LENGTH_SHORT).show()
                }
                it.addOnCameraTrackingChangedListener(object : OnCameraTrackingChangedListener {
                    override fun onCameraTrackingChanged(currentMode: Int) {
                        Toast.makeText(this@MainActivity, currentMode.toString(), Toast.LENGTH_SHORT).show()
                    }

                    override fun onCameraTrackingDismissed() {

                    }
                })
            }

        } catch (exception: Exception) {
            Log.e(javaClass.simpleName, exception.toString())
        }
    }

    lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Mapbox.getInstance(this, mapBoxToken)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this);
    }

    public override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    public override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

}
