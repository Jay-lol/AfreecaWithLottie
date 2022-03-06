package com.jay.josaeworld.presenter

import android.annotation.SuppressLint
import com.jay.josaeworld.contract.BroadContract
import com.jay.josaeworld.domain.SearchKeywordUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class BroadPresenter @Inject constructor(
    private val keywordUseCase: SearchKeywordUseCase,
    private var searchView: BroadContract.View?
) : BroadContract.Presenter {

    @SuppressLint("CheckResult")
    override fun searchJosae() {
        keywordUseCase()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it?.let {
                    searchView?.showSearchResult(it)
                    return@subscribe
                }
                searchView?.showToast("검색 실패! $it")
            }, {
                searchView?.showToast("검색 실패! $it")
            })
    }

    override fun dropView() {
        searchView = null
    }
}
