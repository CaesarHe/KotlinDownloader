package com.kotlindownloader.http

import android.util.Log
import okhttp3.*
import okhttp3.internal.tls.OkHostnameVerifier
import java.io.File
import java.io.IOException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSession

/**
 * Created by heyaokuai on 2017/10/12.
 */
class AccountHttpClient private constructor() {
    private val TAG = "AccountHttpClient"
    /**
     * 只使用一个实例的，以便重用response cache、thread pool、connection re-use 等
     */
    private val mOkHttpCilent: OkHttpClient
    private object Holder { val INSTANCE = AccountHttpClient() }
    companion object {//构造单例
    private var CONNECTION_TIME_OUT = 30 * 1000
        private var READ_TIME_OUT = 30 * 1000
        private var WRITE_TIME_OUT = 30 * 1000
        val  instance: AccountHttpClient by lazy { Holder.INSTANCE }
    }
    //由于primary constructor不能包含任何代码，因此使用 init 代码块对其初始化，同时可以在初始化代码块中使用构造函数的参数
    init {
        mOkHttpCilent = OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIME_OUT.toLong(), TimeUnit.SECONDS)//注意显示转化toLong
                .readTimeout(READ_TIME_OUT.toLong(), TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIME_OUT.toLong(), TimeUnit.SECONDS)
                .hostnameVerifier(AccountHostnameVerifier())
                .build()
    }
    /**
     * 发起get请求
     */
    @Throws(Exception::class)
    operator fun get(url: String, requestParams: List<Pair<String, String>>,
                     requestHeaders: List<Pair<String, String>>): AccountResponse? {
        return execute(Type.GET, url, requestParams, requestHeaders)
    }
    /**
     * 发起post请求
     */
    @Throws(Exception::class)
    fun post(url: String, requestParams: List<Pair<String, String>>?,
             requestHeaders: List<Pair<String, String>>?): AccountResponse? {
        return execute(Type.POST, url, requestParams, requestHeaders)
    }
    /**
     * 上传文件
     */
    @Throws(Exception::class)
    fun uploadFile(url: String, requestParams: List<Pair<String, String>>,
                   requestHeaders: List<Pair<String, String>>, filePath: String): AccountResponse? {
        return execute(url, requestParams, requestHeaders, filePath)
    }
    /**
     * 下载文件
     */
    @Throws(Exception::class)
    fun downloadFile(url: String, requestParams: List<Pair<String, String>>,
                     requestHeaders: List<Pair<String, String>>): AccountResponse? {
        return execute(Type.POST, url, requestParams, requestHeaders)
    }
    /**
     * 执行请求
     */
    @Throws(Exception::class)
    private fun execute(type: Type, url: String, requestParams: List<Pair<String, String>>?,
                        requestHeaders: List<Pair<String, String>>?): AccountResponse? {
        var result: AccountResponse? = null
        try {
            val okResponse = performRequest(OKRequest(type, url, requestParams, requestHeaders))//进行参数封装，发起网络请求
            if (okResponse != null) {
                result = AccountResponse(okResponse.responseCode, okResponse.body, okResponse.headers)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        return result
    }
    @Throws(Exception::class)
    private fun execute(url: String, requestParams: List<Pair<String, String>>,
                        requestHeaders: List<Pair<String, String>>, filePath: String): AccountResponse? {
        var result: AccountResponse? = null
        try {
            val okResponse = performRequest(OKRequest(Type.POST, url, requestParams, requestHeaders, filePath))
            if (okResponse != null) {
                result = AccountResponse(okResponse.responseCode, okResponse.body, okResponse.headers)
            }
        } catch (e: Exception) {
            throw e
        }
        return result
    }
    internal enum class Type {
        GET,
        POST
    }
    /**
     * 请求参数类
     */
    private inner class OKRequest {
        private var mType: Type? = null
        var mUrl: String? = null
            private set
        private var mRequestParams: List<Pair<String, String>>? = null
        private var mRequestHeaders: List<Pair<String, String>>? = null
        private var mFilePath: String? = null
        constructor(type: Type, url: String,
                    requestParams: List<Pair<String, String>>?,
                    requestHeaders: List<Pair<String, String>>?) {
            mType = type
            mUrl = url
            mRequestParams = requestParams
            mRequestHeaders = requestHeaders
        }
        constructor(type: Type, url: String,
                    requestParams: List<Pair<String, String>>,
                    requestHeaders: List<Pair<String, String>>, filePath: String) {
            mType = type
            mUrl = url
            mRequestParams = requestParams
            mRequestHeaders = requestHeaders
            mFilePath = filePath
        }
        fun getDataRequest(): Request {//此处可以改成request的get()方法
            val request: Request
            val builder = Request.Builder()
            builder.url(mUrl!!)
            builder.header("Connection", "Close")
            if (mRequestHeaders?.isNotEmpty() ?: false) {
                for (header in mRequestHeaders!!) {
                    builder.addHeader(header.first, header.second)
                }
            }
            if (mType == Type.GET) {
                builder.get()
            } else {
                if (mFilePath?.isNotEmpty() ?: false) {//设置上传文件
                    val bodyBuilder = MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                    val file = File(mFilePath)
                    if (file.exists()) {
                        bodyBuilder.addFormDataPart("file", file.name, RequestBody.create(null, file))
                    }
                    mRequestParams?.let {
                        for (param in it) {
                            bodyBuilder.addFormDataPart(param.first, param.second)
                        }
                    }
                    builder.post(bodyBuilder.build())
                } else {//设置参数
                    mRequestParams?.let {
                        val formBuilder = FormBody.Builder()
                        for (param in it) {
                            formBuilder.add(param.first, param.second)
                        }
                        builder.post(formBuilder.build())
                    }
                }
            }
            request = builder.build()
            return request
        }
    }
    /**
     * 响应类
     */
    private inner class OKResponse(private val response: Response?) {
        val responseCode: Int
            get() = response!!.code()
        val body: ByteArray?
            get() {
                var result: ByteArray? = null
                if (response?.body() != null) {
                    try {
                        result = response.body()!!.bytes()
                    } catch (e: IOException) {
                        Log.e(TAG, e.message)
                    }
                    headers
                }
                return result
            }
        val headers: MutableMap<String, MutableList<String>>?
            get() {
                var headers: MutableMap<String, MutableList<String>>? = null
                if (response != null && response.headers() != null) {
                    try {
                        headers = response.headers().toMultimap()
                    } catch (e: Exception) {
                    }
                }
                return headers
            }
    }
    @Throws(IOException::class)
    private fun performRequest(request: OKRequest): OKResponse? {
        val call = mOkHttpCilent.newCall(request.getDataRequest())//new一个call，通过okhttp发起请求
        return OKResponse(call.execute())
    }
    /**
     * 证书验证
     */
    private inner class AccountHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            var result = false
            try {
                val certs = session.peerCertificates as Array<X509Certificate>
                if (certs.isNotEmpty()) {
                    for (i in certs.indices) {//把证书取出，一个一个验证
                        result = OkHostnameVerifier.INSTANCE.verify(hostname, certs[i])
                        if (result) {
                            break
                        }
                    }
                } else {
                    result = true
                }
            } catch (e: SSLPeerUnverifiedException) {
                e.printStackTrace()
            }
            return result
        }
    }
}