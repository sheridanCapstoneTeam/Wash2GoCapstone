package project.sheridancollege.wash2goproject.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.Config
import project.sheridancollege.wash2goproject.ui.authentication.MainActivity
import project.sheridancollege.wash2goproject.ui.customer.CustomerActivity
import project.sheridancollege.wash2goproject.ui.detailer.DetailerActivity
import project.sheridancollege.wash2goproject.ui.detailer.setup.DetailerSetupActivity
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.Permission
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class SplashActivity : AppCompatActivity() {

    companion object {
        val TAG: String = SplashActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AppClass.databaseReference.child(Constants.GET_CONFIG)
            .get()
            .addOnCompleteListener(OnCompleteListener { task ->

                if (!task.isSuccessful) {
                    Toast.makeText(
                        this@SplashActivity,
                        "Unable to get configurations",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@OnCompleteListener
                }

                val myConfig = Config(
                    task.result.child("Services").value.toString().split(","),
                    task.result.child("AddsOn").value.toString().split(","),
                    task.result.child("VehicleType").value.toString().split(","),
                    Integer.parseInt(task.result.child("BaseServiceCharges").value.toString()),
                    Integer.parseInt(task.result.child("BaseAddOnsCharges").value.toString()),
                    Integer.parseInt(task.result.child("CleaningSuppliesCharges").value.toString()),
                    task.result.child("CleaningSupplies").value.toString().split(","),
                    Integer.parseInt(task.result.child("CleaningSuppliesMarketPrice").value.toString()),
                    task.result.child("CarConditionImages").value.toString()
                )
                SharedPreferenceUtils.saveAppConfig(Gson().toJson(myConfig))
                Log.e(TAG, "Config json : " + SharedPreferenceUtils.getAppConfig())

                FirebaseAuth.getInstance().currentUser?.let {
                    //Not null
                    Log.e(TAG, "Already logged in " + FirebaseAuth.getInstance().currentUser?.email)
                    if (SharedPreferenceUtils.getUserDetails()?.isProvider!!) {
                        //Start Detailer activity
                        if (SharedPreferenceUtils.getUserDetails()?.isSetupCompleted == false ||
                            SharedPreferenceUtils.getUserDetails()?.haveCleaningKit == false ||
                            SharedPreferenceUtils.getUserDetails()?.isCleaningKitReceive == false ||
                            !Permission.hasLocationPermission(this)
                        ) {
                            //Initital setup is not completed yet. Move to DetailerSetupActivity
                            startActivity(
                                Intent(
                                    this@SplashActivity,
                                    DetailerSetupActivity::class.java
                                )
                            )
                        } else {
                            //Initial Setup is done.
                            startActivity(
                                Intent(
                                    this@SplashActivity,
                                    DetailerActivity::class.java
                                )
                            )
                        }
                        finish()
                        return@OnCompleteListener
                    }


                    //Start Customer Activity
                    startActivity(
                        Intent(
                            this@SplashActivity,
                            CustomerActivity::class.java
                        )
                    )
                    finish()
                    return@OnCompleteListener
                } ?: kotlin.run {
                    //Null
                    Log.e(TAG, "Not logged in")
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }


            })
    }
}