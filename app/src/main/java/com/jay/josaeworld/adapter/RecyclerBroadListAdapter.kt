package com.jay.josaeworld.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.jay.josaeworld.R
import com.jay.josaeworld.adapter.viewHolder.BJViewHolder
import com.jay.josaeworld.model.response.BroadInfo

class RecyclerBroadListAdapter(
    private val glide: RequestManager,
    private val bList: List<BroadInfo>?,
    private val secondSujang: String,
    private val memberClick: (BroadInfo, Int) -> Unit
) : RecyclerView.Adapter<BJViewHolder>() {

    // 목록의 아이템수
    override fun getItemCount(): Int {
        return bList?.size ?: 0
    }

    // 객체생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BJViewHolder {
        return BJViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_broad, parent, false), memberClick, parent.context
        )
    }

    // 객체 바인딩
    override fun onBindViewHolder(holder: BJViewHolder, position: Int) {
        this.bList?.let {
            holder.bind(it[position], glide, secondSujang)
        }
    }
}