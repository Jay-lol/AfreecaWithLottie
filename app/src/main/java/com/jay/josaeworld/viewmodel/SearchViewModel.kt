package com.jay.josaeworld.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jay.josaeworld.domain.SearchKeywordUseCase
import com.jay.josaeworld.domain.model.response.SearchBJInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchKeywordUseCase: SearchKeywordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<SearchSideEffect>()
    val sideEffect: SharedFlow<SearchSideEffect> = _sideEffect.asSharedFlow()

    init {
        fetchSearchResult()
    }

    fun fetchSearchResult() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, items = null) }
            runCatching {
                searchKeywordUseCase()
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        items = result.REAL_BROAD ?: emptyList(),
                        isLoading = false
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false) }
                _sideEffect.emit(SearchSideEffect.ShowToast("검색 실패! $e"))
            }
        }
    }
}

data class SearchUiState(
    val items: List<SearchBJInfo>? = null,
    val isLoading: Boolean = false
)

sealed class SearchSideEffect {
    data class ShowToast(val message: String) : SearchSideEffect()
}
