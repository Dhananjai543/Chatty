package com.chatty.controller;

import com.chatty.dto.ApiResponse;
import com.chatty.dto.AuthResponse;
import com.chatty.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthResponse.UserDTO>>> getAllUsers() {
        log.info("Get all users request");
        List<AuthResponse.UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AuthResponse.UserDTO>> getUserById(@PathVariable String userId) {
        log.info("Get user by id request: {}", userId);
        AuthResponse.UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<AuthResponse.UserDTO>> getUserByUsername(@PathVariable String username) {
        log.info("Get user by username request: {}", username);
        AuthResponse.UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/online")
    public ResponseEntity<ApiResponse<List<AuthResponse.UserDTO>>> getOnlineUsers() {
        log.info("Get online users request");
        List<AuthResponse.UserDTO> users = userService.getOnlineUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserDTO>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get current user request");
        AuthResponse.UserDTO user = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
