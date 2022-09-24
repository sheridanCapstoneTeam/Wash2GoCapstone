package project.sheridancollege.wash2goproject.ui.customer.ui.order

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.common.DetailerServicesPrice
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.util.Constants

class OrderReviewViewModel : ViewModel() {

    private val _detailer: MutableLiveData<User?> = MutableLiveData()
    val detailer: LiveData<User?> = _detailer

    fun getDetailerData(userId: String) {
        AppClass.databaseReference.child(Constants.USER)
            .child(userId)
            .get()
            .addOnCompleteListener(OnCompleteListener{
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