package ru.vasire.netty.kafka.chat.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "message")
public class ChatMessage {
    @Id
    private UUID id;
    @Column(name = "sender_id", nullable = false)
    private UUID senderId;
    @Column(name = "room_id", nullable = false)
    private UUID roomId;
    @Column(name = "message_text", nullable = false)
    private String messageText;
    private Long ts;
    private String sender;
}
