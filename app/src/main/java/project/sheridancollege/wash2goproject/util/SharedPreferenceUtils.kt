package project.sheridancollege.wash2goproject.util

import android.app.Activity
import android.content.SharedPreferences
import com.google.gson.Gson
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.common.Config

object SharedPreferenceUtils {

    internal var TAG = SharedPreferenceUtils::class.java.simpleName

    fun getInstance(): SharedPreferences {
        return AppClass.instance.getSharedPreferences("mSharedPref", Activity.MODE_PRIVATE)
    }

    fun saveAppConfig(appConfig: String) {
        getInstance().edit().putString(Constants.APP_CONFIG, appConfig).apply()
    }

    fun getAppConfig(): Config? {
        return Gson().fromJson(
            getInstance().getString(Constants.APP_CONFIG, null),
            Config::class.java
        )
    }

    fun saveUserDetails(userDetails: User?){
        getInstance().edit().putString(Constants.USER_DETAILS,Gson().toJson(userDetails)).apply()
    }
    fun getUserDetails(): User? {
        return Gson().fromJson(
            getInstance().getString(Constants.USER_DETAILS, null),
            User::class.java
        )
    }
}