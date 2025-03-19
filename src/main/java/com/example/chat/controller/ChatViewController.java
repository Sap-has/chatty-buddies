package com.example.chat.controller;

import com.example.chat.model.Chat;
import com.example.chat.model.ChatInvitation;
import com.example.chat.model.User;
import com.example.chat.repository.ChatInvitationRepository;
import com.example.chat.repository.ChatRepository;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ChatViewController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatInvitationRepository invitationRepository;

    // Dashboard view - shows user's chats and friends
    @GetMapping("/chats")
    public String viewChats(Model model, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName());
        
        // Get all chats where the user is a member
        List<Chat> userChats = chatRepository.findByMembersContaining(user);
        
        // Get all users except the current user (potential friends)
        List<User> potentialFriends = userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equals(auth.getName()))
                .collect(Collectors.toList());
        
        // Get all pending invitations for this user
        List<ChatInvitation> pendingInvitations = invitationRepository.findByInviteeAndStatus(
                user, ChatInvitation.InvitationStatus.PENDING);
        
        model.addAttribute("userChats", userChats);
        model.addAttribute("hostedChats", chatRepository.findByHost(user));
        model.addAttribute("potentialFriends", potentialFriends);
        model.addAttribute("pendingInvitations", pendingInvitations);
        
        return "dashboard";
    }

    // View a specific chat room
    @GetMapping("/chats/{chatCode}")
    public String viewChat(@PathVariable String chatCode, Model model, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName());
        Chat chat = chatRepository.findByChatCode(chatCode);
        
        if (chat == null) {
            return "redirect:/chats?error=Chat+not+found";
        }
        
        // Check if user is a member of this chat
        if (!chat.getMembers().contains(user)) {
            return "redirect:/chats?error=Not+authorized";
        }
        
        model.addAttribute("chat", chat);
        model.addAttribute("isHost", chat.getHost().getId().equals(user.getId()));
        model.addAttribute("currentUser", user);
        
        return "chatroom";
    }

    // Form to create a new chat
    @GetMapping("/chats/new")
    public String newChatForm() {
        return "new-chat";
    }

    // Form to edit a chat (host only)
    @GetMapping("/chats/{chatId}/edit")
    public String editChatForm(@PathVariable Long chatId, Model model, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName());
        Chat chat = chatRepository.findById(chatId).orElse(null);
        
        if (chat == null) {
            return "redirect:/chats?error=Chat+not+found";
        }
        
        // Check if user is the host
        if (!chat.getHost().getId().equals(user.getId())) {
            return "redirect:/chats?error=Not+authorized";
        }
        
        model.addAttribute("chat", chat);
        
        return "edit-chat";
    }
}