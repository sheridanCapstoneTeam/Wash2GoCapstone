package project.sheridancollege.wash2goproject.ui.maps

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
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
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import project.sheridancollege.wash2goproject.ProviderLocation
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.databinding.FragmentMapsBinding
import project.sheridancollege.wash2goproject.service.TrackerService
import project.sheridancollege.wash2goproject.util.Constants.ACTION_SERVICE_START
import project.sheridancollege.wash2goproject.util.Constants.ACTION_SERVICE_STOP
import project.sheridancollege.wash2goproject.util.ExtensionFunctions.disabled
import project.sheridancollege.wash2goproject.util.ExtensionFunctions.enable
import project.sheridancollege.wash2goproject.util.ExtensionFunctions.hide
import project.sheridancollege.wash2goproject.util.ExtensionFunctions.show
import project.sheridancollege.wash2goproject.util.Permission.hasBackgroundLocationPermission
import project.sheridancollege.wash2goproject.util.Permission.requestBackgroundLocationPermission
import javax.inject.Inject


 class MapsFragment :  Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, EasyPermissions.PermissionCallbacks {
     @Inject
     lateinit var notification: NotificationCompat.Builder

     @Inject
     lateinit var notificationManager: NotificationManager
     var providerLocaion: HashMap<String, ProviderLocation> = HashMap()
     var customerLocaion: HashMap<String, ProviderLocation> =
         HashMap() //change type from provider to customer

     private val POLYLINE_STROKE_WIDTH_PX = 12
     private var _binding: FragmentMapsBinding? = null
     private val binding get() = _binding!!
     private lateinit var map: GoogleMap


     private var startTime = 0L
     private var stopTime = 0L

     //observe this list from our tracker service
     private var locationList = mutableListOf<LatLng>()

     //private var providerNearByLocation = mutableListOf<LatLng>()
     private lateinit var mlocation: Location
     private lateinit var plocation: Location


     override fun onCreateView(
         inflater: LayoutInflater, container: ViewGroup?,
         savedInstanceState: Bundle?
     ): View {


         mlocation = Location("")
         plocation = Location("")
         // Get the Intent that started this activity and extract the string
         val intent = activity?.intent
         var customerlot = intent?.getDoubleExtra("cusLot", 0.0)
         var customerlng = intent?.getDoubleExtra("cusLng", 0.0)


         //printing our results:
         print("this is maps fragment lot  " + customerlot + "this is maps fragment lng " + customerlng)

         // coorActivity c = new coorActivity()

         //Customers
         customerLocaion.put("1", ProviderLocation(customerlot, customerlng))

         //Providers
         providerLocaion.put("1", ProviderLocation(43.9675, -79.6877))
         providerLocaion.put("2", ProviderLocation(43.6125, -79.6573))
         providerLocaion.put("3", ProviderLocation(43.5512, -79.7206))
         providerLocaion.put("4", ProviderLocation(43.7162, -79.7426))


         // Inflate the layout for this fragment
         _binding = FragmentMapsBinding.inflate(inflater, container, false)

         binding.startBtn.setOnClickListener {
             onStartButtonClicked()
             var arrayOfResult: ArrayList<Double> = ArrayList<Double>()

             for ((key, value) in providerLocaion) {
                 println("$key = $value")
                 var listOfCustomerAndProvider: MutableList<LatLng> =
                     MapUtil.createAlocalList(
                         value.lat, value.lng,
                         customerLocaion.get("1")!!.lat, customerLocaion.get("1")!!.lng
                     )
                 var result = MapUtil.calculateTheDistance(listOfCustomerAndProvider)
                 print("This is the result" + result.toDouble())
                 arrayOfResult.add(result.toDouble())
             }
             print("This is a list")
             print(arrayOfResult)
             var nearestLoaction = arrayOfResult.minByOrNull { it }!!
             print("the samllest " + nearestLoaction)

             binding.resultTextView.text = "provider is " +
                     nearestLoaction.toString() + " km away"


             /* if (customerlot != null) {
                mlocation.latitude = customerlot
            }
            mlocation.longitude = customerlng!!


            plocation.latitude = providerLocaion.get("1")?.lat ?: 0.0
            plocation.longitude = providerLocaion.get("1")?.lng ?: 0.0


            var time = getDurationForRoute(mlocation.toString() , plocation.toString() )
            print("The time is " + time)*/

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
         val latLng = LatLng(60.374659, -111.494255) //Canada
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
         map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
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
             map.animateCamera(
                 CameraUpdateFactory.newCameraPosition(
                     MapUtil.setCameraPosition(
                         locationList.last()
                     )
                 ), 1000, null
             )
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


     /* fun getDurationForRoute( origin : String, destination : String ) : String
    {// - We need a context to access the API GeoApiContext

        val result = (

            GeoApiContext.Builder().apiKey("AIzaSyBcNe5mLxKAaeJSmsFz0F2E7jd-SmO_v5o").build() )

            var directionsResult: DirectionsResult = DirectionsApi.newRequest(result)

        .mode(TravelMode.DRIVING)
        .origin(origin)
        .destination(destination)
                .awaitIgnoreError()

        // - Parse the result
        var route: DirectionsRoute = directionsResult.routes[0];
        var leg: DirectionsLeg = route.legs [0]
        var duration: Duration = leg.duration
        return duration.humanReadable*/
 //}



//    override fun onLocationChanged(location: Location) {
//        mlocation = location
//        var latLng : LatLng = LatLng(location.latitude, location.longitude)
//        map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
//        val userid = FirebaseAuth.getInstance().currentUser?.providerId
//       geoFire.setLocation(userid, GeoLocation(location.latitude, location.longitude))
//    }

}

