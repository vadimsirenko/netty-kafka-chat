package ru.vasire.netty.kafka.chat.server.websocket.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import ru.vasire.netty.kafka.chat.server.websocket.dto.MessageType;
import ru.vasire.netty.kafka.chat.server.websocket.dto.OperationType;

import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResponseRoomDto extends ResponseDto {

    public ResponseRoomDto(UUID id, String name, Long messageCount, Long ts, OperationType operationType){
        super(0, null, MessageType.ROOM, operationType);
        this.getData().put("id", id);
        this.getData().put("name", name);
        this.getData().put("messageCount", messageCount);
        this.getData().put("ts", ts);
    }
}
