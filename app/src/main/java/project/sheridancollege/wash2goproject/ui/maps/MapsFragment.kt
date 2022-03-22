package project.sheridancollege.wash2goproject.ui.maps

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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


class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, EasyPermissions.PermissionCallbacks{

    private val POLYLINE_STROKE_WIDTH_PX = 12
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap

    private var startTime = 0L
    private var stopTime = 0L

    //observe this list from our tracker service
    private var locationList = mutableListOf<LatLng>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
     _binding = FragmentMapsBinding.inflate(inflater, container, false)

        binding.startBtn.setOnClickListener {
            onStartButtonClicked()

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
    override fun onMapReady(googleMap: GoogleMap){
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

    private fun observeTrackerService(){
        TrackerService.locationList.observe(viewLifecycleOwner) {
            if (it != null) {
                locationList = it
                if(locationList.size > 1){
                    binding.stopBtn.enable()
                }
                drawPolyLine()
                followPolyLine()
            }
        }

        TrackerService.startTime.observe(viewLifecycleOwner){
            startTime = it
        }

        TrackerService.stopTime.observe(viewLifecycleOwner){
            stopTime = it
        }
    }

    private fun drawPolyLine(){
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
    private fun followPolyLine(){
        if(locationList.isNotEmpty()){
            map.animateCamera(CameraUpdateFactory.newCameraPosition(
                MapUtil.setCameraPosition(
                    locationList.last()
                )), 1000, null)
        }
    }

    private fun onStartButtonClicked() {
        if(hasBackgroundLocationPermission(requireContext())){
            startCountDown()
            binding.startBtn.disabled()
            binding.startBtn.hide()
            binding.stopBtn.show()

        }else {
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

        val timer : CountDownTimer = object : CountDownTimer(4000, 1000){
            override fun onTick(milliSecondUntilFinished: Long) {
               val currentSecond = milliSecondUntilFinished / 1000
                if(currentSecond.toString() == "0"){
                    binding.timerTextView.text = "GO"
                    binding.timerTextView.setTextColor(ContextCompat.getColor(requireContext(),
                        R.color.black
                    ))
                }else {
                    binding.timerTextView.text = currentSecond.toString()
                    binding.timerTextView.setTextColor(ContextCompat.getColor(requireContext(),
                        R.color.colorRed
                    ))
                }
            }

            override fun onFinish() {
              binding.timerTextView.hide()
                sendActionCommandToService(ACTION_SERVICE_START)
            }
        }
        timer.start()

    }

    private fun stopForegroundService(){
        binding.startBtn.disabled()
        sendActionCommandToService(ACTION_SERVICE_STOP)
    }
//send action to our service and start the service
    private fun sendActionCommandToService(action: String){
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
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        }else{
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
    override fun onDestroyView() { //to avoid memory leaks
        super.onDestroyView()
        _binding = null
    }
}