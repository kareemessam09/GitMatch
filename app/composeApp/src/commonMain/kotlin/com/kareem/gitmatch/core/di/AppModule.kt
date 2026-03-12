package com.kareem.gitmatch.core.di

import com.kareem.gitmatch.core.network.GitMatchApi
import com.kareem.gitmatch.core.network.HttpClientFactory
import com.kareem.gitmatch.data.local.LocalFeedCache
import com.kareem.gitmatch.data.repository.AuthRepository
import com.kareem.gitmatch.data.repository.AuthRepositoryImpl
import com.kareem.gitmatch.data.repository.FeedRepository
import com.kareem.gitmatch.data.repository.FeedRepositoryImpl
import com.kareem.gitmatch.data.repository.SwipeRepository
import com.kareem.gitmatch.data.repository.SwipeRepositoryImpl
import com.kareem.gitmatch.data.repository.UserRepository
import com.kareem.gitmatch.data.repository.UserRepositoryImpl
import com.kareem.gitmatch.feature.auth.LoginViewModel
import com.kareem.gitmatch.feature.detail.DetailViewModel
import com.kareem.gitmatch.feature.discover.DiscoverViewModel
import com.kareem.gitmatch.feature.onboarding.OnboardingViewModel
import com.kareem.gitmatch.feature.settings.SettingsViewModel
import com.kareem.gitmatch.feature.vault.VaultViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Network
    single { HttpClientFactory.create(get()) }
    single { GitMatchApi(get()) }

    // Local Storage
    single { LocalFeedCache(get()) }

    // Repositories
    single<FeedRepository> { FeedRepositoryImpl(get(), get()) }
    single<SwipeRepository> { SwipeRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    // ViewModels
    viewModel { DiscoverViewModel(get(), get(), get(), get()) }
    viewModel { VaultViewModel(get()) }
    viewModel { OnboardingViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { params -> DetailViewModel(params.get(), get()) }
}
