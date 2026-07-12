package com.example.core.application.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.dashboard.order.domain.OrderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: OrderRepository

    override fun onReceive(context: Context, intent: Intent) {
        val orderId = intent.getIntExtra("order_id", -1)
        if (orderId == -1) return

        when (intent.action) {
            ACTION_TOGGLE_PAUSE -> {
                CoroutineScope(Dispatchers.IO).launch {
                    repository.toggleOrderPause(orderId)
                }
            }
            ACTION_CANCEL_ORDER -> {
                CoroutineScope(Dispatchers.IO).launch {
                    repository.cancelOrder(orderId, "Cancelled by user via notification")
                }
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_PAUSE = "com.example.ACTION_TOGGLE_PAUSE"
        const val ACTION_CANCEL_ORDER = "com.example.ACTION_CANCEL_ORDER"
    }
}
