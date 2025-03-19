// src/main/java/com/example/chat/controller/ChatMessageController.java
package com.example.chat.controller;

import com.example.chat.dto.ChatMessage;
import com.example.chat.model.Chat;
import com.example.chat.model.Message;
import com.example.chat.model.User;
import com.example.chat.repository.ChatRepository;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatMessageController {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Receives messages from clients at "/app/chat.sendMessage".
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage chatMessage, Authentication auth) {
        // Get the sender based on the authentication details.
        User sender = userRepository.findByUsername(auth.getName());
        // Retrieve the chat room by its unique code.
        Chat chat = chatRepository.findByChatCode(chatMessage.getChatCode());
        if(chat != null) {
            // Save the message to the database.
            Message message = new Message();
            message.setChat(chat);
            message.setSender(sender);
            message.setContent(chatMessage.getContent());
            message.setTimestamp(LocalDateTime.now());
            messageRepository.save(message);

            // Broadcast the message to all subscribers of the chat room.
            messagingTemplate.convertAndSend("/topic/chat/" + chat.getChatCode(), chatMessage);
        }
    }
}
