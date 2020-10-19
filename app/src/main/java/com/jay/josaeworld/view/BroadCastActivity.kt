package com.jay.josaeworld.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.jay.josaeworld.adapter.OnItemClick
import com.jay.josaeworld.adapter.RecyclerBroadListAdapter
import com.jay.josaeworld.databinding.ActivityBroadCastBinding
import com.jay.josaeworld.model.BroadInfo
import com.jay.josaeworld.model.DataCallback
import com.jay.josaeworld.model.GetData

class BroadCastActivity : AppCompatActivity(), OnItemClick, DataCallback {
    private lateinit var binding: ActivityBroadCastBinding
    private lateinit var mAdapter: RecyclerBroadListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBroadCastBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val list = intent.getSerializableExtra("teamINfo") as ArrayList<BroadInfo>

        binding.broadRecyclerView.layoutManager = LinearLayoutManager(baseContext)
        mAdapter = RecyclerBroadListAdapter(baseContext, list.sortedByDescending { it.onOff }, this)
        binding.broadRecyclerView.adapter = mAdapter
        binding.bProgress.visibility = View.GONE
//        getData.broadList(rid, object : DataCallback{
//            override fun finishBroadDataLoading(list: List<BroadInfo>) {
//                binding.broadRecyclerView.layoutManager = LinearLayoutManager(baseContext)
//                mAdapter = RecyclerBroadListAdapter(baseContext, list, this@BroadCastActivity)
//                binding.broadRecyclerView.adapter = mAdapter
//                binding.bProgress.visibility= View.GONE
//            }
//        })
        binding.teamName.text = intent.getStringExtra("teamName")
    }

    override fun clickEvent(bid: String) {
        var intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("afreeca://player/live?user_id=$bid")
        try {
            startActivity(intent)
        } catch (e: Exception) {
            intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://m.afreecatv.com/#/player/$bid")
            )
            startActivity(intent)
        }
    }

}