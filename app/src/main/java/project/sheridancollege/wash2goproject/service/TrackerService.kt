package project.sheridancollege.wash2goproject.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import dagger.hilt.android.AndroidEntryPoint
import project.sheridancollege.wash2goproject.util.Constants.ACTION_SERVICE_START
import project.sheridancollege.wash2goproject.util.Constants.ACTION_SERVICE_STOP
import project.sheridancollege.wash2goproject.util.Constants.LOCATION_FASTEST_UPDATE_INTERVAL
import project.sheridancollege.wash2goproject.util.Constants.LOCATION_UPDATE_INTERVAL
import project.sheridancollege.wash2goproject.util.Constants.NOTIFICATION_CHANNEL_ID
import project.sheridancollege.wash2goproject.util.Constants.NOTIFICATION_CHANNEL_NAME
import project.sheridancollege.wash2goproject.util.Constants.NOTIFICATION_ID
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import project.sheridancollege.wash2goproject.ui.maps.MapUtil
import project.sheridancollege.wash2goproject.ui.permission.PermissionFragment
import javax.inject.Inject

@AndroidEntryPoint
class TrackerService: LifecycleService() {

    @Inject
    lateinit var notification: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager


    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    companion object{
        val started = MutableLiveData<Boolean>()

        val locationList =  MutableLiveData<MutableList<LatLng>>()


        val startTime = MutableLiveData<Long>()
        val stopTime = MutableLiveData<Long>()
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result?.locations.let { locations ->
                for(location in locations){
                    updateLocationList(location)
                    updateNotificationPeriodically()
                }
            }

        }
        }


    private fun setInitialValue(){
        started.postValue(false)
        locationList.postValue(mutableListOf()) // empty list and update this list when you receive a new location
        startTime.postValue(0L)
        stopTime.postValue(0L)
    }

    public fun updateLocationList(location: Location){
        val newLatLng = LatLng(location.latitude, location.longitude)
        locationList.value?.apply {
            add(newLatLng)
            locationList.postValue(this)
        }

    }
    override fun onCreate() {
        setInitialValue()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let{
            when(it.action){
                ACTION_SERVICE_START -> {
                    started.postValue(true)
                    startForegroundService()
                    startLocationUpdates()
                }
                ACTION_SERVICE_STOP ->{
                    started.postValue(false)
                    stopForegroundService()
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopForegroundService() {
        removeLocationUpdate()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
            NOTIFICATION_ID)
        stopForeground(true)
        stopSelf()
        stopTime.postValue(System.currentTimeMillis())
    }

    private fun removeLocationUpdate() {
      fusedLocationProviderClient.removeLocationUpdates(locationCallback)

    }

    private fun startForegroundService(){
        createNotificationChannel()
        startForeground(
            NOTIFICATION_ID, notification.build()
        )
    }

    //get location update every 4 seconds
    @SuppressLint("MissingPermission")
    public fun startLocationUpdates(){
        val locationRequest = LocationRequest.create().apply {
            interval = LOCATION_UPDATE_INTERVAL.toLong() //on which we want to receive location update // LOCATION_UPDATE_INTERVAL = 4 seconds
            fastestInterval = LOCATION_FASTEST_UPDATE_INTERVAL.toLong()
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        startTime.postValue(System.currentTimeMillis())
       // observeTrackerService()

    }

    private fun updateNotificationPeriodically() {
        notification.apply {
            setContentTitle("Distance Travelled")
            setContentText(locationList.value?.let { MapUtil.calculateTheDistance(it) } + "km")
        }
        notificationManager.notify(NOTIFICATION_ID, notification.build())

    }


    private fun createNotificationChannel(){
        //o means 26 api and higher
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }


}