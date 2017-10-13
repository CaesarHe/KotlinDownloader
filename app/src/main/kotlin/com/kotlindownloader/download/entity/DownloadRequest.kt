package com.kotlindownloader.download.entity

import android.util.Log
import com.kotlindownloader.MyApplication
import com.kotlindownloader.db.DownloadDBHelper.Companion.TABLE_NAME
import com.kotlindownloader.db.database
import com.kotlindownloader.download.listener.DownloadListenerImp
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.coroutines.experimental.Ref
import org.jetbrains.anko.coroutines.experimental.asReference
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.update
import org.jetbrains.anko.info
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by heyaokuai on 2017/9/27.
 */
class DownloadRequest() : AnkoLogger {
    companion object {
        val STATUS_UNKNOW = 0
        val STATUS_PENDING = 1
        val STATUS_RUNNING = 2
        val STATUS_PAUSE = 3
        val STATUS_FAILED = 4
        val STATUS_COMPLETE = 5
        val STATUS_STOP = 6
        val STATUS_CANCELED = 7
    }

    var id: Long = 0
    var url: String? = null
    var fileName: String? = null
        get() = field ?: Date().toString() + ".tmp"

    var mContentLength: Long = 0
    var mCurrentBytes: Long = 0
    var status: Int = STATUS_UNKNOW
    var localPath: String? = null
        set(value) {
            field = value
            Log.e("123", "set localPath: $localPath")
        }

    constructor(map: MutableMap<String, Any?>) : this() {
        this.id = map.getOrDefault("id", 0L) as Long
        this.url = map.getOrDefault("url", "") as String
        this.fileName = map.getOrDefault("fileName", "") as String
        this.mContentLength = map.getOrDefault("mContentLength", 0L) as Long
        this.mCurrentBytes = map.getOrDefault("mCurrentBytes", 0L) as Long
        this.status = Integer.parseInt("" + map.getOrDefault("status", STATUS_UNKNOW) as Long)
        this.localPath = map.getOrDefault("localPath", "") as String
    }

    constructor(id: Long = 0,
                url: String? = null,
                fileName: String? = null,
                mContentLength: Long = 0,
                mCurrentBytes: Long = 0,
                status: Int = STATUS_UNKNOW,
                localPath: String? = null) : this() {
        this.id = id
        this.url = url
        this.fileName = fileName
        this.mContentLength = mContentLength
        this.mCurrentBytes = mCurrentBytes
        this.status = status
        this.localPath = localPath
    }


    private var listener: DownloadListenerImp? = null


    fun registerListener(init: (DownloadListenerImp.() -> Unit)): DownloadRequest {
        listener?.let { unRegisterListener() }
        var f = DownloadListenerImp()
        f.init()
        listener = f
        return this
    }

    fun unRegisterListener() {
        listener = null
    }

    fun getListener(): DownloadListenerImp? {
        return listener
    }

    fun finish() {
//        ("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun cancel() {
        status = STATUS_CANCELED
    }

    fun pause() {
        status = STATUS_PAUSE
    }

    fun resume() {
        status = STATUS_PENDING
    }

    fun isPaused(): Boolean {
        return status == STATUS_PAUSE
    }

    fun isRuning() = status == STATUS_RUNNING

    fun isCancelled(): Boolean {
        return status == STATUS_CANCELED
    }

    fun isCompleted(): Boolean {
        return status == STATUS_COMPLETE
    }

    fun isFailed(): Boolean {
        return status == STATUS_FAILED
    }

    fun getProgress(): Int = if (mContentLength == 0L) { 0} else (mCurrentBytes * 100 / mContentLength).toInt()

    fun getProgressAsString(): String {
        var progress: Float = if (mContentLength == 0L) {
            0F
        } else (mCurrentBytes.toFloat() / mContentLength.toFloat())
        return "${java.text.DecimalFormat("0.0").format(progress * 100)}%"
    }

    fun copy(id: Long = this.id,
             url: String? = this.url,
             fileName: String? = this.fileName,
             mContentLength: Long = this.mContentLength,
             mCurrentBytes: Long = this.mCurrentBytes,
             status: Int = this.status,
             path: String? = this.localPath
    ) = DownloadRequest(id, url, fileName, mContentLength, mCurrentBytes, status, path)

    fun modifyDB() = MyApplication.app.database.use {
        update(TABLE_NAME,
                "url" to url, "fileName" to fileName, "mContentLength" to mContentLength, "mCurrentBytes" to mCurrentBytes, "status" to status, "localPath" to localPath
        ).whereArgs("id = {id}", "id" to id).exec()
        info("update id:$id")
    }

    fun insertDB() = MyApplication.app.database.use {
        id = insert(TABLE_NAME,
                "url" to url, "fileName" to fileName, "mContentLength" to mContentLength, "mCurrentBytes" to mCurrentBytes, "status" to status, "localPath" to localPath
        )
        info("insert id:$id")
    }

    fun deleteDB() = MyApplication.app.database.use {
        delete(TABLE_NAME, "id = {id}", "id" to id)
        info("delete id:$id")
    }
}