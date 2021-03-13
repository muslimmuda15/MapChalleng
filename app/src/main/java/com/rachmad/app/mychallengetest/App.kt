package com.rachmad.app.mychallengetest

import android.app.Application
import com.rachmad.app.mychallengetest.di.*

const val UNKNOWN_ERROR = 1234567
const val EMPTY_ERROR = 7654321

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerApplicationComponent.builder()
                .appModule(AppModule(this))
                .apiModule(ApiModule())
                .locationModule(LocationModule(this))
                .build()

        appComponent.inject(this)
    }

    companion object {
        @JvmStatic lateinit var appComponent: ApplicationComponent
    }
}