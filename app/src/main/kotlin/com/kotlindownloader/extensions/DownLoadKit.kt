package com.kotlindownloader.extensions

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import com.kotlindownloader.download.DownloadManager
import com.kotlindownloader.download.DownloadTask
import com.kotlindownloader.download.entity.DownloadRequest
import com.kotlindownloader.fragment.RecommendListAdapter

/**
 * Created by heyaokuai on 2017/9/29.
 */
val Context.mDownloadManager: DownloadManager
    get() = DownloadManager.getInstance()
val Fragment.mDownloadManager: DownloadManager
    get() = DownloadManager.getInstance()
val DownloadRequest.mDownloadManager: DownloadManager
    get() = DownloadManager.getInstance()
val RecommendListAdapter.mDownloadManager: DownloadManager
    get() = DownloadManager.getInstance()

inline fun DownloadTask.runOnUiThread(crossinline f: Runnable.() -> Unit) {
    if (ContextHelper.mainThread == Thread.currentThread()) f() else ContextHelper.handler.post { f() }
}

object ContextHelper {
    val handler = Handler(Looper.getMainLooper())
    val mainThread: Thread = Looper.getMainLooper().thread
}