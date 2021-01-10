package com.jay.josaeworld.model.response

import java.io.Serializable

data class BallonInfo(
    val dayballon: String,
    val monthballon: String,
    val monthmaxview: String,
    val monthpay: String,
    val monthtime: String,
    val monthview: String
) : Serializable