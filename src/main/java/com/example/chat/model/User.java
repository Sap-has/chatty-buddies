package com.example.chat.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "chatUsers")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    // In a real application, store hashed passwords only.
    private String password;

    @ManyToMany(mappedBy = "members")
    private Set<Chat> chats;

    // Getters and setters (or use Lombok for brevity)

    // Constructors
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String encode) {
        this.password = encode;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Chat> getChats() {
        return chats;
    }

    public void setChats(Set<Chat> chats) {
        this.chats = chats;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getters and Setters ...
}
