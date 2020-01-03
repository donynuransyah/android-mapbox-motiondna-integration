package com.helloworld.mapbox.mapbox_helloworld.outdoormap

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

    private var mLatLng: LatLng? = null
    private var mMotionDNA: MotionDna? = null
    private lateinit var mLocation: Location
    private val meter = 500
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
    }


    override fun getAppContext(): Context {
        return this.ctx
    }

    override fun receiveNetworkData(p0: MotionDna?) {

    }

    fun locationChange(latLng: LatLng) {
        this.latLng = latLng
        this.locationchange = true
    }

    fun syncWithGPS() {
        this.syncGPS = true
    }

    override fun receiveNetworkData(p0: MotionDna.NetworkCode?, p1: MutableMap<String, out Any>?) {
    }

    fun getMotionDNA(): MotionDna = mMotionDNA!!


    @SuppressLint("LogNotTimber")
    override fun receiveMotionDna(motionDna: MotionDna?) {
        Timber.e("Sync GPS : $syncGPS")
        Timber.e("Sync GPS lat : ${motionDna?.getGPSLocation()?.globalLocation!!.latitude}")
        Timber.e("Sync GPS lon : ${motionDna.getGPSLocation()?.globalLocation!!.longitude}")
        Timber.e("Sync Local lat : ${motionDna.getLocalLocation()?.globalLocation!!.latitude}")
        Timber.e("Sync Local lon : ${motionDna.getLocalLocation()?.globalLocation!!.longitude}")

        mLocation = Location("NAVISENS_LOCATION_PROVIDER")
        motionDna?.let {
            log.log(it)
            mMotionDNA = it
        }
        if (syncGPS) {
            mMotionDNA?.let {
                mLocation.reset()
                mLocation.latitude = it.gpsLocation?.globalLocation?.latitude!!
                mLocation.longitude = it.gpsLocation?.globalLocation?.longitude!!
            }
            locationchange = false
            syncGPS = false
        } else if (locationchange) {
            //manual set location
            mMotionDNA?.let {
                it.invalidateLocation()
                mLocation.latitude = latLng?.latitude!!
                mLocation.longitude = latLng?.longitude!!
                app.setLocationLatitudeLongitude(mLocation.latitude, mLocation.longitude)
                app.setLocationLatitudeLongitudeAndHeadingInDegrees(mLocation.latitude, mLocation.longitude, it.location.heading)
                if (it.movedSuccess()) {
                    locationchange = false
                }
            }
        } else {
            //start walking
            mMotionDNA?.let {
                Timber.e("${latLng?.latitude} : ${mLocation.latitude} : ${it.location?.globalLocation?.latitude!!}: ${it.gpsLocation?.globalLocation?.latitude!!}")
                val latitude = it.location?.getCurrentLat()!!
                val longitude = it.location?.getCurrentLon()!!
                val dy = it.location?.localLocation?.y!! / meter
                val dx = it.location?.localLocation?.x!! / meter
                //set to current latlon
                val stepLat = latitude + (dy / rearth) * (180 / pi)
                val stepLong = longitude + (dx / rearth) * (180 / pi) / cos(latitude * pi / 180)
                mLatLng = LatLng(stepLat, stepLong, it.location.globalLocation.altitude)
                with(mLocation) {
                    mLatLng?.let {
                        this.latitude = it.latitude
                        this.longitude = it.longitude
                        this.altitude = it.altitude
                        mMotionDNA?.motion?.stepFrequency?.toFloat()?.let {
                            this.speed = it
                        }
                    }
                }
            }
        }
        var i = 0
        while (i < locationListeners.size) {
            locationListeners[i].onLocationChanged(mLocation)
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

    private fun MotionDna.invalidateLocation() {
        this.location.localLocation.clear()
        mLocation.reset()
        app.resetLocalEstimation()
        app.resetLocalHeading()

    }
}


private fun MotionDna.Location.getCurrentLon(): Double {
    return this.globalLocation?.longitude!!
}

private fun MotionDna.Location.getCurrentLat(): Double {
    return this.globalLocation?.latitude!!
}
