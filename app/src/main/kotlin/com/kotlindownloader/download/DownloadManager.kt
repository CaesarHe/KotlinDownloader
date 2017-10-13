package com.kotlindownloader.download

import com.kotlindownloader.download.entity.DownloadRequest
import com.kotlindownloader.download.listener.DownloadListenerImp
import com.kotlindownloader.download.listener.IDownloadListener
import org.jetbrains.anko.AnkoLogger
import java.util.*

/**
 * Created by heyaokuai on 2017/9/27.
 */
class DownloadManager private constructor() : AnkoLogger {
    companion object {
        val CMD_NEW: Int = 1
        val CMD_PAUSE: Int = 2
        val CMD_RESUME: Int = 3
        val CMD_CANCEL: Int = 4
        val CMD_RETRY: Int = 5

        fun getInstance(): DownloadManager  = Holder.instance
    }
    private val taskManager: DownloadTaskManager = DownloadTaskManager()

    private object Holder {
        var instance = DownloadManager()

    }

    fun registerListener(init: (DownloadListenerImp.() -> Unit)): DownloadManager {
        var listener = DownloadListenerImp()
        listener.init()
        this.taskManager.registerDownloadManagerListener(listener)
        return this
    }

    fun registerListener(listener: IDownloadListener?): DownloadManager {
        this.taskManager.registerDownloadManagerListener(listener)
        return this
    }

    fun runCmd(cmd: Int, obj: Any?) {
        when (cmd) {
            CMD_NEW -> obj?.let { taskManager.addTask(DownloadTask(obj as DownloadRequest)) }
            CMD_PAUSE -> obj?.let { taskManager.pause(obj as Long) }
            CMD_RESUME -> obj?.let { taskManager.resume(obj as Long) }
            CMD_CANCEL -> obj?.let { taskManager.cancel(obj as Long) }
            CMD_RETRY -> obj?.let { taskManager.retry(obj as Long) }
        }
    }

    fun getStatus(request: DownloadRequest): DownloadRequest? {
        return taskManager.getStatus(request)
    }

    fun getStatus(url: String): DownloadRequest? {
        return taskManager.getStatus(url)
    }

    fun getAllTask(): LinkedList<DownloadTask> {
        return taskManager.getAllTask()
    }

}
