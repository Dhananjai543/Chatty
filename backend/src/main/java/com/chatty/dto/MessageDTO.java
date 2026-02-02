package com.chatty.dto;

import com.chatty.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String senderId;
    private String senderUsername;
    private String senderDisplayName;
    private String senderProfilePicture;
    private String recipientId;
    private String recipientUsername;
    private String chatRoomId;
    private String content;
    private Message.MessageType messageType;
    private LocalDateTime timestamp;
    private boolean isPrivate;
    private boolean isRead;

    public static MessageDTO fromEntity(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .senderUsername(message.getSenderUsername())
                .senderDisplayName(message.getSenderDisplayName())
                .senderProfilePicture(message.getSenderProfilePicture())
                .recipientId(message.getRecipientId())
                .recipientUsername(message.getRecipientUsername())
                .chatRoomId(message.getChatRoomId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .timestamp(message.getTimestamp())
                .isPrivate(message.isPrivate())
                .isRead(message.isRead())
                .build();
    }

    public Message toEntity() {
        return Message.builder()
                .id(this.id)
                .senderId(this.senderId)
                .senderUsername(this.senderUsername)
                .senderDisplayName(this.senderDisplayName)
                .senderProfilePicture(this.senderProfilePicture)
                .recipientId(this.recipientId)
                .recipientUsername(this.recipientUsername)
                .chatRoomId(this.chatRoomId)
                .content(this.content)
                .messageType(this.messageType != null ? this.messageType : Message.MessageType.TEXT)
                .timestamp(this.timestamp != null ? this.timestamp : LocalDateTime.now())
                .isPrivate(this.isPrivate)
                .isRead(this.isRead)
                .build();
    }
}
