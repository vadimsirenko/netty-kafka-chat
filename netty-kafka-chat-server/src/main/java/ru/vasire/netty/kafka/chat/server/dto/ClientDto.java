package ru.vasire.netty.kafka.chat.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ClientDto extends BaseDto {
    private UUID id;
    private String name;
    private String token;

    public ClientDto(OPERATION_TYPE operationType, UUID id, String name,String token) {
        super(MESSAGE_TYPE.CLIENT, operationType);
        this.id = id;
        this.name = name;
        this.token = token;
    }

    public ClientDto(MESSAGE_TYPE messageType, OPERATION_TYPE operationType, Long ts) {
        super(messageType, operationType, ts);
    }

    public ClientDto() {
        super(MESSAGE_TYPE.CLIENT, OPERATION_TYPE.NONE);
    }
}
