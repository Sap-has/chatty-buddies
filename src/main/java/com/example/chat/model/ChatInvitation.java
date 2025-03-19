package com.example.chat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User inviter;

    @ManyToOne
    private User invitee;

    @ManyToOne
    private Chat chat;

    private LocalDateTime createdAt;

    public enum InvitationStatus {
        PENDING, ACCEPTED, DECLINED
    }

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    // Constructors
    public ChatInvitation() {
        this.createdAt = LocalDateTime.now();
        this.status = InvitationStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getInviter() {
        return inviter;
    }

    public void setInviter(User inviter) {
        this.inviter = inviter;
    }

    public User getInvitee() {
        return invitee;
    }

    public void setInvitee(User invitee) {
        this.invitee = invitee;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }
}