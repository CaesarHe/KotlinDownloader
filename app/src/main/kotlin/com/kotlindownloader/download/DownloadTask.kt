package com.kotlindownloader.download

import com.kotlindownloader.download.constans.DownloadConstans
import com.kotlindownloader.download.entity.DownloadRequest
import com.kotlindownloader.download.listener.IDownloadListener
import com.kotlindownloader.extensions.mkfiles
import com.kotlindownloader.extensions.runOnUiThread
import org.apache.http.conn.ConnectTimeoutException
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.*
import java.net.HttpURLConnection.*


/**
 * Created by heyaokuai on 2017/9/27.
 */

class DownloadTask(var request: DownloadRequest) : Thread(), AnkoLogger {
    companion object {
        /** The buffer size used to stream the data  */
        private val BUFFER_SIZE = 4096

        /** The maximum number of redirects.  */
        private val MAX_REDIRECTS = 5 // can't be more than 7.
        private val HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416
        private val HTTP_TEMP_REDIRECT = 307
        /** The default socket timeout in milliseconds  */
        private val DEFAULT_TIMEOUT_MS = 5000
    }

    /** How many times redirects happened during a download request.  */
    private var mRedirectionCount = 0
    private var shouldAllowRedirects = true
    var taskListener: IDownloadListener? = null

    override fun run() {
        mRedirectionCount = 0
        shouldAllowRedirects = true
        executeDownload()
    }

    override fun start() {
        updateDownloadState(DownloadRequest.STATUS_RUNNING)
        super.start()
    }

    private fun executeDownload(downloadUrl: String? = request.url) {
        info("${request.id} start download!!!!!!!!!!!")
        var conn: HttpURLConnection? = null
        try {
            val url = URL(downloadUrl)
            conn = url.openConnection() as HttpURLConnection
            conn!!.instanceFollowRedirects = false
            conn!!.connectTimeout = DEFAULT_TIMEOUT_MS
            conn!!.readTimeout = DEFAULT_TIMEOUT_MS

            if (request.mCurrentBytes > 0) {
                conn!!.setRequestProperty("Range", "bytes=${request.mCurrentBytes}-")
                info("set header Range: bytes=${request.mCurrentBytes}-")
            } else if (File(request.localPath).exists()) {
                File(request.localPath).delete()
            }
            val responseCode = conn!!.responseCode

            info("Response code obtained for downloaded Id ${request.id} : httpResponse Code $responseCode")
            when (responseCode) {
                HTTP_PARTIAL, HTTP_OK -> {
                    shouldAllowRedirects = false
                    if (readResponseHeaders(request, conn)) {
                        transferData(conn)
                    } else {
                        updateDownloadFailed(DownloadConstans.ERROR_DOWNLOAD_SIZE_UNKNOWN, "Transfer-Encoding not found as well as can't know size of download, giving up")
                    }
                    return
                }
                HTTP_MOVED_PERM, HTTP_MOVED_TEMP, HTTP_SEE_OTHER, HTTP_TEMP_REDIRECT , HTTP_MOVED_TEMP-> {
                    // Take redirect url and call executeDownload recursively until
                    // MAX_REDIRECT is reached.
                    while (mRedirectionCount < MAX_REDIRECTS && shouldAllowRedirects) {
                        mRedirectionCount++
                        info("Redirect for downloaded Id ${request.id}")
                        val location = conn!!.getHeaderField("Location")
                        executeDownload(location)
                    }

                    if (mRedirectionCount > MAX_REDIRECTS && shouldAllowRedirects) {
                        updateDownloadFailed(DownloadConstans.ERROR_TOO_MANY_REDIRECTS, "Too many redirects, giving up")
                        return
                    }
                }
                HTTP_REQUESTED_RANGE_NOT_SATISFIABLE -> updateDownloadFailed(HTTP_REQUESTED_RANGE_NOT_SATISFIABLE, conn!!.getResponseMessage())
                HTTP_UNAVAILABLE -> updateDownloadFailed(HTTP_UNAVAILABLE, conn!!.getResponseMessage())
                HTTP_INTERNAL_ERROR -> updateDownloadFailed(HTTP_INTERNAL_ERROR, conn!!.getResponseMessage())
                else -> updateDownloadFailed(DownloadConstans.ERROR_UNHANDLED_HTTP_CODE, "Unhandled HTTP response:" + responseCode + " message:" + conn!!.getResponseMessage())
            }
        } catch (e: MalformedURLException) {
            updateDownloadFailed(DownloadConstans.ERROR_MALFORMED_URI, "MalformedURLException: URI passed is malformed.")
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
            updateDownloadFailed(DownloadConstans.ERROR_CONNECTION_TIMEOUT_AFTER_RETRIES, "sokect timeout")
        } catch (e: ConnectTimeoutException) {
            e.printStackTrace()
            updateDownloadFailed(DownloadConstans.ERROR_CONNECTION_TIMEOUT_AFTER_RETRIES, "connect timeout")
        } catch (e: IOException) {
            e.printStackTrace()
            updateDownloadFailed(DownloadConstans.ERROR_HTTP_DATA_ERROR, "Trouble with low-level sockets")
        } finally {
            conn?.disconnect()
        }
    }


