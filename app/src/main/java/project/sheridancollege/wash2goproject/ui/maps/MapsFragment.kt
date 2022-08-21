package project.sheridancollege.wash2goproject.ui.maps

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.graphics.*
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.example.DirectionApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONException
import project.sheridancollege.wash2goproject.ProviderLocation
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.databinding.FragmentMapsBinding
import project.sheridancollege.wash2goproject.ui.maps.model.Distance
import project.sheridancollege.wash2goproject.ui.maps.model.DistanceMatrix
import project.sheridancollege.wash2goproject.ui.maps.model.Duration
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.ExtensionFunctions.disabled
import project.sheridancollege.wash2goproject.util.ExtensionFunctions.hide
import project.sheridancollege.wash2goproject.util.ExtensionFunctions.show
import project.sheridancollege.wash2goproject.util.Permission
import project.sheridancollege.wash2goproject.util.Permission.hasLocationPermission
import project.sheridancollege.wash2goproject.util.Permission.requestBackgroundLocationPermission
import project.sheridancollege.wash2goproject.util.Permission.requestLocationPermission
import java.io.IOException
import javax.inject.Inject


@Suppress("DEPRECATION")
class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    EasyPermissions.PermissionCallbacks {
    @Inject
    lateinit var notification: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager
    var providerLocaion: HashMap<String, ProviderLocation> = HashMap()
    var customerLocaion: HashMap<String, ProviderLocation> =
        HashMap() //change type from provider to customer
    private lateinit var location: Location
    private val POLYLINE_STROKE_WIDTH_PX = 12
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private var mapUrl: String = ""
    private var startTime = 0L
    private var stopTime = 0L


    //observe this list from our tracker service
//    private var locationList = mutableListOf<LatLng>()

    private lateinit var addressLatLng: LatLng
    private lateinit var client: OkHttpClient

    private lateinit var dstSting: String
    private lateinit var durString: String
    private lateinit var listOfPolyPoints: List<LatLng>

    private lateinit var mCarMarker: Marker
    //private val args:MapsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        location = Location("")
        client = OkHttpClient()
        // Get the Intent that started this activity and extract the string
        //val customerlot = args.latitude.toDouble()
        //val customerlng = args.longitude.toDouble()
        val customerlot = 24.432421
        val customerlng = 63.45467576

        location.latitude = customerlot
        location.longitude = customerlng

        addressLatLng = LatLng(customerlot!!, customerlng!!)
        Log.d("TAG", "Received Cor: $customerlot,$customerlng")
        //Customers
        customerLocaion.put("1", ProviderLocation(customerlot, customerlng))

        //Providers
        providerLocaion.put("1", ProviderLocation(43.9675, -79.6877))
        providerLocaion.put("2", ProviderLocation(43.6125, -79.6573))
        providerLocaion.put("3", ProviderLocation(43.5512, -79.7206))
        providerLocaion.put("4", ProviderLocation(43.7162, -79.7426))
        providerLocaion.put("5", ProviderLocation(43.46984416247697, -79.70092069574098))

        if (!Permission.hasLocationPermission(requireContext())) {
            //findNavController().navigate(MapsFragmentDirections.actionMapsFragmentToPermissionFragment())
        } else {
            doOnPermissionGranted()
        }

