package com.chatty.service;

import com.chatty.dao.UserRepository;
import com.chatty.dto.AuthResponse;
import com.chatty.entity.User;
import com.chatty.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<AuthResponse.UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    public AuthResponse.UserDTO getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return toUserDTO(user);
    }

    public AuthResponse.UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return toUserDTO(user);
    }

    public List<AuthResponse.UserDTO> getOnlineUsers() {
        return userRepository.findByStatus(User.UserStatus.ONLINE).stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    public void updateUserStatus(String userId, User.UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        user.setStatus(status);
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User {} status updated to {}", userId, status);
    }

    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    public User getUserEntityById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    private AuthResponse.UserDTO toUserDTO(User user) {
        return AuthResponse.UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .profilePicture(user.getProfilePicture())
                .build();
    }
}
