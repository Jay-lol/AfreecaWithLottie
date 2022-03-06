package com.jay.josaeworld.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import java.util.*

@Module
@InstallIn(ActivityRetainedComponent::class)
object UtilModule {

    @Provides
    @ActivityRetainedScoped
    fun provideRandom() = Random()
}
