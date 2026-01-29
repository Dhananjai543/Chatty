package com.chatty.websocket;

import com.chatty.dto.MessageDTO;
import com.chatty.entity.Message;
import com.chatty.entity.User;
import com.chatty.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    // Track connected users by session ID
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        String sessionId = headerAccessor.getSessionId();

        if (principal != null && sessionId != null) {
            String username = principal.getName();
            sessionUserMap.put(sessionId, username);

            log.info("User connected: {} (session: {})", username, sessionId);

            try {
                // Update user status to online
                User user = userService.getUserEntityByUsername(username);
                userService.updateUserStatus(user.getId(), User.UserStatus.ONLINE);

                // Broadcast user online notification
                MessageDTO notification = MessageDTO.builder()
                        .senderUsername(username)
                        .messageType(Message.MessageType.JOIN)
                        .content(username + " is now online")
                        .timestamp(LocalDateTime.now())
                        .build();

                messagingTemplate.convertAndSend("/topic/notifications", notification);
            } catch (Exception e) {
                log.error("Error handling user connection: {}", e.getMessage());
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId != null) {
            String username = sessionUserMap.remove(sessionId);

            if (username != null) {
                log.info("User disconnected: {} (session: {})", username, sessionId);

                try {
                    // Update user status to offline
                    User user = userService.getUserEntityByUsername(username);
                    userService.updateUserStatus(user.getId(), User.UserStatus.OFFLINE);

                    // Broadcast user offline notification
                    MessageDTO notification = MessageDTO.builder()
                            .senderUsername(username)
                            .messageType(Message.MessageType.LEAVE)
                            .content(username + " went offline")
                            .timestamp(LocalDateTime.now())
                            .build();

                    messagingTemplate.convertAndSend("/topic/notifications", notification);
                } catch (Exception e) {
                    log.error("Error handling user disconnection: {}", e.getMessage());
                }
            }
        }
    }

    public Map<String, String> getConnectedUsers() {
        return new ConcurrentHashMap<>(sessionUserMap);
    }

    public boolean isUserOnline(String username) {
        return sessionUserMap.containsValue(username);
    }
}
