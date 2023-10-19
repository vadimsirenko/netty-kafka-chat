package ru.vasire.netty.kafka.chat.server.websocket.service;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import ru.vasire.netty.kafka.chat.server.websocket.dto.request.RequestRoomDto;
import ru.vasire.netty.kafka.chat.server.websocket.entity.ChatChannel;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Client;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Message;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Room;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChatEngineService {
    private static final Map<UUID, ChannelGroup> CHANNEL_GROUP_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, String> CHAT_USERS = new ConcurrentHashMap<>();

    public static void addClient(Client client, Channel channel){
        // If it does not exist in the room list, it is the channel, then add a channel ChannelGroup
        if (!CHANNEL_GROUP_MAP.containsKey(client.getRoomId())) {
            CHANNEL_GROUP_MAP.put(client.getRoomId(), new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        }
        UUID clientId = UUID.randomUUID();
        // Make sure there is a room number before adding the message to the channel
        CHANNEL_GROUP_MAP.get(client.getRoomId()).add(new ChatChannel(clientId, channel));
        client.setId(clientId);
        CHAT_USERS.put(clientId, client.getName());
    }

    public static void removeClient(Client client, Channel channel){
        if (client != null && CHANNEL_GROUP_MAP.containsKey(client.getRoomId())) {
            CHANNEL_GROUP_MAP.get(client.getRoomId()).remove(channel);
            CHAT_USERS.remove(client.getName());
        }
    }

    public static Set<Channel> getChannelToSendMessage(Client client, Message message){
        if (CHANNEL_GROUP_MAP.containsKey(client.getRoomId())) {
            if (message.getRecipientId() == null) {
                return CHANNEL_GROUP_MAP.get(client.getRoomId());
            } else {
                return CHANNEL_GROUP_MAP.get(client.getRoomId()).stream()
                        .filter(c->((ChatChannel) c).getClientId().equals(message.getRecipientId())||
                                ((ChatChannel) c).getClientId().equals(client.getId())).collect(Collectors.toSet());
            }
        }
        return new HashSet<>();
    }

    public static Set<Channel> getChannelToSendRoom(){
        return CHANNEL_GROUP_MAP.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public static String getClientName(UUID clientId){
        if(clientId == null || !CHAT_USERS.containsKey(clientId))
            return "anonimus";
        return CHAT_USERS.get(clientId);
    }
}
