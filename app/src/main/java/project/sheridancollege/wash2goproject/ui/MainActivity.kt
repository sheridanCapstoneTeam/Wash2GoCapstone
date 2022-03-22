package project.sheridancollege.wash2goproject.ui

import android.content.Intent
import android.location.LocationRequest
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.*
import com.google.firebase.auth.FirebaseAuth
import project.sheridancollege.wash2goproject.LoginAcivity
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.util.Permission.hasLocationPermission


class MainActivity : AppCompatActivity() {
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private var isPermissionGranted = false
    private lateinit var mapView: MapView
    lateinit var map: GoogleMap //var for map
    lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navController = findNavController(R.id.navHostFragment)

        if(hasLocationPermission(this)){
            navController.navigate(R.id.action_permissionFragment_to_mapsFragment)
        }

//        fusedLocationClient =
//            LocationServices.getFusedLocationProviderClient(this)
//      //  checkPermission()
//     //   if (isPermissionGranted) {
//      //  if (checkGoodlePlayServices()) {
//            val mapFragment =
//                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//            mapFragment.getMapAsync(this)
//
//            Toast.makeText(this, "Google play services are available", Toast.LENGTH_LONG).show()
//       // }
////        } else {
////            Toast.makeText(this, "Google play services are not available", Toast.LENGTH_LONG)
////                .show()
////        }
//
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
//
//
//
//        //RAMA
//        //retriving the current user iauto generated id
//        //val intent = getIntent()
//        //val currentUserId = intent.getStringExtra("currentUserId")
//
//
          mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
//
//
// pass the same server client ID used while implementing the LogIn feature earlier.
        val btnLogout: Button = findViewById(R.id.btnlogout)
        mAuth = FirebaseAuth.getInstance()
        btnLogout.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(
                this@MainActivity,
                LoginAcivity::class.java
            )
            intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            mGoogleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(this, LoginAcivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
////
////    private fun checkGoodlePlayServices(): Boolean {
////        var googleApiAvailability: GoogleApiAvailability = GoogleApiAvailability.getInstance()
////        var resutl = googleApiAvailability.isGooglePlayServicesAvailable(this)
////        if (resutl == ConnectionResult.SUCCESS) {
////            return true
////        } else if (googleApiAvailability.isUserResolvableError(resutl)) {
////            val dialog: Dialog = googleApiAvailability.getErrorDialog(this, resutl, 201,
////                DialogInterface.OnCancelListener {
////                    Toast.makeText(this, "User cancel Dialog", Toast.LENGTH_LONG).show()
////                    // Leave if services are unavailable.
////                    finish()
////                })!!
////            dialog.show()
////        }
////        return false
////    }
//
////    private fun checkPermission() {
////        Dexter.withContext(this)
////            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
////            .withListener(object : PermissionListener {
////                override fun onPermissionGranted(response: PermissionGrantedResponse) {
////                    isPermissionGranted = true
////                    val toast = Toast.makeText(
////                        applicationContext,
////                        "Access Granted", Toast.LENGTH_SHORT
////                    )
////                    toast.show()
////                }
////
////                override fun onPermissionDenied(response: PermissionDeniedResponse) {
////                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
////                    val uri: Uri = Uri.fromParts("package", packageName, "")
////                    intent.data = uri
////                    startActivity(intent)
////                }
////
////                override fun onPermissionRationaleShouldBeShown(
////                    permission: PermissionRequest?,
////                    token: PermissionToken?
////                ) {
////                }
////            }).check()
////    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        map = googleMap
//        val latLng = LatLng(60.374659, -111.494255) //Canada
//        googleMap.addMarker(
//            MarkerOptions()
//                .position(latLng)
//                .title("I am here")
//        )
//        map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
//        map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
//        map.uiSettings.apply {
//            isZoomControlsEnabled = true
//            isCompassEnabled = true
//            isScrollGesturesEnabledDuringRotateOrZoom = true
//            isMyLocationButtonEnabled = true
//        }
//      map.uiSettings.setAllGesturesEnabled(true)
//       map.mapType = GoogleMap.MAP_TYPE_NORMAL
//        checkLocationPermission()
////        if (ActivityCompat.checkSelfPermission(
////                this,
////                Manifest.permission.ACCESS_FINE_LOCATION
////            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
////                this,
////                Manifest.permission.ACCESS_COARSE_LOCATION
////            ) != PackageManager.PERMISSION_GRANTED
////        ) {
////            // TODO: Consider calling
////            //    ActivityCompat#requestPermissions
////            // here to request the missing permissions, and then overriding
////            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
////            //                                          int[] grantResults)
////            // to handle the case where the user grants the permission. See the documentation
////            // for ActivityCompat#requestPermissions for more details.
////            return
////        }
//       // getCurrentLocation()
//
//    }
//
//
////
////    private fun getCurrentLocation() {
////        if (isPermissionGranted) {
////            if (isLocationEnabled()) {
////                if (ActivityCompat.checkSelfPermission(
////                        this,
////                        Manifest.permission.ACCESS_FINE_LOCATION
////                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
////                        this,
////                        Manifest.permission.ACCESS_COARSE_LOCATION
////                    ) != PackageManager.PERMISSION_GRANTED
////                ) {
////                checkPermission()
////                    return
////                }
////                fusedLocationClient.lastLocation.addOnCompleteListener(this){ task->
////                    val location: Location?=task.result
////                    if(location == null){
////                        Toast.makeText(this, "Null Received", Toast.LENGTH_LONG).show()
////                    }else{
////
////                        Toast.makeText(this, location.latitude.toString() + " " + location.longitude.toString(),
////                            Toast.LENGTH_LONG).show()
////
////                    }
////
////                }
////
////            } else {
////                Toast.makeText(this, "turn on location", Toast.LENGTH_LONG).show()
////                val intent= Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
////                startActivity(intent)
////            }
////
////        } else {
////            checkPermission()
////
////        }
////    }
////
////    private fun isLocationEnabled(): Boolean {
////    val locationManager: LocationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
////            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
////                LocationManager.NETWORK_PROVIDER
////            )
////    }
////
////    companion object {
////            private const val PERMISSION_REQUEST_ACCESS_LOCATION=100
////        }
//
//    private fun checkLocationPermission(){
//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==
//                PackageManager.PERMISSION_GRANTED){
//            map.isMyLocationEnabled = true
//            val toast = Toast.makeText(
//                        applicationContext,
//                        "Access Granted", Toast.LENGTH_SHORT
//                    )
//                    toast.show()
//        }else{
//            requestPermissions()
//        }
//    }
//
//    private fun requestPermissions(){
//        ActivityCompat.requestPermissions(this,
//            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
//    }
//
//    @SuppressLint("MissingPermission")
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if(requestCode != 1){
//            return
//        }
//        if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//            Toast.makeText(this, "Already Enabled", Toast.LENGTH_SHORT).show()
//            map.isMyLocationEnabled = true
//        }else {
//            Toast.makeText(this, "We need permission!", Toast.LENGTH_SHORT).show()
//
//        }
//    }
}










