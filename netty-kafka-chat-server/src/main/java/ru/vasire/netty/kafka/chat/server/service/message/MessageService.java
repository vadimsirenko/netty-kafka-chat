package ru.vasire.netty.kafka.chat.server.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.message.Message;
import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.dto.ChatMessageDto;
import ru.vasire.netty.kafka.chat.server.dto.MessageListDto;
import ru.vasire.netty.kafka.chat.server.dto.OPERATION_TYPE;
import ru.vasire.netty.kafka.chat.server.dto.RoomListDto;
import ru.vasire.netty.kafka.chat.server.entity.ChatMessage;
import ru.vasire.netty.kafka.chat.server.entity.Client;
import ru.vasire.netty.kafka.chat.server.entity.Room;
import ru.vasire.netty.kafka.chat.server.mapper.ChatMessageMapper;
import ru.vasire.netty.kafka.chat.server.mapper.RoomMapper;
import ru.vasire.netty.kafka.chat.server.repository.ChatMessageRepository;
import ru.vasire.netty.kafka.chat.server.service.ChatEngineService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public final class MessageService {

    private final ChatEngineService chatEngineService;
    private final ChatMessageRepository chatMessageRepository;

    public static ChatMessage messageEncode(Client client, String requestJson) {
        try {
            ChatMessageDto chatMessageDto = new ObjectMapper().readValue(requestJson, ChatMessageDto.class);

            if (!validateRequest(chatMessageDto))
                throw new RuntimeException("ChatMessage is not valid");

            ChatMessage chatMessage = ChatMessageMapper.INSTANCE.ChatMessageDtoToChatMessage(chatMessageDto);

            chatMessage.setId(UUID.randomUUID());

            if (client.getId() != null)
                chatMessage.setSenderId(client.getId());
            return chatMessage;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void processRequest(Client client, String req) throws JsonProcessingException {
        ChatMessage chatMessage = MessageService.messageEncode(client, req);
        if(!chatMessageRepository.existsById(chatMessage.getId())) {
            chatMessageRepository.saveAndFlush(chatMessage);
        }
        ChatMessageDto res = ChatMessageMapper.INSTANCE.ChatMessageToChatMessageDto(chatMessage);
        String json = new ObjectMapper().writeValueAsString(res);
        chatEngineService.getChannelToSendMessage(client, chatMessage).forEach(c -> c.writeAndFlush(new TextWebSocketFrame(json)));
    }

    public void processMessageListRequest(Client client, String req, Channel channel) throws JsonProcessingException {
        MessageListDto chatMessageDto = new ObjectMapper().readValue(req, MessageListDto.class);
        chatEngineService.addClientToRoom(client, chatMessageDto.getRoomId(), channel);
        List<ChatMessage> messages = chatEngineService.getMessageByRoomId(chatMessageDto.getRoomId());
        chatMessageDto.setMessages(messages.stream().map(ChatMessageMapper.INSTANCE::ChatMessageToChatMessageDto).toList());
        String json = new ObjectMapper().writeValueAsString(chatMessageDto);
        channel.writeAndFlush(new TextWebSocketFrame(json));
    }

    /**
     * Check that message structure is valid
     *
     * @param chatMessageDto
     * @return
     */
    public static boolean validateRequest(ChatMessageDto chatMessageDto) {
        return true;
    }
}
