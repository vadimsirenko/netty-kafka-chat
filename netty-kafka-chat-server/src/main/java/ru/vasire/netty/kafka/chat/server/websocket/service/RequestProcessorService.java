package ru.vasire.netty.kafka.chat.server.websocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.vasire.netty.kafka.chat.server.websocket.dto.request.RequestDto;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Client;

public class RequestProcessorService {
    public static void processRequest(Client client, String requestJson) throws JsonProcessingException {

        RequestDto requestDto = new ObjectMapper().readValue(requestJson, RequestDto.class);

        switch (requestDto.getMessageType()) {
            case MESSAGE:
                MessageService.processMessage(client, requestJson);
                break;
            case ROOM:
                MessageService.processMessage(client, requestJson);
                break;
            case CLIENT:
                break;
            case INFO:
                break;
            case ERROR:
                break;
            default: ;
        }
    }
}
