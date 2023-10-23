package ru.vasire.netty.kafka.chat.server.service;

import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.dto.OPERATION_TYPE;
import ru.vasire.netty.kafka.chat.server.entity.Client;
import ru.vasire.netty.kafka.chat.server.entity.ChatMessage;
import ru.vasire.netty.kafka.chat.server.entity.Room;
import ru.vasire.netty.kafka.chat.server.netty.ClientChannel;
import ru.vasire.netty.kafka.chat.server.repository.ChatMessageRepository;
import ru.vasire.netty.kafka.chat.server.repository.RoomChannelRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatEngineService {

    private final RoomChannelRepository roomChannelRepository;
    private final ChatMessageRepository chatMessageRepository;

    private static final Map<UUID, String> USERS = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> USER_CHAT = new ConcurrentHashMap<>();

    public void addClient(Client client, Channel channel) {
        UUID clientId = UUID.randomUUID();
        client.setId(clientId);
        USERS.put(clientId, client.getName());
        //roomChannelRepository.put(client.getRoomId(), new ClientChannel(client.getId(), channel));
    }

    public void addClientToRoom(Client client, UUID roomId, Channel channel) {
        if(client.getId()!=null && USER_CHAT.containsKey(client.getId()) && USER_CHAT.get(client.getId())!=roomId){
            roomChannelRepository.removeChannel(USER_CHAT.get(client.getId()), client.getId());
            USER_CHAT.remove(client.getId());
        }
        if(!USER_CHAT.containsKey(client.getId())){
            roomChannelRepository.put(roomId, new ClientChannel(client.getId(), channel));
            USER_CHAT.put(client.getId(), roomId);
        }
    }

    public void removeClient(Client client) {
        if (client != null) {
            roomChannelRepository.removeChannel(USER_CHAT.get(client.getId()), client.getId());
            USERS.remove(client.getId());
        }
    }

    public Set<Channel> getChannelToSendMessage(Client client, ChatMessage chatMessage) {
        if (chatMessage.getRecipientId() == null) {
            return roomChannelRepository.getRoomChannels(USER_CHAT.get(client.getId()));
        } else {
            return roomChannelRepository.getChannelByRoomIdAndClientIdList(chatMessage.getRecipientId(), chatMessage.getSenderId());
        }
    }

    public Set<Channel> getChannelToSendRoom() {
        return roomChannelRepository.getAllChannels();
    }

    public static String getClientName(UUID clientId) {
        if (clientId == null || !USERS.containsKey(clientId))
            return "anonimus";
        return USERS.get(clientId);
    }

    public List<Room> getRoomByClientId(UUID clientId) {
       return roomChannelRepository.getAllRoom();
    }

    public boolean applyRoom(OPERATION_TYPE OPERATIONTYPE, Room room) {
        return switch (OPERATIONTYPE) {
            case CREATE:
                yield roomChannelRepository.saveRoom(room);
            case UPDATE:
                yield roomChannelRepository.saveRoom(room);
            case DELETE:
                yield roomChannelRepository.deleteRoom(room);
            default:
                yield false;
        };
    }

    public List<ChatMessage> getMessageByRoomId(UUID roomId) {
        return chatMessageRepository.findByRoomId(roomId);
    }
}
