package com.jay.josaeworld.adapter.viewHolder

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.jay.josaeworld.R
import com.jay.josaeworld.databinding.ViewBjDataBinding
import com.jay.josaeworld.domain.model.response.BroadInfo
import com.jay.josaeworld.extension.pxToDp
import java.util.*

class BJViewHolder(
    private val binding: ViewBjDataBinding,
    private val clickMember: (BroadInfo, Int) -> Unit,
    private val random: Random,
    private val context: Context
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(broadInfo: BroadInfo?, glide: RequestManager, secondSujang: String) {
        if (broadInfo?.onOff == 1) {
            (binding.thumbnail.layoutParams as ConstraintLayout.LayoutParams).apply {
                topMargin = 0
                bottomMargin = 0
            }

            binding.thumbnail.run {
                visibility = View.VISIBLE
                glide.load(broadInfo.imgurl + "${random.nextInt(123456789)}") // 실시간정보를 위해 난수 입력
                    .override(480, 270)
                    .placeholder(R.drawable.placeholder)
                    .centerCrop()
                    .into(this)
            }

            binding.viewCnt.text = broadInfo.viewCnt
        } else {

            val marginInDp = 10f.pxToDp(context)

            (binding.thumbnail.layoutParams as ConstraintLayout.LayoutParams).run {
                topMargin = marginInDp
                bottomMargin = marginInDp
            }

            glide.load(broadInfo?.profilePhoto)
                .override(480, 270)
                .placeholder(R.drawable.placeholder)
                .circleCrop()
                .into(binding.thumbnail)

            binding.viewCnt.text = ""
        }

        binding.secondsujang.run {
            if (broadInfo?.bid == secondSujang) {
                visibility = View.VISIBLE
                playAnimation()
            } else {
                visibility = View.INVISIBLE
                pauseAnimation()
            }
        }

        broadInfo?.run {
            if (incFanCnt.filter { c -> (c.isDigit() || c == '-') }.toInt() < 0) {
                binding.incFanCnt.setTextColor(Color.parseColor("#FF4A4A"))
            } else {
                binding.incFanCnt.setTextColor(Color.parseColor("#FFFFFF"))
            }
            binding.incFanCnt.text = incFanCnt
            binding.fanCnt.text = fanCnt
            binding.okCnt.text = okCnt
            binding.bjname.text = bjname
            binding.title.text = title
            binding.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            binding.mballon.text = balloninfo?.monthballon
            binding.dballon.text = balloninfo?.dayballon
        }

        binding.title.isSelected = true
        binding.bjname.isSelected = true

        itemView.setOnClickListener {
            broadInfo?.let {
                if (broadInfo.onOff == 1) clickMember(broadInfo, 0)
            }
        }

        binding.moreInfo.setOnClickListener {
            broadInfo?.let {
                clickMember(broadInfo, 1)
            }
        }
    }
}
