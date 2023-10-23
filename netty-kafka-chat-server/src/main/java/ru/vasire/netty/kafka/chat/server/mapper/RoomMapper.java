package ru.vasire.netty.kafka.chat.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.vasire.netty.kafka.chat.server.dto.ChatMessageDto;
import ru.vasire.netty.kafka.chat.server.dto.RoomDto;
import ru.vasire.netty.kafka.chat.server.entity.ChatMessage;
import ru.vasire.netty.kafka.chat.server.entity.Room;

@Mapper
public interface RoomMapper {
    RoomMapper INSTANCE = Mappers.getMapper( RoomMapper.class );
    @Mapping(ignore = true, target = "messageType")
    @Mapping(ignore = true, target = "operationType")
    @Mapping(ignore = true, target = "ts")
    RoomDto RoomToRoomDto(Room room);

    Room RoomDtoToRoom(RoomDto roomDto);
}

