package com.jay.josaeworld.presenter

import com.jay.josaeworld.contract.BroadContract
import com.jay.josaeworld.domain.SearchKeywordUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class BroadPresenter @Inject constructor(
    private var searchView: BroadContract.View?,
    private val keywordUseCase: SearchKeywordUseCase
) : BroadContract.Presenter {

    private var disposable: Disposable? = null

    override fun searchJosae() {
        disposable = keywordUseCase()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                searchView?.showSearchResult(it)
            }, {
                searchView?.showToast("검색 실패! $it")
            })
    }

    override fun dropView() {
        searchView = null
        disposable?.dispose()
    }
}
