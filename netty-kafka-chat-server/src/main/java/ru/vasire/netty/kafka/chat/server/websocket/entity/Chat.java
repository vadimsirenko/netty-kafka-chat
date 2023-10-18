package ru.vasire.netty.kafka.chat.server.websocket.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Chat {
    private long id;
    private String name;
}
