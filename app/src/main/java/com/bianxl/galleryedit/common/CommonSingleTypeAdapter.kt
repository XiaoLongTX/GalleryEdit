package com.trionesble.smart.remote.ui.adapter

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.util.ArrayList

abstract class CommonSingleTypeAdapter<E>
/**
 * 默认构造方法
 *
 * @param context       上下文
 * @param layoutManager manager 一定要和recyclerView设置的manager是一致的
 * @param layoutId      要设置的itemlayout
 */
    (protected var context: Context, layoutId: Int, private val mLayoutManager: RecyclerView.LayoutManager? = null) : RecyclerView.Adapter<CommonRecyclerViewHolder>() {

    private var customHeaderView: View? = null
    private var customFooterView: View? = null
    protected var loadingMore: Boolean = false
    private var layoutId = DEFAULT_VALUE
    private var mOnItemClickListener: OnItemClickListener<E>? = null
    private var mOnItemLongClickListener: OnItemLongClickListener<E>? = null
    var dataList: MutableList<E>
    internal var inflate: LayoutInflater = LayoutInflater.from(context)
    internal var drawable: AnimationDrawable? = null

    val headerCount: Int
        get() = if (customHeaderView != null) 1 else 0

    val footerCount: Int
        get() = if (customFooterView != null) 1 else 0

    val count: Int
        get() = dataList.size

    val lastData: E?
        get() = if (count == 0) {
            null
        } else getmDatas()[count - 1]

    init {
        this.dataList = ArrayList()
        if (mLayoutManager != null && mLayoutManager is GridLayoutManager) {
            val gridLayoutManager = mLayoutManager
            val lookup = gridLayoutManager.spanSizeLookup
            val spanCount = gridLayoutManager.spanCount
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val type = getItemViewType(position)
                    return if (type == VIEW_TYPES_HEADER || type == VIEW_TYPES_FOOTER || type == VIEW_TYPES_LOADMODE) {
                        spanCount
                    } else {
                        lookup.getSpanSize(position - headerCount)
                    }
                }
            }
        }
        this.layoutId = layoutId
    }

    /**
     * 带有下拉加载更多的adapter
     *
     * @param context      上下文
     * @param layoutId     layoutId
     * @param recyclerView 当前adapter绑定的recyclerView
     */
    constructor(context: Context, layoutId: Int, recyclerView: RecyclerView) : this(context, layoutId, recyclerView.layoutManager) {
        recyclerView.adapter = this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonRecyclerViewHolder {
        // 头和尾的holder
        return if (viewType == VIEW_TYPES_HEADER && customHeaderView != null) {
            CommonRecyclerViewHolder(customHeaderView!!)
        } else if (viewType == VIEW_TYPES_FOOTER && customFooterView != null) {
            CommonRecyclerViewHolder(customFooterView!!)
        } else {
            CommonRecyclerViewHolder(inflate.inflate(layoutId, parent, false))
        }
    }

    override fun onBindViewHolder(holder: CommonRecyclerViewHolder, position: Int) {
        val type = getItemViewType(position)
        if (type != VIEW_TYPES_HEADER && type != VIEW_TYPES_FOOTER && type != VIEW_TYPES_LOADMODE) {
            val pos = position - headerCount
            if (dataList.size > pos) {
                convert(holder, dataList[pos], position)
                if (mOnItemClickListener != null) {
                    holder.itemView.setOnClickListener { _ ->
                        val index = position - headerCount
                        val data = dataList.getOrNull(index)?.run {
                            mOnItemClickListener!!.onItemClick(holder, this, index)
                        }
                    }
                }
                if (mOnItemLongClickListener != null) {
                    holder.itemView.setOnLongClickListener { _ ->
                        val index = position - headerCount
                        val data = dataList.getOrNull(index)
                        data?.run {
                            mOnItemLongClickListener!!.onItemLongClick(holder, data, index)
                        }
                        true
                    }
                }
            }
        } else if (type == VIEW_TYPES_LOADMODE && drawable != null) {
            drawable!!.start()
        }
    }

    abstract fun convert(helper: CommonRecyclerViewHolder, item: E, pos: Int)

    internal fun setFulSpan(view: View, isloadMore: Boolean) {
        if (mLayoutManager != null && mLayoutManager is StaggeredGridLayoutManager) {
            val layoutParams = StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutParams.isFullSpan = true
            view.layoutParams = layoutParams
        }
    }

    /**
     * 返回adapter中总共的item数目，包括头部和底部
     */
    override fun getItemCount(): Int {
        var headerOrFooter = 0
        if (customHeaderView != null) {
            headerOrFooter++
        }
        if (customFooterView != null) {
            headerOrFooter++
        }
        return count + headerOrFooter
    }

    override fun getItemViewType(position: Int): Int {
        return if (customFooterView != null && position == itemCount - 1) {
            VIEW_TYPES_FOOTER
        } else if (customHeaderView != null && position == 0) {
            VIEW_TYPES_HEADER
        } else {
            super.getItemViewType(position - headerCount)
        }
    }

    fun removeHeaderView() {
        customHeaderView = null
        // notifyItemRemoved(0);如果这里需要做头部的删除动画，
        // 可以复写这个方法，然后进行改写
        notifyDataSetChanged()
    }

    fun removeFooterView() {
        customFooterView = null
        notifyItemRemoved(itemCount)
        // 这里因为删除尾部不会影响到前面的pos的改变，所以不用刷新了。
    }

    fun getmDatas(): List<E> {
        return dataList
    }

    fun setmDatas(mDatas: MutableList<E>) {
        dataList = mDatas
        notifyDataSetChanged()
    }

    fun setmDatas(mDatas: MutableList<E>, mTypeUser: List<*>) {
        dataList = mDatas
        notifyDataSetChanged()
    }

    fun addItemAtFront(item: E) {
        dataList.add(0, item)
        notifyItemInserted(1)
    }

    fun addmDatas(mDatas: List<E>) {
        dataList.addAll(mDatas)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): E {
        return getmDatas()[position]
    }

    fun remove(entity: E) {
        dataList.remove(entity)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener<E>?):CommonSingleTypeAdapter<E> {
        this.mOnItemClickListener = mOnItemClickListener
        return this
    }

    fun setOnItemLongClickListener(mOnItemLongClickListener: OnItemLongClickListener<E>?) {
        this.mOnItemLongClickListener = mOnItemLongClickListener
    }

    interface OnItemClickListener<E> {
        fun onItemClick(holder: CommonRecyclerViewHolder, item: E, position: Int)
    }

    interface OnItemLongClickListener<E> {
        fun onItemLongClick(holder: CommonRecyclerViewHolder, item: E, position: Int)
    }

    interface LoadMoreListener {
        fun onLodingMore()
    }

    fun clearnAdapter() {
        this.dataList = ArrayList()
        notifyDataSetChanged()
    }

    companion object {

        val VIEW_TYPES_HEADER = 99930
        val VIEW_TYPES_FOOTER = 99931
        val VIEW_TYPES_LOADMODE = 99932
        private val DEFAULT_VALUE = -1
    }
}
