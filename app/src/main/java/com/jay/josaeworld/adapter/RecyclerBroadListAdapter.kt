package com.jay.josaeworld.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jay.josaeworld.R
import com.jay.josaeworld.model.BroadInfo
import com.jay.josaeworld.model.Static.Companion.allMemberName
import kotlinx.android.synthetic.main.recycler_broad.view.*

class RecyclerBroadListAdapter(val context: Context, cList: List<BroadInfo>?, listner: OnItemClick) :
    RecyclerView.Adapter<RecyclerBroadListAdapter.bView>() {
    private val TAG: String = "로그"
    private var blist = cList
    private val callback = listner

    class bView(view: View) : RecyclerView.ViewHolder(view) {
        var rid: String? = null
        var title = view.title
        var thumbnail = view.thumbnail
        var name = view.bjname
        var error = view.error_thumbnail
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): bView {
        return bView(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_broad, parent, false)
        )
    }

    override fun onBindViewHolder(holder: bView, position: Int) {

        if (blist?.get(position)?.onOff == 1) {
            holder.error.visibility = View.INVISIBLE
            Glide.with(context).load(blist?.get(position)?.imgurl)
//                .override(660, 371)
                .centerCrop()
                .into(holder.thumbnail)
        } else {
            holder.thumbnail.visibility = View.INVISIBLE
            holder.error.visibility = View.VISIBLE
        }

        holder.name.text = blist?.get(position)?.bjname
        holder.title.text = blist?.get(position)?.title
        holder.title.isSelected = true
        holder.name.isSelected = true
        buttonListener(holder, position)
    }

    private fun buttonListener(holder: bView, position: Int) {
        holder.itemView.setOnClickListener {
            blist?.let {
                if (blist?.get(position)?.onOff == 1)
                    callback.clickEvent(blist!![position].bid)
            }
        }
    }


    override fun getItemCount(): Int {
        return blist?.size ?: 0
    }
}