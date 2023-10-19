package ru.vasire.netty.kafka.chat.server.websocket.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Getter
@Setter
public class Room {
    @Id
    private UUID id;
    private String name;
    private long messageCount;
}
