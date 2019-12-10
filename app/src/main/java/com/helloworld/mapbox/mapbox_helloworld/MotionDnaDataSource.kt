package com.helloworld.mapbox.mapbox_helloworld

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.mapboxsdk.geometry.LatLng
import com.navisens.motiondnaapi.MotionDna
import com.navisens.motiondnaapi.MotionDnaApplication
import com.navisens.motiondnaapi.MotionDnaInterface
import timber.log.Timber
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.cos

class MotionDnaDataSource : MotionDnaInterface, LocationEngine {

    override fun activate() {
    }

    override fun removeLocationUpdates() {
        app.stop()
    }

    override fun isConnected(): Boolean {
        return true
    }

    override fun getLastLocation(): Location {
        return last_location
    }

    override fun deactivate() {
    }

    override fun obtainType(): Type {
        return Type.valueOf("NAVISENS")
    }

    override fun requestLocationUpdates() {

    }


    interface logListener {
        fun log(log: MotionDna)
    }

    private val meter = 1000
    val rearth = 6378.137
    val pi = Math.PI
    private var locationchange: Boolean = false
    private var syncGPS: Boolean = false
    private var latLng: LatLng? = null
    var app: MotionDnaApplication
    var ctx: Context
    var pkg: PackageManager
    var last_location: Location
    var devKey: String
    var log: logListener

    // Constructor with context and packagemanger for our SDK internal usage.
    constructor(ctx: Context, pkg: PackageManager, devKey: String, log: logListener) : super() {
        this.ctx = ctx
        this.log = log
        this.pkg = pkg
        this.last_location = Location("NAVISENS_LOCATION_PROVIDER")
        this.devKey = devKey
        // Instantiating core
        app = MotionDnaApplication(this)
        // Instantiating inertial engine
        app.runMotionDna(devKey)
        // Enabling GPS receivers within SDK.
        app.setExternalPositioningState(MotionDna.ExternalPositioningState.HIGH_ACCURACY)
        // Trigger inertial engine to run with global positional corrections.
        app.setLocationNavisens()
        // Trigger inertial engine to run in pure inertial from given lat lon and heading.
//        app.setLocationLatitudeLongitudeAndHeadingInDegrees(-6.31694, 106.66417, 315.0)
    }


    override fun getAppContext(): Context {
        return this.ctx
    }

    override fun receiveNetworkData(p0: MotionDna?) {
        Log.e(javaClass.simpleName, "Motion ${p0?.motion?.motionType?.name}")
        Log.e(javaClass.simpleName, "Motion ${p0?.motion?.motionType?.ordinal}")
        Log.e(javaClass.simpleName, "Motion ${p0?.motion?.stepFrequency}")
    }

    fun locationChange(latLng: LatLng) {
        this.latLng = latLng
        this.locationchange = true
    }

    fun syncWithGPS() {
        this.syncGPS = true
    }

    override fun receiveNetworkData(p0: MotionDna.NetworkCode?, p1: MutableMap<String, out Any>?) {
        Log.e(javaClass.simpleName, "Motion ${p0?.name}")
        Log.e(javaClass.simpleName, "Motion ${p0?.ordinal}")
    }

    @SuppressLint("LogNotTimber")
    override fun receiveMotionDna(motionDna: MotionDna?) {
        Timber.e("Sync GPS : $syncGPS")
        Timber.e("Sync GPS lat : ${motionDna?.getGPSLocation()?.globalLocation!!.latitude}")
        Timber.e("Sync GPS lon : ${motionDna.getGPSLocation()?.globalLocation!!.longitude}")
        Timber.e("Sync Local lat : ${motionDna.getLocalLocation()?.globalLocation!!.latitude}")
        Timber.e("Sync Local lon : ${motionDna.getLocalLocation()?.globalLocation!!.longitude}")

        var location = Location("NAVISENS_LOCATION_PROVIDER")
        motionDna?.let { log.log(it) }
        if (syncGPS) {
            motionDna?.let {
                //Sync with currency Location
                location.reset()
                location.latitude = it.gpsLocation?.globalLocation?.latitude!!
                location.longitude = it.gpsLocation?.globalLocation?.longitude!!
            }
            locationchange = false
            syncGPS = false
        } else if (locationchange) {
            motionDna?.let {
                it.location.localLocation.clear()
                location.reset()
                app.resetLocalEstimation()
                app.resetLocalHeading()

                location.latitude = latLng?.latitude!!
                location.longitude = latLng?.longitude!!
                app.setLocationLatitudeLongitude(location.latitude, location.longitude)
                app.setLocationLatitudeLongitudeAndHeadingInDegrees(location.latitude, location.longitude, 315.0)

                if (it.movedSuccess()) {
                    locationchange = false
                }
            }
        } else {
            Timber.e("${latLng?.latitude} : ${location.latitude} : ${motionDna?.location?.globalLocation?.latitude!!}: ${motionDna?.gpsLocation?.globalLocation?.latitude!!}")
            motionDna?.let {
                val dy = it.location?.localLocation?.y!! / meter
                val dx = it.location?.localLocation?.x!! / meter
                val latitude = it.location?.globalLocation?.latitude!!
                val longitude = it.location?.globalLocation?.longitude!!

                val newLatitude = latitude + (dy / rearth) * (180 / pi)
                val newLongitude = longitude + (dx / rearth) * (180 / pi) / cos(latitude * pi / 180)

                location.latitude = newLatitude
                location.longitude = newLongitude
            }
        }

        // MotionDna to android.location object conversion
        motionDna?.let {
            location.bearing = it.location.heading.toFloat() // Bearing doesn't seemed to be used in COMPASS rendering however it is used in GPS render mode.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                location.bearingAccuracyDegrees = 1.0.toFloat()
            }
            location.altitude = it.location.absoluteAltitude
            location.accuracy = it.location.uncertainty.x.toFloat()
            location.time = it.timestamp.toLong()
            location.speed = it.motion.stepFrequency.toFloat()
        }
        var i = 0
        while (i < locationListeners.size) {
            locationListeners[i].onLocationChanged(location)
            ++i
        }
    }

    fun MotionDna.getLocalLocation(): MotionDna.Location? {
        return this.location
    }

    fun MotionDna.getGPSLocation(): MotionDna.Location? {
        return this.gpsLocation
    }

    private fun MotionDna.movedSuccess(): Boolean {
        return this.location?.globalLocation?.latitude == latLng?.latitude && this.location?.globalLocation?.longitude == latLng?.longitude
    }

    override fun reportError(errorCode: MotionDna.ErrorCode?, s: String?) {
        when (errorCode) {
            MotionDna.ErrorCode.ERROR_AUTHENTICATION_FAILED -> println("Error: authentication failed $s")
            MotionDna.ErrorCode.ERROR_SDK_EXPIRED -> println("Error: SDK expired $s")
            MotionDna.ErrorCode.ERROR_WRONG_FLOOR_INPUT -> println("Error: wrong floor input $s")
            MotionDna.ErrorCode.ERROR_PERMISSIONS -> println("Error: permissions not granted $s")
            MotionDna.ErrorCode.ERROR_SENSOR_MISSING -> println("Error: sensor missing $s")
            MotionDna.ErrorCode.ERROR_SENSOR_TIMING -> println("Error: sensor timing $s")
        }
    }

    override fun getPkgManager(): PackageManager {
        return this.pkg
    }
}