    private fun writeDataToDestination(request: DownloadRequest, data: ByteArray, bytesRead: Int, randomFile: RandomAccessFile): Boolean {
        try {
            randomFile.write(data, 0, bytesRead)
            return true
        } catch (e: Exception) {
        }
        return false
    }

    private fun readResponseHeaders(request: DownloadRequest, conn: HttpURLConnection): Boolean {
        val transferEncoding = conn.getHeaderField("Transfer-Encoding")
        request.mContentLength = -1L
        if (transferEncoding == null) {
            request.mContentLength = getHeaderFieldLong(conn, "Content-Length", -1L)
            if (request.mContentLength > 0 && request.mCurrentBytes > 0) {
                request.mContentLength += request.mCurrentBytes
            }
        } else {
            info("Ignoring Content-Length since Transfer-Encoding is also defined for Downloaded Id ${request.id}")
        }
        return if (request.mContentLength != -1L) {
            true
        } else if (transferEncoding == null || !transferEncoding.equals("chunked", true)) {
            false
        } else {
            false
        }
    }

    private fun getHeaderFieldLong(conn: URLConnection, field: String, defaultValue: Long): Long {
        try {
            return conn.getHeaderField(field).toLong()
        } catch (e: Exception) {
            return defaultValue
        }
    }

    /**********  transfer data **********/

    private fun transferData(conn: HttpURLConnection) {
        val destinationFile = File(request.localPath).mkfiles() ?: return updateDownloadFailed(DownloadConstans.ERROR_FILE_ERROR,
                "Error in creating destination file")
        var randomFile: RandomAccessFile
        try {
            randomFile = RandomAccessFile(destinationFile, "rwd")
            randomFile.seek(request.mCurrentBytes)
        } catch (e: Exception) {
            error(e)
            updateDownloadFailed(DownloadConstans.ERROR_FILE_ERROR,
                    "Error in writing download contents to the destination file")
            return
        }

        var `in`: InputStream?
        try {
            `in` = conn.inputStream
        } catch (e: IOException) {
            e.printStackTrace()
            updateDownloadFailed(DownloadConstans.ERROR_FILE_ERROR,
                    "Error in creating input stream")
            return
        }
        // Start streaming data
        `in`.use {
            randomFile.use {
                transferData(request, `in`!!, randomFile)
            }
        }
    }

    private fun transferData(request: DownloadRequest, `in`: InputStream, randomFile: RandomAccessFile) {
        val data = ByteArray(BUFFER_SIZE)
        info("Content Length:${request.mContentLength} for Download Id ${request.id}")
        while (true) {
            if (!request.isRuning()) {
                info("Stopping the download as Download Request not is running for Downloaded Id ${request.id}")
                request.finish()
                return
            }
            val bytesRead = readFromResponse(data, `in`)
            if (bytesRead == -1) { // success, end of stream already reached
                updateDownloadComplete()
                return
            } else if (bytesRead == Integer.MIN_VALUE) {
                return
            }
            if (writeDataToDestination(request, data, bytesRead, randomFile)) {
                request.mCurrentBytes += bytesRead
                updateDownloadProgress(request.getProgress(), request.mCurrentBytes)
            } else {
                updateDownloadFailed(DownloadConstans.ERROR_FILE_ERROR, "Failed writing file")
                return
            }
        }
    }

    private fun readFromResponse(data: ByteArray, entityStream: InputStream): Int {
        try {
            return entityStream.read(data)
        } catch (ex: IOException) {
            if ("unexpected end of stream" == ex.message) {
                return -1
            }
            updateDownloadFailed(DownloadConstans.ERROR_HTTP_DATA_ERROR, "IOException: Failed reading response")
            return Integer.MIN_VALUE
        }

    }

    /************ notify status **************/
    fun updateDownloadState(status: Int) {
        request.status = status
        request.modifyDB()
        runOnUiThread {
            taskListener?.stateChange(request.id, status)
            request.getListener()?.stateChange(request.id, status)
        }
    }

    private fun updateDownloadFailed(errorCode: Int, errorMsg: String) {
        shouldAllowRedirects = false
        request.status = DownloadRequest.STATUS_FAILED
        request.modifyDB()
        request.finish()
        error(errorMsg)
        runOnUiThread {
            taskListener?.failed(request.id, errorCode)
            request.getListener()?.failed(request.id, errorCode)
        }

    }

    private fun updateDownloadComplete() {
        request.status = DownloadRequest.STATUS_COMPLETE
        request.finish()
        info("complete: ${request.id}")
        request.modifyDB()
        runOnUiThread {
            taskListener?.complete(request.id)
            request.getListener()?.complete(request.id)
        }
    }

    private var tmpProgress = 0
    private fun updateDownloadProgress(progress: Int, downloadedBytes: Long) {
        request.modifyDB()
        if (progress >= tmpProgress) {
            tmpProgress = progress
            info("progress: $progress")
            runOnUiThread {
                taskListener?.progress(request.id, progress)
                request.getListener()?.progress(request.id, progress)
            }
        }
    }
}


