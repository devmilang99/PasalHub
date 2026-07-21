package com.psl.pasalhub.core.application

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.psl.pasalhub.core.application.domain.AppError
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.application.utils.ConnectivityObserver
import com.psl.pasalhub.core.application.utils.ConnectivityStatus
import com.psl.pasalhub.dashboard.order.domain.OrderRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PasalHubApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var orderRepository: OrderRepository

    @Inject
    lateinit var appPrefsRepository: AppPreferencesRepository

    private val applicationScope = MainScope()

    override fun onCreate() {
        super.onCreate()

        // Observe Connectivity
        applicationScope.launch {
            val observer = ConnectivityObserver(applicationContext)
            observer.observe().collectLatest { status ->
                when (status) {
                    ConnectivityStatus.Lost, ConnectivityStatus.Unavailable -> {
                        appPrefsRepository.emitGlobalError(AppError.Network())
                    }

                    ConnectivityStatus.Available -> {
                        appPrefsRepository.emitGlobalError(null)
                    }

                    else -> {}
                }
            }
        }

        // Ensure all orders are resumed on startup
        applicationScope.launch {
            try {
                orderRepository.resumeAllPausedOrders()
            } catch (e: Exception) {
                appPrefsRepository.emitGlobalError(AppError.Database(e.message ?: "Database Error"))
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
