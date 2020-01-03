package com.helloworld.mapbox.mapbox_helloworld.indoormap

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.helloworld.mapbox.mapbox_helloworld.outdoormap.MainActivity
import com.helloworld.mapbox.mapbox_helloworld.outdoormap.MotionDnaDataSource
import com.helloworld.mapbox.mapbox_helloworld.R
import com.helloworld.mapbox.mapbox_helloworld.imagelibrary.ImageSource
import com.navisens.motiondnaapi.MotionDna
import kotlinx.android.synthetic.main.activity_indoormap.*
import kotlin.math.roundToInt


class ImageViewMap : AppCompatActivity() {
    private var centerX: Int = 0
    private var centerY: Int = 0
    private var mapBitmap: Bitmap? = null
    private val LOG_TAG: String = javaClass.simpleName
    val MAP_URL = "https://i.stack.imgur.com/ZntUT.jpg"
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
                        mapBitmap = resource
                        imageView.setImage(ImageSource.cachedBitmap(resource))
                        initNavi()
                        var centreX = imageView.x + imageView.width / 2
                        var centreY = imageView.y + imageView.height / 2
                        imageView.setInitializeDot(PointF(centreX, centreY))
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

    }




    private val fieldImgXY = IntArray(2)
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        imageView.getLocationOnScreen(fieldImgXY)
        centerX = fieldImgXY[0] / 2
        centerY = fieldImgXY[1] / 2
        imageView.setInitializeDot(PointF(centerX.toFloat(), centerY.toFloat()))
        Log.e(javaClass.simpleName, "fieldImage lockation on screen: " + xyString(fieldImgXY[0], fieldImgXY[1]))
    }

    //
    private fun xyString(i: Int, i1: Int): String? {
        return "$i and $i1"
    }

    val ZOOM = 2
    //
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        Toast.makeText(this@ImageViewMap, "EVENT ${event.action}", Toast.LENGTH_SHORT).show()
//        if (event.pointerCount == ZOOM) {
//            Toast.makeText(this@ImageViewMap, "ZOOM ${event.action}:${event.pointerCount}", Toast.LENGTH_SHORT).show()
//        }
//        if (event.action == MotionEvent.ACTION_DOWN) {
//            Toast.makeText(this@ImageViewMap, "DOWN", Toast.LENGTH_SHORT).show()
//            Log.e(LOG_TAG, "touch event - down")
//            val eventX = event.x.toInt()
//            val eventY = event.y.toInt()
//            Log.e(LOG_TAG, "event (x, y) = " + xyString(eventX, eventY))
//            val xOnField = eventX - fieldImgXY[0]
//            val yOnField = eventY - fieldImgXY[1]
//            Log.e(LOG_TAG, "on field (x, y) = " + xyString(xOnField, yOnField))

//            mapBitmap?.let {
//                val bitmapWidth: Float = it.width.toFloat()
//                val bitmapHeight: Float = it.height.toFloat()
//
//                val imageWidth: Float = imageView.width.toFloat()
//                val imageHeight: Float = imageView.height.toFloat()
//
//                val proportionateWidth = bitmapWidth / imageWidth
//                val proportionateHeight = bitmapHeight / imageHeight
//
//                val tapX = (event.x * proportionateWidth).toInt()
//                val tapY = (event.y * proportionateHeight).toInt()
//                Toast.makeText(this@ImageViewMap, "$tapX : $tapY", Toast.LENGTH_SHORT).show()
//            } ?: Toast.makeText(this@ImageViewMap, "Map not ready yet", Toast.LENGTH_SHORT).show()
//        }
//        return super.onTouchEvent(event)
//    }

    private fun initNavi() {
        try {
            motionDnaRuntimeSource = MotionDnaDataSource(applicationContext, packageManager, MainActivity.navisensDevKey, object : MotionDnaDataSource.logListener {
                override fun log(log: MotionDna) {
                    val point = PointF(log.location.localLocation.x.toFloat(), log.location.localLocation.y.toFloat())
                    imageView.setHeading(log.location.heading)
                    imageView.moveDot(point)

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