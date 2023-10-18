package ru.vasire.netty.kafka.chat.server.websocket.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDto extends RequestDto {
    @JsonProperty("recipient_id")
    private Long recipientId;
    private String messageText;

}
