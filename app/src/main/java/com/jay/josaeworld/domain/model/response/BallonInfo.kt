package com.jay.josaeworld.domain.model.response

import java.io.Serializable

data class BallonInfo(
    val dayballon: String = "0",
    val monthballon: String = "0",
    val monthmaxview: String = "0",
    val monthtime: String = "0분",
    val monthview: String = "0",
    val monthpay: String = "0"
) : Serializable
