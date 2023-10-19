package ru.vasire.netty.kafka.chat.server.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OperationType {
    @JsonProperty("CREATE")
    CREATE,
    @JsonProperty("UPDATE")
    UPDATE,
    @JsonProperty("DELETE")
    DELETE,
    @JsonProperty("NONE")
    NONE
}
