package ru.vasire.netty.kafka.chat.server.websocket.service;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import ru.vasire.netty.kafka.chat.server.websocket.entity.ChatChannel;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Client;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChatEngineService {
    private static final Map<Long, ChannelGroup> CHANNEL_GROUP_MAP = new ConcurrentHashMap<>();
    private static final Map<Long, String> CHAT_USERS = new ConcurrentHashMap<>();
    private static Long newClientIndex = 1L;

    public static void addClient(Client client, Channel channel){
        // If it does not exist in the room list, it is the channel, then add a channel ChannelGroup
        if (!CHANNEL_GROUP_MAP.containsKey(client.getChatId())) {
            CHANNEL_GROUP_MAP.put(client.getChatId(), new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        }

        // Make sure there is a room number before adding the message to the channel
        CHANNEL_GROUP_MAP.get(client.getChatId()).add(new ChatChannel(newClientIndex, channel));
        client.setId(newClientIndex);
        CHAT_USERS.put(newClientIndex, client.getName());
        newClientIndex++;
    }

    public static void removeClient(Client client, Channel channel){
        if (client != null && CHANNEL_GROUP_MAP.containsKey(client.getChatId())) {
            CHANNEL_GROUP_MAP.get(client.getChatId()).remove(channel);
            CHAT_USERS.remove(client.getName());
        }
    }

    public static Set<Channel> getChannelToSendMessage(Client client, Message message){
        if (CHANNEL_GROUP_MAP.containsKey(client.getChatId())) {
            if (message.getRecipientId() != 0) {
                return CHANNEL_GROUP_MAP.get(client.getChatId());
            } else {
                return CHANNEL_GROUP_MAP.get(client.getChatId()).stream().filter(c -> ((ChatChannel) c).getClientId() == client.getId() ||  ((ChatChannel) c).getClientId() == message.getRecipientId()).collect(Collectors.toSet());
            }
        }
        return new HashSet<>();
    }

    public static String getClientName(Long clientId){
        if(!CHAT_USERS.containsKey(clientId))
            return "anonimus" + clientId;
        return CHAT_USERS.get(clientId);
    }
}
