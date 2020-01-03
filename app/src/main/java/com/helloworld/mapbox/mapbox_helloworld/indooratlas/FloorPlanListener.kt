package com.helloworld.mapbox.mapbox_helloworld.indooratlas

class FloorPlanListener(var listener: ListenerFloorPlan?) {
    var currentFloorPlan = -1
    fun setFloorPlan(floorplan: Int){
        if (currentFloorPlan != floorplan) {
            listener?.onFloorChanged(floorplan)
            currentFloorPlan = floorplan
        }
    }
    fun removeListener(){
        listener = null
    }
}


interface ListenerFloorPlan{
    fun onFloorChanged(floorplan: Int)
}