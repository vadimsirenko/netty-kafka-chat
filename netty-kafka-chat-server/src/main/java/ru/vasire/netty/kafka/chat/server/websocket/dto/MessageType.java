package ru.vasire.netty.kafka.chat.server.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MessageType {
    @JsonProperty("CHAT_MESSAGE")
    CHAT_MESSAGE,
    @JsonProperty("CHAT")
    CHAT,
    @JsonProperty("CLIENT")
    CLIENT
}
