package com.kotlindownloader

import android.app.Application
import com.kotlindownloader.config.GlideApp
import com.kotlindownloader.extensions.mDownloadManager

/**
 * Created by heyaokuai on 2017/10/11.
 */
class MyApplication : Application() {
    companion object {
        lateinit var app: MyApplication
    }
    override fun onCreate() {
        super.onCreate()
        app = this
        mDownloadManager.getAllTask()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        GlideApp.get(this).onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        GlideApp.get(this).onTrimMemory(level)
    }
}