package ru.vasire.netty.kafka.chat.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "room", uniqueConstraints = @UniqueConstraint(name = "UK_Room_Name", columnNames={"name"} ))
public class Room {
    @Id
    @Column(name = "id")
    private UUID id;
    @Column(name = "name", nullable = false)
    private String name;
    @Transient
    private long messageCount;
}
