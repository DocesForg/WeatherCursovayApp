package com.docesforg.bura.account

import com.docesforg.bura.auth.AuthSessionRepository
import com.docesforg.bura.platform.local.AccountEntity
import com.docesforg.bura.platform.local.BuraDao
import com.docesforg.bura.platform.remote.AuthResponse
import com.docesforg.bura.platform.remote.BuraBackendApi
import com.docesforg.bura.platform.remote.LoginRequest
import com.docesforg.bura.platform.remote.RegisterRequest
import com.docesforg.bura.platform.remote.UpdateNameRequestDto
import com.docesforg.bura.platform.remote.UpdatePasswordRequestDto
import com.docesforg.bura.place.saved.SavedPlacesRepository

class AccountRepository(
    private val api: BuraBackendApi,
    private val dao: BuraDao,
    private val authSessionRepository: AuthSessionRepository,
    private val savedPlacesRepository: SavedPlacesRepository,
) {
    suspend fun login(email: String, password: String): AuthResponse {
        val response = api.login(LoginRequest(email, password))
        dao.upsertAccount(AccountEntity(response.account.id, response.account.email, response.account.displayName))
        return response
    }

    suspend fun register(email: String, name: String, password: String): AuthResponse {
        val response = api.register(RegisterRequest(email, name, password))
        dao.upsertAccount(AccountEntity(response.account.id, response.account.email, response.account.displayName))
        return response
    }

    suspend fun getLocalAccount(): AccountEntity? {
        val accountId = authSessionRepository.accountId() ?: return null
        return dao.getAccount(accountId)
    }

    suspend fun updateLocalName(name: String) {
        val accountId = authSessionRepository.accountId() ?: return
        val response = api.updateName(accountId, UpdateNameRequestDto(displayName = name))
        dao.upsertAccount(AccountEntity(response.id, response.email, response.displayName))
    }

    suspend fun updatePassword(password: String) {
        val accountId = authSessionRepository.accountId() ?: return
        api.updatePassword(accountId, UpdatePasswordRequestDto(password = password))
    }

    suspend fun stats(): AccountStats {
        val accountId = authSessionRepository.accountId() ?: error("Account id is missing in session")
        return api.stats(accountId).let {
            AccountStats(it.favorites, it.radioTests, it.supportRequests)
        }
    }

    suspend fun deleteCurrentAccount() {
        val accountId = authSessionRepository.accountId() ?: return
        runCatching { api.deleteAccount(accountId) }
        dao.deleteFavorites(accountId)
        dao.deleteSupportTickets(accountId)
        dao.deleteRadioTests(accountId)
        dao.deleteAccount(accountId)
        savedPlacesRepository.deletePlacesForAccount(accountId)
    }
}

data class AccountStats(
    val favorites: Int,
    val radioTests: Int,
    val supportRequests: Long,
)
