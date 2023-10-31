package com.bianxl.galleryedit

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View.OnClickListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bianxl.galleryedit.common.ItemSpacingDecoration
import com.trionesble.smart.remote.ui.adapter.CommonRecyclerViewHolder
import com.trionesble.smart.remote.ui.adapter.CommonSingleTypeAdapter

/**
 *
 * author maxbian
 *
 * date 2023/10/30
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val videoRecyclerView = findViewById<RecyclerView>(R.id.video_list)
        videoRecyclerView.addItemDecoration(
            ItemSpacingDecoration(10).setDivider(
                ColorDrawable(
                    Color.parseColor(
                        "#33333333"
                    )
                )
            )
        )
        videoRecyclerView.layoutManager =
            GridLayoutManager(this@MainActivity, 2, LinearLayoutManager.VERTICAL, false)
        object : CommonSingleTypeAdapter<MainItem>(
            this@MainActivity,
            R.layout.item_main_list,
            videoRecyclerView
        ) {
            override fun convert(helper: CommonRecyclerViewHolder, item: MainItem, pos: Int) {
                helper.setText(R.id.title, item.title)
                helper.setImageRes(R.id.icon, item.icon)
            }
        }.setOnItemClickListener(object :
            CommonSingleTypeAdapter.OnItemClickListener<MainItem> {
            override fun onItemClick(
                holder: CommonRecyclerViewHolder,
                item: MainItem,
                position: Int
            ) {
                item.clickListener.onClick(holder.itemView)
            }

        }).setmDatas(
            arrayListOf(
                MainItem(
                    R.drawable.icon_video,
                    "视频压缩"
                ) {
                    startActivity(Intent(this@MainActivity, ScanVideoActivity::class.java))
                },
                MainItem(
                    R.drawable.icon_about,
                    "关于小枸杞"
                ) {
                    // TODO: about
                },
            )
        )
    }

    data class MainItem(
        val icon: Int,
        val title: String,
        val clickListener: OnClickListener
    )
}