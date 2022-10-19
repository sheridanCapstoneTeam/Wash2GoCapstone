package project.sheridancollege.wash2goproject.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils


class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private val TAG: String = MyFirebaseMessagingService::class.java.simpleName
    }

    private val ADMIN_CHANNEL_ID = "admin_channel"

    override fun onNewToken(token: String) {
        Log.e(TAG, "New Token : $token")
        AppClass.FCMToken = token


        val user = SharedPreferenceUtils.getUserDetails()
        if(user != null){
            user.fcmToken = AppClass.FCMToken

            AppClass.databaseReference.child(Constants.USER).child(user.userId)
                .setValue(user)
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(
                            AppClass.instance, task.exception?.localizedMessage, Toast.LENGTH_LONG
                        ).show()
                        return@OnCompleteListener
                    }
                    Log.e(TAG,"FCM token udpated successfully on firebase")
                })
        }


    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.e(TAG, remoteMessage.data.toString())

        if (remoteMessage.data.isNotEmpty()) {
            val notificationType = remoteMessage.data["notification"]
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            Log.e(TAG, "Notification Type : $notificationType")
            Log.e(TAG, "title : $title")
            Log.e(TAG, "body: $body")

            showNotification(title, body)


        }
    }

    private fun showNotification(title: String?, body: String?) {

        val pm = packageManager
        val launchIntent = pm.getLaunchIntentForPackage("project.sheridancollege.wash2goproject")

        val pendingIntent = PendingIntent.getActivity(this, 0, launchIntent,0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel(ADMIN_CHANNEL_ID)
            val name: CharSequence = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(ADMIN_CHANNEL_ID, name, importance)

            val notification: NotificationCompat.Builder = NotificationCompat.Builder(
                this,
                ADMIN_CHANNEL_ID
            )
                .setSmallIcon(R.drawable.sedan_car)
                .setContentTitle(title)
                .setContentText(body)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
            notificationManager.createNotificationChannel(mChannel)
            notificationManager.notify(1, notification.build())
            return
        }

        val notificationManager =
            this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val alarmNotificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_action_home)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setContentIntent(pendingIntent)

        notificationManager.notify(1, alarmNotificationBuilder.build())
    }
}