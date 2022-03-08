package com.jay.josaeworld.base

interface BaseView {
    fun showError(code: Int)
    fun showToast(msg: String, isCenter: Boolean = false)
}
