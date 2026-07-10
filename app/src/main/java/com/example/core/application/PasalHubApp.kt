package com.example.core.application

import android.app.Application
import com.example.initial.di.AppContainer
import com.example.initial.di.AppContainerImpl

class PasalHubApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }
}
