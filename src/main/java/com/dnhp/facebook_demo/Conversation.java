package com.dnhp.facebook_demo;

import java.util.List;

public class Conversation {
    private List<Message> messages;

    public Conversation(List<Message> messages) {
        this.messages = messages;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void removeMessage(Message message) {
        this.messages.remove(message);
    }

    public void clearMessages() {
        this.messages.clear();
    }

    public Message getLastMessage() {
        return this.messages.get(this.messages.size() - 1);
    }



}
