package ru.vasire.netty.kafka.chat.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.vasire.netty.kafka.chat.server.dto.UserProfileDto;
import ru.vasire.netty.kafka.chat.server.entity.Client;

@Mapper
public interface UserProfileDtoMapper {
    UserProfileDtoMapper INSTANCE = Mappers.getMapper(UserProfileDtoMapper.class);

    UserProfileDto ClientToUserProfileDto(Client client);
}
