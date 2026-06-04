package com.docesforg.bura.server.support;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SupportService {
    private static final String USER_SENDER = "USER";
    private static final String ADMIN_SENDER = "ADMIN";

    private final SupportMessageRepository messageRepository;
    private final String supportMailbox;

    public SupportService(
            SupportMessageRepository messageRepository,
            @Value("${app.support.mailbox}") String supportMailbox
    ) {
        this.messageRepository = messageRepository;
        this.supportMailbox = supportMailbox;
    }

    public record SupportMessageResponse(Long id, Long accountId, String sender, String message, Instant createdAt) {
    }

    public record SupportConversationResponse(Long accountId, String email, String name, String forwardTo,
                                              List<SupportMessageResponse> messages) {
    }

    public record AdminConversationSummaryResponse(
            Long accountId,
            String email,
            String name,
            String lastMessage,
            Instant lastMessageAt,
            boolean unread
    ) {
    }

    public SupportMessageResponse sendAccountMessage(long accountId, String email, String name, String text) {
        return messageResponse(saveMessage(accountId, email, name, USER_SENDER, text, false));
    }

    public SupportConversationResponse conversation(long accountId) {
        return conversationInternal(accountId, false);
    }

    public List<AdminConversationSummaryResponse> adminConversations() {
        List<SupportMessageEntity> messages = messageRepository.findAllByOrderByCreatedAtDesc();
        Map<Long, SupportMessageEntity> latestByAccount = new LinkedHashMap<>();
        for (SupportMessageEntity message : messages) {
            latestByAccount.putIfAbsent(message.getAccountId(), message);
        }

        List<AdminConversationSummaryResponse> result = new ArrayList<>();
        for (SupportMessageEntity latest : latestByAccount.values()) {
            boolean unread = messageRepository.existsByAccountIdAndSenderAndSeenByAdminFalse(latest.getAccountId(), USER_SENDER);
            result.add(new AdminConversationSummaryResponse(
                    latest.getAccountId(),
                    latest.getEmail(),
                    latest.getName(),
                    latest.getMessage(),
                    latest.getCreatedAt(),
                    unread
            ));
        }
        return result;
    }

    public SupportConversationResponse adminConversation(long accountId) {
        return conversationInternal(accountId, true);
    }

    public SupportMessageResponse sendAdminMessage(long accountId, String text) {
        SupportMessageEntity latest = messageRepository.findFirstByAccountIdOrderByCreatedAtDesc(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support conversation not found"));
        return messageResponse(saveMessage(accountId, latest.getEmail(), latest.getName(), ADMIN_SENDER, text, true));
    }

    public void deleteConversation(long accountId) {
        if (!messageRepository.existsByAccountId(accountId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Support conversation not found");
        }
        messageRepository.deleteAllByAccountId(accountId);
    }

    private SupportConversationResponse conversationInternal(long accountId, boolean markSeenForAdmin) {
        List<SupportMessageEntity> messages = messageRepository.findAllByAccountIdOrderByCreatedAtAsc(accountId);
        if (messages.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Support conversation not found");
        }

        if (markSeenForAdmin) {
            List<SupportMessageEntity> unread = messageRepository.findAllByAccountIdAndSenderAndSeenByAdminFalse(accountId, USER_SENDER);
            unread.forEach(it -> it.setSeenByAdmin(true));
            if (!unread.isEmpty()) {
                messageRepository.saveAll(unread);
                messages = messageRepository.findAllByAccountIdOrderByCreatedAtAsc(accountId);
            }
        }

        SupportMessageEntity head = messages.get(0);
        return new SupportConversationResponse(
                accountId,
                head.getEmail(),
                head.getName(),
                head.getForwardTo(),
                messages.stream().map(this::messageResponse).toList()
        );
    }

    private SupportMessageEntity saveMessage(long accountId, String email, String name, String sender, String text, boolean seenByAdmin) {
        SupportMessageEntity message = new SupportMessageEntity();
        message.setAccountId(accountId);
        message.setEmail(email);
        message.setName(name);
        message.setForwardTo(supportMailbox);
        message.setSender(sender);
        message.setMessage(text);
        message.setCreatedAt(Instant.now());
        message.setSeenByAdmin(seenByAdmin);
        return messageRepository.save(message);
    }

    private SupportMessageResponse messageResponse(SupportMessageEntity message) {
        return new SupportMessageResponse(
                message.getId(),
                message.getAccountId(),
                message.getSender(),
                message.getMessage(),
                message.getCreatedAt()
        );
    }
}
