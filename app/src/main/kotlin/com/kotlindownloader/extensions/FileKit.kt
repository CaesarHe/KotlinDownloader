package com.kotlindownloader.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import com.kotlindownloader.BuildConfig
import java.io.File
import java.io.IOException
import java.util.regex.Pattern

/**
 * Created by heyaokuai on 2017/10/11.
 */
fun File.mkfiles(): File? {
    if (!this.exists()) {
        try {
            // Check localPath
            val parentPath = this.parentFile
            if (parentPath != null && !parentPath.exists()) {
                parentPath.mkdirs()
            }
            if (!this.createNewFile()) {
                return null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    return this
}

fun File.getFileName(): String? {
    val pattern = Pattern.compile("[^/\\\\]+$")
    val matcher = pattern.matcher(this.path)
    if (matcher.find()) {
        return matcher.group()
    }
    return null
}
fun File.deleteSafe() {
    if (this.exists()) this.delete()
}


fun File.toUri(context: Context?): Uri {
    if (context == null || this == null) {
        throw NullPointerException()
    }
    return if (Build.VERSION.SDK_INT >= 24) {
        FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", this)
    } else {
        Uri.fromFile(this)
    }
}

/**
 * 打开文件
 * 兼容7.0
 *
 * @param context     activity
 * @param file        File
 * @param contentType 文件类型如：文本（text/html）
 */
fun File.startAction(context: Context?, contentType: String) {
    if (context == null || this == null) {
        return
    }
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    intent.setDataAndType(this.toUri(context), contentType)
    if (context !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    safe {
        context.startActivity(intent)
    }
}