package com.chatty.dao;

import com.chatty.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    Optional<ChatRoom> findByName(String name);

    boolean existsByName(String name);

    // Find all public chat rooms
    List<ChatRoom> findByIsPublicTrue();

    // Find chat rooms where user is a member
    @Query("{ 'memberIds': ?0 }")
    List<ChatRoom> findByMemberId(String userId);

    // Find public rooms or rooms where user is a member
    @Query("{ $or: [ { 'isPublic': true }, { 'memberIds': ?0 } ] }")
    List<ChatRoom> findAccessibleRooms(String userId);

    // Find rooms created by a specific user
    List<ChatRoom> findByCreatedBy(String userId);
}
