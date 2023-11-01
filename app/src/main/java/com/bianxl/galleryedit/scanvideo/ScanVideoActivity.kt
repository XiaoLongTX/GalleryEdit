package com.bianxl.galleryedit.scanvideo

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bianxl.galleryedit.R
import com.bianxl.galleryedit.VideoUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.vmadalin.easypermissions.EasyPermissions


class ScanVideoActivity : FragmentActivity(), EasyPermissions.PermissionCallbacks {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_video)
        Log.i(TAG, "onCreate: ")
        if (EasyPermissions.hasPermissions(this@ScanVideoActivity, *permissions)) {
            initListView()
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
        Toast.makeText(this@ScanVideoActivity, "未授权，无法操作", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        initListView()
    }

    private fun initListView() {
        val pager = findViewById<ViewPager2>(R.id.video_pager)
        pager.adapter = object : FragmentStateAdapter(this@ScanVideoActivity) {
            override fun getItemCount(): Int {
                return 3
            }

            override fun createFragment(position: Int): Fragment {
                return ScanVideoFragment().apply {
                    arguments = Bundle().apply {
                        this.putInt(
                            "video_level", when (position) {
                                0 -> VideoUtils.LEVEL_MIN
                                1 -> VideoUtils.LEVEL_MID
                                else -> VideoUtils.LEVEL_MAX
                            }
                        )
                    }
                }
            }
        }
        val tab = findViewById<TabLayout>(R.id.video_tab)
        TabLayoutMediator(
            tab, pager
        ) { tab, position ->
            tab.text = when (position) {
                0 -> "低画质"
                1 -> "中画质"
                else -> "高画质"
            }
        }.attach()
    }
}


