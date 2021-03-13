package com.rachmad.app.mychallengetest.di

import android.app.Application
import com.google.android.gms.location.*
import com.rachmad.app.mychallengetest.helper.StoreLocation
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LocationModule(val app: Application) {
    @Provides
    fun provideLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.setWaitForAccurateLocation(false)
        locationRequest.setInterval(1000L)
        locationRequest.setFastestInterval(5000L)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        return locationRequest
    }

    @Singleton
    @Provides
    fun provideFusedLocationProvideClient(): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(app)

    @Singleton
    @Provides
    fun provideSettingClient(): SettingsClient = LocationServices.getSettingsClient(app)

    @Provides
    fun provideLocationSettingRequest(): LocationSettingsRequest{
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(provideLocationRequest())
        return builder.build()
    }

    @Singleton
    @Provides
    fun provideStoreLocation(): StoreLocation = StoreLocation(app)
}