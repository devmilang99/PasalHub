package com.example.core.application.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.core.application.receiver.NotificationActionReceiver
import androidx.core.graphics.toColorInt

class NotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "order_updates_channel_v2"
        const val CHANNEL_NAME = "Order Status Updates"
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
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("RemoteViewLayout")
    fun showOrderUpdateNotification(
        orderId: Int,
        status: String,
        progress: Int,
        itemsSummary: String = "Order Items",
        seller: String = "Pasal Hub",
        isPaused: Boolean = false
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_order_status)
        
        // Items Summary parsing for title
        val displayTitle = if (itemsSummary.contains("|")) itemsSummary.split("|").first() else itemsSummary
        remoteViews.setTextViewText(R.id.notification_title, "#ORD-${1000+orderId} is $status")
        remoteViews.setTextViewText(R.id.notification_seller, seller)

        // Update progress steps
        val activeColor = "#BB86FC".toColorInt()
        val pausedColor = "#FFB74D".toColorInt() // Orange for paused
        val inactiveColor = "#444444".toColorInt()

        val currentColor = if (isPaused) pausedColor else activeColor

        remoteViews.setInt(R.id.progress_step_1, "setBackgroundColor", if (progress >= 5) currentColor else inactiveColor)
        remoteViews.setInt(R.id.progress_step_2, "setBackgroundColor", if (progress >= 51) currentColor else inactiveColor)
        remoteViews.setInt(R.id.progress_step_3, "setBackgroundColor", if (progress >= 100) currentColor else inactiveColor)

        // If in cancellation window (Placing), show status
        if (status == "Placing") {
            remoteViews.setTextViewText(R.id.notification_title, if (isPaused) "Order Paused" else "Cancellation Window...")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(status != "Delivered" && status != "Completed" && status != "Cancelled")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setContentTitle(displayTitle)
            .setContentText(when {
                isPaused -> "Paused"
                status == "Placing" -> "Cancellation Window"
                else -> status
            })
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Set promoted ongoing for Android 15+ (Live Update)
        if (Build.VERSION.SDK_INT >= 35) {
            builder.setRequestPromotedOngoing(true)
            val shortStatus = when(status) {
                "Placing" -> "Canceling"
                "Sent for Delivery" -> "Shipping"
                "Packaging" -> "Packing"
                else -> status
            }
            builder.setShortCriticalText(if (isPaused) "Paused" else shortStatus)
        }

        notificationManager.notify(orderId, builder.build())
    }
}
