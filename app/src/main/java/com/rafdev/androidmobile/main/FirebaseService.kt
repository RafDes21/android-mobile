package com.rafdev.androidmobile.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rafdev.androidmobile.R
import kotlin.random.Random

class FirebaseService : FirebaseMessagingService() {

    private val random = Random

    override fun onMessageReceived(message: RemoteMessage) {
        message.notification?.let {
            Log.i("FCM Title", "${it.title}")
            Log.i("FCM Body", "${it.body}")
            sendNotification(it)
        }
    }

    private fun sendNotification(it: RemoteMessage.Notification) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE)

        val channelId = this.getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(it.title)
            .setContentText(it.body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_google)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
        manager.notify(random.nextInt(), notificationBuilder.build())

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    companion object {
        const val CHANNEL_NAME = "FCM notification channel"
    }
}