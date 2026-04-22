package com.mukrram.timetable.data.di

import android.content.Context
import com.mukrram.timetable.data.local.AuthTokenStore
import com.mukrram.timetable.data.local.TimetablePreferences
import com.mukrram.timetable.data.remote.NetworkModule
import com.mukrram.timetable.data.remote.TimetableApi
import com.mukrram.timetable.data.repository.TimetableRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object ServiceLocator {

    private lateinit var appContext: Context

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val tokenStore: AuthTokenStore by lazy { AuthTokenStore() }

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
    }

    val preferences: TimetablePreferences by lazy { TimetablePreferences(appContext) }

    private val networkModule: NetworkModule by lazy {
        NetworkModule(bearerToken = { tokenStore.token })
    }

    val api: TimetableApi by lazy { networkModule.retrofit.create(TimetableApi::class.java) }

    val repository: TimetableRepository by lazy {
        TimetableRepository(
            api = api,
            preferences = preferences,
            tokenStore = tokenStore,
            applicationScope = applicationScope,
        )
    }
}
