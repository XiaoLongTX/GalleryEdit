package com.bianxl.galleryedit.scanvideo

import android.app.AlertDialog
import android.app.RecoverableSecurityException
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
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
    private val TAG = "ScanVideoFragment"
    private var rootView: View? = null
    private var video2del: VideoUtils.Video? = null
    private var videoLever: Int = VideoUtils.LEVEL_MIN
    private lateinit var itemAdapter: VideoItemAdapter
    private var launch: ActivityResultLauncher<IntentSenderRequest>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launch = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) {
            VideoUtils.deleteVideo(
                activity ?: return@registerForActivityResult,
                video2del ?: return@registerForActivityResult,
                { path -> reloadData(path) }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_scan_video, container)
        videoLever = arguments?.getInt("video_level", VideoUtils.LEVEL_MIN) ?: VideoUtils.LEVEL_MIN
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val videoRecyclerView = rootView?.findViewById<RecyclerView>(R.id.video_list) ?: return
        videoRecyclerView.addItemDecoration(ItemSpacingDecoration(10))
        videoRecyclerView.layoutManager =
            GridLayoutManager(this.activity, 3, LinearLayoutManager.VERTICAL, false)
        itemAdapter = VideoItemAdapter(
            this.activity ?: return,
            R.layout.item_video_list,
            videoRecyclerView
        ).apply {
            this.setOnItemClickListener(scanVideoItemClickListener).setmDatas(
                VideoUtils.queryVideo(
                    activity ?: return,
                    videoLever
                )
            )
        }
    }

    private fun reloadData(path: String) {
        scanFileAndReload(activity ?: return, path) {
            rootView?.post {
                itemAdapter.setmDatas(
                    VideoUtils.queryVideo(
                        activity ?: return@post,
                        videoLever
                    )
                )
            }
        }
    }

    private val scanVideoItemClickListener = object :
        CommonSingleTypeAdapter.OnItemClickListener<VideoUtils.Video> {
        override fun onItemClick(
            holder: CommonRecyclerViewHolder,
            item: VideoUtils.Video,
            position: Int
        ) {
            val builder = AlertDialog.Builder(activity ?: return)
            builder.setTitle("提示")
                .setMessage(
                    when (videoLever) {
                        VideoUtils.LEVEL_MID -> "视频大小已经较小，是否继续压缩"
                        VideoUtils.LEVEL_MAX -> "视频体积较大，建议压缩"
                        else -> "已经是最优了，可直接播放"
                    }
                )

            when (videoLever) {
                VideoUtils.LEVEL_MID -> {
                    builder.setMessage("视频大小已经较小，是否继续压缩")
                        .setNeutralButton("播放") { _, _ -> }
                        .setPositiveButton("压到最小") { _, _ -> transform(item, 640f) }
                        .create().show()
                }

                VideoUtils.LEVEL_MAX -> {
                    builder.setMessage("视频体积较大，建议压缩")
                        .setNeutralButton("播放") { _, _ -> }
                        .setNegativeButton("压缩") { _, _ -> transform(item, 720f) }
                        .setPositiveButton("压到最小") { _, _ -> transform(item, 640f) }
                        .create().show()
                }

                else -> {
                    builder.setMessage("已经是最优了，可直接播放")
                        .setNeutralButton("播放") { _, _ -> }
                        .setPositiveButton("取消") { _, _ -> }
                        .create().show()
                }
            }
        }
    }

    private fun transform(item: VideoUtils.Video, videoSize: Float) {
        VideoTransformerUtil.getInstance(context ?: return)
            .transformVideo(item, videoSize) { outputPath ->
                reloadData(outputPath)
                showDeleteDialog(item)
            }
    }

    private fun showDeleteDialog(video: VideoUtils.Video) {
        val builder = AlertDialog.Builder(this.context ?: return)
        builder.setMessage("确定要删除吗？")
            .setPositiveButton("删除") { _, _ -> // 用户点击了删除按钮，执行删除操作
                video2del = video
                VideoUtils.deleteVideo(
                    this.activity ?: return@setPositiveButton,
                    video,
                    { path -> reloadData(path) },
                    {
                        // 若启用了分区存储，上面代码delete将会报错，显示没有权限。
                        // 需要捕获这个异常，并用下面代码，使用startIntentSenderForResult弹出弹窗向用户请求修改当前图片的权限
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val recoverableSecurityException: RecoverableSecurityException =
                                if (it is RecoverableSecurityException) {
                                    it
                                } else {
                                    throw RuntimeException(it.message, it)
                                }
                            val intentSender =
                                recoverableSecurityException.userAction.actionIntent.intentSender
                            try {
                                launch?.launch(IntentSenderRequest.Builder(intentSender).build())
                            } catch (e: IntentSender.SendIntentException) {
                                e.printStackTrace()
                            }
                        } else {
                            throw RuntimeException(it.message, it)
                        }

                    }
                )
            }
            .setNegativeButton("取消") { dialog, _ -> // 用户点击了取消按钮，关闭对话框
                dialog.dismiss()
            }

        // 创建并显示对话框
        val dialog = builder.create()
        dialog.show()
    }
}