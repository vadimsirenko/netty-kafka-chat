package ru.vasire.netty.kafka.chat.server.websocket.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestDto {
    private MessageType messageType;
    private OperationType operationType;
}
