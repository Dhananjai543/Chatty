package com.chatty.service;

import com.chatty.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageService {

    private final KafkaTemplate<String, MessageDTO> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${kafka.topics.public-messages}")
    private String publicMessagesTopic;

    @Value("${kafka.topics.private-messages}")
    private String privateMessagesTopic;

    @Value("${kafka.topics.notifications}")
    private String notificationsTopic;

    public void sendPublicMessage(MessageDTO message) {
        log.debug("Sending public message to Kafka topic: {}", publicMessagesTopic);
        kafkaTemplate.send(publicMessagesTopic, message.getChatRoomId(), message);
    }

    public void sendPrivateMessage(MessageDTO message) {
        log.debug("Sending private message to Kafka topic: {}", privateMessagesTopic);
        kafkaTemplate.send(privateMessagesTopic, message.getRecipientId(), message);
    }

    public void sendNotification(MessageDTO notification) {
        log.debug("Sending notification to Kafka topic: {}", notificationsTopic);
        kafkaTemplate.send(notificationsTopic, notification.getSenderId(), notification);
    }

    @KafkaListener(
            topics = "${kafka.topics.public-messages}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePublicMessage(MessageDTO message) {
        log.debug("Consumed public message from Kafka for room: {}", message.getChatRoomId());
        
        // Broadcast to all subscribers of this room
        String destination = "/topic/public." + message.getChatRoomId();
        messagingTemplate.convertAndSend(destination, message);
    }

    @KafkaListener(
            topics = "${kafka.topics.private-messages}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePrivateMessage(MessageDTO message) {
        log.debug("Consumed private message from Kafka for user: {}", message.getRecipientUsername());
        
        // Send to specific user's private queue (use username to match Principal)
        messagingTemplate.convertAndSendToUser(
                message.getRecipientUsername(),
                "/queue/private",
                message
        );
        
        // Also send to sender so they see their own message
        messagingTemplate.convertAndSendToUser(
                message.getSenderUsername(),
                "/queue/private",
                message
        );
    }

    @KafkaListener(
            topics = "${kafka.topics.notifications}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotification(MessageDTO notification) {
        log.debug("Consumed notification from Kafka");
        
        // Broadcast notifications to all connected users
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
}
