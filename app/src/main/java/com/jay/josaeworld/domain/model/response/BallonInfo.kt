package com.jay.josaeworld.domain.model.response

import java.io.Serializable

data class BallonInfo(
    val dayballon: String,
    val monthballon: String,
    val monthmaxview: String,
    val monthtime: String,
    val monthview: String,
    val monthpay: String
) : Serializable
