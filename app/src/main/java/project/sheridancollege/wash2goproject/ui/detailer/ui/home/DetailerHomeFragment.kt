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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import project.sheridancollege.wash2goproject.common.Order
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.common.UserStatus
import project.sheridancollege.wash2goproject.databinding.FragmentDetailerHomeBinding
import project.sheridancollege.wash2goproject.ui.detailer.bottomsheet.JobBottomSheetFragment
import project.sheridancollege.wash2goproject.ui.maps.MapUtil
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils


@Suppress("DEPRECATION")
class DetailerHomeFragment : Fragment(), OnMapReadyCallback, BottomSheetClickListener,View.OnClickListener {

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
    private var user: User? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var detailerHomeViewModel: DetailerHomeViewModel
    private var jobBottomSheetFragment: JobBottomSheetFragment? = null
    private  var activeJobList: ArrayList<Order> = ArrayList()
    private  var newJobList: ArrayList<Order> = ArrayList()
    private  var completedJobList: ArrayList<Order> =  ArrayList()
    private  var declinedJobList: ArrayList<Order> = ArrayList()
    private  var order: Order? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        detailerHomeViewModel =
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
                /*val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))*/

                detailerHomeViewModel.updateCurrentLocation(locationResult)
            }
        }

        setLocationRequest()

        user = SharedPreferenceUtils.getUserDetails()


        if(user?.fcmToken == "N/A"){
            detailerHomeViewModel.user.observe(viewLifecycleOwner){
                user = it
                SharedPreferenceUtils.saveUserDetails(user)
            }
            detailerHomeViewModel.updateFCMToken()
        }

        when (user?.status) {
            UserStatus.ONLINE -> {
                goOnline()
            }
            UserStatus.OFFLINE -> {
                goOffline()
            }
        }

        initListeners()

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
            user?.currentLat = it.lastLocation.latitude
            user?.currentLong = it.lastLocation.longitude

            SharedPreferenceUtils.saveUserDetails(user)
        }

        detailerHomeViewModel.order.observe(viewLifecycleOwner){
            order = it

            detailerHomeViewModel.sendNotificationToCustomer(order)

            if(order?.status == AppEnum.DECLINED.toString() || order?.status == AppEnum.COMPLETED.toString()){
                order = null
            }

            setOrderView()
        }


        return root
    }

    private fun initListeners() {
        binding.statusBtn.setOnClickListener(this)

        binding.cardView1.setOnClickListener(this)
        binding.cardView2.setOnClickListener(this)
        binding.cardView3.setOnClickListener(this)
        binding.cardView4.setOnClickListener(this)

        binding.acceptBtn.setOnClickListener(this)
        binding.declineBtn.setOnClickListener(this)
        binding.arriveBtn.setOnClickListener(this)
        binding.startBtn.setOnClickListener(this)
        binding.finishBtn.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.status_btn -> {
                when (user?.status) {
                    UserStatus.ONLINE -> {
                        detailerHomeViewModel.updateUserStatus(UserStatus.OFFLINE)
                    }
                    UserStatus.OFFLINE -> {
                        detailerHomeViewModel.updateUserStatus(UserStatus.ONLINE)
                    }
                }
            }

            R.id.cardView1 -> {
                jobBottomSheetFragment = JobBottomSheetFragment.newInstance(activeJobList,AppEnum.ACTIVE.toString(),this)
                jobBottomSheetFragment?.show(childFragmentManager,"JobBottomSheet")
            }
            R.id.cardView2 -> {
                jobBottomSheetFragment = JobBottomSheetFragment.newInstance(newJobList,AppEnum.NEW.toString(),this)
                jobBottomSheetFragment?.show(childFragmentManager,"JobBottomSheet")
            }
            R.id.cardView3 -> {
                jobBottomSheetFragment = JobBottomSheetFragment.newInstance(completedJobList,AppEnum.COMPLETED.toString(),this)
                jobBottomSheetFragment?.show(childFragmentManager,"JobBottomSheet")
            }
            R.id.cardView4 -> {
                jobBottomSheetFragment = JobBottomSheetFragment.newInstance(declinedJobList,AppEnum.DECLINED.toString(),this)
                jobBottomSheetFragment?.show(childFragmentManager,"JobBottomSheet")
            }

            R.id.acceptBtn -> {
                detailerHomeViewModel.updateOrderStatus(order,user?.userId.toString(),AppEnum.ACTIVE)
            }
            R.id.declineBtn -> {
                detailerHomeViewModel.updateOrderStatus(order,user?.userId.toString(),AppEnum.DECLINED)
            }
            R.id.startBtn -> {
                detailerHomeViewModel.updateOrderStatus(order,user?.userId.toString(),AppEnum.STARTED)
            }
            R.id.arriveBtn -> {
                detailerHomeViewModel.updateOrderStatus(order,user?.userId.toString(),AppEnum.ARRIVED)
            }
            R.id.finishBtn -> {
                detailerHomeViewModel.updateOrderStatus(order,user?.userId.toString(),AppEnum.COMPLETED)
            }
        }
    }

    private fun loadDashboardData() {
        Log.e(TAG,"loadDashboardData")

        binding.helloTitleTv.text = "Hello ${user?.firstName} !"

        detailerHomeViewModel.detailerServicePrice.observe(viewLifecycleOwner) {
            binding.ratingBar.rating = it.rating.toFloat()
            binding.ratingTv.text = "(${it.rating.toFloat()})"
            binding.totalEarningTv.text = "${it.totalEarning}$"
        }
        detailerHomeViewModel.getRatingAndEarnings(user?.userId.toString())


        detailerHomeViewModel.orders.observe(viewLifecycleOwner) {

            if(jobBottomSheetFragment !=null && jobBottomSheetFragment?.isVisible!!){
                jobBottomSheetFragment?.dismiss()
            }

            activeJobList.clear()
            newJobList.clear()
            completedJobList.clear()
            declinedJobList.clear()

            if (it.isNullOrEmpty()) {
                binding.activeCountTv.text = activeJobList.size.toString()
                binding.newCountTv.text = newJobList.size.toString()
                binding.completeCountTv.text = completedJobList.size.toString()
                binding.cancelCountTv.text = declinedJobList.size.toString()
                return@observe
            }

            for(order in it){
                when(order.status) {
                    AppEnum.NEW.toString() -> {
                        newJobList.add(order)
                    }
                    AppEnum.STARTED.toString(),
                    AppEnum.ARRIVED.toString(),
                    AppEnum.ACTIVE.toString() -> {
                        activeJobList.add(order)
                    }
                    AppEnum.COMPLETED.toString() -> {
                        completedJobList.add(order)
                    }
                    AppEnum.DECLINED.toString() -> {
                        declinedJobList.add(order)
                    }
                }
            }
            binding.activeCountTv.text = activeJobList.size.toString()
            binding.newCountTv.text = newJobList.size.toString()
            binding.completeCountTv.text = completedJobList.size.toString()
            binding.cancelCountTv.text = declinedJobList.size.toString()
        }
        detailerHomeViewModel.getDetailerOrders(user?.userId.toString())

    }

    private fun goOnline() {
        loadDashboardData()
        startLocationRequest()
        binding.offlineGroup.visibility = View.GONE
        binding.onlineGroup.visibility = View.VISIBLE
        binding.widgetsView.setBackgroundColor(requireContext().getColor(android.R.color.transparent))
        binding.statusBtn.setBackgroundColor(requireContext().getColor(android.R.color.holo_red_dark))
        binding.statusBtn.text = "Go Offline"

        setOrderView()
    }

    private fun goOffline() {
        loadDashboardData()
        stopLocationUpdates()
        binding.offlineGroup.visibility = View.VISIBLE
        binding.onlineGroup.visibility = View.VISIBLE
        binding.widgetsView.setBackgroundColor(requireContext().getColor(R.color.blue_500_transparent))
        binding.statusBtn.setBackgroundColor(requireContext().getColor(android.R.color.holo_green_dark))
        binding.statusBtn.text = "Go Online"

        order = null
        setOrderView()
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
        if (locationUpdateState && user?.status == UserStatus.ONLINE) {
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

    override fun OrderViewBtnClick(order: Order) {
        if(user?.status == UserStatus.OFFLINE){
            Toast.makeText(requireContext(),"Please go online first!",Toast.LENGTH_SHORT).show()
            return
        }

        Log.e(TAG,"Selected order -> "+Gson().toJson(order))
        this.order = order

        setOrderView()


    }

    private fun setOrderView() {
        if(order == null){
            binding.orderStatusBtnsLyt.visibility = View.GONE
            if(::mMap.isInitialized){
                mMap.clear()
            }
            return
        }

        binding.orderStatusBtnsLyt.visibility = View.VISIBLE

        when(order?.status){
            AppEnum.NEW.toString() -> {
                binding.acceptDeclineGroup.visibility = View.VISIBLE
                binding.startBtn.visibility  = View.GONE
                binding.arriveBtn.visibility  = View.GONE
                binding.finishBtn.visibility  = View.GONE
            }
            AppEnum.ACTIVE.toString() -> {
                binding.acceptDeclineGroup.visibility = View.GONE
                binding.startBtn.visibility  = View.VISIBLE
                binding.arriveBtn.visibility  = View.GONE
                binding.finishBtn.visibility  = View.GONE
            }
            AppEnum.STARTED.toString() ->{
                binding.acceptDeclineGroup.visibility = View.GONE
                binding.startBtn.visibility  = View.GONE
                binding.arriveBtn.visibility  = View.VISIBLE
                binding.finishBtn.visibility  = View.GONE
            }
            AppEnum.ARRIVED.toString() ->{
                binding.acceptDeclineGroup.visibility = View.GONE
                binding.startBtn.visibility  = View.GONE
                binding.arriveBtn.visibility  = View.GONE
                binding.finishBtn.visibility  = View.VISIBLE
            }
        }

        mMap.clear()

        val boundBuilder: LatLngBounds.Builder = LatLngBounds.builder()

        val detailerMarker = MarkerOptions()

        if (this::lastLocation.isInitialized) {
            detailerMarker.position(LatLng(lastLocation.latitude, lastLocation.longitude))
            detailerMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.detailer_map_icon))
            mMap.addMarker(detailerMarker)

            boundBuilder.include(detailerMarker.position)
        }

        val customerMarker = MarkerOptions()
        customerMarker.position(LatLng(order?.customerLat!!, order?.customerLong!!))
        customerMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.customer_map_icon))
        mMap.addMarker(customerMarker)

        boundBuilder.include(customerMarker.position)

        mMap.addPolyline(MapUtil.getCurvePolyline(detailerMarker.position,customerMarker.position,5.0))

        val bounds: LatLngBounds = boundBuilder.build()
        val padding = 100
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mMap.animateCamera(cameraUpdate)

    }


}
interface BottomSheetClickListener {
    fun OrderViewBtnClick(order: Order)
}