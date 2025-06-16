package com.wneth.mpadtransport.configurations

import android.app.Application
import androidx.camera.core.CameraXConfig
import androidx.work.WorkManager
import java.util.concurrent.Executors

class MPADTransportApp: Application() {

    private lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }

    fun getAppComponent(): AppComponent {
        return appComponent
    }

    override fun onTerminate() {
        super.onTerminate()
        WorkManager.getInstance(this).cancelUniqueWork("DatabaseBackup")
    }

}