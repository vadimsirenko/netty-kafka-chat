package ru.vasire.netty.kafka.chat.server.websocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.vasire.netty.kafka.chat.server.websocket.dto.request.ClientDto;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClientService {

    public static Client clientRegister(String request) {
        try {
            String json = new String(Base64.getDecoder().decode(request), StandardCharsets.UTF_8);
            ClientDto clientDto = new ObjectMapper().readValue(json, ClientDto.class);

            if (!checkToken(clientDto))
                throw new RuntimeException("User is not authorized");

            Client client = new Client();

            if (clientDto.getName() != null)
                client.setName(clientDto.getName());
            if (clientDto.getRoomId() != null)
                client.setRoomId(clientDto.getRoomId());
            return client;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the token from redis according to the id and compare it with it
     */
    private static boolean checkToken(ClientDto req) {
        return true;
    }
}
