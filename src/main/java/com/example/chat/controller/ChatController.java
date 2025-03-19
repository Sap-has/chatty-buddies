// src/main/java/com/example/chat/controller/ChatController.java
package com.example.chat.controller;

import com.example.chat.model.Chat;
import com.example.chat.model.User;
import com.example.chat.repository.ChatRepository;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    // Create a new chat room. The authenticated user becomes the host.
    @PostMapping("/create")
    public Chat createChat(@RequestParam String chatName, Authentication auth) {
        User host = userRepository.findByUsername(auth.getName());
        Chat chat = new Chat();
        chat.setChatName(chatName);
        chat.setHost(host);
        // Generate a short random unique code for the chat.
        chat.setChatCode(UUID.randomUUID().toString().substring(0, 8));
        Set<User> members = new HashSet<>();
        members.add(host);
        chat.setMembers(members);
        return chatRepository.save(chat);
    }

    // Invite a user to a chat by username.
    @PostMapping("/{chatId}/invite")
    public String inviteUser(@PathVariable Long chatId, @RequestParam String username) {
        Chat chat = chatRepository.findById(chatId).orElse(null);
        if(chat == null) {
            return "Chat not found";
        }
        User user = userRepository.findByUsername(username);
        if(user == null) {
            return "User not found";
        }
        chat.getMembers().add(user);
        chatRepository.save(chat);
        return "User invited";
    }

    // Allow a user to leave a chat.
    @PostMapping("/{chatId}/leave")
    public String leaveChat(@PathVariable Long chatId, Authentication auth) {
        Chat chat = chatRepository.findById(chatId).orElse(null);
        if(chat == null) {
            return "Chat not found";
        }
        User user = userRepository.findByUsername(auth.getName());
        if(chat.getMembers().contains(user)) {
            chat.getMembers().remove(user);
            chatRepository.save(chat);
            return "Left chat successfully";
        }
        return "User not a member of the chat";
    }

    // Allow the host to delete the chat.
    @DeleteMapping("/{chatId}/delete")
    public String deleteChat(@PathVariable Long chatId, Authentication auth) {
        Chat chat = chatRepository.findById(chatId).orElse(null);
        if(chat == null) {
            return "Chat not found";
        }
        User user = userRepository.findByUsername(auth.getName());
        if(chat.getHost().getId().equals(user.getId())) {
            chatRepository.delete(chat);
            return "Chat deleted successfully";
        }
        return "Only host can delete the chat";
    }
}
