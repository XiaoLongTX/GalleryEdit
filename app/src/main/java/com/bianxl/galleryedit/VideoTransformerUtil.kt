package com.bianxl.galleryedit

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import com.bianxl.galleryedit.common.SingletonHolder

/**
 *
 * author maxbian
 *
 * date 2023/10/31
 */
@SuppressLint("UnsafeOptInUsageError")
class VideoTransformerUtil(val context: Context) {
    companion object : SingletonHolder<VideoTransformerUtil, Context>(::VideoTransformerUtil)

    val transformer: Transformer = Transformer.Builder(context)
        .setTransformationRequest(TransformationRequest.Builder().build())
        .build()
    val progressHolder = ProgressHolder()
    val handler: Handler = Handler(Looper.getMainLooper())
    private var progressDialog: ProgressDialog? = null

    fun transformVideo(item: VideoUtils.Video, transforSize: Float, onCompleted:(outputPath:String)->Unit) {
        val outputPath = "${item.path.substring(0, item.path.lastIndexOf("/") + 1)}${
            item.name.substring(
                0,
                item.name.lastIndexOf(".")
            )
        }_${transforSize}p.mp4"
        val inputMediaItem = MediaItem.fromUri(item.path)
        val scal =
            if (item.width > item.height) transforSize / item.height else transforSize / item.width
        val editedMediaItem =
            EditedMediaItem.Builder(inputMediaItem).setEffects(Effects(
                arrayListOf(),
                arrayListOf<Effect>().apply {
                    this.add(
                        ScaleAndRotateTransformation.Builder()
                            .setScale(scal, scal).build()
                    )
                }
            )).build()
        transformer.addListener(object : Transformer.Listener {
            override fun onCompleted(
                composition: Composition,
                exportResult: ExportResult
            ) {
                onCompleted(outputPath)
                progressDialog?.dismiss()
            }
        })
        transformer.start(
            editedMediaItem,
            outputPath
        )
        showProgressDialog()
        handler.post(object : Runnable {
            override fun run() {
                val progressState: @Transformer.ProgressState Int =
                    transformer.getProgress(progressHolder) ?: 0
                updataDialog(progressHolder.progress)
                if (progressState != Transformer.PROGRESS_STATE_NOT_STARTED) {
                    handler.postDelayed(/* r= */this,  /* delayMillis= */500)
                }
            }
        })
    }

    private fun showProgressDialog() {
        // 在需要显示进度的地方创建并显示ProgressDialog
        progressDialog = ProgressDialog(context)
        progressDialog?.setMessage("正在压缩视频...")
        progressDialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog?.setProgressDrawable(context.getDrawable(R.drawable.progressbar_bg))
        progressDialog?.isIndeterminate = false // 设置为确定模式
        progressDialog?.setCancelable(false)
        progressDialog?.max = 100 // 进度条最大值
        progressDialog?.setCancelable(false) // 防止用户取消
        progressDialog?.setButton("取消"
        ) { _, _ ->
            transformer.cancel()
            progressDialog?.dismiss()
        }
        progressDialog?.show()
    }

    private fun updataDialog(progress: Int) {
        progressDialog?.progress = progress
    }
}