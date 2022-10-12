package project.sheridancollege.wash2goproject.ui.customer.ui.order

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.common.AppEnum
import project.sheridancollege.wash2goproject.common.Order
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.common.UserStatus
import project.sheridancollege.wash2goproject.firebase.FCMCallback
import project.sheridancollege.wash2goproject.firebase.FCMHandler
import project.sheridancollege.wash2goproject.util.Constants

class OrderReviewViewModel : ViewModel() {

    private val _detailer: MutableLiveData<User?> = MutableLiveData()
    val detailer: LiveData<User?> = _detailer

    private val _orderResult: MutableLiveData<Boolean> = MutableLiveData()
    val orderResult: LiveData<Boolean> = _orderResult

    fun insertOrder(order: Order) {

        // Check detailer is online or not before inserting the order.
        AppClass.databaseReference.child(Constants.USER)
            .child(order.detailerId)
            .get()
            .addOnCompleteListener(OnCompleteListener {

                if (!it.isSuccessful) {
                    Toast.makeText(
                        AppClass.instance,
                        "Unable to connect to server",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@OnCompleteListener
                }

                val user: User? =
                    it.result.getValue(User::class.java)

                if(user?.status == UserStatus.ONLINE) {

                    AppClass.databaseReference.child(Constants.ORDER)
                        .child(order.detailerId)
                        .child(order.orderId)
                        .setValue(order)
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Toast.makeText(
                                    AppClass.instance,
                                    "Unable to save order",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@OnCompleteListener
                            }

                            //Send FCM To Detailer
                            FCMHandler.sendFCM(user.fcmToken,AppEnum.DETAILER_REQUEST,"Hello Detailer!","You have a new job.")

                            _orderResult.postValue(true)
                        })

                } else {

                    Toast.makeText(
                        AppClass.instance,
                        "Detailer Goes Offline. Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            })
    }

    fun getDetailerData(userId: String) {
        AppClass.databaseReference.child(Constants.USER)
            .child(userId)
            .get()
            .addOnCompleteListener(OnCompleteListener {
                if (!it.isSuccessful) {
                Toast.makeText(
                    AppClass.instance,
                    "Unable to get detailer data",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnCompleteListener
            }

                val user: User? =
                    it.result.getValue(User::class.java)

                _detailer.postValue(user)
            })
    }


}