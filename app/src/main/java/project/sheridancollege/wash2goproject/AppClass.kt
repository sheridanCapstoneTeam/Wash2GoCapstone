package project.sheridancollege.wash2goproject

import android.app.Application
import com.google.api.Context
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

open class AppClass : Application() {
    companion object {
        lateinit var instance: AppClass
        lateinit var databaseReference: DatabaseReference
        val TAG: String = AppClass::class.java.simpleName
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        databaseReference = FirebaseDatabase.getInstance().reference.root
    }
}