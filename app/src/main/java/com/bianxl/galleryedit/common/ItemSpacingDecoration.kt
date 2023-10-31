package com.bianxl.galleryedit.common

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView


/**
 *
 * author maxbian
 *
 * date 2023/10/30
 */
class ItemSpacingDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    private var mDivider: Drawable? = null

    fun setDivider(divider: Drawable): ItemSpacingDecoration {
        this.mDivider = divider
        return this
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (mDivider == null) {
            return
        }
        drawHorizontal(c, parent)
        drawVertical(c, parent)
    }

    private fun drawHorizontal(c: Canvas?, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val left = child.left - params.leftMargin
            val right = (child.right + params.rightMargin + spacing)
            val top = child.bottom + params.bottomMargin
            val bottom = top + spacing
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(c!!)
        }
    }

    private fun drawVertical(c: Canvas?, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.top - params.topMargin
            val bottom = child.bottom + params.bottomMargin
            val left = child.right + params.rightMargin
            val right = left + spacing
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(c!!)
        }
    }

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