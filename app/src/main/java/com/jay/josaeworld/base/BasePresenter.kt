package com.jay.josaeworld.base

interface BasePresenter<T> {
    fun takeView(view : T)
    fun dropView()
}