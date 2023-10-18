package ru.vasire.netty.kafka.chat.server.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientDto extends RequestDto {
    private Long id;
    private String name;
    @JsonProperty("chat_id")
    private Long chatId;
    private String token;
}
