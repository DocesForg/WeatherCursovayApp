package com.docesforg.bura

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.docesforg.bura.common.AppTheme
import com.docesforg.bura.common.Theme
import com.docesforg.bura.common.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransparentSystemBars()
        setContent {
            val themeViewModel = viewModel<ThemeViewModel>(factory = ThemeViewModel.Factory)
            val theme = themeViewModel.state.collectAsState().value
            val useDarkTheme = when (theme) {
                Theme.Dark -> true
                Theme.Light -> false
                Theme.FollowSystem -> isSystemInDarkTheme()
            }

            LaunchedEffect(useDarkTheme) {
                setSystemBarIconColors(useDarkTheme)
            }
            AppTheme(useDarkTheme) {
                AppNavHost(
                    theme = theme,
                    onThemeClick = themeViewModel::setTheme
                )
            }
        }
    }

    private fun setTransparentSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun setSystemBarIconColors(darkTheme: Boolean) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
    }
}