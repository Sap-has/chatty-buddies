package com.example.chat.repository;

import com.example.chat.model.Chat;
import com.example.chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Chat findByChatCode(String chatCode);
    List<Chat> findByHost(User host);
    List<Chat> findByMembersContaining(User member);
}