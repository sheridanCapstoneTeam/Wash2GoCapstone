package project.sheridancollege.wash2goproject.ui.detailer.ui.home

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.common.DetailerServicesPrice
import project.sheridancollege.wash2goproject.common.Order
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.common.UserStatus
import project.sheridancollege.wash2goproject.ui.customer.ui.home.CustomerHomeFragment
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class DetailerHomeViewModel : ViewModel() {

    private lateinit var orderEventListener: ValueEventListener
    private lateinit var detailerDetailsEventListener: ValueEventListener

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> = _user

    private val _userLocation: MutableLiveData<LocationResult> = MutableLiveData()
    val userLocation: LiveData<LocationResult> = _userLocation

    private val _detailerServicePrice: MutableLiveData<DetailerServicesPrice> = MutableLiveData()
    val detailerServicePrice: LiveData<DetailerServicesPrice> = _detailerServicePrice

    private val _orders: MutableLiveData<HashMap<String,HashMap<String,String>>> = MutableLiveData()
    val orders: LiveData<HashMap<String,HashMap<String,String>>> = _orders

    fun updateFCMToken(){
        val user = SharedPreferenceUtils.getUserDetails()
        user.fcmToken = AppClass.FCMToken

        AppClass.databaseReference.child(Constants.USER).child(user.userId)
            .setValue(user)
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(
                        AppClass.instance, task.exception?.localizedMessage, Toast.LENGTH_LONG
                    ).show()
                    return@OnCompleteListener
                }
                _user.postValue(user)
            })
    }
    fun updateUserStatus(userStatus: UserStatus) {
        val user = SharedPreferenceUtils.getUserDetails()
        user.status = userStatus


        AppClass.databaseReference.child(Constants.USER).child(user.userId)
            .setValue(user)
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {

                    Toast.makeText(
                        AppClass.instance, task.exception?.localizedMessage, Toast.LENGTH_LONG
                    ).show()

                    return@OnCompleteListener
                }
                _user.postValue(user)
            })
    }

    fun updateCurrentLocation(locationResult: LocationResult) {
        val user = SharedPreferenceUtils.getUserDetails()
        user.currentLat = locationResult.lastLocation.latitude
        user.currentLong = locationResult.lastLocation.longitude

        AppClass.databaseReference.child(Constants.USER).child(user.userId)
            .setValue(user)
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {

                    Toast.makeText(
                        AppClass.instance, task.exception?.localizedMessage, Toast.LENGTH_LONG
                    ).show()

                    return@OnCompleteListener
                }
                _userLocation.postValue(locationResult)
            })
    }

    fun getRatingAndEarnings(detailerId: String) {
        detailerDetailsEventListener = AppClass.databaseReference.child(Constants.DETAILER_SERVICE_PRICE)
            .child(detailerId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    _detailerServicePrice.postValue(snapshot.getValue(DetailerServicesPrice::class.java))
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(AppClass.instance, error.message, Toast.LENGTH_SHORT)
                }
            })
    }

    fun getDetailerOrders(detailerId: String) {
        orderEventListener = AppClass.databaseReference
            .child(Constants.ORDER)
            .child(detailerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _orders.postValue(snapshot.value as HashMap<String, HashMap<String, String>>?)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(AppClass.instance, error.message, Toast.LENGTH_SHORT)
                }
            })

//        AppClass.databaseReference.child(Constants.ORDER)
//            .child(detailerId)
//            .get()
//            .addOnCompleteListener(OnCompleteListener {
//                if (!it.isSuccessful) {
//                    Toast.makeText(
//                        AppClass.instance,
//                        "Unable to update dashboard",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return@OnCompleteListener
//                }
//                _orders.postValue(it.result.value as HashMap<String, HashMap<String, String>>?)
//
//            })
    }


}