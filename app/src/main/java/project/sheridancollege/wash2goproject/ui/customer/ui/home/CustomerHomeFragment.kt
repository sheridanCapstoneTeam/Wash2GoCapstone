package project.sheridancollege.wash2goproject.ui.customer.ui.home

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.AppEnum
import project.sheridancollege.wash2goproject.common.DetailerServicesPrice
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.databinding.FragmentCustomerHomeBinding
import project.sheridancollege.wash2goproject.ui.customer.listener.CustomerHomeListener
import project.sheridancollege.wash2goproject.ui.detailer.ui.dialog.DetailerDetailsDialog
import project.sheridancollege.wash2goproject.ui.detailer.ui.home.DetailerHomeFragment
import project.sheridancollege.wash2goproject.util.Permission
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

@Suppress("DEPRECATION")
class CustomerHomeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    CustomerHomeListener {

    companion object {
        val TAG: String = CustomerHomeFragment::class.java.simpleName
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    private var _binding: FragmentCustomerHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private var user: User?=null
    private lateinit var locationCallback: LocationCallback
    val markerList: ArrayList<MarkerOptions> = ArrayList()
    private lateinit var customerHomeViewModel: CustomerHomeViewModel
    private lateinit var activeMarker: Marker
    private lateinit var detailerDetailDialog: DetailerDetailsDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Permission.hasLocationPermission(requireContext())) {
            findNavController().navigate(
                R.id.action_customerHomeToPermission,
                bundleOf("permissionFrom" to AppEnum.CUSTOMER)
            )
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        customerHomeViewModel =
            ViewModelProvider(this).get(CustomerHomeViewModel::class.java)

        _binding = FragmentCustomerHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
//        lastLocation = fusedLocationClient.lastLocation.result
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                lastLocation = locationResult.lastLocation
                Log.e(
                    DetailerHomeFragment.TAG,
                    "onLocation Update = " + lastLocation.latitude + "," + lastLocation.longitude
                )


                if (markerList.size > 0) {
                    val boundBuilder: LatLngBounds.Builder = LatLngBounds.builder()

                    boundBuilder.include(LatLng(lastLocation.latitude, lastLocation.longitude))

                    for (marker in markerList) {
                        boundBuilder.include(marker.position)
                    }

                    val bounds: LatLngBounds = boundBuilder.build()
                    val padding = 100
                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                    mMap.animateCamera(cameraUpdate)
                } else {
                    val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                }
            }
        }

        user = SharedPreferenceUtils.getUserDetails()

        if(user?.fcmToken == "N/A"){
            customerHomeViewModel.user.observe(viewLifecycleOwner){
                user = it
                SharedPreferenceUtils.saveUserDetails(user)
            }
            customerHomeViewModel.updateFCMToken()
        }

        createLocationRequest()

        customerHomeViewModel.getOnlineDetailers()
        customerHomeViewModel.onlineDetailers.observe(viewLifecycleOwner) {
            Log.e(TAG, "Online Detailers = " + it.size)
            Log.e(TAG, "Detailers Detail : " + Gson().toJson(it))
            drawDetailersOnMap(it)
        }

        customerHomeViewModel.detailerServicePrice.observe(viewLifecycleOwner) {
            Log.e(TAG, "Detailer service details : " + Gson().toJson(it))
            if (this::activeMarker.isInitialized) {
                detailerDetailDialog = DetailerDetailsDialog(this)

                val bundle = Bundle()
                bundle.putString("detailer_name", activeMarker.title)
                bundle.putString("detailer_user_id", activeMarker.tag.toString())
                bundle.putString("detailer_service_price", Gson().toJson(it))
                bundle.putParcelable("customerLocation",lastLocation)
                detailerDetailDialog.arguments = bundle
                detailerDetailDialog.show(
                    requireActivity().supportFragmentManager,
                    "DetailerDetailDialog"
                )
            }
        }

        return root
    }


    override fun onMarkerClick(marker: Marker): Boolean {
        Log.e(TAG, "Marker Click : " + marker.tag)
        customerHomeViewModel.getDetailerDetails(marker.tag.toString())
        activeMarker = marker
        return false
    }

    private fun drawDetailersOnMap(onlineDetailerList: ArrayList<User>?) {
        markerList.clear()
        mMap.clear()

        if (onlineDetailerList?.size != 0) {
            for (detailer in onlineDetailerList!!) {
                val marker = MarkerOptions()
                marker.position(LatLng(detailer.currentLat, detailer.currentLong))
                marker.title(detailer.firstName + " " + detailer.lastName)
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.detailer_map_icon))
                markerList.add(marker)

                var mapMarker: Marker? = mMap.addMarker(marker)
                mapMarker?.tag = detailer.userId

            }

            val boundBuilder: LatLngBounds.Builder = LatLngBounds.builder()

            if (this::lastLocation.isInitialized) {
                boundBuilder.include(LatLng(lastLocation.latitude, lastLocation.longitude))
            }

            for (marker in markerList) {
                boundBuilder.include(marker.position)
            }

            val bounds: LatLngBounds = boundBuilder.build()
            val padding = 100
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            mMap.animateCamera(cameraUpdate)
        } else {
            if (this::lastLocation.isInitialized) {
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            lastLocation.latitude,
                            lastLocation.longitude
                        ), 12f
                    )
                )
            }

        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }

        task.addOnFailureListener { e ->
            locationUpdateState = false
            if (e is ResolvableApiException) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        requireActivity(),
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            Log.e(DetailerHomeFragment.TAG, "Result Code $resultCode")
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()!!
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val startLatLng = LatLng(24.8800563, 67.1223229)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 12f))

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }

        mMap.setOnMarkerClickListener(this)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        if (locationUpdateState) {
            startLocationUpdates()
        }
    }

    override fun bookAppoinmentClick(
        detailerId: String,
        detailerServicePrice: DetailerServicesPrice,
        customerLocation: Location
    ) {
        if (detailerDetailDialog.isVisible) {
            detailerDetailDialog.dismiss()
        }
        Log.e(TAG, "detailerId : $detailerId")
        val bundle = Bundle()
        bundle.putString("detailerId", detailerId)
        bundle.putString("detailerServicePrice", Gson().toJson(detailerServicePrice))
        bundle.putParcelable("customerLocation", customerLocation)
        findNavController().navigate(
            R.id.action_go_to_schedule, bundle
        )
    }
}
