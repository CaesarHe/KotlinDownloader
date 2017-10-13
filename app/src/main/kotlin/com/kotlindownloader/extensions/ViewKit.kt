package com.kotlindownloader.extensions

import android.view.View

/**
 * Created by heyaokuai on 2017/10/12.
 */
inline fun View.show() {
    this.visibility = View.VISIBLE
}
inline fun View.gone() {
    this.visibility = View.GONE
}

inline fun View.hiden() {
    this.visibility = View.INVISIBLE
}