package com.example.chat.dto;

public class ChatMessage {
    private String chatCode;
    private String sender;
    private String content;

    // Constructors
    public ChatMessage() {}

    public ChatMessage(String chatCode, String sender, String content) {
        this.chatCode = chatCode;
        this.sender = sender;
        this.content = content;
    }

    // Getters and Setters
    public String getChatCode() {
        return chatCode;
    }
    public void setChatCode(String chatCode) {
        this.chatCode = chatCode;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
