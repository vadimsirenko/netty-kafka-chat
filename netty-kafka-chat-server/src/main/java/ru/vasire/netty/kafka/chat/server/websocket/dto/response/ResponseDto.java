package ru.vasire.netty.kafka.chat.server.websocket.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.vasire.netty.kafka.chat.server.websocket.dto.MessageType;
import ru.vasire.netty.kafka.chat.server.websocket.dto.OperationType;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResponseDto {
    private final Integer errorCode;
    private final OperationType operationType;
    private final MessageType messageType;
    private final String errorMessage;
    private final Map<String, Object> data = new HashMap<>();

    public ResponseDto(int errorCode, String errorMessage, MessageType messageType, OperationType operationType) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.operationType = operationType;
        this.messageType = messageType;
    }

}
