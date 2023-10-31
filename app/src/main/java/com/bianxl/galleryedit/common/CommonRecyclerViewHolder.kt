package com.trionesble.smart.remote.ui.adapter

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CommonRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun setText(viewId: Int, text: String?) {
        itemView.findViewById<TextView>(viewId).text = text
    }

    fun setText(viewId: Int, textSrc: Int) {
        itemView.findViewById<TextView>(viewId).setText(textSrc)
    }

    fun setText(viewId: Int, text: CharSequence?) {
        itemView.findViewById<TextView>(viewId).text = text
    }

    fun setOnclickListener(viewId: Int, listener: View.OnClickListener) {
        itemView.findViewById<View>(viewId).setOnClickListener(listener)
    }


    fun setViewVisibility(viewId: Int, visibility: Int) {
        itemView.findViewById<View>(viewId).visibility = visibility
    }

    fun <T : View> getViewById(viewId: Int): T {
        return itemView.findViewById(viewId)
    }

    fun setImageBitmap(viewId: Int, bitMap: Bitmap) {
        val imageView = itemView.findViewById<ImageView>(viewId)
        Glide.with(imageView).load(bitMap).into(imageView)
    }

    fun setImageRes(viewId: Int, resid: Int) {
        val imageView = itemView.findViewById<ImageView>(viewId)
        Glide.with(imageView).load(resid).into(imageView)
    }

    fun getItemView():View{
        return itemView
    }

}
