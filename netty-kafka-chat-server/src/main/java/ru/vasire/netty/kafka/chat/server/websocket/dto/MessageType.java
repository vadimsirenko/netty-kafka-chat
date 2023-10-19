package ru.vasire.netty.kafka.chat.server.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MessageType {
    @JsonProperty("MESSAGE")
    MESSAGE,
    @JsonProperty("ROOM")
    ROOM,
    @JsonProperty("CLIENT")
    CLIENT,
    @JsonProperty("INFO")
    INFO,
    @JsonProperty("ERROR")
    ERROR
}
