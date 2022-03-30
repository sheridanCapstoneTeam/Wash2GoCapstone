package project.sheridancollege.wash2goproject.ui.maps

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.text.DecimalFormat

object MapUtil {


    fun setCameraPosition(location:LatLng): CameraPosition{
        return CameraPosition.Builder()
            .target(location)
            .zoom(18f)
            .build()
    }

    fun calculateElapsedTime(startTime:Long, stopTime:Long): String{
        val elapsedTime = stopTime - startTime

        //get seconds
        val seconds = (elapsedTime/ 1000).toInt() % 60

        //get minutes
        val minutes = (elapsedTime/ (1000*60) % 60)

        //get hours
        val hours = (elapsedTime/ (1000*60*60) % 24)

        return "$minutes"
    }

    fun calculateTheDistance(locationList: MutableList<LatLng>): String{
        if(locationList.size > 1){
            val meters =
                SphericalUtil.computeDistanceBetween(locationList[0], locationList.last())
            val kilometers = meters / 1000
            return DecimalFormat("#.##").format(kilometers)
        }
        return "0.00"
    }

    fun createAlocalList(lat:Double, lng:Double, latC:Double, lngC: Double)
    : MutableList<LatLng>{

        val localList = mutableListOf<LatLng>()
        val newLatLng = LatLng(lat, lng)
        val latLngC = LatLng(latC, lngC)
        localList.add(0, newLatLng)
        localList.add(1,  latLngC)

        return localList
    }
}