package com.kotlindownloader.download.constans

/**
 * Created by heyaokuai on 2017/9/27.
 */
object DownloadConstans {
    /**
     * Error code when writing download content to the destination file.
     */
    val ERROR_FILE_ERROR = 1001

    /**
     * Error code when an HTTP code was received that download manager can't
     * handle.
     */
    val ERROR_UNHANDLED_HTTP_CODE = 1002

    /**
     * Error code when an error receiving or processing data occurred at the
     * HTTP level.
     */
    val ERROR_HTTP_DATA_ERROR = 1004

    /**
     * Error code when there were too many redirects.
     */
    val ERROR_TOO_MANY_REDIRECTS = 1005

    /**
     * Error code when size of the file is unknown.
     */
    val ERROR_DOWNLOAD_SIZE_UNKNOWN = 1006

    /**
     * Error code when passed URI is malformed.
     */
    val ERROR_MALFORMED_URI = 1007

    /**
     * Error code when there is connection timeout after maximum retries
     */
    val ERROR_CONNECTION_TIMEOUT_AFTER_RETRIES = 1009


}