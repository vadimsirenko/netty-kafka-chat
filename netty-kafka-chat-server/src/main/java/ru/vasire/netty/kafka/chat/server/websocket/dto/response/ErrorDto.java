package ru.vasire.netty.kafka.chat.server.websocket.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import ru.vasire.netty.kafka.chat.server.websocket.dto.MessageType;
import ru.vasire.netty.kafka.chat.server.websocket.dto.OperationType;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorDto extends ResponseDto {

    public ErrorDto(int errorCode, String errorMessage){
        super(errorCode, errorMessage, MessageType.ERROR, OperationType.NONE);
    }
}
