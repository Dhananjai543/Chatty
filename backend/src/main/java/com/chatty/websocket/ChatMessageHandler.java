package com.chatty.websocket;

import com.chatty.dto.MessageDTO;
import com.chatty.entity.Message;
import com.chatty.service.KafkaMessageService;
import com.chatty.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageHandler {

    private final MessageService messageService;
    private final KafkaMessageService kafkaMessageService;

    /**
     * Handle public chat messages
     * Client sends to: /app/chat.public.{roomId}
     * Broadcasts to: /topic/public.{roomId}
     */
    @MessageMapping("/chat.public.{roomId}")
    public void handlePublicMessage(
            @DestinationVariable String roomId,
            @Payload MessageDTO messageDTO,
            Principal principal) {
        
        log.debug("Received public message for room {} from user {}", roomId, principal.getName());

        messageDTO.setChatRoomId(roomId);
        
        // Save to MongoDB
        Message savedMessage = messageService.savePublicMessage(messageDTO, principal.getName());
        
        // Create response DTO with saved data
        MessageDTO responseDTO = MessageDTO.fromEntity(savedMessage);
        
        // Send to Kafka for distribution
        kafkaMessageService.sendPublicMessage(responseDTO);
    }

    /**
     * Handle private chat messages
     * Client sends to: /app/chat.private.{recipientId}
     * Delivers to: /user/{recipientId}/queue/private
     */
    @MessageMapping("/chat.private.{recipientId}")
    public void handlePrivateMessage(
            @DestinationVariable String recipientId,
            @Payload MessageDTO messageDTO,
            Principal principal) {
        
        log.debug("Received private message for user {} from user {}", recipientId, principal.getName());

        messageDTO.setRecipientId(recipientId);
        messageDTO.setPrivate(true);
        
        // Save to MongoDB
        Message savedMessage = messageService.savePrivateMessage(messageDTO, principal.getName());
        
        // Create response DTO with saved data
        MessageDTO responseDTO = MessageDTO.fromEntity(savedMessage);
        
        // Send to Kafka for distribution
        kafkaMessageService.sendPrivateMessage(responseDTO);
    }

    /**
     * Handle typing indicator
     * Client sends to: /app/chat.typing.{roomId}
     */
    @MessageMapping("/chat.typing.{roomId}")
    public void handleTypingIndicator(
            @DestinationVariable String roomId,
            @Payload MessageDTO notification,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        
        log.debug("Typing indicator for room {} from user {}", roomId, principal.getName());

        notification.setMessageType(Message.MessageType.SYSTEM);
        notification.setChatRoomId(roomId);
        notification.setSenderUsername(principal.getName());
        
        // Send notification through Kafka
        kafkaMessageService.sendNotification(notification);
    }
}
