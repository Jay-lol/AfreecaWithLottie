package com.jay.josaeworld.di

import android.app.Activity
import com.jay.josaeworld.contract.BroadContract
import com.jay.josaeworld.contract.MainContract
import com.jay.josaeworld.contract.SplashContract
import com.jay.josaeworld.view.BroadCastActivity
import com.jay.josaeworld.view.MainActivity
import com.jay.josaeworld.view.SplashActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class PresenterModule {

    @Binds
    abstract fun bindsSplashContractView(activity: SplashActivity): SplashContract.View

    @Binds
    abstract fun bindsMainContractView(activity: MainActivity): MainContract.View

    @Binds
    abstract fun bindsBroadContractView(activity: BroadCastActivity): BroadContract.View

    companion object {
        @Provides
        fun providesSplashActivity(activity: Activity): SplashActivity {
            return activity as SplashActivity
        }

        @Provides
        fun providesActivity(activity: Activity): MainActivity {
            return activity as MainActivity
        }

        @Provides
        fun providesBroadCastActivity(activity: Activity): BroadCastActivity {
            return activity as BroadCastActivity
        }
    }
}
