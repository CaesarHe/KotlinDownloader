package com.kotlindownloader.config

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.load.engine.cache.LruResourceCache



/**
 * Created by heyaokuai on 2017/10/13.
 */
@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context?, builder: GlideBuilder?) {
        val memoryCacheSizeBytes = 1024 * 1024 * 20 // 20mb
        builder?.setMemoryCache(LruResourceCache(memoryCacheSizeBytes))
    }
}