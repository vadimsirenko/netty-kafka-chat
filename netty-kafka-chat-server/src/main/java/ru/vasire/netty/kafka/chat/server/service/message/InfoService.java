package ru.vasire.netty.kafka.chat.server.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.dto.InfoDto;
import ru.vasire.netty.kafka.chat.server.dto.OPERATION_TYPE;

import java.util.List;
import java.util.Set;

@Service
public final class InfoService {

    private void sendInfoMessage(Set<Channel> channels, OPERATION_TYPE operationType,String messageText){
        InfoDto infoDto = new InfoDto(operationType, messageText);
        String json = null;
        try {
            json = new ObjectMapper().writeValueAsString(infoDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String finalJson = json;
        channels.forEach(c -> c.writeAndFlush(new TextWebSocketFrame(finalJson)));
    }

    public void sendLogoffInfo(Set<Channel> channels, String nickName){
        sendInfoMessage(channels, OPERATION_TYPE.LOGOFF, nickName);
    }

    public void sendLogonInfo(Set<Channel> channels, String nickName){
        sendInfoMessage(channels, OPERATION_TYPE.LOGON, nickName);
    }
}


