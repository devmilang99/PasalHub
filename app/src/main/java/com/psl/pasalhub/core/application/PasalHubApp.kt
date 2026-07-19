package com.psl.pasalhub.core.application

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.psl.pasalhub.dashboard.order.domain.OrderRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PasalHubApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var orderRepository: OrderRepository

    private val applicationScope = MainScope()

    override fun onCreate() {
        super.onCreate()

        // Ensure all orders are resumed on startup
        applicationScope.launch {
            orderRepository.resumeAllPausedOrders()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
