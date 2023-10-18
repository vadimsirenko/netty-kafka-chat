package ru.vasire.netty.kafka.chat.server.websocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.vasire.netty.kafka.chat.server.websocket.dto.MessageDto;
import ru.vasire.netty.kafka.chat.server.websocket.dto.ResponseDto;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Client;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Message;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageService {

    public static ResponseDto sendMessage(Client client, Message message) {
        ResponseDto res = new ResponseDto();
        res.getData().put("sender", client.getName());
        res.getData().put("message", message.getMessageText());
        res.getData().put("recipient", message.getRecipient());
        res.getData().put("ts", System.currentTimeMillis());
        return res;
    }

    public static Message messageEncode(Client client, String requestJson) {
        try {
            MessageDto messageDto = new ObjectMapper().readValue(requestJson, MessageDto.class);

            if (!validateMessage(messageDto))
                throw new RuntimeException("Message is not valid");

            Message message = new Message();

            if (messageDto.getMessageText() != null)
                message.setMessageText(messageDto.getMessageText());
            if (messageDto.getRecipient() != null)
                message.setRecipient(messageDto.getRecipient());
            if (client.getName() != null)
                message.setSender(client.getName());
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
    private static boolean validateMessage(MessageDto messageDto) {
        return true;
    }
}
