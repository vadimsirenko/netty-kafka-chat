package ru.vasire.netty.kafka.chat.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class MessageListDto extends BaseDto{
    @JsonProperty("messages")
    private List<ChatMessageDto> messages;
    private UUID roomId;
    private UUID senderId;

    public MessageListDto(OPERATION_TYPE operationType, UUID roomId, List<ChatMessageDto> messages) {
        super(MESSAGE_TYPE.MESSAGE_LIST, operationType);
        this.roomId = roomId;
        this.messages = messages;
    }
    public MessageListDto() {
        super(MESSAGE_TYPE.MESSAGE_LIST, OPERATION_TYPE.UPDATE);
    }
}
