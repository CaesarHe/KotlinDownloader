package com.kotlindownloader.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

/**
 * Created by heyaokuai on 2017/10/11.
 */
class DownloadDBHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "download", null, 1) {
    companion object {
        val TABLE_NAME: String = "DOWNLOADLIST"
        private var instance: DownloadDBHelper? = null
        @Synchronized
        fun getInstance(ctx: Context): DownloadDBHelper {
            if (instance == null) {
                instance = DownloadDBHelper(ctx.applicationContext)
            }
            return instance!!
        }

    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(TABLE_NAME, true,
                "id" to INTEGER_PRIMARY_KEY_AUTOINCREMENT, "url" to TEXT, "fileName" to TEXT, "mContentLength" to LONG, "mCurrentBytes" to LONG, "status" to INTEGER, "localPath" to TEXT)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable(TABLE_NAME, true)
    }

}

val Context.database: DownloadDBHelper
    get() = DownloadDBHelper.getInstance(applicationContext)

val LONG: SqlType = SqlType.create("LONG")
val INTEGER_PRIMARY_KEY_AUTOINCREMENT: SqlType = SqlType.create("INTEGER PRIMARY KEY AUTOINCREMENT")