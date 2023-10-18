package ru.vasire.netty.kafka.chat.server.websocket.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.vasire.netty.kafka.chat.server.websocket.dto.request.RequestDto;

@Getter
@Setter
public class RoomDto extends RequestDto {
    @JsonProperty("room_id")
    private Long chatId;
    @JsonProperty("room_name")
    private String chatName;
}
