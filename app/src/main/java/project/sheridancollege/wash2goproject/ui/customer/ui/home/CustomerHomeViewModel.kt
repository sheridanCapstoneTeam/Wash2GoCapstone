package project.sheridancollege.wash2goproject.ui.customer.ui.home

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.common.DetailerServicesPrice
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.common.UserStatus
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class CustomerHomeViewModel : ViewModel() {

    private lateinit var valueEventListener: ValueEventListener

    private val _onlineDetailers: MutableLiveData<ArrayList<User>> = MutableLiveData()
    val onlineDetailers: LiveData<ArrayList<User>> = _onlineDetailers

    private val _detailerServicePrice: MutableLiveData<DetailerServicesPrice> = MutableLiveData()
    val detailerServicePrice: LiveData<DetailerServicesPrice> = _detailerServicePrice

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> = _user


    fun updateFCMToken() {
        val user = SharedPreferenceUtils.getUserDetails()
        user?.fcmToken = AppClass.FCMToken

        AppClass.databaseReference.child(Constants.USER).child(user?.userId.toString())
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

    fun getOnlineDetailers() {
        valueEventListener = AppClass.databaseReference
            .child(Constants.USER)
            .orderByChild(Constants.IS_PROVIDER)
            .equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.e(CustomerHomeFragment.TAG, "Total Detailers = " + snapshot.childrenCount)

                    val list: ArrayList<User> = ArrayList()
                    for (child in snapshot.children) {
                        val detailer: User? =
                            child.getValue(
                                User::class.java
                            )
                        if (detailer?.status == UserStatus.ONLINE) {
                            list.add(detailer)
                        }
                    }
                    _onlineDetailers.postValue(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(AppClass.instance, error.message, Toast.LENGTH_SHORT)
                }
            })
    }

    fun getDetailerDetails(userId: String) {
        AppClass.databaseReference.child(Constants.DETAILER_SERVICE_PRICE)
            .child(userId)
            .get()
            .addOnCompleteListener(OnCompleteListener {
                if (!it.isSuccessful) {
                    Toast.makeText(
                        AppClass.instance,
                        "Unable to get detailer details",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@OnCompleteListener
                }

                val detailerServicesPrice: DetailerServicesPrice? =
                    it.result.getValue(DetailerServicesPrice::class.java)

                _detailerServicePrice.postValue(detailerServicesPrice)
            })
    }

}