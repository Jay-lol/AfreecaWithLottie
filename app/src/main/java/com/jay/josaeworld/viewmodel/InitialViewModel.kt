package com.jay.josaeworld.viewmodel

import android.content.Context
import android.os.Build
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jay.josaeworld.domain.GetInitTeamDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InitialViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getInitTeamDataUseCase: GetInitTeamDataUseCase
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<InitialSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    fun getInitTeamData() {
        getInitTeamDataUseCase(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val value = snapshot.children
                        val list = arrayListOf<String>()
                        var time = 0L
                        var minversionCode = 123456789
                        var currentversionCode = 123456789
                        for (x in value) {
                            when (x.key) {
                                "LastUpDateTime" -> time = x.value as Long
                                "minversionCode" -> minversionCode = (x.value as Long).toInt()
                                "currentversionCode" ->
                                    currentversionCode =
                                        (x.value as Long).toInt()
                                else -> {
                                    if (x.key?.isDigitsOnly() == true) {
                                        list.add(x.value as String)
                                    }
                                }
                            }
                        }
                        checkVersionAndNavigate(list, time, minversionCode, currentversionCode)
                    } catch (e: Exception) {
                        viewModelScope.launch {
                            _sideEffect.emit(InitialSideEffect.ShowToast(e.message ?: "Unknown Error"))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    viewModelScope.launch {
                        _sideEffect.emit(InitialSideEffect.ShowToast(error.message))
                    }
                }
            }
        )
    }

    private fun checkVersionAndNavigate(
        newList: List<String>,
        time: Long,
        minversionCode: Int,
        currentversionCode: Int
    ) {
        val pi = context.packageManager.getPackageInfo(context.packageName, 0)
        val vc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pi.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            pi.versionCode.toLong()
        }

        val code = when {
            vc < minversionCode -> 3
            vc < currentversionCode -> 2
            else -> 0
        }

        viewModelScope.launch {
            _sideEffect.emit(InitialSideEffect.NavigateToMain(newList, time, code))
        }
    }
}

sealed class InitialSideEffect {
    data class NavigateToMain(val newList: List<String>, val time: Long, val code: Int) : InitialSideEffect()
    data class ShowToast(val message: String) : InitialSideEffect()
}
