package com.kareem.gitmatch

import androidx.compose.ui.window.ComposeUIViewController
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.kareem.gitmatch.core.di.appModule
import com.kareem.gitmatch.data.local.PreferencesManager
import okio.Path.Companion.toPath
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App(
        onOpenUrl = { url ->
            NSURL.URLWithString(url)?.let { nsUrl ->
                UIApplication.sharedApplication.openURL(nsUrl)
            }
        }
    )
}

private var koinInitialized = false

private fun initKoin() {
    if (koinInitialized) return
    koinInitialized = true

    startKoin {
        modules(appModule, iosModule)
    }
}

private val iosModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                "gitmatch_prefs.preferences_pb".toPath()
            }
        )
    }
    single { PreferencesManager(get()) }
}