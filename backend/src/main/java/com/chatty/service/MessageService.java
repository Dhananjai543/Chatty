package com.chatty.service;

import com.chatty.dao.MessageRepository;
import com.chatty.dto.MessageDTO;
import com.chatty.entity.Message;
import com.chatty.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService;
    private final ChatRoomService chatRoomService;

    @Value("${redis.cache.message-ttl:3600}")
    private long messageTtl;

    @Value("${redis.cache.max-messages:50}")
    private int maxCachedMessages;

    private static final String ROOM_MESSAGES_KEY = "chat:room:%s:messages";
    private static final String PRIVATE_MESSAGES_KEY = "chat:private:%s:%s:messages";

    public Message savePublicMessage(MessageDTO messageDTO, String senderUsername) {
        User sender = userService.getUserEntityByUsername(senderUsername);

        Message message = Message.builder()
                .senderId(sender.getId())
                .senderUsername(sender.getUsername())
                .senderDisplayName(sender.getDisplayName())
                .chatRoomId(messageDTO.getChatRoomId())
                .content(messageDTO.getContent())
                .messageType(messageDTO.getMessageType() != null ? messageDTO.getMessageType() : Message.MessageType.TEXT)
                .timestamp(LocalDateTime.now())
                .isPrivate(false)
                .build();

        Message savedMessage = messageRepository.save(message);

        // Update chat room last message
        chatRoomService.updateLastMessage(messageDTO.getChatRoomId(), savedMessage.getId(), savedMessage.getTimestamp());

        // Cache the message
        cacheRoomMessage(messageDTO.getChatRoomId(), savedMessage);

        log.debug("Saved public message in room {}", messageDTO.getChatRoomId());
        return savedMessage;
    }

    public Message savePrivateMessage(MessageDTO messageDTO, String senderUsername) {
        User sender = userService.getUserEntityByUsername(senderUsername);
        User recipient = userService.getUserEntityById(messageDTO.getRecipientId());

        Message message = Message.builder()
                .senderId(sender.getId())
                .senderUsername(sender.getUsername())
                .senderDisplayName(sender.getDisplayName())
                .recipientId(recipient.getId())
                .recipientUsername(recipient.getUsername())
                .content(messageDTO.getContent())
                .messageType(messageDTO.getMessageType() != null ? messageDTO.getMessageType() : Message.MessageType.TEXT)
                .timestamp(LocalDateTime.now())
                .isPrivate(true)
                .isRead(false)
                .build();

        Message savedMessage = messageRepository.save(message);

        // Cache the message
        cachePrivateMessage(sender.getId(), recipient.getId(), savedMessage);

        log.debug("Saved private message from {} to {}", sender.getUsername(), recipient.getUsername());
        return savedMessage;
    }

    public List<MessageDTO> getRoomMessages(String roomId, int page, int size) {
        // Try to get from cache first (only for first page)
        if (page == 0) {
            List<MessageDTO> cachedMessages = getCachedRoomMessages(roomId);
            if (!cachedMessages.isEmpty()) {
                log.debug("Returning cached messages for room {}", roomId);
                return cachedMessages;
            }
        }

        // Get from database
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<Message> messagesPage = messageRepository.findByChatRoomIdOrderByTimestampDesc(roomId, pageRequest);

        List<MessageDTO> messages = messagesPage.getContent().stream()
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());

        // Cache first page results
        if (page == 0 && !messages.isEmpty()) {
            cacheRoomMessages(roomId, messages);
        }

        // Reverse to get chronological order
        Collections.reverse(messages);
        return messages;
    }

    public List<MessageDTO> getPrivateMessages(String userId1, String userId2, int page, int size) {
        // Try to get from cache first (only for first page)
        if (page == 0) {
            List<MessageDTO> cachedMessages = getCachedPrivateMessages(userId1, userId2);
            if (!cachedMessages.isEmpty()) {
                log.debug("Returning cached private messages between {} and {}", userId1, userId2);
                return cachedMessages;
            }
        }

        // Get from database
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<Message> messagesPage = messageRepository.findPrivateMessagesBetweenUsersPaged(userId1, userId2, pageRequest);

        List<MessageDTO> messages = messagesPage.getContent().stream()
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());

        // Cache first page results
        if (page == 0 && !messages.isEmpty()) {
            cachePrivateMessages(userId1, userId2, messages);
        }

        // Reverse to get chronological order
        Collections.reverse(messages);
        return messages;
    }

    public void markMessagesAsRead(String recipientId, String senderId) {
        List<Message> unreadMessages = messageRepository.findByRecipientIdAndIsReadFalse(recipientId);
        unreadMessages.stream()
                .filter(m -> m.getSenderId().equals(senderId))
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });
        log.debug("Marked messages as read for recipient {} from sender {}", recipientId, senderId);
    }

    public long getUnreadCount(String userId) {
        return messageRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    // Redis caching methods
    private void cacheRoomMessage(String roomId, Message message) {
        try {
            String key = String.format(ROOM_MESSAGES_KEY, roomId);
            redisTemplate.opsForList().rightPush(key, MessageDTO.fromEntity(message));
            redisTemplate.opsForList().trim(key, -maxCachedMessages, -1);
            redisTemplate.expire(key, Duration.ofSeconds(messageTtl));
        } catch (Exception e) {
            log.warn("Failed to cache room message: {}", e.getMessage());
        }
    }

    private void cacheRoomMessages(String roomId, List<MessageDTO> messages) {
        try {
            String key = String.format(ROOM_MESSAGES_KEY, roomId);
            redisTemplate.delete(key);
            for (MessageDTO message : messages) {
                redisTemplate.opsForList().rightPush(key, message);
            }
            redisTemplate.expire(key, Duration.ofSeconds(messageTtl));
        } catch (Exception e) {
            log.warn("Failed to cache room messages: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<MessageDTO> getCachedRoomMessages(String roomId) {
        try {
            String key = String.format(ROOM_MESSAGES_KEY, roomId);
            List<Object> cached = redisTemplate.opsForList().range(key, 0, -1);
            if (cached != null && !cached.isEmpty()) {
                return cached.stream()
                        .map(obj -> (MessageDTO) obj)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Failed to get cached room messages: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private void cachePrivateMessage(String userId1, String userId2, Message message) {
        try {
            String key = getPrivateMessageKey(userId1, userId2);
            redisTemplate.opsForList().rightPush(key, MessageDTO.fromEntity(message));
            redisTemplate.opsForList().trim(key, -maxCachedMessages, -1);
            redisTemplate.expire(key, Duration.ofSeconds(messageTtl));
        } catch (Exception e) {
            log.warn("Failed to cache private message: {}", e.getMessage());
        }
    }

    private void cachePrivateMessages(String userId1, String userId2, List<MessageDTO> messages) {
        try {
            String key = getPrivateMessageKey(userId1, userId2);
            redisTemplate.delete(key);
            for (MessageDTO message : messages) {
                redisTemplate.opsForList().rightPush(key, message);
            }
            redisTemplate.expire(key, Duration.ofSeconds(messageTtl));
        } catch (Exception e) {
            log.warn("Failed to cache private messages: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<MessageDTO> getCachedPrivateMessages(String userId1, String userId2) {
        try {
            String key = getPrivateMessageKey(userId1, userId2);
            List<Object> cached = redisTemplate.opsForList().range(key, 0, -1);
            if (cached != null && !cached.isEmpty()) {
                return cached.stream()
                        .map(obj -> (MessageDTO) obj)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Failed to get cached private messages: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private String getPrivateMessageKey(String userId1, String userId2) {
        // Sort user IDs to ensure consistent key regardless of sender/recipient
        String sortedId1 = userId1.compareTo(userId2) < 0 ? userId1 : userId2;
        String sortedId2 = userId1.compareTo(userId2) < 0 ? userId2 : userId1;
        return String.format(PRIVATE_MESSAGES_KEY, sortedId1, sortedId2);
    }
}
