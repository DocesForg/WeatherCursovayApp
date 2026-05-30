package com.docesforg.bura.server.support;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping
public class SupportController {
    private static final String USER_SENDER = "USER";
    private static final String ADMIN_SENDER = "ADMIN";

    private final SupportMessageRepository messageRepository;
    private final String supportMailbox;

    public SupportController(
            SupportMessageRepository messageRepository,
            @Value("${app.support.mailbox}") String supportMailbox
    ) {
        this.messageRepository = messageRepository;
        this.supportMailbox = supportMailbox;
    }

    public record CreateMessageRequest(@Email String email, @NotBlank String name, @NotBlank String message) {
    }

    public record SendMessageRequest(@NotBlank String message) {
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

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @PostMapping("/api/accounts/{accountId}/support/messages")
    public SupportMessageResponse sendAccountMessage(@PathVariable long accountId, @RequestBody CreateMessageRequest request) {
        SupportMessageEntity saved = saveMessage(accountId, request.email(), request.name(), USER_SENDER, request.message(), false);
        return messageResponse(saved);
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @GetMapping("/api/accounts/{accountId}/support/messages")
    public SupportConversationResponse conversation(@PathVariable long accountId) {
        return conversationInternal(accountId, false);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/support/conversations")
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/support/accounts/{accountId}/messages")
    public SupportConversationResponse adminConversation(@PathVariable long accountId) {
        return conversationInternal(accountId, true);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/support/accounts/{accountId}/messages")
    public SupportMessageResponse sendAdminMessage(@PathVariable long accountId, @RequestBody SendMessageRequest request) {
        SupportMessageEntity latest = messageRepository.findFirstByAccountIdOrderByCreatedAtDesc(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support conversation not found"));
        SupportMessageEntity saved = saveMessage(accountId, latest.getEmail(), latest.getName(), ADMIN_SENDER, request.message(), true);
        return messageResponse(saved);
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @DeleteMapping("/api/accounts/{accountId}/support/messages")
    public void deleteConversation(@PathVariable long accountId) {
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
