@file:Suppress("DEPRECATION")

package project.sheridancollege.wash2goproject.firebase

import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.json.JSONObject
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.common.AppEnum
import project.sheridancollege.wash2goproject.util.Constants

object FCMHandler {
    private val TAG = FCMHandler::class.java.simpleName

    fun sendFCM(sendTo: String, requestType: AppEnum, title: String, body: String) {
        var jsonBody: JSONObject? = null

        try{
            jsonBody = JSONObject("{\n" +
                    "  \"time_to_live\": 0,\n" +
                    "  \"to\":\"" + sendTo + "\",\n" +
                    "  \"data\": {\n" +
                    "    \"notification\": \"" + requestType.toString() + "\",\n" +
                    "\"title\": \"" + title + "\",\n" +
                    "\"body\": \"" + body + "\"\n" +
                    "  }\n" +
                    "}")

            Log.e(TAG,"SEND_FCM $jsonBody")

        }catch (exception:Exception){
            exception.printStackTrace()
        }

        val request= object: JsonObjectRequest(Request.Method.POST,Constants.FCM_URL,jsonBody,{
            Log.e(TAG,"Response : ${Gson().toJson(it)}")


        },{
            Log.e(TAG,"Error : ${it.message}")
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = Constants.SERVER_KEY
                headers["Content-Type"] ="application/json"
                return headers
            }
        }

        Volley.newRequestQueue(AppClass.instance.applicationContext).add(request)
    }
}