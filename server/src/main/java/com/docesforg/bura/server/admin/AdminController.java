package com.docesforg.bura.server.admin;

import com.docesforg.bura.server.admin.AdminService.AccountAdminView;
import com.docesforg.bura.server.admin.AdminService.DashboardResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    public record AccountRoleUpdateRequest(@NotBlank String role) {
    }

    @GetMapping("/api/admin/dashboard")
    public DashboardResponse dashboard() {
        return adminService.dashboard();
    }

    @GetMapping("/api/admin/accounts")
    public List<AccountAdminView> accounts() {
        return adminService.accounts();
    }

    @PatchMapping("/api/admin/accounts/{accountId}/role")
    public AccountAdminView updateRole(@PathVariable long accountId, @Valid @RequestBody AccountRoleUpdateRequest request) {
        return adminService.updateRole(accountId, request.role());
    }

    @GetMapping(value = "/admin/panel", produces = MediaType.TEXT_HTML_VALUE)
    public String panel() {
        return """
                <!doctype html>
                <html lang=\"ru\">
                <head>
                  <meta charset=\"utf-8\"/>
                  <title>Bura Admin</title>
                  <style>
                    body { font-family: Arial, sans-serif; margin: 0; background:#f4f6fb; color:#111; }
                    .layout { display:grid; grid-template-columns: 340px 1fr; height:100vh; }
                    .sidebar { border-right:1px solid #dde2ef; background:white; display:flex; flex-direction:column; }
                    .sidebar h2 { margin:16px; }
                    .users { overflow:auto; padding:0 8px 8px; flex:1; }
                    .user { padding:10px 12px; border-radius:12px; cursor:pointer; display:flex; justify-content:space-between; gap:10px; align-items:flex-start; margin-bottom:6px; }
                    .user:hover { background:#f1f4ff; }
                    .user.active { background:#e6eeff; }
                    .user .meta { font-size:12px; color:#5f6a85; margin-top:4px; }
                    .dot { width:10px; height:10px; border-radius:50%; background:#2f80ff; margin-top:6px; flex:0 0 auto; }
                    .chat { display:flex; flex-direction:column; }
                    .chat-header { padding:16px; border-bottom:1px solid #dde2ef; background:white; }
                    .chat-messages { flex:1; overflow:auto; padding:16px; display:flex; flex-direction:column; gap:10px; }
                    .bubble { max-width:70%; padding:10px 12px; border-radius:14px; }
                    .bubble.user { align-self:flex-end; background:#347CF3; color:white; }
                    .bubble.admin { align-self:flex-start; background:#e9e9ee; color:#000; }
                    .bubble .sender { font-weight:700; margin-bottom:4px; }
                    .bubble .time { font-size:11px; opacity:.75; margin-top:6px; }
                    .chat-input { border-top:1px solid #dde2ef; padding:12px; background:white; display:flex; gap:8px; }
                    .chat-input input { flex:1; border:1px solid #c7cfdf; border-radius:10px; padding:10px 12px; font-size:14px; }
                    .chat-input button { border:0; background:#347CF3; color:white; border-radius:10px; padding:0 16px; font-weight:700; cursor:pointer; }
                    .placeholder { margin:auto; color:#6c7896; }
                  </style>
                </head>
                <body>
                  <div class=\"layout\">
                    <aside class=\"sidebar\">
                      <h2>Пользовательские чаты</h2>
                      <div id=\"users\" class=\"users\"></div>
                    </aside>

                    <section class=\"chat\">
                      <div id=\"chatHeader\" class=\"chat-header\">Выберите пользователя из списка слева</div>
                      <div id=\"chatMessages\" class=\"chat-messages\"><div class=\"placeholder\">Нет выбранного чата</div></div>
                      <div class=\"chat-input\">
                        <input id=\"messageInput\" placeholder=\"Введите ответ пользователю\" />
                        <button id=\"sendButton\">Отправить</button>
                      </div>
                    </section>
                  </div>

                  <script>
                    const usersEl = document.getElementById('users');
                    const chatHeaderEl = document.getElementById('chatHeader');
                    const chatMessagesEl = document.getElementById('chatMessages');
                    const inputEl = document.getElementById('messageInput');
                    const sendBtn = document.getElementById('sendButton');

                    let selectedAccountId = null;
                    let conversations = [];
                    const tokenFromUrl = new URLSearchParams(window.location.search).get('token');
                    const tokenFromStorage = window.localStorage.getItem('bura_admin_token');
                    const authToken = tokenFromUrl || tokenFromStorage || null;

                    async function apiFetch(url, options = {}) {
                      const headers = Object.assign({}, options.headers || {});
                      if (authToken) headers['Authorization'] = `Bearer ${authToken}`;
                      return fetch(url, Object.assign({}, options, { headers, credentials: 'include' }));
                    }

                    function escapeHtml(text) {
                      return (text || '').replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');
                    }

                    async function loadConversations() {
                      const res = await apiFetch('/api/admin/support/conversations');
                      if (!res.ok) {
                        usersEl.innerHTML = `<div class=\"placeholder\" style=\"padding-top:16px;\">Не удалось загрузить чаты (HTTP ${res.status})</div>`;
                        return;
                      }
                      conversations = await res.json();
                      renderUserList();
                    }

                    function renderUserList() {
                      usersEl.innerHTML = conversations.length ? '' : '<div class="placeholder" style="padding-top:16px;">Пока нет обращений</div>';
                      conversations.forEach(item => {
                        const row = document.createElement('div');
                        row.className = 'user' + (item.accountId === selectedAccountId ? ' active' : '');
                        row.innerHTML = `
                          <div style=\"min-width:0\">\
                            <div><b>${escapeHtml(item.name)}</b></div>\
                            <div class=\"meta\">ID: ${item.accountId} · ${escapeHtml(item.email)}</div>\
                            <div class=\"meta\">${escapeHtml(item.lastMessage || '')}</div>\
                          </div>
                          ${item.unread ? '<div class="dot"></div>' : ''}
                        `;
                        row.onclick = () => openConversation(item.accountId);
                        usersEl.appendChild(row);
                      });
                    }

                    async function openConversation(accountId) {
                      selectedAccountId = accountId;
                      renderUserList();

                      const res = await apiFetch(`/api/admin/support/accounts/${accountId}/messages`);
                      if (!res.ok) {
                        chatMessagesEl.innerHTML = `<div class=\"placeholder\">Не удалось открыть чат (HTTP ${res.status})</div>`;
                        return;
                      }
                      const data = await res.json();

                      chatHeaderEl.textContent = `${data.name} (ID: ${data.accountId}) · ${data.email}`;
                      chatMessagesEl.innerHTML = '';

                      data.messages.forEach(m => {
                        const isAdmin = m.sender === 'ADMIN';
                        const div = document.createElement('div');
                        div.className = 'bubble ' + (isAdmin ? 'admin' : 'user');
                        div.innerHTML = `
                          <div class=\"sender\">${isAdmin ? 'Админ' : 'Пользователь'}</div>
                          <div>${escapeHtml(m.message)}</div>
                          <div class=\"time\">${escapeHtml(m.createdAt)}</div>
                        `;
                        chatMessagesEl.appendChild(div);
                      });
                      chatMessagesEl.scrollTop = chatMessagesEl.scrollHeight;
                      await loadConversations();
                    }

                    async function sendMessage() {
                      const text = inputEl.value.trim();
                      if (!text || !selectedAccountId) return;
                      const res = await apiFetch(`/api/admin/support/accounts/${selectedAccountId}/messages`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ message: text })
                      });
                      if (!res.ok) {
                        chatMessagesEl.innerHTML = `<div class=\"placeholder\">Не удалось отправить сообщение (HTTP ${res.status})</div>`;
                        return;
                      }
                      inputEl.value = '';
                      await openConversation(selectedAccountId);
                    }

                    sendBtn.onclick = sendMessage;
                    inputEl.addEventListener('keydown', e => {
                      if (e.key === 'Enter') sendMessage();
                    });

                    loadConversations();
                    setInterval(async () => {
                      await loadConversations();
                      if (selectedAccountId) await openConversation(selectedAccountId);
                    }, 5000);
                  </script>
                </body>
                </html>
                """;
    }
}
