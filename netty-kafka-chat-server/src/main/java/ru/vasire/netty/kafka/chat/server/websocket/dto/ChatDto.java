package ru.vasire.netty.kafka.chat.server.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatDto extends RequestDto {
    @JsonProperty("chat_id")
    private Long chatId;
    @JsonProperty("chat_name")
    private String chatName;
}
