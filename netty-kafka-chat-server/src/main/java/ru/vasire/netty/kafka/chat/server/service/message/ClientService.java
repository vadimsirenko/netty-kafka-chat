package ru.vasire.netty.kafka.chat.server.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.dto.ClientDto;
import ru.vasire.netty.kafka.chat.server.dto.OPERATION_TYPE;
import ru.vasire.netty.kafka.chat.server.entity.Client;
import ru.vasire.netty.kafka.chat.server.mapper.ClientMapper;
import ru.vasire.netty.kafka.chat.server.repository.ClientRepository;
import ru.vasire.netty.kafka.chat.server.repository.RoomChannelRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public final class ClientService {
    @Data
    @AllArgsConstructor
    class UserProfile{
        private UUID id;
        private String login;
        private String nickName;
        private UUID roomId;
    }
    private static final Map<UUID, UserProfile> USER_PROFILES = new ConcurrentHashMap<>();
    private final ClientRepository clientRepository;
    private final InfoService infoService;
    private final RoomChannelRepository roomChannelRepository;
    public Client clientRegister(String request) {
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

    public Client clientRegister(Client client) {
        final String login = client.getLogin();
        UserProfile profile = USER_PROFILES.values().stream().filter(p->p.getLogin()==login).findAny().orElse(null);
        if(profile != null){
            client.setId(profile.getId());
            client.setNickName(profile.getNickName());
        }else{
            client = clientRepository.findByLogin(client.getLogin()).orElse(null);
            USER_PROFILES.put(client.getId(), new UserProfile(client.getId(), client.getLogin(), client.getNickName(), null));
        }
        return client;
    }

    public void addClientToRoom(UUID clientId, UUID roomId) {
        UserProfile userProfile = USER_PROFILES.get(clientId);
        if(userProfile.getRoomId()!=null && userProfile.getRoomId()!=roomId){
            logOffFromRoomInfo(userProfile.getRoomId(), userProfile.getNickName());
        }
        userProfile.setRoomId(roomId);
        USER_PROFILES.put(clientId, userProfile);
        logOnFromRoomInfo(roomId, userProfile.getNickName());
    }

    public UUID getRoomByClientId(UUID clientId){
        return USER_PROFILES.get(clientId).getRoomId();
    }

    public void removeProfile(UUID clientId, Channel channel) {
        UserProfile userProfile = USER_PROFILES.get(clientId);
        roomChannelRepository.removeChannel(userProfile.getRoomId(), channel);
        USER_PROFILES.remove(clientId);
        logOffFromRoomInfo(userProfile.getRoomId(), userProfile.getNickName());
    }

    private void logOffFromRoomInfo(UUID roomId, String nickName){
        infoService.sendLogoffInfo(roomChannelRepository.getRoomChannels(roomId), nickName);
    }
    private void logOnFromRoomInfo(UUID roomId, String nickName){
        infoService.sendLogonInfo (roomChannelRepository.getRoomChannels(roomId), nickName);
    }
}


