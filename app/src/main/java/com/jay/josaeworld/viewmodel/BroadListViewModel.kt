package com.jay.josaeworld.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jay.josaeworld.domain.model.response.BroadInfo
import com.jay.josaeworld.view.BroadListActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BroadListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BroadListUiState())
    val uiState: StateFlow<BroadListUiState> = _uiState.asStateFlow()

    init {
        val teamName: String = savedStateHandle[BroadListActivity.KEY_TEAM_NAME] ?: ""
        val teamList: ArrayList<BroadInfo>? = savedStateHandle[BroadListActivity.KEY_TEAM_DATA_LIST]
        val underBoss: String = savedStateHandle[BroadListActivity.KEY_UNDER_BOSS_NAME] ?: "1"

        _uiState.update { state ->
            state.copy(
                teamName = teamName,
                underBoss = underBoss,
                items = sortTeamList(teamList ?: emptyList(), underBoss)
            )
        }
    }

    private fun sortTeamList(list: List<BroadInfo>, underBoss: String): List<BroadInfo> {
        return list.sortedWith(
            compareBy(
                { -(it.viewCnt.filter { c -> c.isDigit() }.toIntOrNull() ?: 0) },
                { -it.onOff },
                { it.streamerId != underBoss },
                { -(it.balloninfo?.dayballon?.filter { c -> c.isDigit() }?.toIntOrNull() ?: 0) },
                { -(it.balloninfo?.monthballon?.filter { c -> c.isDigit() }?.toIntOrNull() ?: 0) },
                { -(it.fanCnt.filter { c -> c.isDigit() }.toIntOrNull() ?: 0) }
            )
        )
    }

    fun showDialog(dialogType: BroadListDialogType?) {
        _uiState.update { it.copy(dialogType = dialogType) }
    }
}

data class BroadListUiState(
    val teamName: String = "",
    val items: List<BroadInfo> = emptyList(),
    val underBoss: String = "1",
    val isLoading: Boolean = false,
    val dialogType: BroadListDialogType? = null
)

sealed class BroadListDialogType {
    data class Info(val broadInfo: BroadInfo) : BroadListDialogType()
    data class Move(val broadInfo: BroadInfo) : BroadListDialogType()
}
