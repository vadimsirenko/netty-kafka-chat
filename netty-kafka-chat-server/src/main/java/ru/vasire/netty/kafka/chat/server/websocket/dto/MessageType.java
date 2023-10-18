package ru.vasire.netty.kafka.chat.server.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MessageType {
    @JsonProperty("MESSAGE")
    CHAT_MESSAGE,
    @JsonProperty("ROOM")
    CHAT,
    @JsonProperty("CLIENT")
    CLIENT
}
