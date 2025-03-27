package com.example.chat.controller;

import com.example.chat.model.Chat;
import com.example.chat.model.ChatInvitation;
import com.example.chat.model.User;
import com.example.chat.repository.ChatInvitationRepository;
import com.example.chat.repository.ChatRepository;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ChatInvitationRepository invitationRepository;

    // Create a new chat room. The authenticated user becomes the host.
    @PostMapping("/create")
    public ResponseEntity<?> createChat(@RequestParam String chatName, Authentication auth) {
        User host = userRepository.findByUsername(auth.getName());
        Chat chat = new Chat();
        chat.setChatName(chatName);
        chat.setHost(host);
        // Generate a short random unique code for the chat.
        chat.setChatCode(UUID.randomUUID().toString().substring(0, 8));
        Set<User> members = new HashSet<>();
        members.add(host);
        chat.setMembers(members);
        chatRepository.save(chat);

        Map<String, String> response = new HashMap<>();
        response.put("chatCode", chat.getChatCode());
        response.put("chatName", chat.getChatName());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Update chat name (host only)
    @PutMapping("/{chatId}/name")
    public ResponseEntity<?> updateChatName(
            @PathVariable Long chatId, 
            @RequestParam String newName, 
            Authentication auth) {
        
        Chat chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chat not found");
        }
        
        User user = userRepository.findByUsername(auth.getName());
        if (!chat.getHost().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only host can update chat name");
        }
        
        chat.setChatName(newName);
        chatRepository.save(chat);
        
        return ResponseEntity.ok(chat);
    }

    // Invite a user to a chat by username (host only).
    @PostMapping("/{chatId}/invite")
    public ResponseEntity<?> inviteUser(
            @PathVariable Long chatId, 
            @RequestParam String username, 
            Authentication auth) {
        
        Chat chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chat not found");
        }
        
        User host = userRepository.findByUsername(auth.getName());
        if (!chat.getHost().getId().equals(host.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only host can invite users");
        }
        
        User invitee = userRepository.findByUsername(username);
        if (invitee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        
        // Check if user is already a member
        if (chat.getMembers().contains(invitee)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is already a member");
        }
        
        // Check if invitation already exists
        ChatInvitation existingInvitation = invitationRepository.findByInviteeAndChatAndStatus(
                invitee, chat, ChatInvitation.InvitationStatus.PENDING);
        
        if (existingInvitation != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invitation already sent");
        }
        
        // Create and save invitation
        ChatInvitation invitation = new ChatInvitation();
        invitation.setChat(chat);
        invitation.setInviter(host);
        invitation.setInvitee(invitee);
        invitationRepository.save(invitation);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Invitation sent successfully");
        response.put("invitation", invitation);
        
        return ResponseEntity.ok(response);
    }
    
    // Accept an invitation
    @PostMapping("/invitation/{invitationId}/accept")
    public ResponseEntity<?> acceptInvitation(
            @PathVariable Long invitationId, 
            Authentication auth) {
        
        ChatInvitation invitation = invitationRepository.findById(invitationId).orElse(null);
        if (invitation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invitation not found");
        }
        
        User user = userRepository.findByUsername(auth.getName());
        if (!invitation.getInvitee().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized");
        }
        
        if (invitation.getStatus() != ChatInvitation.InvitationStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invitation already processed");
        }
        
        // Add user to chat members
        Chat chat = invitation.getChat();
        chat.getMembers().add(user);
        chatRepository.save(chat);
        
        // Update invitation status
        invitation.setStatus(ChatInvitation.InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
        
        return ResponseEntity.ok("Invitation accepted");
    }
    
    // Decline an invitation
    @PostMapping("/invitation/{invitationId}/decline")
    public ResponseEntity<?> declineInvitation(
            @PathVariable Long invitationId, 
            Authentication auth) {
        
        ChatInvitation invitation = invitationRepository.findById(invitationId).orElse(null);
        if (invitation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invitation not found");
        }
        
        User user = userRepository.findByUsername(auth.getName());
        if (!invitation.getInvitee().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized");
        }
        
        if (invitation.getStatus() != ChatInvitation.InvitationStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invitation already processed");
        }
        
        // Update invitation status
        invitation.setStatus(ChatInvitation.InvitationStatus.DECLINED);
        invitationRepository.save(invitation);
        
        return ResponseEntity.ok("Invitation declined");
    }

    // Remove a member from chat (host only).
    @DeleteMapping("/{chatId}/members/{username}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long chatId, 
            @PathVariable String username, 
            Authentication auth) {
        
        Chat chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chat not found");
        }
        
        User host = userRepository.findByUsername(auth.getName());
        if (!chat.getHost().getId().equals(host.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only host can remove members");
        }
        
        User memberToRemove = userRepository.findByUsername(username);
        if (memberToRemove == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        
        // Can't remove the host
        if (memberToRemove.getId().equals(host.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Host cannot be removed");
        }
        
        // Check if user is a member
        if (!chat.getMembers().contains(memberToRemove)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not a member");
        }
        
        chat.getMembers().remove(memberToRemove);
        chatRepository.save(chat);
        
        return ResponseEntity.ok("Member removed successfully");
    }

    // Allow a user to leave a chat.
    @PostMapping("/{chatId}/leave")
    public ResponseEntity<?> leaveChat(@PathVariable Long chatId, Authentication auth) {
        Chat chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chat not found");
        }
        
        User user = userRepository.findByUsername(auth.getName());
        
        // Host can't leave their own chat
        if (chat.getHost().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Host cannot leave chat. You must delete it or transfer ownership.");
        }
        
        if (!chat.getMembers().contains(user)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not a member");
        }
        
        chat.getMembers().remove(user);
        chatRepository.save(chat);
        
        return ResponseEntity.ok("Left chat successfully");
    }

    // Allow the host to delete the chat.
    @DeleteMapping("/{chatId}")
    public ResponseEntity<?> deleteChat(@PathVariable Long chatId, Authentication auth) {
        Chat chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chat not found");
        }
        
        User user = userRepository.findByUsername(auth.getName());
        if (!chat.getHost().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only host can delete the chat");
        }
        
        chatRepository.delete(chat);
        
        return ResponseEntity.ok("Chat deleted successfully");
    }
    
    // Get all chats for the current user
    @GetMapping("/my-chats")
    public ResponseEntity<?> getMyChats(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName());
        return ResponseEntity.ok(chatRepository.findByMembersContaining(user));
    }
    
    // Get all chats hosted by the current user
    @GetMapping("/my-hosted-chats")
    public ResponseEntity<?> getMyHostedChats(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName());
        return ResponseEntity.ok(chatRepository.findByHost(user));
    }
    
    // Get all pending invitations for the current user
    @GetMapping("/my-invitations")
    public ResponseEntity<?> getMyInvitations(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName());
        return ResponseEntity.ok(invitationRepository.findByInviteeAndStatus(
                user, ChatInvitation.InvitationStatus.PENDING));
    }
}