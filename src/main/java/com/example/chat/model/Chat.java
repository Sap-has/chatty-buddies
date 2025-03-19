package com.example.chat.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chatName;

    // A unique code used to identify the chat.
    private String chatCode;

    // The creator/host of the chat.
    @ManyToOne
    private User host;

    @ManyToMany
    @JoinTable(name = "chat_members",
        joinColumns = @JoinColumn(name = "chat_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> members;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private Set<Message> messages;

    // Constructors, Getters and Setters
    public Chat() {}

    // Getters and Setters ...
    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getChatCode() {
        return chatCode;
    }

    public void setChatCode(String chatCode) {
        this.chatCode = chatCode;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }

}
