package com.jay.josaeworld.adapter.viewHolder

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.RequestManager
import com.jay.josaeworld.R
import com.jay.josaeworld.model.response.BroadInfo
import kotlinx.android.synthetic.main.recycler_broad.view.*
import java.util.*

class BJViewHolder(itemView: View, memberClick: (BroadInfo, Int) -> Unit, cntext: Context) : RecyclerView.ViewHolder(itemView) {
    private val random = Random()

    private val context = cntext
    private val title: TextView = itemView.title
    private val thumbnail: ImageView = itemView.thumbnail
    private val name: TextView = itemView.bjname
    private val error: LottieAnimationView = itemView.error_thumbnail
    private val viewCnt: TextView = itemView.view_cnt
    private val fanCnt: TextView = itemView.fan_cnt
    private val okCnt: TextView = itemView.ok_cnt
    private val incFanCnt: TextView = itemView.incFanCnt
    private val mballon: TextView = itemView.mballon
    private val dballon: TextView = itemView.dballon
    private val secondSj: LottieAnimationView = itemView.secondsujang
    private val infobutton: LottieAnimationView = itemView.moreInfo

    private val clickMember: (BroadInfo, Int) -> Unit = memberClick

    fun bind(broadInfo: BroadInfo?, glide: RequestManager, secondSujang: String) {
        if (broadInfo?.onOff == 1) {
            val x = thumbnail.layoutParams as ConstraintLayout.LayoutParams
            x.topMargin = 0
            x.bottomMargin = 0

            error.visibility = View.INVISIBLE
            thumbnail.visibility = View.VISIBLE
            glide.load(broadInfo.imgurl + "${random.nextInt(123456789)}") // 실시간정보를 위해 난수 입력
                .override(480, 270)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .into(thumbnail)
            viewCnt.text = broadInfo.viewCnt
        } else {
            error.visibility = View.INVISIBLE

            val sizeInDP = 10
            val marginInDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDP.toFloat(), context.resources.displayMetrics
            ).toInt()

            val x = thumbnail.layoutParams as ConstraintLayout.LayoutParams
            x.topMargin = marginInDp
            x.bottomMargin = marginInDp

            glide.load(broadInfo?.profilePhoto)
                .override(480, 270)
                .placeholder(R.drawable.placeholder)
                .circleCrop()
                .into(thumbnail)
            viewCnt.text = ""
        }
//        else {
//            thumbnail.visibility = View.INVISIBLE
//            viewCnt.text = ""
//            error.visibility = View.VISIBLE
//        }

        if (broadInfo?.bid == secondSujang) {
            secondSj.visibility = View.VISIBLE
            secondSj.playAnimation()
        } else {
            secondSj.visibility = View.INVISIBLE
            secondSj.pauseAnimation()
        }

        broadInfo?.let {
            if (it.incFanCnt.filter { c -> (c.isDigit() || c == '-') }.toInt() < 0) {
                incFanCnt.setTextColor(Color.parseColor("#FF4A4A"))
            } else {
                incFanCnt.setTextColor(Color.parseColor("#FFFFFF"))
            }
            incFanCnt.text = it.incFanCnt
            fanCnt.text = it.fanCnt
            okCnt.text = it.okCnt
            name.text = it.bjname
            title.text = it.title
            mballon.text = it.balloninfo?.monthballon
            dballon.text = it.balloninfo?.dayballon
        }
        title.isSelected = true
        name.isSelected = true

        itemView.setOnClickListener {
            broadInfo?.let {
                if (broadInfo.onOff == 1) clickMember(broadInfo, 0)
            }
        }
        infobutton.setOnClickListener {
            broadInfo?.let {
                clickMember(broadInfo, 1)
            }
        }
    }
}
