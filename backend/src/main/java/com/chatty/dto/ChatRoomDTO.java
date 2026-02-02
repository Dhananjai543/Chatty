package com.chatty.dto;

import com.chatty.entity.ChatRoom;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {

    private String id;

    @NotBlank(message = "Room name is required")
    @Size(min = 2, max = 50, message = "Room name must be between 2 and 50 characters")
    private String name;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    private List<String> memberIds;
    private int memberCount;
    
    @JsonProperty("isPublic")
    private boolean isPublic;
    
    private String secretCode;  // Only included for room creator/members
    private String createdBy;
    private String profilePicture;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    public static ChatRoomDTO fromEntity(ChatRoom chatRoom) {
        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .description(chatRoom.getDescription())
                .memberIds(chatRoom.getMemberIds())
                .memberCount(chatRoom.getMemberIds() != null ? chatRoom.getMemberIds().size() : 0)
                .isPublic(chatRoom.isPublic())
                .secretCode(chatRoom.getSecretCode())
                .createdBy(chatRoom.getCreatedBy())
                .profilePicture(chatRoom.getProfilePicture())
                .createdAt(chatRoom.getCreatedAt())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .build();
    }

    // Version that hides secret code (for public listings)
    public static ChatRoomDTO fromEntityWithoutSecretCode(ChatRoom chatRoom) {
        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .description(chatRoom.getDescription())
                .memberIds(chatRoom.getMemberIds())
                .memberCount(chatRoom.getMemberIds() != null ? chatRoom.getMemberIds().size() : 0)
                .isPublic(chatRoom.isPublic())
                .secretCode(null)
                .createdBy(chatRoom.getCreatedBy())
                .profilePicture(chatRoom.getProfilePicture())
                .createdAt(chatRoom.getCreatedAt())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .build();
    }

    // Version that includes secret code only if user is creator or member
    public static ChatRoomDTO fromEntityForUser(ChatRoom chatRoom, String userId) {
        boolean canSeeSecretCode = chatRoom.getCreatedBy().equals(userId) || 
                                   (chatRoom.getMemberIds() != null && chatRoom.getMemberIds().contains(userId));
        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .description(chatRoom.getDescription())
                .memberIds(chatRoom.getMemberIds())
                .memberCount(chatRoom.getMemberIds() != null ? chatRoom.getMemberIds().size() : 0)
                .isPublic(chatRoom.isPublic())
                .secretCode(canSeeSecretCode ? chatRoom.getSecretCode() : null)
                .createdBy(chatRoom.getCreatedBy())
                .profilePicture(chatRoom.getProfilePicture())
                .createdAt(chatRoom.getCreatedAt())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .build();
    }

    public ChatRoom toEntity() {
        return ChatRoom.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .memberIds(this.memberIds)
                .isPublic(this.isPublic)
                .secretCode(this.secretCode)
                .profilePicture(this.profilePicture)
                .build();
    }
}
