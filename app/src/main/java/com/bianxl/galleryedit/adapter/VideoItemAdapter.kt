package com.bianxl.galleryedit.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.bianxl.galleryedit.R
import com.bianxl.galleryedit.VideoUtils
import com.trionesble.smart.remote.ui.adapter.CommonRecyclerViewHolder
import com.trionesble.smart.remote.ui.adapter.CommonSingleTypeAdapter

/**
 *
 * author maxbian
 *
 * date 2023/10/31
 */
class VideoItemAdapter(context: Context, layoutId: Int, recyclerView: RecyclerView) :
    CommonSingleTypeAdapter<VideoUtils.Video>(context, layoutId, recyclerView) {
    override fun convert(
        helper: CommonRecyclerViewHolder,
        item: VideoUtils.Video,
        pos: Int
    ) {
        helper.setImageBitmap(
            R.id.video_image,
            VideoUtils.getVideoBitmap(context, item.uri, item.id)
        )
        helper.setText(R.id.video_name, item.name)
        helper.setText(R.id.vide_width_heigh, "${item.width} * ${item.height}")
        helper.setText(
            R.id.video_bitrate,
            "码率:${String.format("%.2f", item.bitrate)}M/s"
        )
        helper.setText(
            R.id.video_durition,
            "${String.format("%.1f", item.duration)}s / ${
                String.format(
                    "%.2f",
                    item.size
                )
            }M"
        )
    }
}