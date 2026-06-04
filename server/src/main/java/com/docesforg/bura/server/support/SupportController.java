package com.docesforg.bura.server.support;

import com.docesforg.bura.server.support.SupportService.AdminConversationSummaryResponse;
import com.docesforg.bura.server.support.SupportService.SupportConversationResponse;
import com.docesforg.bura.server.support.SupportService.SupportMessageResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class SupportController {
    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    public record CreateMessageRequest(@Email String email, @NotBlank String name, @NotBlank String message) {
    }

    public record SendMessageRequest(@NotBlank String message) {
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @PostMapping("/api/accounts/{accountId}/support/messages")
    public SupportMessageResponse sendAccountMessage(@PathVariable long accountId, @RequestBody CreateMessageRequest request) {
        return supportService.sendAccountMessage(accountId, request.email(), request.name(), request.message());
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @GetMapping("/api/accounts/{accountId}/support/messages")
    public SupportConversationResponse conversation(@PathVariable long accountId) {
        return supportService.conversation(accountId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/support/conversations")
    public List<AdminConversationSummaryResponse> adminConversations() {
        return supportService.adminConversations();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/support/accounts/{accountId}/messages")
    public SupportConversationResponse adminConversation(@PathVariable long accountId) {
        return supportService.adminConversation(accountId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/support/accounts/{accountId}/messages")
    public SupportMessageResponse sendAdminMessage(@PathVariable long accountId, @RequestBody SendMessageRequest request) {
        return supportService.sendAdminMessage(accountId, request.message());
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @DeleteMapping("/api/accounts/{accountId}/support/messages")
    public void deleteConversation(@PathVariable long accountId) {
        supportService.deleteConversation(accountId);
    }
}
