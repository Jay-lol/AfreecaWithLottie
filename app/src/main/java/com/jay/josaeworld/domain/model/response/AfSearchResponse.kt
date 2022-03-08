package com.jay.josaeworld.domain.model.response

import com.google.gson.annotations.SerializedName
import com.jay.josaeworld.domain.model.response.gsonParse.Broad
import com.jay.josaeworld.domain.model.response.gsonParse.Station

data class AfSearchResponse(
    @SerializedName("profile_image")
    val profile: String,
    val station: Station?,
    val broad: Broad?
)
