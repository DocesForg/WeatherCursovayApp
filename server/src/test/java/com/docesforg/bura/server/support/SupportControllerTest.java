package com.docesforg.bura.server.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SupportControllerTest {
    @Mock SupportMessageRepository messageRepository;

    @Test
    void adminConversationMarksUnreadMessagesAsSeen() {
        long accountId = 77L;
        SupportMessageEntity first = message(1L, accountId, "USER", false, "u1", Instant.parse("2026-01-01T10:00:00Z"));
        SupportMessageEntity second = message(2L, accountId, "ADMIN", true, "a1", Instant.parse("2026-01-01T10:02:00Z"));
        SupportMessageEntity unread = message(3L, accountId, "USER", false, "u2", Instant.parse("2026-01-01T10:03:00Z"));

        when(messageRepository.findAllByAccountIdOrderByCreatedAtAsc(accountId))
                .thenReturn(List.of(first, second))
                .thenReturn(List.of(first, second, unread));
        when(messageRepository.findAllByAccountIdAndSenderAndSeenByAdminFalse(accountId, "USER"))
                .thenReturn(List.of(unread));

        SupportService service = new SupportService(messageRepository, "support@bura.app");
        SupportService.SupportConversationResponse response = service.adminConversation(accountId);

        assertEquals(3, response.messages().size());
        assertEquals(true, unread.isSeenByAdmin());
        verify(messageRepository).saveAll(List.of(unread));
    }

    @Test
    void conversationDoesNotMarkSeenForRegularUserPath() {
        long accountId = 88L;
        SupportMessageEntity only = message(4L, accountId, "USER", false, "hello", Instant.parse("2026-02-01T10:00:00Z"));
        when(messageRepository.findAllByAccountIdOrderByCreatedAtAsc(accountId)).thenReturn(List.of(only));

        SupportService service = new SupportService(messageRepository, "support@bura.app");
        SupportService.SupportConversationResponse response = service.conversation(accountId);

        assertEquals(1, response.messages().size());
        verify(messageRepository).findAllByAccountIdOrderByCreatedAtAsc(accountId);
        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    void adminConversationReturnsNotFoundWhenNoMessages() {
        when(messageRepository.findAllByAccountIdOrderByCreatedAtAsc(9L)).thenReturn(List.of());
        SupportService service = new SupportService(messageRepository, "support@bura.app");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.adminConversation(9L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    private SupportMessageEntity message(Long id, long accountId, String sender, boolean seenByAdmin, String text, Instant at) {
        SupportMessageEntity entity = new SupportMessageEntity();
        ReflectionTestUtils.setField(entity, "id", id);
        entity.setAccountId(accountId);
        entity.setEmail("user@example.com");
        entity.setName("User");
        entity.setForwardTo("support@bura.app");
        entity.setSender(sender);
        entity.setMessage(text);
        entity.setCreatedAt(at);
        entity.setSeenByAdmin(seenByAdmin);
        return entity;
    }
}
