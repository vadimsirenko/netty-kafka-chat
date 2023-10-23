package ru.vasire.netty.kafka.chat.server.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import org.h2.command.ddl.CreateTable;
import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.dto.ChatMessageDto;
import ru.vasire.netty.kafka.chat.server.dto.InfoDto;
import ru.vasire.netty.kafka.chat.server.entity.Client;
import ru.vasire.netty.kafka.chat.server.entity.Room;
import ru.vasire.netty.kafka.chat.server.mapper.ChatMessageMapper;
import ru.vasire.netty.kafka.chat.server.netty.ClientChannel;
import ru.vasire.netty.kafka.chat.server.service.message.InfoService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomChannelRepository {

    private final RoomRepository roomRepository;
    private final Map<UUID, ChannelGroup> CHANNEL_GROUP_MAP = new ConcurrentHashMap<>();

    public void put(UUID roomId, Channel channel, UUID oldRoomId) {
        if (roomId == oldRoomId) return;
        if (roomId != null) {
            // If it does not exist in the room list, it is the channel, then add a channel ChannelGroup
            if (!CHANNEL_GROUP_MAP.containsKey(roomId)) {
                CHANNEL_GROUP_MAP.put(roomId, new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
            }
            if(oldRoomId != null) {
                CHANNEL_GROUP_MAP.get(oldRoomId).remove(channel);
            }
            CHANNEL_GROUP_MAP.get(roomId).add(channel);
        }
    }

    public Set<Channel> getAllChannels() {
        return CHANNEL_GROUP_MAP.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public Set<Channel> getRoomChannels(UUID roomId) {
        if(CHANNEL_GROUP_MAP.containsKey(roomId)){
            return CHANNEL_GROUP_MAP.get(roomId);
        }
        return new HashSet<>();
    }

    public void removeChannel(UUID roomId, Channel channel) {
        CHANNEL_GROUP_MAP.get(roomId).remove(channel);
    }

    public List<Room> getAllRoom(){
        return roomRepository.findAll();
    }

    public boolean isRoomExists(String name){
        return roomRepository.findByName(name).isPresent();
    }

    public boolean saveRoom(Room room) {
        room = roomRepository.save(room);
        if (!CHANNEL_GROUP_MAP.containsKey(room.getId())) {
            CHANNEL_GROUP_MAP.put(room.getId(), new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        }
        return true;
    }

    public boolean deleteRoom(Room room) {
        CHANNEL_GROUP_MAP.remove(room.getId());
        if(roomRepository.findByName(room.getName()).isPresent()){
            roomRepository.deleteById(room.getId());
            return true;
        }
        return false;
    }
}
