package ru.vasire.netty.kafka.chat.server.websocket.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import ru.vasire.netty.kafka.chat.server.websocket.dto.MessageType;
import ru.vasire.netty.kafka.chat.server.websocket.dto.OperationType;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResponseMessageDto extends ResponseDto {

    public ResponseMessageDto(String sender,  String message, String recipient, Long ts, OperationType operationType){
        super(0, null, MessageType.MESSAGE, operationType);
        this.getData().put("sender", sender);
        this.getData().put("message", message);
        this.getData().put("recipient", recipient);
        this.getData().put("ts", ts);
    }
}
