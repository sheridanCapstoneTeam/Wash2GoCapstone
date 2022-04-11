package project.sheridancollege.wash2goproject.ui.maps

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import org.json.JSONArray
import org.json.JSONObject
import project.sheridancollege.wash2goproject.ProviderLocation
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.databinding.FragmentMapsBinding
import project.sheridancollege.wash2goproject.service.TrackerService
import project.sheridancollege.wash2goproject.ui.maps.model.Distance
import project.sheridancollege.wash2goproject.ui.maps.model.DistanceMatrix
import project.sheridancollege.wash2goproject.ui.maps.model.Duration
import project.sheridancollege.wash2goproject.util.Constants.ACTION_SERVICE_START
import project.sheridancollege.wash2goproject.util.Constants.ACTION_SERVICE_STOP
import project.sheridancollege.wash2goproject.util.ExtensionFunctions.disabled
import project.sheridancollege.wash2goproject.util.ExtensionFunctions.enable
import project.sheridancollege.wash2goproject.util.ExtensionFunctions.hide
import project.sheridancollege.wash2goproject.util.ExtensionFunctions.show
import project.sheridancollege.wash2goproject.util.Permission.hasBackgroundLocationPermission
import project.sheridancollege.wash2goproject.util.Permission.requestBackgroundLocationPermission
import project.sheridancollege.wash2goproject.util.coorActivity
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
    private lateinit var location : Location
    private val POLYLINE_STROKE_WIDTH_PX = 12
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private var mapUrl: String = ""
    private var startTime = 0L
    private var stopTime = 0L

    //observe this list from our tracker service
    private var locationList = mutableListOf<LatLng>()

    private lateinit var addressLatLng:LatLng

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        location = Location("")

        // Get the Intent that started this activity and extract the string
        val intent = activity?.intent
        var customerlot = intent?.getDoubleExtra(coorActivity.EXTRA_MESSAGELot, 0.0)
        var customerlng = intent?.getDoubleExtra(coorActivity.EXTRA_MESSAGELng, 0.0)
        if (customerlot != null) {
            location.latitude = customerlot
        }

        if (customerlng != null) {
            location.longitude = customerlng
        }
        addressLatLng = LatLng(customerlot!! ,customerlng!!)
        Log.d("TAG","Received Cor: $customerlot,$customerlng")
        //Customers
        customerLocaion.put("1", ProviderLocation(customerlot, customerlng))

        //Providers
        providerLocaion.put("1", ProviderLocation(43.9675, -79.6877))
        providerLocaion.put("2", ProviderLocation(43.6125, -79.6573))
        providerLocaion.put("3", ProviderLocation(43.5512, -79.7206))
        providerLocaion.put("4", ProviderLocation(43.7162, -79.7426))

        // Inflate the layout for this fragment
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        binding.hintTextView.hide()
        binding.startBtn.show()

        binding.startBtn.setOnClickListener {

            onStartButtonClicked()
            var arrayOfResult: ArrayList<Double> = ArrayList<Double>()
            var arrayTime: ArrayList<String> = ArrayList<String>()
            val client: OkHttpClient = OkHttpClient()
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
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$tempLatO%2C$tempLngO&destinations=$destinations&key=AIzaSyBcNe5mLxKAaeJSmsFz0F2E7jd-SmO_v5o"

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

                        val distanceMatrix = Gson().fromJson<DistanceMatrix>(response.body()!!.string(),DistanceMatrix::class.java)
                        val elements = distanceMatrix.rows[0].elements
                        val distances = mutableListOf<Distance?>()
                        val durations = mutableListOf<Duration?>()
                        var minDistance= Int.MAX_VALUE
                        var minDuration = Int.MAX_VALUE
                        var minDistanceIndex = 0
                        var dstSting = ""
                        var durString = ""

                        for ((i,element) in elements.withIndex()){
                             distances.add(element.distance)
                             durations.add(element.duration)
                            var dst = element.distance?.value ?: Int.MAX_VALUE

                            if(dst < minDistance){
                                minDistance = dst
                                dstSting = element.distance?.text.toString()
                                minDistanceIndex = i
                            }
                            val dur = element.duration?.value ?: Int.MAX_VALUE
                            if(dur < minDuration){
                                minDuration = dur
                                durString = element.duration?.text.toString()
                            }
                        }

                        val destProviderLocation = providerLocaion[(minDistanceIndex+1).toString()]
                        val destLatLng = LatLng (destProviderLocation?.lat ?: 0.0,destProviderLocation?.lng ?: 0.0)

                        requireActivity().runOnUiThread {
                            map.addMarker(
                                MarkerOptions().position(addressLatLng)
                                    .title("User Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            )

                            map.addMarker(
                                MarkerOptions().position(destLatLng)
                                    .title("Provider Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            )

                            map.addPolyline(
                                PolylineOptions()
                                    .add(addressLatLng)
                                    .add(destLatLng)
                            )

                            val cameraPosition = CameraPosition.Builder()
                                .target(destLatLng)
                                .bearing(45f)
                                .tilt(90f)
                                .zoom(map.cameraPosition.zoom)
                                .build()
                            map.animateCamera(
                                CameraUpdateFactory.newCameraPosition(cameraPosition),
                                ANIMATE_DURATION,object : GoogleMap.CancelableCallback{
                                    override fun onCancel() {

                                    }

                                    override fun onFinish() {
                                        map.addMarker(
                                            MarkerOptions().position(destLatLng)
                                                .title("Provider Location")
                                                .icon(generateBitmapDescriptor(requireActivity(),R.drawable.rectangle_shape,dstSting,durString))
                                                .anchor(1f,0f)
                                        )
                                    }

                                }
                            )
                        }
                    }
                }
            })
        }

        binding.stopBtn.setOnClickListener {
            onStopButtonClick()
        }

        binding.resetBtn.setOnClickListener {

        }

        return binding.root
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

        map.isMyLocationEnabled = true
        map.setOnMyLocationButtonClickListener(this)
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
        }
        map.uiSettings.setAllGesturesEnabled(true)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f))
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
       observeTrackerService()
    }


    private fun observeTrackerService() {
        TrackerService.locationList.observe(viewLifecycleOwner) {
            if (it != null) {
                locationList = it
                if (locationList.size > 1) {
                    binding.stopBtn.enable()
                }

                drawPolyLine()
                followPolyLine()
            }
        }

        TrackerService.startTime.observe(viewLifecycleOwner) {
            startTime = it
        }

        TrackerService.stopTime.observe(viewLifecycleOwner) {
            stopTime = it
        }
    }

    private fun drawPolyLine() {
        val polyLine = map.addPolyline(
            PolylineOptions().apply {
                width(POLYLINE_STROKE_WIDTH_PX.toFloat())
                color(Color.RED)
                jointType(JointType.ROUND)
                startCap(ButtCap())
                endCap(ButtCap())
                addAll(locationList)
            }
        )

    }

    //this function will set the camera position everytime we receive a new location
    //and for camera postion will choose last location in the locationlist
    private fun followPolyLine() {
        if (locationList.isNotEmpty()) {
            /*map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    MapUtil.setCameraPosition(
                        locationList.last()
                    )
                ), 1000, null*/

        }
    }

    private fun onStartButtonClicked() {
        if (hasBackgroundLocationPermission(requireContext())) {
            startCountDown()
            binding.startBtn.disabled()
            binding.startBtn.hide()
            binding.stopBtn.show()

        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    private fun onStopButtonClick() {
        stopForegroundService()
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
                sendActionCommandToService(ACTION_SERVICE_START)
            }
        }
        timer.start()

    }

    private fun stopForegroundService() {
        binding.startBtn.disabled()
        sendActionCommandToService(ACTION_SERVICE_STOP)
    }

    //send action to our service and start the service
    private fun sendActionCommandToService(action: String) {
        Intent(
            requireContext(),
            TrackerService::class.java
        ).apply {
            this.action = action
            requireContext().startService(this) //refer to intent
        }
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

     fun generateBitmapDescriptor(context:Context,resId:Int,distance:String,duration:String):BitmapDescriptor?{
        val drawable = ContextCompat.getDrawable(context,resId)

        drawable!!.setBounds(0,0,drawable.intrinsicWidth,drawable.intrinsicHeight)

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth,drawable.intrinsicHeight,Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        drawable.draw(canvas)

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.BLUE
        paint.textSize = 40f

        canvas.drawText("Distance: $distance",0f,(bitmap.height/3).toFloat(),paint)
        canvas.drawText("Duration: $duration",0f,(bitmap.height/1.5).toFloat(),paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    companion object{
        const val ANIMATE_DURATION = 10000
    }
}


