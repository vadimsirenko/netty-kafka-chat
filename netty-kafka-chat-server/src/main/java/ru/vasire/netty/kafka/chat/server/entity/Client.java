package ru.vasire.netty.kafka.chat.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "client", uniqueConstraints = @UniqueConstraint(name = "UK_Client_Name", columnNames={"name"} ))
public class Client {
    @Id
    private UUID id;
    @Column(name = "name", nullable = false)
    private String name;
   // @Transient
   // private UUID roomId;
}
