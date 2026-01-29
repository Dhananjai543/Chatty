package com.chatty.dao;

import com.chatty.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    // Find messages by chat room, ordered by timestamp descending
    List<Message> findByChatRoomIdOrderByTimestampDesc(String chatRoomId);

    Page<Message> findByChatRoomIdOrderByTimestampDesc(String chatRoomId, Pageable pageable);

    // Find private messages between two users
    @Query("{ $or: [ " +
            "{ 'senderId': ?0, 'recipientId': ?1, 'isPrivate': true }, " +
            "{ 'senderId': ?1, 'recipientId': ?0, 'isPrivate': true } " +
            "], $orderby: { 'timestamp': -1 } }")
    List<Message> findPrivateMessagesBetweenUsers(String userId1, String userId2);

    @Query("{ $or: [ " +
            "{ 'senderId': ?0, 'recipientId': ?1, 'isPrivate': true }, " +
            "{ 'senderId': ?1, 'recipientId': ?0, 'isPrivate': true } " +
            "] }")
    Page<Message> findPrivateMessagesBetweenUsersPaged(String userId1, String userId2, Pageable pageable);

    // Find messages by sender
    List<Message> findBySenderIdOrderByTimestampDesc(String senderId);

    // Find unread messages for a user
    List<Message> findByRecipientIdAndIsReadFalse(String recipientId);

    // Count unread messages for a user
    long countByRecipientIdAndIsReadFalse(String recipientId);

    // Find messages after a certain timestamp
    List<Message> findByChatRoomIdAndTimestampAfterOrderByTimestampAsc(String chatRoomId, LocalDateTime timestamp);

    // Delete messages older than a certain date
    void deleteByTimestampBefore(LocalDateTime timestamp);
}
