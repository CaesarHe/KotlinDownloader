package com.kotlindownloader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.kotlindownloader.fragment.RecommendListFragment
import org.jetbrains.anko.*

object ViewID {
    val CONTENT_LAYOUT = 1000
    val RECYCLERVIEW_ID = 1001
}

class MainActivity : AppCompatActivity() {
    val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        downloadManager
        super.onCreate(savedInstanceState)
        ActivityUI().setContentView(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
        }
        var view = RecommendListFragment.instance
        supportFragmentManager.beginTransaction().add(ViewID.CONTENT_LAYOUT, view, RecommendListFragment.TAG).commit()
    }

    private class ActivityUI : AnkoComponent<MainActivity> {
        override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
            verticalLayout {
                id = ViewID.CONTENT_LAYOUT
            }
        }
    }


}


