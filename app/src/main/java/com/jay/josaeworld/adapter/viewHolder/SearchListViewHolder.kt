package com.jay.josaeworld.adapter.viewHolder

import android.os.Build
import android.text.Html
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.jay.josaeworld.databinding.RecyclerBroad2Binding
import com.jay.josaeworld.model.response.SearchBJInfo
import java.util.*

class SearchListViewHolder(
    private val binding: RecyclerBroad2Binding,
    private val memberClick: (SearchBJInfo) -> Unit,
    private val random: Random
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(searchBJInfo: SearchBJInfo, glide: RequestManager) {

        glide.load(searchBJInfo.broad_img + "?dummy=${random.nextInt(123456789)}")
            .override(480, 270)
            .into(binding.searchThumbnail)

        // HTML 이스케이프 문제로 언이스케이프 처리리.
        // 안드로이드 API에서 Html 클래스가 있으니 fromHtml로 부르기

        val broadtitle: String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                Html.fromHtml(searchBJInfo.broad_title, Html.FROM_HTML_MODE_LEGACY).toString()
            else
                Html.fromHtml(searchBJInfo.broad_title).toString()

        binding.searchTitle.text = broadtitle
        binding.searchBjname.text = searchBJInfo.user_nick
        binding.viewCnt.text = searchBJInfo.total_view_cnt

        itemView.setOnClickListener {
            memberClick(searchBJInfo)
        }
    }
}
