package com.helloworld.mapbox.mapbox_helloworld

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.activity_indoormap.*

class ImageViewMap : AppCompatActivity() {
    val MAP_URL = "https://i.stack.imgur.com/ZntUT.jpg"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_indoormap)
        Glide.with(this)
                .asBitmap()
                .load(MAP_URL)
                .into(object : CustomTarget<Bitmap>(){
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
}