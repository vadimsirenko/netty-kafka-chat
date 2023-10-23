package ru.vasire.netty.kafka.chat.server.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.dto.OPERATION_TYPE;
import ru.vasire.netty.kafka.chat.server.dto.RoomDto;
import ru.vasire.netty.kafka.chat.server.dto.RoomListDto;
import ru.vasire.netty.kafka.chat.server.entity.Client;
import ru.vasire.netty.kafka.chat.server.entity.Room;
import ru.vasire.netty.kafka.chat.server.mapper.RoomMapper;
import ru.vasire.netty.kafka.chat.server.repository.RoomChannelRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public final class RoomService {

    private final RoomChannelRepository roomChannelRepository;

     public void processRequest(String requestJson) throws JsonProcessingException {
        try {
            RoomDto roomDto = new ObjectMapper().readValue(requestJson, RoomDto.class);
            Room room = new Room();
            room.setName(roomDto.getName());
            room.setId(roomDto.getId());

            if (!validateRoom(room))
                throw new RuntimeException("ChatMessage is not valid");

           // boolean isFire = chatEngineService.applyRoom(roomDto.getOperationType(), room);

          //  if(isFire){
                sendRoom(roomDto.getOperationType(), room);
           // }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendRoom(OPERATION_TYPE operationType, Room room) throws JsonProcessingException {
        RoomDto res = RoomMapper.INSTANCE.RoomToRoomDto(room);
        res.setOperationType(operationType);
        String json = new ObjectMapper().writeValueAsString(res);
        roomChannelRepository.getAllChannels().forEach(c -> c.writeAndFlush(new TextWebSocketFrame(json)));
    }
    public void sendRoomList(UUID clientId, Channel channel) throws JsonProcessingException {
        List<Room> rooms = roomChannelRepository.getAllRoom();
        RoomListDto roomListDto = new RoomListDto(OPERATION_TYPE.UPDATE, rooms.stream().map(r-> RoomMapper.INSTANCE.RoomToRoomDto(r)).toList());
        String json = new ObjectMapper().writeValueAsString(roomListDto);
        channel.writeAndFlush(new TextWebSocketFrame(json));
    }
    /**
     * Check that room structure is valid
     *
     * @param room
     * @return
     */
    private static boolean validateRoom(Room room) {
        return true;
    }
}
