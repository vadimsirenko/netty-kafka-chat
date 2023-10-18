package ru.vasire.netty.kafka.chat.server.websocket.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Client {
    private long id;
    private String name;
    private long chatId;
}
