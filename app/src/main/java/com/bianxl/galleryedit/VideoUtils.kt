package com.bianxl.galleryedit

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size

/**
 *
 * author maxbian
 *
 * date 2023/10/31
 */
object VideoUtils {
    private const val TAG = "VideoUtils"
    val REQUEST_CODE_DELETE_VIDEO = 1001
    val LEVEL_MIN = 1
    val LEVEL_MID = 2
    val LEVEL_MAX = 3

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

    fun queryVideo(context: Context, level: Int): MutableList<Video> {
        val videoList = mutableListOf<Video>()
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
        val selection =
            "${MediaStore.Video.Media.BITRATE} >= ? and ${MediaStore.Video.Media.BITRATE} < ?"
        val selectionArgs = arrayOf(
            when (level) {
                LEVEL_MID -> "${8 * 1024 * 1024 * 1}"
                LEVEL_MAX -> "${8 * 1024 * 1024 * 3}"
                else -> "0"
            },
            when (level) {
                LEVEL_MID -> "${8 * 1024 * 1024 * 3}"
                LEVEL_MAX -> "${Long.MAX_VALUE}"
                else -> "${8 * 1024 * 1024 * 1}"
            }
        )

        // 按照时间戳降序排列
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        val query = context.contentResolver.query(
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
        }
        return videoList
    }

    fun deleteVideo(
        activity: Activity,
        video: Video,
        callback: (path: String) -> Unit,
        securityCallback: (exception: SecurityException) -> Unit = {}
    ) {
        Log.d(TAG, "deleteVideo:${video.path}")
        val mContentResolver = activity.contentResolver
        try {
            // 对于未启用分区存储的情况，若权限申请到位，则以下代码可以执行成功直接删除
            val count = mContentResolver.delete(video.uri, null, null)
            val result = count == 1
            Log.d(TAG, "DeleteImage result:$result")
            callback.invoke(video.path)
        } catch (exception: SecurityException) {
            securityCallback.invoke(exception)
        }
    }

    fun getVideoBitmap(context: Context, contentUri: Uri, id: Long): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(contentUri, Size(512, 384), null)
        } else {
            MediaStore.Video.Thumbnails.getThumbnail(
                context.contentResolver,
                id,
                MediaStore.Video.Thumbnails.MINI_KIND,
                BitmapFactory.Options().apply {
                    this.inPreferredConfig = Bitmap.Config.RGB_565
                })
        }
    }

    fun scanFileAndReload(
        context: Context,
        outputPath: String?,
        onScanCompleted: () -> Unit
    ) {
        MediaScannerConnection.scanFile(
            context, arrayOf(outputPath), null
        ) { _, _ ->
            onScanCompleted.invoke()
        }
    }
}