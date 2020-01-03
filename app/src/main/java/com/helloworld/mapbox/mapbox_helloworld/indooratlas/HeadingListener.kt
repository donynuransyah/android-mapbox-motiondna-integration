package com.helloworld.mapbox.mapbox_helloworld.indooratlas

class HeadingListener(var listener : HeadingListenerInterface?) {

    fun setHeading(bearier : Float){
        listener?.onHeadingChanged(bearier)
    }

    fun removeListener(){
        listener = null
    }

}

interface HeadingListenerInterface{
    fun onHeadingChanged(value : Float)
}