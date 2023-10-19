package ru.vasire.netty.kafka.chat.server.websocket.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class Message {
    private UUID senderId;
    private UUID recipientId;
    private UUID roomId;
    private String messageText;
}
