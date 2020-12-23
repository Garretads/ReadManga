package ru.garretech.readmanga

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import io.reactivex.plugins.RxJavaPlugins

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)
        BigImageViewer.initialize(GlideImageLoader.with(applicationContext))


        // Инициализируем firebase логирование только если сборка в release варианте
        if (!BuildConfig.DEBUG) {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build()
            remoteConfig.setConfigSettingsAsync(configSettings)
        }

        if (!BuildConfig.DEBUG) RxJavaPlugins.setErrorHandler { t: Throwable? ->
            Log.e(
                "RxJava error",
                "Произошла ошибка",
                t
            )
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}