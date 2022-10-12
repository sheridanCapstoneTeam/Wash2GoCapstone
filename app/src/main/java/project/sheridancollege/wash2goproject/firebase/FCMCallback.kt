package project.sheridancollege.wash2goproject.firebase

import com.android.volley.VolleyError
import org.json.JSONObject

interface FCMCallback {
    fun onResponse(response:JSONObject)
    fun onFailuer(error:VolleyError)
}