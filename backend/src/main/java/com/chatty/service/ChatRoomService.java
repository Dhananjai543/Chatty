package com.chatty.service;

import com.chatty.dao.ChatRoomRepository;
import com.chatty.dto.ChatRoomDTO;
import com.chatty.entity.ChatRoom;
import com.chatty.exception.ChatRoomNotFoundException;
import com.chatty.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @PostConstruct
    public void init() {
        // Create default "General" chat room if it doesn't exist
        if (!chatRoomRepository.existsByName("General")) {
            ChatRoom generalRoom = ChatRoom.builder()
                    .name("General")
                    .description("General chat room for everyone")
                    .isPublic(true)
                    .createdBy("system")
                    .createdAt(LocalDateTime.now())
                    .build();
            chatRoomRepository.save(generalRoom);
            log.info("Created default 'General' chat room");
        }
    }

    public List<ChatRoomDTO> getAllPublicRooms() {
        return chatRoomRepository.findByIsPublicTrue().stream()
                .map(ChatRoomDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ChatRoomDTO> getUserAccessibleRooms(String userId) {
        return chatRoomRepository.findAccessibleRooms(userId).stream()
                .map(ChatRoomDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ChatRoomDTO getRoomById(String roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found with id: " + roomId));
        return ChatRoomDTO.fromEntity(room);
    }

    public ChatRoomDTO getRoomByName(String name) {
        ChatRoom room = chatRoomRepository.findByName(name)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found with name: " + name));
        return ChatRoomDTO.fromEntity(room);
    }

    @Transactional
    public ChatRoomDTO createRoom(ChatRoomDTO request, String creatorId) {
        if (chatRoomRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Chat room with name '" + request.getName() + "' already exists");
        }

        ChatRoom room = ChatRoom.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isPublic(request.isPublic())
                .createdBy(creatorId)
                .createdAt(LocalDateTime.now())
                .build();

        room.addMember(creatorId);
        ChatRoom savedRoom = chatRoomRepository.save(room);
        
        log.info("Chat room '{}' created by user {}", savedRoom.getName(), creatorId);
        return ChatRoomDTO.fromEntity(savedRoom);
    }

    @Transactional
    public ChatRoomDTO joinRoom(String roomId, String userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found with id: " + roomId));

        room.addMember(userId);
        ChatRoom savedRoom = chatRoomRepository.save(room);
        
        log.info("User {} joined room {}", userId, room.getName());
        return ChatRoomDTO.fromEntity(savedRoom);
    }

    @Transactional
    public ChatRoomDTO leaveRoom(String roomId, String userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found with id: " + roomId));

        room.removeMember(userId);
        ChatRoom savedRoom = chatRoomRepository.save(room);
        
        log.info("User {} left room {}", userId, room.getName());
        return ChatRoomDTO.fromEntity(savedRoom);
    }

    @Transactional
    public void updateLastMessage(String roomId, String messageId, LocalDateTime timestamp) {
        chatRoomRepository.findById(roomId).ifPresent(room -> {
            room.setLastMessageId(messageId);
            room.setLastMessageAt(timestamp);
            chatRoomRepository.save(room);
        });
    }

    public ChatRoom getRoomEntityById(String roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found with id: " + roomId));
    }

    public boolean isUserInRoom(String roomId, String userId) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room == null) return false;
        return room.isPublic() || room.hasMember(userId);
    }
}
