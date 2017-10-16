package com.kotlindownloader.download

import android.util.Log
import com.kotlindownloader.MyApplication
import com.kotlindownloader.db.DownloadDBHelper
import com.kotlindownloader.download.entity.DownloadRequest
import com.kotlindownloader.download.listener.IDownloadListener
import com.kotlindownloader.extensions.database
import com.kotlindownloader.extensions.deleteSafe
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.SelectQueryBuilder
import org.jetbrains.anko.db.select
import org.jetbrains.anko.info
import org.jetbrains.anko.runOnUiThread
import java.io.File
import java.util.*

/**
 * Created by heyaokuai on 2017/9/27.
 */
class DownloadTaskManager : AnkoLogger {
    companion object {
        val MAX_THREAD_COUNT = 2
    }
    private var downloadManagerListener: IDownloadListener? = null
    private var runningList: LinkedList<DownloadTask> = LinkedList<DownloadTask>()
    private var taskListener = object : IDownloadListener {
        override fun progress(id: Long, progress: Int) {
            downloadManagerListener?.progress(id, progress)
        }

        override fun newTask(id: Long) {
            downloadManagerListener?.newTask(id)
        }

        override fun complete(id: Long) {
            info("complete: $id")
            schedule()
            downloadManagerListener?.complete(id)
        }

        override fun failed(id: Long, errorCode: Int) {
            info("failed: $id  $errorCode")
            schedule()
            downloadManagerListener?.failed(id, errorCode)
        }

        override fun stateChange(id: Long, state: Int) {
            info("stateChange: $id  $state")
            schedule()
            downloadManagerListener?.stateChange(id, state)
        }

    }

    init {
        database.use {
            runningList.clear()
            select(DownloadDBHelper.TABLE_NAME).parseList {
                var task = DownloadTask(DownloadRequest(HashMap(it)))
                if (!checkContain(task)) {
                    task.taskListener = taskListener
                    if (task.request.isRuning && !task.isAlive) {
                        info("resume task from db")
                        Log.e("123", "${task.request.fileName} from db")
                        task.start()
                    }
                    runningList.add(task)
                }
            }
            schedule()
        }
    }

    fun <T : Any> SelectQueryBuilder.parseList(parser: (Map<String, Any?>) -> T): List<T> {
        return parseList(object : MapRowParser<T> {
            override fun parseRow(columns: Map<String, Any?>): T {
                return parser(columns)
            }
        })
    }

    @Synchronized
    private fun schedule() {
        info("method: schedule")
        var runingSize = runningList.filter { it.request.isRuning }.size
        if (runingSize < MAX_THREAD_COUNT) {
            poolOne(runningList)?.let {
                it.start()
            }
        }
    }

    @Synchronized
    private fun poolOne(list: LinkedList<DownloadTask>): DownloadTask? {
        return list.filter { it.request.status == DownloadRequest.STATUS_PENDING }.firstOrNull()
    }

    @Synchronized
    fun addTask(task: DownloadTask): Boolean {
        if (checkContain(task)) {
            return false
        }
        task.taskListener = taskListener
        runningList.add(task)
        task.request.insertDB()

        MyApplication.app.runOnUiThread {
            with(task) {
                taskListener?.newTask(request.id)
                request.getListener()?.newTask(request.id)
            }
        }
        task.updateDownloadState(DownloadRequest.STATUS_PENDING)
        info("add new task ${task.request.fileName}")
        return true
    }

    @Synchronized
    fun retry(id: Long) {
        getAllTask().firstOrNull { it.request.id == id }?.let {
            it.request.status = DownloadRequest.STATUS_PENDING
            info("retry task ${it.request.fileName}")
            it.updateDownloadState(it.request.status)
        }
    }

    @Synchronized
    fun pause(id: Long) {
        var find = getTask(id)
        find ?: let { info("pause task$id error ") }
        find?.let {
            it.request.pause()
            info("pause task ${it.request.fileName}")
            it.updateDownloadState(it.request.status)
        }
    }

    @Synchronized
    fun resume(id: Long) {
        getTask(id)?.let {
            it.request.resume()
            info("resume task ${it.request.fileName}")
            it.updateDownloadState(it.request.status)
        }
    }

    @Synchronized
    fun cancel(id: Long) {
        getTask(id)?.let {
            it.request.cancel()
            it.request.deleteDB()
            runningList.remove(it)
            info("cancel task ${it.request.fileName}")
            it.updateDownloadState(it.request.status)
            it.request.localPath?.let {
                File(it).deleteSafe()
                info("delete download tmp file $it")
            }
        }
    }

    fun getStatus(request: DownloadRequest): DownloadRequest? {
        return runningList.firstOrNull { it.request.id == request.id }?.request
    }

    fun getStatus(url: String): DownloadRequest? {
        return runningList.firstOrNull { it.request.url.equals(url) }?.request
    }

    private fun checkContain(task: DownloadTask): Boolean {
        var contain = false
        runningList.filter { it.request.url.equals(task.request.url) }.forEach {
            contain = true
            task.request = it.request.copy()
        }
        return contain
    }

    fun getAllTask(): LinkedList<DownloadTask> {
        return runningList
    }

    fun getTask(id: Long): DownloadTask? {
        return runningList.firstOrNull { it.request.id == id }
    }

    fun registerDownloadManagerListener(listener: IDownloadListener?) {
        this.downloadManagerListener = listener
    }

}