package ru.vasire.netty.kafka.chat.server.websocket.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestMessageDto extends RequestDto {
    @JsonProperty("recipient_id")
    private UUID recipientId;
    private String messageText;

}
