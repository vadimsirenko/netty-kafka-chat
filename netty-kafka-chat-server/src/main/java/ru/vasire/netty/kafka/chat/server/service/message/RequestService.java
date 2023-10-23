package ru.vasire.netty.kafka.chat.server.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.dto.BaseDto;
import ru.vasire.netty.kafka.chat.server.entity.Client;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final MessageService messageService;
    private final RoomService roomService;

    public void processRequest(String requestJson, Channel channel) throws JsonProcessingException {

        BaseDto baseDto = new ObjectMapper().readValue(requestJson, BaseDto.class);

        switch (baseDto.getMessageType()) {
            case MESSAGE:
                messageService.processRequest(requestJson);
                break;
            case ROOM:
                roomService.processRequest(requestJson);
                break;
            case MESSAGE_LIST:
                messageService.processMessageListRequest(requestJson, channel);
                break;
            default: ;
        }
    }
}