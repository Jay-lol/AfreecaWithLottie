package com.jay.josaeworld.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.jay.josaeworld.adapter.viewHolder.SearchListViewHolder
import com.jay.josaeworld.databinding.ViewKeywordSearchBinding
import com.jay.josaeworld.domain.model.response.SearchBJInfo
import java.util.*

class RecyclerSearchListAdapter(
    private val glide: RequestManager,
    private val bList: List<SearchBJInfo>?,
    private val searchMemberClick: (SearchBJInfo) -> Unit,
    private val random: Random
) : RecyclerView.Adapter<SearchListViewHolder>() {

    // 목록의 아이템수
    override fun getItemCount(): Int {
        return bList?.size ?: 0
    }

    // 객체생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListViewHolder {
        return SearchListViewHolder(
            ViewKeywordSearchBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            searchMemberClick,
            random
        )
    }

    // 객체 바인딩
    override fun onBindViewHolder(holder: SearchListViewHolder, position: Int) {
        bList?.let {
            holder.bind(it[position], glide)
        }
    }
}
