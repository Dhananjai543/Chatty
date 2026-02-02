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
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final AvatarService avatarService;
    
    private static final String SECRET_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int SECRET_CODE_LENGTH = 8;
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a random 8-character alphanumeric secret code.
     * Excludes similar-looking characters (0, O, 1, I) for clarity.
     */
    private String generateSecretCode() {
        StringBuilder code = new StringBuilder(SECRET_CODE_LENGTH);
        for (int i = 0; i < SECRET_CODE_LENGTH; i++) {
            code.append(SECRET_CODE_CHARS.charAt(secureRandom.nextInt(SECRET_CODE_CHARS.length())));
        }
        return code.toString();
    }

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
                .map(ChatRoomDTO::fromEntityWithoutSecretCode)
                .collect(Collectors.toList());
    }

    private static final String GENERAL_ROOM_NAME = "General";

    public List<ChatRoomDTO> getUserAccessibleRooms(String userId) {
        // Get rooms where user is a member
        List<ChatRoom> memberRooms = chatRoomRepository.findByMemberId(userId);
        
        // Always include the General room for all users
        Optional<ChatRoom> generalRoom = chatRoomRepository.findByName(GENERAL_ROOM_NAME);
        
        List<ChatRoom> accessibleRooms = new ArrayList<>(memberRooms);
        
        // Add General room if not already in the list
        if (generalRoom.isPresent()) {
            boolean hasGeneralRoom = memberRooms.stream()
                    .anyMatch(room -> GENERAL_ROOM_NAME.equals(room.getName()));
            if (!hasGeneralRoom) {
                accessibleRooms.add(0, generalRoom.get()); // Add at the beginning
            }
        }
        
        return accessibleRooms.stream()
                .map(room -> ChatRoomDTO.fromEntityForUser(room, userId))
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

        // Generate secret code for private rooms
        String secretCode = null;
        if (!request.isPublic()) {
            secretCode = generateSecretCode();
            // Ensure uniqueness (very unlikely collision, but let's be safe)
            while (chatRoomRepository.findBySecretCode(secretCode).isPresent()) {
                secretCode = generateSecretCode();
            }
        }

        ChatRoom room = ChatRoom.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isPublic(request.isPublic())
                .secretCode(secretCode)
                .createdBy(creatorId)
                .profilePicture(avatarService.getRandomGroupAvatar())
                .createdAt(LocalDateTime.now())
                .build();

        room.addMember(creatorId);
        ChatRoom savedRoom = chatRoomRepository.save(room);
        
        log.info("Chat room '{}' created by user {} (public: {}, secretCode: {})", 
                savedRoom.getName(), creatorId, savedRoom.isPublic(), 
                secretCode != null ? "generated" : "none");
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
    public ChatRoomDTO joinRoomByCode(String secretCode, String userId) {
        ChatRoom room = chatRoomRepository.findBySecretCode(secretCode.toUpperCase())
                .orElseThrow(() -> new ChatRoomNotFoundException("Invalid secret code. No room found."));

        if (room.hasMember(userId)) {
            log.info("User {} is already a member of room {}", userId, room.getName());
            return ChatRoomDTO.fromEntity(room);
        }

        room.addMember(userId);
        ChatRoom savedRoom = chatRoomRepository.save(room);
        
        log.info("User {} joined private room {} using secret code", userId, room.getName());
        return ChatRoomDTO.fromEntity(savedRoom);
    }

    public ChatRoomDTO getRoomByIdForUser(String roomId, String userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found with id: " + roomId));
        return ChatRoomDTO.fromEntityForUser(room, userId);
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
        // General room is accessible to all users
        if (GENERAL_ROOM_NAME.equals(room.getName())) {
            return true;
        }
        // Other rooms require membership
        return room.hasMember(userId);
    }
}
