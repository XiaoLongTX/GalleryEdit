package com.bianxl.galleryedit

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bianxl.galleryedit.common.ItemSpacingDecoration
import com.trionesble.smart.remote.ui.adapter.CommonRecyclerViewHolder
import com.trionesble.smart.remote.ui.adapter.CommonSingleTypeAdapter
import com.trionesble.smart.remote.ui.adapter.CommonSingleTypeAdapter.OnItemClickListener
import com.vmadalin.easypermissions.EasyPermissions


@SuppressLint("UnsafeOptInUsageError")
class ScanVideoActivity : Activity(), EasyPermissions.PermissionCallbacks {
    private val TAG = "MainActivity"

    private val permissions = if (Build.VERSION.SDK_INT > 18) {
        arrayOf(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
        )
    } else {
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
        )
    }

    var transformer: Transformer? = null
    val progressHolder = ProgressHolder()
    val handler: Handler = Handler(Looper.getMainLooper())
    var progressDialog: ProgressDialog? = null
    val REQUEST_CODE_DELETE_VIDEO = 1001
    var video2del: Video? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_video)
        Log.i(TAG, "onCreate: ")
        if (EasyPermissions.hasPermissions(this@ScanVideoActivity, *permissions)) {
            queryVideo()
        } else {
            EasyPermissions.requestPermissions(
                this@ScanVideoActivity,
                "获取媒体文件权限", 10010, *permissions
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Log.i(TAG, "onPermissionsDenied: $perms")
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        queryVideo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DELETE_VIDEO) {
            if (resultCode == RESULT_OK) {
                // 用户授予修改图片权限，再次执行删除操作
                deleteVideo(this@ScanVideoActivity, video2del ?: return)
            } else {
                /* Edit request not granted; explain to the user. */
            }
        }
    }

    private fun queryVideo() {
        // Need the READ_EXTERNAL_STORAGE permission if accessing video files that your app didn't create.
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

        // 过滤码率大于3的视频
        val selection = "${MediaStore.Video.Media.BITRATE} >= ?"
        val selectionArgs = arrayOf(
            "${8 * 1024 * 1024 * 3}"
        )

        // 按照时间戳降序排列
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        val query = this@ScanVideoActivity.contentResolver.query(
            collection,
            null,
            selection,
            selectionArgs,
            sortOrder
        )
        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val bitrateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BITRATE)
            val framerateColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.CAPTURE_FRAMERATE)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val orientationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ORIENTATION)
            val videoList = mutableListOf<Video>()
            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)
                val bitrate = cursor.getInt(bitrateColumn)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val framerate = cursor.getInt(framerateColumn)
                val height = cursor.getInt(heightColumn)
                val width = cursor.getInt(widthColumn)
                val path = cursor.getString(pathColumn)
                val orientation = cursor.getInt(orientationColumn)
                // Stores column values and the contentUri in a local object
                // that represents the media file.
                videoList += Video(
                    id,
                    contentUri,
                    name,
                    duration / 1000.0,
                    size / 1024.0 / 1024,
                    bitrate / 8.0 / 1024 / 1024,
                    framerate,
                    height,
                    width,
                    path,
                    orientation
                )
            }
            cursor.close()
            val videoRecyclerView = findViewById<RecyclerView>(R.id.video_list)
            videoRecyclerView.addItemDecoration(ItemSpacingDecoration(10))
            videoRecyclerView.layoutManager =
                GridLayoutManager(this@ScanVideoActivity, 3, LinearLayoutManager.VERTICAL, false)
            object : CommonSingleTypeAdapter<Video>(
                this@ScanVideoActivity,
                R.layout.item_video_list,
                videoRecyclerView
            ) {
                override fun convert(helper: CommonRecyclerViewHolder, item: Video, pos: Int) {
                    helper.setImageBitmap(R.id.video_image, getVideoBitmap(item.uri, item.id))
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
            }.setOnItemClickListener(object : OnItemClickListener<Video> {
                override fun onItemClick(
                    holder: CommonRecyclerViewHolder,
                    item: Video,
                    position: Int
                ) {
                    val outputPath = item.path.substring(
                        0,
                        item.path.lastIndexOf("/") + 1
                    ) + item.name.substring(
                        0, item.name.lastIndexOf(".")
                    ) + "(1).mp4"
                    val inputMediaItem = MediaItem.fromUri(item.path)
                    val scal =
                        if (item.width > item.height) 1280f / item.width else 1280f / item.height
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
                    transformer = Transformer.Builder(this@ScanVideoActivity)
                        .setTransformationRequest(TransformationRequest.Builder().build())
                        .addListener(object : Transformer.Listener {
                            override fun onCompleted(
                                composition: Composition,
                                exportResult: ExportResult
                            ) {
                                super.onCompleted(composition, exportResult)
                                progressDialog?.dismiss()
                                showDeleteDialog(item)
                                scanFileAndReload(outputPath)
                            }

                            override fun onError(
                                composition: Composition,
                                exportResult: ExportResult,
                                exportException: ExportException
                            ) {
                                super.onError(composition, exportResult, exportException)
                                Log.d(
                                    TAG,
                                    "onError() called with: composition = $composition, exportResult = $exportResult, exportException = $exportException"
                                )
                            }
                        })
                        .build()
                    transformer?.start(
                        editedMediaItem,
                        outputPath
                    )
                    showProgressDialog()
                    handler.post(object : Runnable {
                        override fun run() {
                            val progressState: @Transformer.ProgressState Int =
                                transformer?.getProgress(progressHolder) ?: 0
                            updataDialog(progressHolder.progress)
                            if (progressState != Transformer.PROGRESS_STATE_NOT_STARTED) {
                                handler.postDelayed(/* r= */this,  /* delayMillis= */500)
                            }
                        }
                    })
                }

            }).setmDatas(videoList)
        }
    }

    private fun getVideoBitmap(contentUri: Uri, id: Long): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver.loadThumbnail(contentUri, Size(512, 384), null)
        } else {
            MediaStore.Video.Thumbnails.getThumbnail(
                contentResolver,
                id,
                MediaStore.Video.Thumbnails.MINI_KIND,
                BitmapFactory.Options().apply {
                    this.inPreferredConfig = Bitmap.Config.RGB_565
                })
        }
    }

    private fun showProgressDialog() {
        // 在需要显示进度的地方创建并显示ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("正在压缩视频...")
        progressDialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog?.setProgressDrawable(resources.getDrawable(R.drawable.progressbar_bg))
        progressDialog?.isIndeterminate = false // 设置为确定模式
        progressDialog?.setCancelable(false)
        progressDialog?.max = 100 // 进度条最大值
        progressDialog?.setCancelable(false) // 防止用户取消
        progressDialog?.setButton("取消", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                transformer?.cancel()
                progressDialog?.dismiss()
            }
        })
        progressDialog?.show()
    }

    private fun updataDialog(progress: Int) {
        progressDialog?.progress = progress
    }

    private fun scanFileAndReload(outputPath: String?) {
        MediaScannerConnection.scanFile(
            this@ScanVideoActivity, arrayOf(outputPath), null
        ) { path, uri ->
            runOnUiThread { queryVideo() }
        }
    }

    private fun showDeleteDialog(video: Video) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("确定要删除吗？")
            .setPositiveButton("删除") { dialog, id -> // 用户点击了删除按钮，执行删除操作
                deleteVideo(this@ScanVideoActivity, video)
            }
            .setNegativeButton("取消") { dialog, id -> // 用户点击了取消按钮，关闭对话框
                dialog.dismiss()
            }

        // 创建并显示对话框
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteVideo(activity: Activity, video: Video) {
        Log.d(TAG, "deleteVideo:${video.path}")
        val mContentResolver = activity.contentResolver
        try {
            // 对于未启用分区存储的情况，若权限申请到位，则以下代码可以执行成功直接删除
            val count = mContentResolver.delete(video.uri, null, null)
            val result = count == 1
            Log.d(TAG, "DeleteImage result:$result")
            scanFileAndReload(video.path)
        } catch (exception: SecurityException) {
            // 若启用了分区存储，上面代码delete将会报错，显示没有权限。
            // 需要捕获这个异常，并用下面代码，使用startIntentSenderForResult弹出弹窗向用户请求修改当前图片的权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                video2del = video
                val recoverableSecurityException: RecoverableSecurityException =
                    if (exception is RecoverableSecurityException) {
                        exception
                    } else {
                        throw RuntimeException(exception.message, exception)
                    }
                val intentSender = recoverableSecurityException.userAction.actionIntent.intentSender
                try {
                    activity.startIntentSenderForResult(
                        intentSender,
                        REQUEST_CODE_DELETE_VIDEO,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            } else {
                throw RuntimeException(exception.message, exception)
            }
        }
    }

    data class Video(
        val id: Long,
        val uri: Uri,
        val name: String,
        val duration: Double,
        val size: Double,
        val bitrate: Double,
        val framerate: Int,
        val height: Int,
        val width: Int,
        val path: String,
        val orientation: Int
    )
}