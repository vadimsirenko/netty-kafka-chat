package ru.vasire.netty.kafka.chat.server.websocket.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Message {
    private String sender;
    private String recipient;
    private String messageText;
}
