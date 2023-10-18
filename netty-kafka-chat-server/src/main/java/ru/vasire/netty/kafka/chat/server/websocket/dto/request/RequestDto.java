package ru.vasire.netty.kafka.chat.server.websocket.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import ru.vasire.netty.kafka.chat.server.websocket.dto.MessageType;
import ru.vasire.netty.kafka.chat.server.websocket.dto.OperationType;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestDto {
    private MessageType messageType;
    private OperationType operationType;
}
