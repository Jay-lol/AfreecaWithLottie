package com.jay.josaeworld.di

import android.app.Activity
import com.jay.josaeworld.contract.BroadContract
import com.jay.josaeworld.view.BroadCastActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class PresenterModule {

    @Binds
    abstract fun bindsBroadContractView(activity: BroadCastActivity): BroadContract.View

    companion object {
        @Provides
        fun providesBroadCastActivity(activity: Activity): BroadCastActivity {
            return activity as BroadCastActivity
        }
    }
}
