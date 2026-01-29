package com.chatty.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
@CompoundIndexes({
    @CompoundIndex(name = "chatroom_timestamp", def = "{'chatRoomId': 1, 'timestamp': -1}"),
    @CompoundIndex(name = "private_chat", def = "{'senderId': 1, 'recipientId': 1, 'timestamp': -1}")
})
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Indexed
    private String senderId;

    private String senderUsername;

    private String senderDisplayName;

    @Indexed
    private String recipientId;

    private String recipientUsername;

    @Indexed
    private String chatRoomId;

    private String content;

    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @CreatedDate
    private LocalDateTime timestamp;

    @Builder.Default
    private boolean isPrivate = false;

    @Builder.Default
    private boolean isRead = false;

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        SYSTEM,
        JOIN,
        LEAVE
    }
}
