package ru.vasire.netty.kafka.chat.server.websocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.vasire.netty.kafka.chat.server.websocket.dto.request.ChatMessageDto;
import ru.vasire.netty.kafka.chat.server.websocket.dto.response.ResponseDto;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Client;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Message;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageService {

    public static ResponseDto sendMessage(Client client, Message message) {
        ResponseDto res = new ResponseDto();
        res.getData().put("sender", client.getName());
        res.getData().put("message", message.getMessageText());
        res.getData().put("recipient", ChatEngineService.getClientName(message.getRecipientId()));
        res.getData().put("ts", System.currentTimeMillis());
        return res;
    }

    public static Message messageEncode(Client client, String requestJson) {
        try {
            ChatMessageDto messageDto = new ObjectMapper().readValue(requestJson, ChatMessageDto.class);

            if (!validateMessage(messageDto))
                throw new RuntimeException("Message is not valid");

            Message message = new Message();

            if (messageDto.getMessageText() != null)
                message.setMessageText(messageDto.getMessageText());
            if (messageDto.getRecipientId() != null)
                message.setRecipientId(messageDto.getRecipientId());
            if (client.getName() != null)
                message.setSenderId(client.getId());
            if (client.getRoomId() != 0)
                message.setRoomId(client.getRoomId());
            return message;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check that message structure is valid
     * @param messageDto
     * @return
     */
    private static boolean validateMessage(ChatMessageDto messageDto) {
        return true;
    }
}
