package com.dnhp.facebook_demo;

public class Message {
    private String messageText;
    private long timestamp;
    private String senderID;
    private String recipientID;

    public Message() {
    }

    public Message(String messageText, long timestamp, String senderID, String recipientID) {
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.senderID = senderID;
        this.recipientID = recipientID;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getRecipientID() {
        return recipientID;
    }

    public void setRecipientID(String recipientID) {
        this.recipientID = recipientID;
    }

}
