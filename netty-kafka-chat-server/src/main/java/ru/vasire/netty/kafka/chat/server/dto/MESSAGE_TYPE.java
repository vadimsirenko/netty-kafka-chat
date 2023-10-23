package ru.vasire.netty.kafka.chat.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MESSAGE_TYPE {
    @JsonProperty("MESSAGE")
    MESSAGE,
    @JsonProperty("MESSAGE_LIST")
    MESSAGE_LIST,
    @JsonProperty("ROOM")
    ROOM,
    @JsonProperty("ROOM_LIST")
    ROOM_LIST,
    @JsonProperty("CLIENT")
    CLIENT,
    @JsonProperty("INFO")
    INFO,
    @JsonProperty("ERROR")
    ERROR,
    @JsonProperty("UNKNOWN")
    UNKNOWN
}
