package com.docesforg.bura

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.docesforg.bura.account.AccountDestination
import com.docesforg.bura.auth.AuthDestination
import com.docesforg.bura.common.Theme
import com.docesforg.bura.graphs.EssentialGraphsDestination
import com.docesforg.bura.place.saved.FavoritesDestination
import com.docesforg.bura.radio.RadioSignalDestination
import com.docesforg.bura.settings.SettingsDestination
import com.docesforg.bura.summary.SummaryDestination
import com.docesforg.bura.support.SupportDestination
import kotlinx.serialization.Serializable
import java.time.LocalDate

sealed interface AppRoutes {
    @Serializable
    data object Home : AppRoutes

    @Serializable
    data object Favorites : AppRoutes

    @Serializable
    data object Account : AppRoutes

    @Serializable
    data object Support : AppRoutes

    @Serializable
    data object RadioSignal : AppRoutes

    @Serializable
    data class EssentialGraphs(val initialDay: String? = null) : AppRoutes

    @Serializable
    data object Settings : AppRoutes
}

private data class BottomTab(val route: AppRoutes, val icon: @Composable () -> Unit)

@Composable
fun AppNavHost(theme: Theme, onThemeClick: (Theme) -> Unit) {
    val app = LocalContext.current.applicationContext as App
    val loggedIn by app.container.authSessionRepository.loggedIn.collectAsState()

    if (!loggedIn) {
        AuthDestination(onSuccess = {})
        return
    }

    val controller = rememberNavController()
    val tabs = listOf(
        BottomTab(AppRoutes.Home) {
            Icon(imageVector = Icons.Outlined.Home, contentDescription = null)
        },
        BottomTab(AppRoutes.Favorites) {
            Icon(imageVector = Icons.Outlined.FavoriteBorder, contentDescription = null)
        },
        BottomTab(AppRoutes.Account) {
            Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = null)
        },
        BottomTab(AppRoutes.Support) {
            Icon(painter = painterResource(id = R.drawable.help_outline), contentDescription = null)
        },
        BottomTab(AppRoutes.RadioSignal) {
            Icon(painter = painterResource(id = R.drawable.antenna_outline), contentDescription = null)
        },
    )

    var selectedTab by remember { mutableStateOf<AppRoutes>(AppRoutes.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab.route,
                        onClick = {
                            selectedTab = tab.route
                            controller.navigate(tab.route) {
                                launchSingleTop = true
                            }
                        },
                        icon = tab.icon,
                        alwaysShowLabel = false,
                        label = null
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = controller,
            startDestination = AppRoutes.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<AppRoutes.Home> {
                SummaryDestination(
                    onHourlySectionClick = {
                        controller.navigate(AppRoutes.EssentialGraphs())
                    },
                    onDayClick = {
                        controller.navigate(AppRoutes.EssentialGraphs(initialDay = it.toString()))
                    },
                    onSettingsButtonClick = {
                        controller.navigate(AppRoutes.Settings)
                    },
                    onPrecipitationClick = {
                        controller.navigate(AppRoutes.EssentialGraphs())
                    }
                )
            }
            composable<AppRoutes.Favorites> {
                FavoritesDestination(
                    onOpenPlace = {
                        selectedTab = AppRoutes.Home
                        controller.navigate(AppRoutes.Home) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable<AppRoutes.Account> {
                AccountDestination(onLoggedOut = {})
            }
            composable<AppRoutes.Support> {
                SupportDestination()
            }
            composable<AppRoutes.RadioSignal> {
                RadioSignalDestination()
            }
            composable<AppRoutes.EssentialGraphs> { backStackEntry ->
                EssentialGraphsDestination(
                    initialDay = backStackEntry.toRoute<AppRoutes.EssentialGraphs>().initialDay?.let(LocalDate::parse),
                    onSelectPlaceClick = controller::navigateUp,
                    onBackClick = controller::navigateUp
                )
            }
            composable<AppRoutes.Settings> {
                SettingsDestination(
                    theme = theme,
                    onThemeClick = onThemeClick,
                    onBackClick = controller::navigateUp
                )
            }
        }
    }
}
