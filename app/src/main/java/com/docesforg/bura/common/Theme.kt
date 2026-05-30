package com.docesforg.bura.common

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.docesforg.bura.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val DARK_MODE_KEY = "dark_mode"

class ThemeViewModel(private val prefs: SharedPreferences) : ViewModel() {
    private val _state = MutableStateFlow(
        prefs.getString(DARK_MODE_KEY, null)?.let(Theme::valueOf) ?: Theme.FollowSystem
    )
    val state = _state.asStateFlow()

    fun setTheme(value: Theme) {
        prefs.edit { putString(DARK_MODE_KEY, value.name) }
        _state.value = value
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return ThemeViewModel(container.prefs) as T
            }
        }
    }
}

enum class Theme {
    Dark, Light, FollowSystem
}