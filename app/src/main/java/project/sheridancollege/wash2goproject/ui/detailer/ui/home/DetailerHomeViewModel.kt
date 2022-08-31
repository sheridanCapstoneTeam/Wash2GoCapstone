package project.sheridancollege.wash2goproject.ui.detailer.ui.home

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.OnCompleteListener
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.common.UserStatus
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class DetailerHomeViewModel : ViewModel() {

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> = _user

    private val _userLocation: MutableLiveData<LocationResult> = MutableLiveData()
    val userLocation: LiveData<LocationResult> = _userLocation

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
        user.currenLat = locationResult.lastLocation.latitude
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

}