        return binding.root
    }

    private fun doOnPermissionGranted() {


        binding.hintTextView.hide()
        binding.startBtn.show()


        binding.startBtn.setOnClickListener {
            onStartButtonClicked()
            callDisplayMatrixApi()

        }

        binding.stopBtn.setOnClickListener {
            onStopButtonClick()
        }

        binding.resetBtn.setOnClickListener {

        }
    }

    private fun callDirectionsApi(source: LatLng, destination: LatLng) {
        val url =
            "https://maps.googleapis.com/maps/api/directions/json?origin=${source.latitude},${source.longitude}&destination=${destination.latitude},${destination.longitude}&key=" + Constants.GOOGLE_API_KEY
        val urlMap = url
        val request = Request.Builder()
            .url(urlMap)
            .method("GET", null)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("TAG", "Exception : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        try {
                            val jsonStr = response.body()!!.string()
                            val directions =
                                Gson().fromJson<DirectionApi>(jsonStr, DirectionApi::class.java)
                            val encodedPolyLines = directions.routes[0].overviewPolyline?.points
                            listOfPolyPoints = decodePolyPoints(encodedPolyLines!!)
                            mPathPolygonPoints = mutableListOf()
                            mPathPolygonPoints.add(addressLatLng)
                            for (point in listOfPolyPoints) {
                                mPathPolygonPoints.add(point)
                            }
                            mPathPolygonPoints.add(destination)


                            requireActivity().runOnUiThread {
                                map.addMarker(
                                    MarkerOptions().position(addressLatLng)
                                        .title("User Location")
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_BLUE
                                            )
                                        )
                                )

                                map.addMarker(
                                    MarkerOptions().position(destination)
                                        .title("Provider Location")
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_GREEN
                                            )
                                        )
                                )

                                mMarkerIcon = ContextCompat.getDrawable(
                                    requireActivity(),
                                    R.drawable.caar_car
                                )!!.toBitmap()



                                mCarMarker = map.addMarker(
                                    MarkerOptions().position(addressLatLng)
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_ORANGE
                                            )
                                        ).flat(true).anchor(0.5.toFloat(), 0.5.toFloat())

                                )!!

                                map.addPolyline(
                                    PolylineOptions().addAll(listOfPolyPoints)
                                )
                                map.addPolyline(
                                    PolylineOptions()
                                        .add(addressLatLng)
                                        .add(mPathPolygonPoints[1])
                                )
                                map.addPolyline(
                                    PolylineOptions()
                                        .add(mPathPolygonPoints[mPathPolygonPoints.size - 2])
                                        .add(destination)
                                )

                                animateCarMove(
                                    mCarMarker,
                                    mPathPolygonPoints[0],
                                    mPathPolygonPoints[1],
                                    MOVE_ANIMATION_DURATION
                                )

                                /* val cameraPosition = CameraPosition.Builder()
                                     .target(destination)
                                     .bearing(45f)
                                     .tilt(90f)
                                     .zoom(map.cameraPosition.zoom)
                                     .build()

                                 map.animateCamera(
                                     CameraUpdateFactory.newCameraPosition(cameraPosition),
                                     ANIMATE_DURATION, object : GoogleMap.CancelableCallback {
                                         override fun onCancel() {

                                         }

                                         override fun onFinish() {
                                             map.addMarker(
                                                 MarkerOptions().position(destination)
                                                     .title("Provider Location")
                                                     .icon(
                                                         generateBitmapDescriptor(
                                                             requireActivity(),
                                                             R.drawable.rectangle_shape,
                                                             dstSting,
                                                             durString
                                                         )
                                                     )
                                                     .anchor(1f, 0f)
                                             )
                                         }

                                     }
                                 )*/
                            }

                        } catch (e: JSONException) {
                            throw JSONException("Unexpected error ${e.message}")
                        }
                    } else {
                        throw IOException("Unexpected error $response")
                    }

                }
            }

        })

    }


    private fun decodePolyPoints(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly

    }

    private fun callDisplayMatrixApi() {
        var arrayOfResult: ArrayList<Double> = ArrayList<Double>()
        var arrayTime: ArrayList<String> = ArrayList<String>()
        var destinations: String = ""

        for ((key, value) in providerLocaion) {
            println("$key = $value")
            var listOfCustomerAndProvider: MutableList<LatLng> =
                MapUtil.createAlocalList(
                    value.lat, value.lng,
                    customerLocaion.get("1")!!.lat, customerLocaion.get("1")!!.lng
                )

            //                var result = MapUtil.calculateTheDistance(listOfCustomerAndProvider)
            //                print("This is the result" + result.toDouble())
            //                arrayOfResult.add(result.toDouble())

            var tempLatD = value.lat.toString()
            var tempLngD = value.lng.toString()
            destinations += tempLatD + "%2C" + tempLngD + "%7C"
        }
        var tempLatO = customerLocaion.get("1")!!.lat
        var tempLngO = customerLocaion.get("1")!!.lng
        mapUrl =
            "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$tempLatO%2C$tempLngO&destinations=$destinations&key=" + Constants.GOOGLE_API_KEY


        val urlMap = mapUrl
        val request = Request.Builder()
            .url(urlMap)
            .build()
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                print("we got error")
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val distanceMatrix = Gson().fromJson<DistanceMatrix>(
                        response.body()!!.string(),
                        DistanceMatrix::class.java
                    )
                    val elements = distanceMatrix.rows[0].elements
                    val distances = mutableListOf<Distance?>()
                    val durations = mutableListOf<Duration?>()
                    var minDistance = Int.MAX_VALUE
                    var minDuration = Int.MAX_VALUE
                    var minDistanceIndex = 0
                    dstSting = ""
                    durString = ""

                    for ((i, element) in elements.withIndex()) {
                        distances.add(element.distance)
                        durations.add(element.duration)
                        var dst = element.distance?.value ?: Int.MAX_VALUE

                        if (dst < minDistance) {
                            minDistance = dst
                            dstSting = element.distance?.text.toString()
                            minDistanceIndex = i
                        }
                        val dur = element.duration?.value ?: Int.MAX_VALUE
                        if (dur < minDuration) {
                            minDuration = dur
                            durString = element.duration?.text.toString()
                        }
                    }

                    val destProviderLocation = providerLocaion[(minDistanceIndex + 1).toString()]
                    val destLatLng =
                        LatLng(destProviderLocation?.lat ?: 0.0, destProviderLocation?.lng ?: 0.0)
                    binding.resultTextView.text = "\tThe distance is : " + dstSting +
                            "\n\tThe duration is: " + durString
                    callDirectionsApi(addressLatLng, destLatLng)
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap!!
        val latLng = LatLng(location.latitude, location.longitude) //Canada
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("I am here")
        )

        if (Permission.hasLocationPermission(requireActivity())) {
            map.isMyLocationEnabled = true
            map.setOnMyLocationButtonClickListener(this)
        }
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
        }
        map.uiSettings.setAllGesturesEnabled(true)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

    }

    private fun onStartButtonClicked() {
        if (hasLocationPermission(requireContext())) {
            startCountDown()

            binding.startBtn.disabled()
            binding.startBtn.hide()
            binding.stopBtn.hide()

        } else {
            requestLocationPermission(this)
        }
    }

    private fun onStopButtonClick() {
        binding.stopBtn.hide()
        binding.startBtn.show()
    }

    private fun startCountDown() {
        binding.timerTextView.show()
        binding.stopBtn.disabled()

        val timer: CountDownTimer = object : CountDownTimer(4000, 1000) {
            override fun onTick(milliSecondUntilFinished: Long) {
                val currentSecond = milliSecondUntilFinished / 1000
                if (currentSecond.toString() == "0") {
                    binding.timerTextView.text = "GO"
                    binding.timerTextView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
                } else {
                    binding.timerTextView.text = currentSecond.toString()
                    binding.timerTextView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorRed
                        )
                    )
                }
            }

            override fun onFinish() {
                binding.timerTextView.hide()
            }
        }
        timer.start()

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onStartButtonClicked()
    }

    override fun onMyLocationButtonClick(): Boolean {
        binding.hintTextView.animate().alpha(0f).duration = 1500
        lifecycleScope.launch {
            delay(2500)
            binding.hintTextView.hide()
            binding.startBtn.show()
        }
        return false
    }

    //    fun displayNotification(value: Double){
