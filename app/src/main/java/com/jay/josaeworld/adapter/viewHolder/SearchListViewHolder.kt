package com.jay.josaeworld.adapter.viewHolder

import android.os.Build
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.jay.josaeworld.model.response.SearchBJInfo
import kotlinx.android.synthetic.main.recycler_broad2.view.*
import java.util.*

class SearchListViewHolder(itemView: View, memberClick: (SearchBJInfo) -> Unit) :
    RecyclerView.ViewHolder(itemView) {

    private val random = Random()

    private val title: TextView = itemView.search_title
    private val thumbnail: ImageView = itemView.search_thumbnail
    private val bjname: TextView = itemView.search_bjname
    private val viewCnt: TextView = itemView.viewCnt

    private val clickMember: (SearchBJInfo) -> Unit = memberClick

    fun bind(searchBJInfo: SearchBJInfo, glide: RequestManager) {

        glide.load(searchBJInfo.broad_img + "?dummy=${random.nextInt(123456789)}")
            .override(480, 270)
            .into(thumbnail)

        // HTML 이스케이프 문제로 언이스케이프 처리리.
        // 안드로이드 API에서 Html 클래스가 있으니 fromHtml로 부르기

        val broadtitle: String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                Html.fromHtml(searchBJInfo.broad_title, Html.FROM_HTML_MODE_LEGACY).toString()
            else
                Html.fromHtml(searchBJInfo.broad_title).toString()

        title.text = broadtitle
        bjname.text = searchBJInfo.user_nick
        viewCnt.text = searchBJInfo.total_view_cnt

        itemView.setOnClickListener {
            clickMember(searchBJInfo)
        }
    }
}
