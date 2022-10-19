package project.sheridancollege.wash2goproject.ui.maps

import android.graphics.Color
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import java.text.DecimalFormat
import java.util.*


object MapUtil {


    fun setCameraPosition(location: LatLng): CameraPosition {
        return CameraPosition.Builder()
            .target(location)
            .zoom(18f)
            .build()
    }

    fun calculateElapsedTime(startTime: Long, stopTime: Long): String {
        val elapsedTime = stopTime - startTime

        //get seconds
        val seconds = (elapsedTime / 1000).toInt() % 60

        //get minutes
        val minutes = (elapsedTime / (1000 * 60) % 60)

//        //get hours
//        val hours = (elapsedTime/ (1000*60*60) % 24)

        return "$minutes"
    }

    fun calculateTheDistance(locationList: MutableList<LatLng>): String {
        if (locationList.size > 1) {
            val meters =
                SphericalUtil.computeDistanceBetween(locationList[0], locationList.last())
            val kilometers = meters / 1000
            return DecimalFormat("#.##").format(kilometers)
        }
        return "0.00"
    }

    fun createAlocalList(lat: Double, lng: Double, latC: Double, lngC: Double)
            : MutableList<LatLng> {

        val localList = mutableListOf<LatLng>()
        val newLatLng = LatLng(lat, lng)
        val latLngC = LatLng(latC, lngC)
        localList.add(0, newLatLng)
        localList.add(1, latLngC)

        return localList
    }

    fun getCurvePolyline(p1: LatLng, p2: LatLng, k: Double): PolylineOptions {
        //Calculate distance and heading between two points
        val d = SphericalUtil.computeDistanceBetween(p1, p2)
        val h = SphericalUtil.computeHeading(p1, p2)

        //Midpoint position
        val p = SphericalUtil.computeOffset(p1, d * 0.5, h)

        //Apply some mathematics to calculate position of the circle center
        val x = (1 - k * k) * d * 0.5 / (2 * k)
        val r = (1 + k * k) * d * 0.5 / (2 * k)
        val c = SphericalUtil.computeOffset(p, x, h + 90.0)

        //Polyline options
        val options = PolylineOptions()
        val pattern: List<PatternItem> = Arrays.asList(Dash(30f),Gap(20f))


        //Calculate heading between circle center and two points
        val h1 = SphericalUtil.computeHeading(c, p1)
        val h2 = SphericalUtil.computeHeading(c, p2)

        //Calculate positions of points on circle border and add them to polyline options
        val numpoints = 100
        val step = (h2 - h1) / numpoints
        for (i in 0 until numpoints) {
            val pi = SphericalUtil.computeOffset(c, r, h1 + i * step)
            options.add(pi)
        }

        options.width(7f).color(Color.BLACK).geodesic(false).pattern(pattern)
        //Draw polyline
        return options
    }


}