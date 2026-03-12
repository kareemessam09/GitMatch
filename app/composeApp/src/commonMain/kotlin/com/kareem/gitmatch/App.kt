package com.kareem.gitmatch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kareem.gitmatch.core.theme.GitMatchTheme
import com.kareem.gitmatch.data.local.PreferencesManager
import com.kareem.gitmatch.feature.auth.LoginIntent
import com.kareem.gitmatch.feature.auth.LoginScreen
import com.kareem.gitmatch.feature.auth.LoginViewModel
import com.kareem.gitmatch.feature.detail.DetailScreen
import com.kareem.gitmatch.feature.detail.DetailViewModel
import com.kareem.gitmatch.feature.discover.DiscoverScreen
import com.kareem.gitmatch.feature.discover.DiscoverViewModel
import com.kareem.gitmatch.feature.onboarding.OnboardingScreen
import com.kareem.gitmatch.feature.onboarding.OnboardingViewModel
import com.kareem.gitmatch.feature.settings.SettingsScreen
import com.kareem.gitmatch.feature.settings.SettingsViewModel
import com.kareem.gitmatch.feature.vault.VaultScreen
import com.kareem.gitmatch.feature.vault.VaultViewModel
import com.kareem.gitmatch.navigation.Screen
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf


@Composable
fun App(
    onOpenOAuthUrl: (String) -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
    deepLinkToken: String? = null
) {
    val preferencesManager = koinInject<PreferencesManager>()
    val isDarkMode by preferencesManager.isDarkMode.collectAsState(initial = true)

    GitMatchTheme(darkTheme = isDarkMode) {
        var startDestination by remember { mutableStateOf<Screen?>(null) }

        // Determine start destination: Login → Onboarding → Discover
        LaunchedEffect(Unit) {
            val isLoggedIn = preferencesManager.getAuthTokenOnce() != null
            if (!isLoggedIn) {
                startDestination = Screen.Login
            } else {
                val completed = preferencesManager.hasCompletedOnboarding.first()
                startDestination = if (completed) Screen.Discover else Screen.Onboarding
            }
        }

        val dest = startDestination
        if (dest == null) {
            // Show a brief loading while we check onboarding status
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@GitMatchTheme
        }

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()

        // Determine which screens show the bottom bar
        val showBottomBar = navBackStackEntry?.destination?.let { navDest ->
            navDest.hasRoute<Screen.Discover>() ||
                navDest.hasRoute<Screen.Vault>() ||
                navDest.hasRoute<Screen.Settings>()
        } ?: false

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    GitMatchBottomBar(
                        currentRoute = navBackStackEntry?.destination,
                        onNavigate = { screen ->
                            navController.navigate(screen) {
                                popUpTo<Screen.Discover> { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = dest,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable<Screen.Login> {
                    val viewModel = koinViewModel<LoginViewModel>()
                    val uiState by viewModel.uiState.collectAsState()

                    // Handle deep link token from OAuth redirect
                    LaunchedEffect(deepLinkToken) {
                        deepLinkToken?.let { token ->
                            viewModel.onIntent(LoginIntent.HandleDeepLinkToken(token))
                        }
                    }

                    LaunchedEffect(uiState.isLoggedIn) {
                        if (uiState.isLoggedIn) {
                            val completed = preferencesManager.hasCompletedOnboarding.first()
                            val nextScreen = if (completed) Screen.Discover else Screen.Onboarding
                            navController.navigate(nextScreen) {
                                popUpTo<Screen.Login> { inclusive = true }
                            }
                        }
                    }

                    LoginScreen(
                        uiState = uiState,
                        onIntent = viewModel::onIntent,
                        onOpenOAuthUrl = onOpenOAuthUrl,
                        getOAuthUrl = viewModel::getOAuthUrl
                    )
                }

                composable<Screen.Onboarding> {
                    val viewModel = koinViewModel<OnboardingViewModel>()
                    val uiState by viewModel.uiState.collectAsState()

                    LaunchedEffect(uiState.isSaved) {
                        if (uiState.isSaved) {
                            navController.navigate(Screen.Discover) {
                                popUpTo<Screen.Onboarding> { inclusive = true }
                            }
                        }
                    }

                    OnboardingScreen(
                        uiState = uiState,
                        onIntent = viewModel::onIntent
                    )
                }

                composable<Screen.Discover> {
                    val viewModel = koinViewModel<DiscoverViewModel>()
                    val uiState by viewModel.uiState.collectAsState()

                    DiscoverScreen(
                        uiState = uiState,
                        onIntent = viewModel::onIntent,
                        onNavigateToDetail = { cardId ->
                            navController.navigate(Screen.Detail(cardId))
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings)
                        }
                    )
                }

                composable<Screen.Vault> {
                    val viewModel = koinViewModel<VaultViewModel>()
                    val uiState by viewModel.uiState.collectAsState()

                    VaultScreen(
                        uiState = uiState,
                        onIntent = viewModel::onIntent,
                        onNavigateToDetail = { cardId ->
                            navController.navigate(Screen.Detail(cardId))
                        }
                    )
                }

                composable<Screen.Detail> { backStackEntry ->
                    val route = backStackEntry.toRoute<Screen.Detail>()
                    val viewModel = koinViewModel<DetailViewModel>(
                        parameters = { parametersOf(route.cardId) }
                    )
                    val uiState by viewModel.uiState.collectAsState()

                    DetailScreen(
                        uiState = uiState,
                        onBack = { navController.popBackStack() },
                        onOpenUrl = onOpenUrl
                    )
                }

                composable<Screen.Settings> {
                    val viewModel = koinViewModel<SettingsViewModel>()
                    val uiState by viewModel.uiState.collectAsState()

                    LaunchedEffect(uiState.isLoggedOut) {
                        if (uiState.isLoggedOut) {
                            navController.navigate(Screen.Login) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }

                    SettingsScreen(
                        uiState = uiState,
                        onIntent = viewModel::onIntent
                    )
                }
            }
        }
    }
}

@Composable
private fun GitMatchBottomBar(
    currentRoute: androidx.navigation.NavDestination?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = currentRoute?.hasRoute<Screen.Discover>() == true,
            onClick = { onNavigate(Screen.Discover) },
            icon = {
                val isSelected = currentRoute?.hasRoute<Screen.Discover>() == true
                Icon(
                    imageVector = if (isSelected) Icons.Rounded.Explore else Icons.Outlined.Explore,
                    contentDescription = "Discover"
                )
            },
            label = { Text("Discover") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )
        NavigationBarItem(
            selected = currentRoute?.hasRoute<Screen.Vault>() == true,
            onClick = { onNavigate(Screen.Vault) },
            icon = {
                val isSelected = currentRoute?.hasRoute<Screen.Vault>() == true
                Icon(
                    imageVector = if (isSelected) Icons.Rounded.Bookmarks else Icons.Outlined.Bookmarks,
                    contentDescription = "Vault"
                )
            },
            label = { Text("Vault") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )
        NavigationBarItem(
            selected = currentRoute?.hasRoute<Screen.Settings>() == true,
            onClick = { onNavigate(Screen.Settings) },
            icon = {
                val isSelected = currentRoute?.hasRoute<Screen.Settings>() == true
                Icon(
                    imageVector = if (isSelected) Icons.Rounded.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}