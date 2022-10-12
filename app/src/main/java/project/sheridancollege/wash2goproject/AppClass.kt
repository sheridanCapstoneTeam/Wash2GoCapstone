package project.sheridancollege.wash2goproject

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.api.Context
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

open class AppClass : Application() {
    companion object {
        lateinit var instance: AppClass
        lateinit var FCMToken: String
        lateinit var databaseReference: DatabaseReference
        val TAG: String = AppClass::class.java.simpleName
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        databaseReference = FirebaseDatabase.getInstance().reference.root


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(TAG,"Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            FCMToken = task.result
            Log.e(TAG, "FCM Token $FCMToken")

        })
    }
}