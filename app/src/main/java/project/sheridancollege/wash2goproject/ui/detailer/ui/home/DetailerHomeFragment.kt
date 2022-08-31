package project.sheridancollege.wash2goproject.ui.detailer.ui.home

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.common.UserStatus
import project.sheridancollege.wash2goproject.databinding.FragmentDetailerHomeBinding
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

@Suppress("DEPRECATION")
class DetailerHomeFragment : Fragment(), OnMapReadyCallback {

    companion object {
        val TAG: String = DetailerHomeFragment::class.java.simpleName
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    private var _binding: FragmentDetailerHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private lateinit var user: User
    private lateinit var locationCallback: LocationCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val detailerHomeViewModel =
            ViewModelProvider(this).get(DetailerHomeViewModel::class.java)

        _binding = FragmentDetailerHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                lastLocation = locationResult.lastLocation
                Log.e(
                    TAG,
                    "onLocation Update = " + lastLocation.latitude + "," + lastLocation.longitude
                )
                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))

                detailerHomeViewModel.updateCurrentLocation(locationResult)
            }
        }

        setLocationRequest()

        user = SharedPreferenceUtils.getUserDetails()


        when (user.status) {
            UserStatus.ONLINE -> {
                goOnline()
            }
            UserStatus.OFFLINE -> {
                goOffline()
            }
        }

        binding.statusBtn.setOnClickListener(View.OnClickListener {
            when (user.status) {
                UserStatus.ONLINE -> {
                    detailerHomeViewModel.updateUserStatus(UserStatus.OFFLINE)
                }
                UserStatus.OFFLINE -> {
                    detailerHomeViewModel.updateUserStatus(UserStatus.ONLINE)
                }
            }
        })

        detailerHomeViewModel.user.observe(viewLifecycleOwner) {
            user = it
            SharedPreferenceUtils.saveUserDetails(user)
            when (it.status) {
                UserStatus.ONLINE -> {
                    goOnline()
                }
                UserStatus.OFFLINE -> {
                    goOffline()
                }
            }
        }

        detailerHomeViewModel.userLocation.observe(viewLifecycleOwner) {
            user.currenLat = it.lastLocation.latitude
            user.currentLong = it.lastLocation.longitude

            SharedPreferenceUtils.saveUserDetails(user)
        }



        return root
    }

    private fun goOnline() {
        startLocationRequest()
        binding.offlineGroup.visibility = View.GONE
        binding.widgetsView.setBackgroundColor(requireContext().getColor(android.R.color.transparent))
        binding.statusBtn.setBackgroundColor(requireContext().getColor(android.R.color.holo_red_dark))
        binding.statusBtn.text = "Go Offline"
    }

    private fun goOffline() {
        stopLocationUpdates()
        binding.offlineGroup.visibility = View.VISIBLE
        binding.widgetsView.setBackgroundColor(requireContext().getColor(R.color.blue_500_transparent))
        binding.statusBtn.setBackgroundColor(requireContext().getColor(android.R.color.holo_green_dark))
        binding.statusBtn.text = "Go Online"
    }

    private fun setLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun startLocationRequest() {

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
            Log.e(TAG, "Result Code $resultCode")
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        if (locationUpdateState && user.status == UserStatus.ONLINE) {
            startLocationUpdates()
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
    }
}