package ru.vasire.netty.kafka.chat.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserProfileDto{
    private UUID id;
    private String login;
    private String nickName;
    private UUID roomId;
}