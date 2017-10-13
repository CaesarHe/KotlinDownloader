package com.kotlindownloader.biz

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.kotlindownloader.MyApplication

/**
 * Created by heyaokuai on 2017/10/12.
 */
object AppListManager {
    fun getList(): Array<Recommend> {
        MyApplication.app.assets.open("data.json").use {
            var array = JsonParser().parse(it.reader().readText()).asJsonArray
            return Gson().fromJson(array, Array<Recommend>::class.java)
        }
    }
}
