package com.chatty.controller;

import com.chatty.dto.ApiResponse;
import com.chatty.dto.ChatRoomDTO;
import com.chatty.dto.JoinByCodeRequest;
import com.chatty.dto.MessageDTO;
import com.chatty.entity.User;
import com.chatty.service.ChatRoomService;
import com.chatty.service.MessageService;
import com.chatty.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final UserService userService;

    // Chat Room Endpoints

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomDTO>>> getAllRooms(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get all rooms request from user: {}", userDetails.getUsername());
        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        List<ChatRoomDTO> rooms = chatRoomService.getUserAccessibleRooms(user.getId());
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    @GetMapping("/rooms/public")
    public ResponseEntity<ApiResponse<List<ChatRoomDTO>>> getPublicRooms() {
        log.info("Get public rooms request");
        List<ChatRoomDTO> rooms = chatRoomService.getAllPublicRooms();
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDTO>> getRoomById(@PathVariable String roomId) {
        log.info("Get room by id request: {}", roomId);
        ChatRoomDTO room = chatRoomService.getRoomById(roomId);
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomDTO>> createRoom(
            @Valid @RequestBody ChatRoomDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Create room request from user: {}", userDetails.getUsername());
        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        ChatRoomDTO room = chatRoomService.createRoom(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room created successfully", room));
    }

    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<ApiResponse<ChatRoomDTO>> joinRoom(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Join room request for room {} from user: {}", roomId, userDetails.getUsername());
        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        ChatRoomDTO room = chatRoomService.joinRoom(roomId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Joined room successfully", room));
    }

    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<ApiResponse<ChatRoomDTO>> leaveRoom(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Leave room request for room {} from user: {}", roomId, userDetails.getUsername());
        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        ChatRoomDTO room = chatRoomService.leaveRoom(roomId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Left room successfully", room));
    }

    @PostMapping("/rooms/join-by-code")
    public ResponseEntity<ApiResponse<ChatRoomDTO>> joinRoomByCode(
            @Valid @RequestBody JoinByCodeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Join room by code request from user: {}", userDetails.getUsername());
        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        ChatRoomDTO room = chatRoomService.joinRoomByCode(request.getSecretCode(), user.getId());
        return ResponseEntity.ok(ApiResponse.success("Joined room successfully", room));
    }

    // Message Endpoints

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getRoomMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        log.info("Get messages for room: {}", roomId);
        List<MessageDTO> messages = messageService.getRoomMessages(roomId, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/private/{userId}/messages")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getPrivateMessages(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get private messages between {} and {}", userDetails.getUsername(), userId);
        User currentUser = userService.getUserEntityByUsername(userDetails.getUsername());
        List<MessageDTO> messages = messageService.getPrivateMessages(currentUser.getId(), userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/private/{userId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @PathVariable String userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Mark messages as read from {} by {}", userId, userDetails.getUsername());
        User currentUser = userService.getUserEntityByUsername(userDetails.getUsername());
        messageService.markMessagesAsRead(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("Messages marked as read", null));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get unread count for user: {}", userDetails.getUsername());
        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        long count = messageService.getUnreadCount(user.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
