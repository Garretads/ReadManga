package ru.garretech.readmanga

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import io.fabric.sdk.android.Fabric
import io.reactivex.plugins.RxJavaPlugins

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        BigImageViewer.initialize(GlideImageLoader.with(applicationContext))
        val crashlyticsKit = Crashlytics.Builder()
            .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build()
        if (!BuildConfig.DEBUG) Fabric.with(baseContext, crashlyticsKit)
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