package com.chatty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class ChattyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChattyApplication.class, args);
    }
}
