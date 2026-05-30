package com.docesforg.bura.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.docesforg.bura.App
import com.docesforg.bura.auth.AuthSessionRepository
import com.docesforg.bura.platform.local.AccountEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun AccountDestination(onLoggedOut: () -> Unit) {
    val viewModel = viewModel<AccountViewModel>(factory = AccountViewModel.Factory)
    val account by viewModel.account.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val accountPassword by viewModel.password.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var name by remember(account?.displayName) { mutableStateOf(account?.displayName ?: "Пользователь") }
    var email by remember(account?.email) { mutableStateOf(account?.email ?: "user@example.com") }
    var password by remember(accountPassword) { mutableStateOf(accountPassword) }

    val danger = Color(0xFFFF2B2B)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
                Text("👤 Аккаунт", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("◌", color = Color.White, style = MaterialTheme.typography.displaySmall)
                        }

                        OutlinedTextField(value = name, onValueChange = { name = it }, enabled = isEditing, modifier = Modifier.fillMaxWidth(), label = { Text("Имя") })
                        OutlinedTextField(value = email, onValueChange = { email = it }, enabled = false, modifier = Modifier.fillMaxWidth(), label = { Text("Email") })
                        OutlinedTextField(value = password, onValueChange = { password = it }, enabled = isEditing, modifier = Modifier.fillMaxWidth(), label = { Text("Пароль") })

                        stats?.let {
                            Text(
                                "Избранное: ${it.favorites} • Радиотесты: ${it.radioTests} • Обращения: ${it.supportRequests}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isEditing) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.updateLocalName(name)
                                        viewModel.updatePassword(password)
                                        isEditing = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0CBB61), contentColor = Color.White)
                                ) { Text("Сохранить") }
                                OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) { Text("Отмена") }
                            }
                        } else {
                            Button(
                                onClick = { isEditing = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                            ) { Text("Редактировать", fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("⚠️ Опасная зона", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Удаление аккаунта приведет к потере всех данных, включая избранные города и историю.")
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = danger, contentColor = Color.White)
                        ) { Text("🗑 Удалить аккаунт") }
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.logout(); onLoggedOut() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Выйти") }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Вы уверены?") },
            text = { Text("Это действие нельзя отменить. Все ваши данные будут безвозвратно удалены.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount {
                            onLoggedOut()
                            showDeleteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = danger)
                ) { Text("Удалить") }
            },
            dismissButton = { OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Отмена") } }
        )
    }
}

class AccountViewModel(
    private val repository: AccountRepository,
    private val authSessionRepository: AuthSessionRepository,
) : ViewModel() {
    private val _account = MutableStateFlow<AccountEntity?>(null)
    val account = _account.asStateFlow()
    private val _stats = MutableStateFlow<AccountStats?>(null)
    val stats = _stats.asStateFlow()
    private val _password = MutableStateFlow(authSessionRepository.accountPassword())
    val password = _password.asStateFlow()

    init {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            _account.value = repository.getLocalAccount()
            _stats.value = runCatching { repository.stats() }.getOrNull()
        }
    }

    fun updateLocalName(name: String) {
        viewModelScope.launch {
            repository.updateLocalName(name)
            refresh()
        }
    }

    fun updatePassword(password: String) {
        if (password.isBlank()) return
        viewModelScope.launch {
            repository.updatePassword(password)
            authSessionRepository.savePassword(password)
            _password.value = password
        }
    }

    fun logout() {
        authSessionRepository.clearSession()
    }

    fun deleteAccount(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteCurrentAccount()
            authSessionRepository.clearSession()
            onDone()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return AccountViewModel(container.accountRepository, container.authSessionRepository) as T
            }
        }
    }
}
