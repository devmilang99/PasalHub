package com.example.core.application.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R

class NotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "order_updates_channel"
        const val CHANNEL_NAME = "Order Updates"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for order progress updates"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showOrderUpdateNotification(orderId: Int, status: String, progress: Int) {
        val title = "Order #ORD-${1000 + orderId}"
        val contentText = when (status) {
            "Placed" -> "Order placed! We're preparing it."
            "Packaging", "Packing" -> "We're packing your order."
            "Sent for Delivery", "On Way" -> "Your order is on the way!"
            "Delivered" -> "Order delivered! Enjoy your purchase."
            else -> "Status: $status"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with app icon
            .setContentTitle(title)
            .setContentText(contentText)
            .setOngoing(status != "Delivered" && status != "Completed" && status != "Cancelled")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(100, progress, false)
            .setAutoCancel(true)

        // Set promoted ongoing for Android 15+ (Live Update)
        if (Build.VERSION.SDK_INT >= 35) {
             builder.setRequestPromotedOngoing(true)
             builder.setShortCriticalText(status)
        }

        notificationManager.notify(orderId, builder.build())
    }
}
