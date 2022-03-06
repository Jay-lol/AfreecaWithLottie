package com.jay.josaeworld.presenter

import com.jay.josaeworld.contract.BroadContract
import javax.inject.Inject

class BroadPresenter @Inject constructor(
    private var searchView: BroadContract.View?,
) : BroadContract.Presenter {
    override fun dropView() {
        searchView = null
    }
}
