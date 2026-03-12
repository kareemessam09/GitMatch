package com.kareem.gitmatch

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.kareem.gitmatch.core.di.appModule
import com.kareem.gitmatch.data.local.PreferencesManager
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class GitMatchApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@GitMatchApplication)
            modules(appModule, androidModule)
        }
    }

    private val androidModule = module {
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.createWithPath(
                produceFile = {
                    applicationContext.filesDir.resolve("gitmatch_prefs.preferences_pb").absolutePath.toPath()
                }
            )
        }
        single { PreferencesManager(get()) }
    }
}