//        notification.apply {
//            setContentTitle("Distance Travelled")
//            setContentText(value.toString() + "km")
//        }
//        notificationManager.notify(Constants.NOTIFICATION_ID, notification.build())
//    }
    override fun onDestroyView() { //to avoid memory leaks
        super.onDestroyView()
        _binding = null
    }

    fun generateBitmapDescriptor(
        context: Context,
        resId: Int,
        distance: String,
        duration: String
    ): BitmapDescriptor? {
        val drawable = ContextCompat.getDrawable(context, resId)

        drawable!!.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.draw(canvas)

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.BLUE
        paint.textSize = 40f

        canvas.drawText("Distance: $distance", 0f, (bitmap.height / 3).toFloat(), paint)
        canvas.drawText("Duration: $duration", 0f, (bitmap.height / 1.5).toFloat(), paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private lateinit var mPathPolygonPoints: MutableList<LatLng>
    private var mIndexCurrentPoint = 0
    private lateinit var mMarkerIcon: Bitmap

    private fun nextTurnAnimation() {
        mIndexCurrentPoint++
        if (mIndexCurrentPoint < mPathPolygonPoints.size - 1) {
            animateCamera(mPathPolygonPoints.get(mIndexCurrentPoint + 1))
            val prevLatLng: LatLng = mPathPolygonPoints.get(mIndexCurrentPoint - 1)
            val currLatLng: LatLng = mPathPolygonPoints.get(mIndexCurrentPoint)
            val nextLatLng: LatLng = mPathPolygonPoints.get(mIndexCurrentPoint + 1)
            val beginAngle = (180 * getAngle(prevLatLng, currLatLng) / Math.PI).toFloat()
            val endAngle = (180 * getAngle(currLatLng, nextLatLng) / Math.PI).toFloat()
            animateCarTurn(mCarMarker, beginAngle, endAngle, TURN_ANIMATION_DURATION)
        }
    }

    private fun animateCarMove(
        marker: Marker,
        beginLatLng: LatLng,
        endLatLng: LatLng,
        duration: Long
    ) {
        val handler = Handler()
        val startTime = SystemClock.uptimeMillis()
        val interpolator: Interpolator = LinearInterpolator()

        // set car bearing for current part of path
        val angleDeg = (180 * getAngle(beginLatLng, endLatLng) / Math.PI).toFloat()
        val matrix = Matrix()
        matrix.postRotate(angleDeg)

        marker.setIcon(
            BitmapDescriptorFactory.fromBitmap(
                Bitmap.createBitmap(
                    mMarkerIcon,
                    0,
                    0,
                    mMarkerIcon.getWidth(),
                    mMarkerIcon.getHeight(),
                    matrix,
                    true
                )
            )
        )
        handler.post(object : Runnable {
            override fun run() {
                // calculate phase of animation
                val elapsed = SystemClock.uptimeMillis() - startTime
                val t: Float = interpolator.getInterpolation(elapsed.toFloat() / duration)
                // calculate new position for marker
                val lat = (endLatLng.latitude - beginLatLng.latitude) * t + beginLatLng.latitude
                var lngDelta = endLatLng.longitude - beginLatLng.longitude
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360
                }
                val lng = lngDelta * t + beginLatLng.longitude
                marker.position = LatLng(lat, lng)

                // if not end of line segment of path
                if (t < 1.0) {
                    // call next marker position
                    handler.postDelayed(this, 16)
                } else {
                    // call turn animation
                    nextTurnAnimation()
                }
            }
        })
    }

    private fun getAngle(beginLatLng: LatLng, endLatLng: LatLng): Double {
        val f1 = Math.PI * beginLatLng.latitude / 180
        val f2 = Math.PI * endLatLng.latitude / 180
        val dl = Math.PI * (endLatLng.longitude - beginLatLng.longitude) / 180
        return Math.atan2(
            Math.sin(dl) * Math.cos(f2),
            Math.cos(f1) * Math.sin(f2) - Math.sin(f1) * Math.cos(f2) * Math.cos(dl)
        )
    }

    private fun animateCarTurn(marker: Marker, startAngle: Float, endAngle: Float, duration: Long) {
        val handler = Handler()
        val startTime = SystemClock.uptimeMillis()
        val interpolator: Interpolator = LinearInterpolator()
        val dAndgle = endAngle - startAngle
        val matrix = Matrix()
        matrix.postRotate(startAngle)
        val rotatedBitmap = Bitmap.createBitmap(
            mMarkerIcon,
            0,
            0,
            mMarkerIcon.getWidth(),
            mMarkerIcon.getHeight(),
            matrix,
            true
        )
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(rotatedBitmap))
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - startTime
                val t = interpolator.getInterpolation(elapsed.toFloat() / duration)
                val m = Matrix()
                m.postRotate(startAngle + dAndgle * t)
                marker.setIcon(
                    BitmapDescriptorFactory.fromBitmap(
                        Bitmap.createBitmap(
                            mMarkerIcon,
                            0,
                            0,
                            mMarkerIcon.getWidth(),
                            mMarkerIcon.getHeight(),
                            m,
                            true
                        )
                    )
                )
                if (t < 1.0) {
                    handler.postDelayed(this, 16)
                } else {
                    nextMoveAnimation()
                }
            }
        })
    }

    private fun nextMoveAnimation() {
        if (mIndexCurrentPoint < mPathPolygonPoints.size - 1) {
            animateCamera(mPathPolygonPoints.get(mIndexCurrentPoint + 1))
            animateCarMove(
                mCarMarker,
                mPathPolygonPoints.get(mIndexCurrentPoint),
                mPathPolygonPoints.get(mIndexCurrentPoint + 1),
                MOVE_ANIMATION_DURATION
            )
        }
    }

    fun animateCamera(targetLatLng: LatLng) {
        val cameraPosition = CameraPosition.Builder()
            .target(targetLatLng)
            .bearing(45f)
            .tilt(90f)
            .zoom(map.cameraPosition.zoom)
            .build()


        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition),
            MOVE_ANIMATION_DURATION.toInt(),
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    if (mIndexCurrentPoint == mPathPolygonPoints.size - 1) {
                        map.addMarker(
                            MarkerOptions().position(targetLatLng)
                                .title("Marker on Point B").icon(
                                    generateBitmapDescriptor(
                                        requireActivity(),
                                        R.drawable.rectangle_shape,
                                        dstSting,
                                        durString
                                    )
                                ).anchor(1f, 0f)
                        )
                    }

                }

                override fun onCancel() {}
            }
        )

    }


    companion object {
        const val MOVE_ANIMATION_DURATION = 1000L
        const val TURN_ANIMATION_DURATION = 100L

        const val ANIMATE_DURATION = 10000
    }


}


