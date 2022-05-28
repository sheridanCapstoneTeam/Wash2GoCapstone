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
import project.sheridancollege.wash2goproject.R


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

       /* if(hasLocationPermission(this)){
            navController.navigate(R.id.action_permissionFragment_to_mapsFragment)
        }*/


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

          mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
// pass the same server client ID used while implementing the LogIn feature earlier.

        mAuth = FirebaseAuth.getInstance()
       /* btnLogout.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(
                this@MainActivity,
                LoginAcivity::class.java
            )
            intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
           *//* mGoogleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(this, LoginAcivity::class.java)
                startActivity(intent)
                finish()
            }*//*
        }*/
    }
}










