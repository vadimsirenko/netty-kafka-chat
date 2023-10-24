package ru.vasire.netty.kafka.chat.server.service.message;

import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.dto.InfoDto;
import ru.vasire.netty.kafka.chat.server.dto.OPERATION_TYPE;

@Service
public final class InfoService {
    public static InfoDto getLogoffInfo(String messageText) {
        return new InfoDto(OPERATION_TYPE.LOGOFF, messageText);
    }

    public static InfoDto getLogonInfo(String nickName) {
        return new InfoDto(OPERATION_TYPE.LOGON, nickName);
    }
}


