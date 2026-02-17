package com.jay.josaeworld.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class PresenterModule {
    // BroadPresenter 및 BroadCastActivity 제거로 인해 비워둠
}
