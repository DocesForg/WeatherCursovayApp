package com.docesforg.bura.support

import com.docesforg.bura.auth.AuthSessionRepository
import com.docesforg.bura.platform.local.BuraDao
import com.docesforg.bura.platform.local.SupportTicketEntity
import com.docesforg.bura.platform.remote.BuraBackendApi
import com.docesforg.bura.platform.remote.SendSupportMessageRequestDto
import com.docesforg.bura.platform.remote.SupportConversationDto

class SupportRepository(
    private val api: BuraBackendApi,
    private val dao: BuraDao,
    private val authSessionRepository: AuthSessionRepository,
) {
    suspend fun send(question: String): SupportTicketEntity {
        val accountId = authSessionRepository.accountId() ?: error("Account id is missing in session")
        val cached = dao.getSupportTickets(accountId).firstOrNull()
        val email = cached?.email ?: "user-$accountId@bura.app"
        val name = cached?.name ?: "User #$accountId"

        val message = api.sendSupportMessage(accountId, SendSupportMessageRequestDto(email, name, question))

        val entity = SupportTicketEntity(
            id = message.id,
            accountId = accountId,
            email = email,
            name = name,
            question = message.message,
            sender = message.sender,
            createdAt = message.createdAt,
        )
        dao.upsertSupport(entity)
        return entity
    }

    suspend fun history(): List<SupportTicketEntity> {
        val accountId = authSessionRepository.accountId() ?: return emptyList()
        val remote = runCatching {
            api.supportConversation(accountId)
        }.getOrElse { return dao.getSupportTickets(accountId) }

        val entities = remote.toEntities(accountId)
        entities.forEach { dao.upsertSupport(it) }
        return entities
    }

    private fun SupportConversationDto.toEntities(accountId: Long): List<SupportTicketEntity> = messages.map {
        SupportTicketEntity(
            id = it.id,
            accountId = accountId,
            email = email,
            name = name,
            question = it.message,
            sender = it.sender,
            createdAt = it.createdAt,
        )
    }
}
