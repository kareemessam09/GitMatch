package com.kareem.gitmatch

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.kareem.gitmatch.core.di.appModule
import com.kareem.gitmatch.data.local.PreferencesManager
import okio.Path.Companion.toPath
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main() {
    startKoin {
        modules(appModule, jvmModule)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "GitMatch",
        ) {
            App()
        }
    }
}

private val jvmModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                val userHome = System.getProperty("user.home")
                "$userHome/.gitmatch/prefs.preferences_pb".toPath()
            }
        )
    }
    single { PreferencesManager(get()) }
}