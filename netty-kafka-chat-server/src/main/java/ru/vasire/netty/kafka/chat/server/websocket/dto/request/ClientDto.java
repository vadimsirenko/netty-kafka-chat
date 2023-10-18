package ru.vasire.netty.kafka.chat.server.websocket.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientDto extends RequestDto {
    private Long id;
    private String name;
    @JsonProperty("room_id")
    private Long roomId;
    private String token;
}
