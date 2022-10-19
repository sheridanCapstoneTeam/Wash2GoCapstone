package project.sheridancollege.wash2goproject.ui.detailer.ui.home

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
import project.sheridancollege.wash2goproject.common.*
import project.sheridancollege.wash2goproject.firebase.FCMHandler
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class DetailerHomeViewModel : ViewModel() {

    private lateinit var orderEventListener: ValueEventListener
    private lateinit var detailerDetailsEventListener: ValueEventListener

    private val _order: MutableLiveData<Order> = MutableLiveData()
    val order: LiveData<Order> = _order

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> = _user

    private val _userLocation: MutableLiveData<LocationResult> = MutableLiveData()
    val userLocation: LiveData<LocationResult> = _userLocation

    private val _detailerServicePrice: MutableLiveData<DetailerServicesPrice> = MutableLiveData()
    val detailerServicePrice: LiveData<DetailerServicesPrice> = _detailerServicePrice

    private val _orders: MutableLiveData<ArrayList<Order>> = MutableLiveData()
    val orders: LiveData<ArrayList<Order>> = _orders

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

    fun updateUserStatus(userStatus: UserStatus) {
        val user = SharedPreferenceUtils.getUserDetails()
        user?.status = userStatus


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

    fun updateCurrentLocation(locationResult: LocationResult) {
        val user = SharedPreferenceUtils.getUserDetails()
        user?.currentLat = locationResult.lastLocation.latitude
        user?.currentLong = locationResult.lastLocation.longitude

        AppClass.databaseReference.child(Constants.USER).child(user?.userId.toString())
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
        detailerDetailsEventListener =
            AppClass.databaseReference.child(Constants.DETAILER_SERVICE_PRICE)
                .child(detailerId)
                .addValueEventListener(object : ValueEventListener {
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
            .orderByChild("orderDateTime")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val list: ArrayList<Order> = ArrayList()
                    for (child in snapshot.children) {
                        val order: Order? =
                            child.getValue(
                                Order::class.java
                            )
                        list.add(order!!)
                    }
                    _orders.postValue(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(AppClass.instance, error.message, Toast.LENGTH_SHORT)
                }
            })
    }


    fun sendNotificationToCustomer(order: Order?) {
        AppClass.databaseReference.child(Constants.USER)
            .child(order?.customerId!!)
            .get()
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(
                        AppClass.instance, task.exception?.localizedMessage, Toast.LENGTH_LONG
                    ).show()

                    return@OnCompleteListener
                }

                val customerDetail: User? =
                    task.result.getValue(User::class.java)

                //Send FCM To Customer
                val title = "Hello ${customerDetail?.firstName}!"
                var body = ""
                when(order.status){
                    AppEnum.ACTIVE.toString() ->{
                        body = "Your Order ${order.orderId} has been accepted!"
                    }
                    AppEnum.STARTED.toString() ->{
                        body = "Detailer is on it's way!"
                    }
                    AppEnum.ARRIVED.toString() ->{
                        body = "Detailer has been arrived!"
                    }
                    AppEnum.COMPLETED.toString() ->{
                        body = "Your Order ${order.orderId} has been completed!"
                    }
                    AppEnum.DECLINED.toString() ->{
                        body = "Your Order ${order.orderId} has been declined!"
                    }
                }

                FCMHandler.sendFCM(customerDetail?.fcmToken!!,AppEnum.CUSTOMER_REQUEST, title, body)

            })
    }

    fun updateOrderStatus(order: Order?, detailerId: String, status: AppEnum) {

        order?.status = status.toString()

        AppClass.databaseReference.child(Constants.ORDER)
            .child(detailerId)
            .child(order?.orderId!!)
            .setValue(order)
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(
                        AppClass.instance, task.exception?.localizedMessage, Toast.LENGTH_LONG
                    ).show()

                    return@OnCompleteListener
                }


                _order.postValue(order)
            })
    }

}