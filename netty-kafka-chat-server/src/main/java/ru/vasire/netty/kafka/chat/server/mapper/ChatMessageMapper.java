package ru.vasire.netty.kafka.chat.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.vasire.netty.kafka.chat.server.dto.ChatMessageDto;
import ru.vasire.netty.kafka.chat.server.entity.ChatMessage;

@Mapper
public interface ChatMessageMapper {
    ChatMessageMapper INSTANCE = Mappers.getMapper( ChatMessageMapper.class );
    @Mapping(ignore = true, target = "messageType")
    @Mapping(ignore = true, target = "operationType")
    ChatMessageDto ChatMessageToChatMessageDto(ChatMessage chatMessage);

    ChatMessage ChatMessageDtoToChatMessage(ChatMessageDto chatMessageDto);
}

