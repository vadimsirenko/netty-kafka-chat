package ru.vasire.netty.kafka.chat.server.websocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.vasire.netty.kafka.chat.server.websocket.dto.OperationType;
import ru.vasire.netty.kafka.chat.server.websocket.dto.request.RequestMessageDto;
import ru.vasire.netty.kafka.chat.server.websocket.dto.response.ResponseMessageDto;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Client;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Message;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageService {

    public static ResponseMessageDto createResponse(OperationType operationType, Client client, Message message) {
        ResponseMessageDto res = new ResponseMessageDto(
                client.getName(),
                message.getMessageText(),
                ChatEngineService.getClientName(message.getRecipientId()),
                System.currentTimeMillis(),
                operationType);
        return res;
    }

    public static Message messageEncode(Client client, String requestJson) {
        try {
            RequestMessageDto messageDto = new ObjectMapper().readValue(requestJson, RequestMessageDto.class);

            if (!validateMessage(messageDto))
                throw new RuntimeException("Message is not valid");

            Message message = new Message();

            if (messageDto.getMessageText() != null)
                message.setMessageText(messageDto.getMessageText());
            if (messageDto.getRecipientId() != null)
                message.setRecipientId(messageDto.getRecipientId());
            if (client.getName() != null)
                message.setSenderId(client.getId());
            if (client.getRoomId() != null)
                message.setRoomId(client.getRoomId());
            return message;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void processMessage(Client client, String req) throws JsonProcessingException {
        Message message = MessageService.messageEncode(client, req);
        ResponseMessageDto res = MessageService.createResponse(OperationType.CREATE, client, message);
        String json = new ObjectMapper().writeValueAsString(res);
        ChatEngineService.getChannelToSendMessage(client, message).forEach(c->c.writeAndFlush(new TextWebSocketFrame(json)));
    }

    /**
     * Check that message structure is valid
     * @param messageDto
     * @return
     */
    private static boolean validateMessage(RequestMessageDto messageDto) {
        return true;
    }
}
