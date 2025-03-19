package com.example.chat.repository;

import com.example.chat.model.Chat;
import com.example.chat.model.ChatInvitation;
import com.example.chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatInvitationRepository extends JpaRepository<ChatInvitation, Long> {
    List<ChatInvitation> findByInviteeAndStatus(User invitee, ChatInvitation.InvitationStatus status);
    ChatInvitation findByInviteeAndChatAndStatus(User invitee, Chat chat, ChatInvitation.InvitationStatus status);
    List<ChatInvitation> findByChatAndStatus(Chat chat, ChatInvitation.InvitationStatus status);
}