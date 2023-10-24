package ru.vasire.netty.kafka.chat.server.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.dto.BaseDto;
import ru.vasire.netty.kafka.chat.server.dto.OPERATION_TYPE;
import ru.vasire.netty.kafka.chat.server.dto.RoomDto;
import ru.vasire.netty.kafka.chat.server.dto.RoomListDto;
import ru.vasire.netty.kafka.chat.server.entity.Client;
import ru.vasire.netty.kafka.chat.server.entity.Room;
import ru.vasire.netty.kafka.chat.server.mapper.RoomMapper;
import ru.vasire.netty.kafka.chat.server.repository.RoomChannelRepository;
import ru.vasire.netty.kafka.chat.server.repository.RoomRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public final class RoomService {

    private final RoomRepository roomRepository;

     public RoomDto processRequest(String requestJson) throws JsonProcessingException {
        try {
            RoomDto roomDto = new ObjectMapper().readValue(requestJson, RoomDto.class);
            Room room = RoomMapper.INSTANCE.RoomDtoToRoom(roomDto);
            if (room == null)
                throw new RuntimeException("ChatMessage is not valid");
            room = roomRepository.saveAndFlush(room);
            return RoomMapper.INSTANCE.RoomToRoomDto(room);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public RoomListDto getRoomList(UUID clientId){
         RoomListDto roomListDto = new RoomListDto();
         roomListDto.setRooms(roomRepository.findAll().stream().map(RoomMapper.INSTANCE::RoomToRoomDto).toList());
         return roomListDto;
    }
}
