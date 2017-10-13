package com.kotlindownloader.fragment

import android.content.Context
import android.os.Environment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.kotlindownloader.R
import com.kotlindownloader.biz.Recommend
import com.kotlindownloader.config.GlideApp
import com.kotlindownloader.download.DownloadManager
import com.kotlindownloader.download.entity.DownloadRequest
import com.kotlindownloader.extensions.*
import org.jetbrains.anko.sdk15.listeners.onClick
import java.io.File


/**
 * Created by heyaokuai on 2017/9/28.
 */
class RecommendListAdapter(val context: Context, var list: List<Recommend>) : RecyclerView.Adapter<RecommendListAdapter.ViewHolder>() {
    override fun getItemCount() = list.size
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recommend_list_item, null))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(list[position]) {
            holder.request?.let { it.unRegisterListener() }
            holder.item_no.text = "${position + 1}."
            holder.item_title.text = name
            holder.item_desc_tv.text = editorRecommend
            holder.item_middle_second.text = "${(size * 1024L).formatFileSize}·$categoryName"

            GlideApp.with(context).load(iconUrl).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(holder.item_icon)
            holder.right_btn.onClick { doCmd(holder, downloadUrl!!, packageName!!) }
            refreshItem(holder, downloadUrl!!)
        }
    }

    private fun doCmd(holder: ViewHolder, downloadUrl: String, packageName: String) {
        var request = mDownloadManager.getStatus(downloadUrl!!)
        if (request == null) {
            val request = DownloadRequest(
                    url = downloadUrl,
                    fileName = "$packageName.apk",
                    localPath = "${Environment.getExternalStorageDirectory()}/kotlin/download/$packageName.apk"
            )
            mDownloadManager.runCmd(DownloadManager.CMD_NEW, request)
            refreshItem(holder, downloadUrl!!)
        } else {
            with(request) {
                when (status) {
                    com.kotlindownloader.download.entity.DownloadRequest.STATUS_CANCELED -> {
                        mDownloadManager.runCmd(com.kotlindownloader.download.DownloadManager.CMD_NEW, this)
                        refreshItem(holder, downloadUrl!!)
                    }
                    com.kotlindownloader.download.entity.DownloadRequest.STATUS_UNKNOW, com.kotlindownloader.download.entity.DownloadRequest.STATUS_FAILED ->
                        mDownloadManager.runCmd(com.kotlindownloader.download.DownloadManager.CMD_RETRY, this.id)
                    com.kotlindownloader.download.entity.DownloadRequest.STATUS_RUNNING -> mDownloadManager.runCmd(DownloadManager.CMD_PAUSE, this.id)
                    com.kotlindownloader.download.entity.DownloadRequest.STATUS_PAUSE -> mDownloadManager.runCmd(DownloadManager.CMD_RESUME, this.id)
                    com.kotlindownloader.download.entity.DownloadRequest.STATUS_COMPLETE -> {
                        File(request.localPath!!).startAction(context, "application/vnd.android.package-archive")
                    }
                    else -> {
                        mDownloadManager.runCmd(com.kotlindownloader.download.DownloadManager.CMD_CANCEL, this.id)
                    }
                }
            }
        }
    }

    private fun refreshItem(holder: ViewHolder, downloadUrl: String) {
        holder.request = mDownloadManager.getStatus(downloadUrl!!)
        resetViewState(holder)
        holder.request?.let {
            holder.item_progress.show()
            holder.item_progress_tv.show()
            holder.item_status_tv.show()
            holder.item_middle_second.gone()
            holder.item_desc_tv.gone()
            holder.item_middle_first.show()
            bindItemData(holder, holder.request!!)
        }
    }

    private fun bindItemData(holder: ViewHolder, request: DownloadRequest) = with(request) {
        registerListener {
            onProgress { id, progress ->
                refreshItem(holder, url!!)
            }

            onNewTask { id ->
                refreshItem(holder, url!!)
            }

            onComplete { id ->
                refreshItem(holder, url!!)
            }
            onFailed { id, errorCode ->
                refreshItem(holder, url!!)
            }

            onStateChange { id, state ->
                refreshItem(holder, url!!)
            }
        }
        holder.item_progress.progress = getProgress()
        holder.item_progress_tv.text = getProgressAsString()
        when (status) {
            DownloadRequest.STATUS_PENDING -> {
                holder.right_btn.text = "等待"
                holder.item_status_tv.text = "排队中"
            }
            DownloadRequest.STATUS_RUNNING -> {
                holder.right_btn.text = "暂停"
                holder.item_status_tv.text = "${mCurrentBytes.formatFileSize}"
            }
            DownloadRequest.STATUS_PAUSE -> {
                holder.item_status_tv.text = "已暂停"
                holder.right_btn.text = "继续"
            }
            DownloadRequest.STATUS_FAILED -> {
                holder.item_status_tv.text = "失败"
                holder.right_btn.text = "重试"
            }
            DownloadRequest.STATUS_COMPLETE -> {
                holder.item_status_tv.text = "${mCurrentBytes.formatFileSize}"
            }
            DownloadRequest.STATUS_STOP -> {
            }
            DownloadRequest.STATUS_CANCELED -> {
            }
        }
    }

    private fun resetViewState(holder: ViewHolder) {
        holder.item_middle_second.show()
        holder.item_progress.gone()
        holder.item_progress_tv.gone()
        holder.item_status_tv.gone()
        holder.item_middle_first.gone()
        holder.item_desc_tv.show()
        holder.right_btn.text = "安装"
    }

    /**
     * 自定义ViewHolder
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var request: DownloadRequest? = null
        var item_no = view.findViewById<TextView>(R.id.item_no)
        var item_icon = view.findViewById<ImageView>(R.id.item_icon)
        var item_title = view.findViewById<TextView>(R.id.item_title)
        var item_middle_first = view.findViewById<View>(R.id.item_middle_first)
        var item_middle_second = view.findViewById<TextView>(R.id.item_middle_second)
        var item_status_tv = view.findViewById<TextView>(R.id.item_status_tv)
        var item_progress_tv = view.findViewById<TextView>(R.id.item_progress_tv)
        var item_desc_tv = view.findViewById<TextView>(R.id.item_desc_tv)
        var item_progress = view.findViewById<ProgressBar>(R.id.item_progress)
        var right_btn = view.findViewById<Button>(R.id.right_btn)
    }
}
