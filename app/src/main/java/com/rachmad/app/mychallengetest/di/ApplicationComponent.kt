package com.rachmad.app.mychallengetest.di

import android.app.Application
import com.rachmad.app.mychallengetest.ui.MapsActivity
import com.rachmad.app.mychallengetest.helper.StoreLocation
import com.rachmad.app.mychallengetest.viewmodel.ViewModelApp
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, ApiModule::class, LocationModule::class))
interface ApplicationComponent {
    fun inject(activity: MapsActivity)
    fun inject(storeLocation: StoreLocation)
    fun inject(app: Application)
    fun inject(viewModelApp: ViewModelApp)
}