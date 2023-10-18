package ru.vasire.netty.kafka.chat.server.websocket.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Message {
    private Long senderId;
    private Long recipientId;
    private Long chatId;
    private String messageText;
}
