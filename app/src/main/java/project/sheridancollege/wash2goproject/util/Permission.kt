package project.sheridancollege.wash2goproject.util

import android.content.Context
import com.vmadalin.easypermissions.EasyPermissions
import android.Manifest
import android.os.Build
import androidx.fragment.app.Fragment
import project.sheridancollege.wash2goproject.util.Constants.PERMISSION_BACKGROUND_LOCATION_CODE
import project.sheridancollege.wash2goproject.util.Constants.PERMISSION_LOCATION_REQUEST_CODE

object Permission {
    fun hasLocationPermission(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )


    fun requestLocationPermission(fragment: Fragment){
        EasyPermissions.requestPermissions(
            fragment,
            "This application cannot work without Location Permission",
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun hasBackgroundLocationPermission(context: Context) : Boolean{
        //Q is Api level 29
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            return EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
        return true
    }

    fun requestBackgroundLocationPermission(fragment: Fragment){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                fragment,
                "Background location permission is essential to provide the service",
                PERMISSION_BACKGROUND_LOCATION_CODE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }
}