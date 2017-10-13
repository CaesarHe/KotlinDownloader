package com.kotlindownloader.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kotlindownloader.ViewID
import com.kotlindownloader.biz.AppListManager
import com.kotlindownloader.biz.Recommend
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.verticalLayout
import java.util.*

/**
 * Created by heyaokuai on 2017/10/12.
 */
class RecommendListFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var rootView: View
    var dataList = ArrayList<Recommend>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = UI {
            verticalLayout {
                lparams(width = matchParent, height = matchParent)//设置布局的宽高
                recyclerView {
                    //设置滚动视图RecyclerView
                    id = ViewID.RECYCLERVIEW_ID//控件的id
                }.lparams(width = matchParent, height = matchParent)//控件的宽高
            }
        }.view
        recyclerView = rootView.findViewById<RecyclerView>(ViewID.RECYCLERVIEW_ID) // 根据id获取指定控件
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindAdapter()
        dataList.clear()
        dataList.addAll(AppListManager.getList())
        recyclerView.adapter.notifyDataSetChanged()
    }

    private fun bindAdapter() {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = RecommendListAdapter(activity, dataList)
    }

    companion object {
        val instance = RecommendListFragment()
        val TAG = RecommendListFragment::class.java.simpleName
    }
}