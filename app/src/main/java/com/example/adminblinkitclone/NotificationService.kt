package com.example.adminblinkitclone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat

import com.example.adminblinkitclone.activity.AdminMainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random


class NotificationService: FirebaseMessagingService() {
    override fun onCreate() {
        super.onCreate()
        Log.d("chen", "NotificationService created")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("NotificationService", "onMessageReceived: ${message.data}")

        val title = message.data["title"] ?: "No Title"
        val body = message.data["body"] ?: "No Body"
        Log.d("ggg", "Notification Title: $title")
        Log.d("ggg", "Notification Body: $body")

        val channelId = "AdminsBlinkit"
        val channel = NotificationChannel(channelId, "Blinkit", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Blinkit messages"
            enableLights(true)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, AdminMainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.app_icon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(1234, notification)
    }
}
