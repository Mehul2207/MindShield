package com.example.mindshield.models;

public class Message {

    public String messageId;
    public String senderId;
    public String receiverId;
    public String text;
    public long timestamp;
    public String type; // text, image (future use)

    public Message() {}

    public Message(String messageId, String senderId, String receiverId,
                   String text, long timestamp, String type) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}