package com.jay.josaeworld.model.response

data class SearchBJInfo(
    val broad_title: String,
    val total_view_cnt: String,
    val user_id: String,
    val user_nick: String,
    var broad_img: String
)
