package ru.vasire.netty.kafka.chat.server.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.dto.ClientDto;
import ru.vasire.netty.kafka.chat.server.dto.UserProfileDto;
import ru.vasire.netty.kafka.chat.server.entity.Client;
import ru.vasire.netty.kafka.chat.server.mapper.UserProfileDtoMapper;
import ru.vasire.netty.kafka.chat.server.repository.ClientRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public final class ClientService {
    private static final Map<String, UserProfileDto> USER_PROFILES = new ConcurrentHashMap<>();
    private final ClientRepository clientRepository;

    public Client clientLogin(String request, String channelLongId) {
        try {
            String json = new String(Base64.getDecoder().decode(request), StandardCharsets.UTF_8);
            ClientDto clientDto = new ObjectMapper().readValue(json, ClientDto.class);
            if (!checkToken(clientDto)) {
                // TODO обработать неверный вход
                throw new RuntimeException("User is not authorized");
            }
            Client client = clientRepository.findByLogin(clientDto.getLogin()).orElse(null);
            if (client == null) {
                // TODO обработать неверный вход
                throw new RuntimeException("User is not authorized");
            }
            client.setRoomId(clientDto.getRoomId());
            savePfofile(client, channelLongId);
            return client;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void savePfofile(Client client, String channelLongId) {
        if (channelLongId != null && client != null) {
            USER_PROFILES.put(channelLongId, UserProfileDtoMapper.INSTANCE.ClientToUserProfileDto(client));
        }
    }
    public UserProfileDto getPfofile(String channelLongId) {
        if (channelLongId != null) {
            return USER_PROFILES.get(channelLongId);
        }
        return null;
    }
    private static boolean checkToken(ClientDto req) {
        return true;
    }
}


