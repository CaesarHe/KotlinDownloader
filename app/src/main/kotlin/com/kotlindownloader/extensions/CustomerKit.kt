package com.kotlindownloader.extensions

import android.text.format.Formatter
import android.util.Log
import com.kotlindownloader.MyApplication

/**
 * Created by heyaokuai on 2017/10/13.
 */
inline fun safe(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
       Log.e("CustomerKit",  e.toString())
    }
}

val Long.formatFileSize: String
    get() = Formatter.formatFileSize(MyApplication.app, this)