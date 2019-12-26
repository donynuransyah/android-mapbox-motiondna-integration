package com.helloworld.mapbox.mapbox_helloworld

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.navisens.motiondnaapi.MotionDna
import com.navisens.motiondnaapi.MotionDnaApplication
import kotlinx.android.synthetic.main.activity_indoormap.*
import kotlin.math.roundToInt


class ImageViewMap : AppCompatActivity() {
    private val LOG_TAG: String = javaClass.simpleName
    val MAP_URL = "https://i.stack.imgur.com/ZntUT.jpg"
    lateinit var bluedotMap: SubsamplingScaleImageView
    var motionDnaRuntimeSource: MotionDnaDataSource? = null


    override fun onResume() {
        super.onResume()
        //ActivityCompat.requestPermissions(this, MotionDnaApplication.needsRequestingPermissions(), MainActivity.REQUEST_MDNA_PERMISSIONS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_indoormap)

        Glide.with(this)
                .asBitmap()
                .load(MAP_URL)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        imageView.setImage(ImageSource.cachedBitmap(resource))
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // this is called when imageView is cleared on lifecycle call or for
                        // some other reason.
                        // if you are referencing the bitmap somewhere else too other than this imageView
                        // clear it here as you can no longer have the bitmap
                    }
                })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        //initNavi()
    }


    private val fieldImgXY = IntArray(2)


    // Use onWindowFocusChanged to get the placement of
    // the image because we have to wait until the image
    // has actually been placed on the screen  before we
    // get the coordinates. That makes it impossible to
    // do in onCreate, that would just give us (0, 0).
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        bluedotMap.getLocationOnScreen(fieldImgXY)
        Log.e(javaClass.simpleName, "fieldImage lockation on screen: " + xyString(fieldImgXY[0], fieldImgXY[1]))
    }
//
    private fun xyString(i: Int, i1: Int): String? {
        return "$i and $i1"
    }
//
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            Log.e(LOG_TAG, "touch event - down")
            val eventX = event.x.toInt()
            val eventY = event.y.toInt()
            Log.e(LOG_TAG, "event (x, y) = " + xyString(eventX, eventY))
            val xOnField = eventX - fieldImgXY[0]
            val yOnField = eventY - fieldImgXY[1]
            Log.e(LOG_TAG, "on field (x, y) = " + xyString(xOnField, yOnField))
        }
        return super.onTouchEvent(event)
    }

    private fun initNavi() {
        try {
            motionDnaRuntimeSource = MotionDnaDataSource(applicationContext, packageManager, MainActivity.navisensDevKey, object : MotionDnaDataSource.logListener {
                override fun log(log: MotionDna) {
                    val point = PointF(log.location.localLocation.x.toFloat(), log.location.localLocation.y.toFloat())
//                    bluedotMap.setDotCenter(point)
//                    bluedotMap.postInvalidate()
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
                            "heading : ${log.location.heading}"
                    findViewById<TextView>(R.id.logCat).text = logs
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}