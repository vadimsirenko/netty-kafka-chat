package ru.vasire.netty.kafka.chat.server.repository;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.entity.Room;
import ru.vasire.netty.kafka.chat.server.netty.ClientChannel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomChannelRepository {

    private final RoomRepository roomRepository;
    private final Map<UUID, ChannelGroup> CHANNEL_GROUP_MAP = new ConcurrentHashMap<>();

    public RoomChannelRepository put(UUID roomId, ClientChannel channel) {
        // If it does not exist in the room list, it is the channel, then add a channel ChannelGroup
        if (!CHANNEL_GROUP_MAP.containsKey(roomId)) {
            CHANNEL_GROUP_MAP.put(roomId, new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        }
        CHANNEL_GROUP_MAP.get(roomId).add(channel);
        return this;
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

    public Set<Channel> getChannelByRoomIdAndClientIdList(UUID... clientId) {
       return CHANNEL_GROUP_MAP.values().stream().flatMap(Collection::stream)
                .filter(c-> Arrays.stream(clientId).toList().contains(((ClientChannel)c).getClientId()))
                .collect(Collectors.toSet());
    }

    public void removeRoom(UUID roomId) {
        roomRepository.deleteById(roomId);
        this.CHANNEL_GROUP_MAP.remove(roomId);
    }

    public void removeChannel(UUID roomId, UUID clientID) {
        if(CHANNEL_GROUP_MAP.containsKey(roomId)){
            Optional<Channel> channelOpt = CHANNEL_GROUP_MAP.get(roomId).stream().filter(ch->((ClientChannel)ch).getClientId()==clientID).findAny();
            channelOpt.ifPresent(channel -> CHANNEL_GROUP_MAP.get(roomId).remove(channel));
            if(CHANNEL_GROUP_MAP.get(roomId).isEmpty()){
                CHANNEL_GROUP_MAP.remove(roomId);
            }
        }
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
        removeRoom(room.getId());
        if(roomRepository.findByName(room.getName()).isPresent()){
            roomRepository.deleteById(room.getId());
            return true;
        }
        return false;
    }
}
