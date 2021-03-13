package com.rachmad.app.mychallengetest.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(val app: Application) {

    @Singleton
    @Provides
    fun provideContext(): Context = app

    @Provides
    @Singleton
    fun provideApplication(): Application = this.app
}