package com.chatty.dto;

import com.chatty.entity.ChatRoom;
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
    private boolean isPublic;
    private String createdBy;
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
                .createdBy(chatRoom.getCreatedBy())
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
                .build();
    }
}
