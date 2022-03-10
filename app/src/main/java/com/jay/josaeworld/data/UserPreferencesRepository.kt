package com.jay.josaeworld.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private val KEY_CLICK_COACH_MARK_CNT = intPreferencesKey("click_coach_mark_cnt")

    val coachMarkCount: Int
        get() = runBlocking { dataStore.data.map { it[KEY_CLICK_COACH_MARK_CNT] ?: 0 }.first() }

    suspend fun incrementCoachMarkCount() {
        dataStore.edit {
            val coachMarkCount = it[KEY_CLICK_COACH_MARK_CNT] ?: 0
            if (coachMarkCount <= 1) {
                it[KEY_CLICK_COACH_MARK_CNT] = coachMarkCount + 1
            }
        }
    }
}
