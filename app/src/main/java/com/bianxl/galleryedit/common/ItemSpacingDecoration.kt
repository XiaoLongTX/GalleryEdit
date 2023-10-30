package com.bianxl.galleryedit.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


/**
 *
 * author maxbian
 *
 * date 2023/10/30
 */
class ItemSpacingDecoration(val spacing: Int): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.right = spacing
        outRect.bottom = spacing

        // 第一列不添加左边距，以保持左对齐
        if (parent.getChildAdapterPosition(view) % 3 == 0) {
            outRect.left = spacing
        }

        if (parent.getChildAdapterPosition(view) < 3) {
            outRect.top = spacing
        }
    }
}