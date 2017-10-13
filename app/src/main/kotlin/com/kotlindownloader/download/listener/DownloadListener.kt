package com.kotlindownloader.download.listener

/**
 * Created by heyaokuai on 2017/9/27.
 */
interface IDownloadListener {
    fun progress(id: Long, progress: Int)
    fun newTask(id: Long)
    fun complete(id: Long)
    fun failed(id: Long, errorCode: Int)
    fun stateChange(id: Long, state: Int)
}

open class DownloadListenerImp : IDownloadListener {
    private var _progress: ((id: Long, progress: Int) -> Unit)? = null
    private var _newTask: ((id: Long) -> Unit)? = null
    private var _complete: ((id: Long) -> Unit)? = null
    private var _failed: ((id: Long, errorCode: Int) -> Unit)? = null
    private var _stateChange: ((id: Long, state: Int) -> Unit)? = null
    override fun progress(id: Long, progress: Int) {
        _progress?.invoke(id, progress)
    }

    override fun newTask(id: Long) {
        _newTask?.invoke(id)
    }

    override fun complete(id: Long) {
        _complete?.invoke(id)
    }

    override fun failed(id: Long, errorCode: Int) {
        _failed?.invoke(id, errorCode)
    }

    override fun stateChange(id: Long, state: Int) {
        _stateChange?.invoke(id, state)
    }

    /**
     * support user imp
     */
    fun onProgress(block: (id: Long, progress: Int) -> Unit) {
        _progress = block
    }

    fun onNewTask(block: (id: Long) -> Unit) {
        _newTask = block
    }

    fun onComplete(block: (id: Long) -> Unit) {
        _complete = block
    }

    fun onFailed(block: (id: Long, errorCode: Int) -> Unit) {
        _failed = block
    }

    fun onStateChange(block: (id: Long, state: Int) -> Unit) {
        _stateChange = block
    }
}