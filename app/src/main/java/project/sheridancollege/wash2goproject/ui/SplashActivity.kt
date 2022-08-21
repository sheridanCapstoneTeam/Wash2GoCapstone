package project.sheridancollege.wash2goproject.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import com.google.gson.Gson
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.Config
import project.sheridancollege.wash2goproject.ui.authentication.MainActivity
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class SplashActivity : AppCompatActivity() {

    companion object {
        val TAG: String = SplashActivity::class.java.simpleName
    }
    private lateinit var valueEventListener:ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        valueEventListener = AppClass.databaseReference.child(Constants.GET_CONFIG)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val myConfig = Config(
                        snapshot.child("Services").value.toString().split(","),
                        snapshot.child("AddsOn").value.toString().split(","),
                        snapshot.child("VehicleType").value.toString().split(",")
                    )

                    SharedPreferenceUtils.saveAppConfig(Gson().toJson(myConfig))
                    Log.e(TAG, "Config json : " + SharedPreferenceUtils.getAppConfig())

                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@SplashActivity,
                        "Unable to get configurations",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

            })
    }

    override fun onStop() {
        super.onStop()
        AppClass.databaseReference.child(Constants.GET_CONFIG).removeEventListener(valueEventListener)
    }
}