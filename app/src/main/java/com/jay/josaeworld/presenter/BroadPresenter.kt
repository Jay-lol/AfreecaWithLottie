package com.jay.josaeworld.presenter

import com.jay.josaeworld.contract.BroadContract
import com.jay.josaeworld.domain.SearchKeywordUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class BroadPresenter @Inject constructor(
    private var searchView: BroadContract.View?,
    private val keywordUseCase: SearchKeywordUseCase
) : BroadContract.Presenter {

    private var job: Job? = null

    override fun searchJosae() {
        job = CoroutineScope(Dispatchers.Main).launch {
            runCatching {
                keywordUseCase()
            }.onSuccess {
                searchView?.showSearchResult(it)
            }.onFailure {
                searchView?.showToast("검색 실패! $it")
            }
        }
    }

    override fun dropView() {
        searchView = null
        job?.cancel()
    }
}
