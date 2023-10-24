package ru.vasire.netty.kafka.chat.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ClientDto extends BaseDto implements ClientMessage {
    private UUID id;
    private String login;
    private String email;
    private String nickName;
    private String token;
    private UUID roomId;

    public ClientDto() {
        super(MESSAGE_TYPE.CLIENT, OPERATION_TYPE.NONE);
    }
}
