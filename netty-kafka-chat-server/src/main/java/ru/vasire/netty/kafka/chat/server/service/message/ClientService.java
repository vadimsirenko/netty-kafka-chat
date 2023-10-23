package ru.vasire.netty.kafka.chat.server.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.dto.ClientDto;
import ru.vasire.netty.kafka.chat.server.dto.OPERATION_TYPE;
import ru.vasire.netty.kafka.chat.server.dto.RoomListDto;
import ru.vasire.netty.kafka.chat.server.entity.Client;
import ru.vasire.netty.kafka.chat.server.entity.Room;
import ru.vasire.netty.kafka.chat.server.mapper.ClientMapper;
import ru.vasire.netty.kafka.chat.server.mapper.RoomMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
@Service
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClientService {

    public static Client clientRegister(String request) {
        try {
            String json = new String(Base64.getDecoder().decode(request), StandardCharsets.UTF_8);
            ClientDto clientDto = new ObjectMapper().readValue(json, ClientDto.class);
            if (!checkToken(clientDto))
                throw new RuntimeException("User is not authorized");
            return ClientMapper.INSTANCE.ClientDtoToClient(clientDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendClientProfile(Client client, Channel channel) throws JsonProcessingException {
        ClientDto clientDto = ClientMapper.INSTANCE.ClientToClientDto(client);
        clientDto.setOperationType(OPERATION_TYPE.UPDATE);
        String json = new ObjectMapper().writeValueAsString(clientDto);
        channel.writeAndFlush(new TextWebSocketFrame(json));
    }
    /**
     * Get the token from redis according to the id and compare it with it
     */
    private static boolean checkToken(ClientDto req) {
        return true;
    }
}
