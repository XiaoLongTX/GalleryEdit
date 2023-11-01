package com.bianxl.galleryedit.scanvideo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bianxl.galleryedit.R
import com.bianxl.galleryedit.VideoTransformerUtil
import com.bianxl.galleryedit.VideoUtils
import com.bianxl.galleryedit.VideoUtils.scanFileAndReload
import com.bianxl.galleryedit.adapter.VideoItemAdapter
import com.bianxl.galleryedit.common.ItemSpacingDecoration
import com.trionesble.smart.remote.ui.adapter.CommonRecyclerViewHolder
import com.trionesble.smart.remote.ui.adapter.CommonSingleTypeAdapter

/**
 *
 * author maxbian
 *
 * date 2023/11/1
 */
class ScanVideoFragment : Fragment() {
    var rootView: View? = null
    private var video2del: VideoUtils.Video? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_scan_video, container)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val videoRecyclerView = rootView?.findViewById<RecyclerView>(R.id.video_list) ?: return
        videoRecyclerView.addItemDecoration(ItemSpacingDecoration(10))
        videoRecyclerView.layoutManager =
            GridLayoutManager(this.context, 3, LinearLayoutManager.VERTICAL, false)
        VideoItemAdapter(
            this.context ?: return,
            R.layout.item_video_list,
            videoRecyclerView
        ).setOnItemClickListener(scanVideoItemClickListener).setmDatas(
            VideoUtils.queryVideo(
                this?.context ?: return,
                arguments?.getInt("video_level") ?: return
            )
        )
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult(), object :
            ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result.resultCode == FragmentActivity.RESULT_OK) {
                    // 用户授予修改图片权限，再次执行删除操作
                    VideoUtils.deleteVideo(
                        activity ?: return,
                        video2del ?: return
                    ) { path ->
                        scanFileAndReload(activity ?: return@deleteVideo, path) {
                            // TODO:
                        }
                    }
                } else {
                    /* Edit request not granted; explain to the user. */
                }
            }
        })
    }

    private val scanVideoItemClickListener = object :
        CommonSingleTypeAdapter.OnItemClickListener<VideoUtils.Video> {
        override fun onItemClick(
            holder: CommonRecyclerViewHolder,
            item: VideoUtils.Video,
            position: Int
        ) {
            VideoTransformerUtil.getInstance(context ?: return)
                .transformVideo(item, 720f) { outputPath ->
                    scanFileAndReload(activity ?: return@transformVideo, outputPath) {
                        activity?.runOnUiThread {
                            // TODO:  
                        }
                    }
                    showDeleteDialog(item)
                }
        }
    }

    private fun showDeleteDialog(video: VideoUtils.Video) {
        val builder = AlertDialog.Builder(this?.context ?: return)
        builder.setMessage("确定要删除吗？")
            .setPositiveButton("删除") { _, _ -> // 用户点击了删除按钮，执行删除操作
                video2del = video
                VideoUtils.deleteVideo(
                    this.activity ?: return@setPositiveButton,
                    video
                ) { path ->
                    scanFileAndReload(activity ?: return@deleteVideo, path) {
                        activity?.runOnUiThread {
                            // TODO:  
                        }
                    }
                }
            }
            .setNegativeButton("取消") { dialog, id -> // 用户点击了取消按钮，关闭对话框
                dialog.dismiss()
            }

        // 创建并显示对话框
        val dialog = builder.create()
        dialog.show()
    }


}