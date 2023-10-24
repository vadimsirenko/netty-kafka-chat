package ru.vasire.netty.kafka.chat.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "client", uniqueConstraints = @UniqueConstraint(name = "UK_Client_Login", columnNames={"login"} ))
public class Client {
    @Id
    private UUID id;
    @Column(name = "login", nullable = false)
    private String login;
    private String email;
    @Column(name = "nick_name")
    private String nickName;
    private String token;
    @Transient
    private UUID roomId;
}
