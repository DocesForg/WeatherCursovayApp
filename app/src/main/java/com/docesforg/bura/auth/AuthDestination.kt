package com.docesforg.bura.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.docesforg.bura.App
import com.docesforg.bura.account.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@Composable
fun AuthDestination(onSuccess: () -> Unit) {
    val viewModel = viewModel<AuthViewModel>(factory = AuthViewModel.Factory)
    val state by viewModel.state.collectAsState()

    val email = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    var showRegistrationForm by remember { mutableStateOf(false) }

    if (state.loggedIn) onSuccess()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                    Text(
                        text = if (showRegistrationForm) "Регистрация в Bura" else "Вход в Bura",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = if (showRegistrationForm) {
                            "Создайте аккаунт для синхронизации избранных мест."
                        } else {
                            "Войдите, чтобы открыть все возможности приложения."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Почта") }
            )
                    if (showRegistrationForm) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = name.value,
                            onValueChange = { name.value = it },
                            label = { Text("Имя") }
                        )
                    }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Пароль") }
            )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.loading,
                        onClick = {
                            if (showRegistrationForm) {
                                viewModel.register(email.value, name.value, password.value)
                            } else {
                                viewModel.login(email.value, password.value)
                            }
                        }
                    ) {
                        Text(if (showRegistrationForm) "Зарегистрироваться" else "Войти")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!showRegistrationForm) {
                            Text(
                                text = "Нет аккаунта?",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(onClick = { showRegistrationForm = true }) {
                                Text("Регистрация")
                            }
                        } else {
                            Text(
                                text = "Уже есть аккаунт?",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(onClick = { showRegistrationForm = false }) {
                                Text("Войти")
                            }
                        }
                    }
                    if (state.loading) {
                        CircularProgressIndicator()
                    }
                    state.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

data class AuthState(
    val loading: Boolean = false,
    val loggedIn: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(
    private val accountRepository: AccountRepository,
    private val sessionRepository: AuthSessionRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState(loggedIn = sessionRepository.isLoggedIn()))
    val state = _state.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { accountRepository.login(email, password) }
                .onSuccess {
                    sessionRepository.saveSession(it.token, it.account.id, password)
                    _state.value = AuthState(loggedIn = true)
                }
                .onFailure {
                    _state.value = _state.value.copy(loading = false, error = mapAuthError(it, isRegistration = false))
                }
        }
    }

    fun register(email: String, name: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { accountRepository.register(email, name, password) }
                .onSuccess {
                    sessionRepository.saveSession(it.token, it.account.id, password)
                    _state.value = AuthState(loggedIn = true)
                }
                .onFailure {
                    _state.value = _state.value.copy(loading = false, error = mapAuthError(it, isRegistration = true))
                }
        }
    }

    private fun mapAuthError(error: Throwable, isRegistration: Boolean): String {
        val fallback = if (isRegistration) "Ошибка регистрации" else "Ошибка входа"
        return when (error) {
            is HttpException -> {
                when (error.code()) {
                    409 -> "Пользователь с такой почтой уже существует. Войдите в аккаунт или используйте другой email."
                    401 -> "Неверная почта или пароль."
                    500 -> if (isRegistration) {
                        "Не удалось зарегистрироваться. Возможно, такая почта уже существует."
                    } else {
                        "Сервер временно недоступен (HTTP 500). Попробуйте снова чуть позже."
                    }
                    else -> fallback
                }
            }
            is IOException -> "Нет подключения к интернету. Проверьте сеть и попробуйте снова."
            else -> error.message ?: fallback
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return AuthViewModel(container.accountRepository, container.authSessionRepository) as T
            }
        }
    }
}
