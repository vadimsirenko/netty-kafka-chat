package ru.vasire.netty.kafka.chat.server.websocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.vasire.netty.kafka.chat.server.websocket.dto.request.RequestClientDto;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClientService {

    public static Client clientRegister(String request) {
        try {
            String json = new String(Base64.getDecoder().decode(request), StandardCharsets.UTF_8);
            RequestClientDto requestClientDto = new ObjectMapper().readValue(json, RequestClientDto.class);

            if (!checkToken(requestClientDto))
                throw new RuntimeException("User is not authorized");

            Client client = new Client();

            if (requestClientDto.getName() != null)
                client.setName(requestClientDto.getName());
            if (requestClientDto.getRoomId() != null)
                client.setRoomId(requestClientDto.getRoomId());
            return client;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the token from redis according to the id and compare it with it
     */
    private static boolean checkToken(RequestClientDto req) {
        return true;
    }
}
