package project.sheridancollege.wash2goproject.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import project.sheridancollege.wash2goproject.util.HTTPDataHandler

class LoginViewModel : ViewModel() {
    private val _coOrdinates: MutableLiveData<String?> = MutableLiveData()
    val coOrdinates: LiveData<String?> = _coOrdinates

    fun  getCoOrdinates(address:String){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val response: String
                try {
                    val http = HTTPDataHandler()
                    val url = String.format(
                        "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=AIzaSyBcNe5mLxKAaeJSmsFz0F2E7jd-SmO_v5o",
                        address
                    )
                    response = http.getHTTPData(url)
                    _coOrdinates.postValue(response)
                } catch (ex: Exception) {
                    _coOrdinates.postValue("")
                }
            }
        }
    }

    fun resetResponse() {
        _coOrdinates.postValue(null)
    }
}