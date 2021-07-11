package com.example.tulook.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tulook.MainActivity
import com.example.tulook.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


val TOKEN_KEY = "FIREBASE_MESSAGING_TOKEN"

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val CHANNEL_ID = "All Notifications"
    private val CHANNEL_NAME = "All Notifications"
    private val CHANNEL_DESC = "All Notifications"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("token", token)

        getSharedPreferences(TOKEN_KEY, MODE_PRIVATE).edit().putString(TOKEN_KEY, token).apply()
    }

    // https://stackoverflow.com/questions/41750548/show-notification-of-firebase-message-onreceived-when-app-is-in-background-throu
    // https://stackoverflow.com/questions/45462666/notificationcompat-builder-deprecated-in-android-o
    // https://stackoverflow.com/questions/48031336/developer-warning-for-package-failed-to-post-notification-on-channel-null-see-l/52027397
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notification = remoteMessage.notification ?: return

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            channel.description = CHANNEL_DESC
            notificationManager.createNotificationChannel(channel)
        }

        val noti = NotificationCompat.Builder(this, CHANNEL_NAME)
            .setContentTitle(notification.title ?: "TuLook")
            .setSmallIcon(R.drawable.logo_inicial)

        if (notification.title == "Turno Aceptado" || notification.title == "Turno Cancelado") {
            Log.d("token", "mensaje de turno aceptado recibido")
            noti.setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
        } else {
            noti.setContentText(notification.body)
        }

        // Create an Intent for the activity you want to start
        val resultIntent = Intent(this, MainActivity::class.java)
        // Create the TaskStackBuilder
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        noti.setContentIntent(resultPendingIntent)
        noti.setAutoCancel(true)

        notificationManager.notify(0, noti.build())

    }
}

fun getToken(context: Context): String? {
    return context.getSharedPreferences(TOKEN_KEY, FirebaseMessagingService.MODE_PRIVATE).getString(TOKEN_KEY, "empty")
}
