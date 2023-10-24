package ru.vasire.netty.kafka.chat.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class InfoDto extends BaseDto implements RoomMessage {
    private final String messageText;
    public InfoDto(OPERATION_TYPE operationType, String messageText){
        super(MESSAGE_TYPE.INFO, operationType);
        this.messageText = messageText;
    }

    public InfoDto() {
        super(MESSAGE_TYPE.INFO, OPERATION_TYPE.NONE);
        this.messageText = "";
    }
    public InfoDto(String messageText) {
        this(OPERATION_TYPE.NONE, messageText);
    }
}
