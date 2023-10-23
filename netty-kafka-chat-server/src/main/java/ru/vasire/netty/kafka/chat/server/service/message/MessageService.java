package ru.vasire.netty.kafka.chat.server.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.dto.ChatMessageDto;
import ru.vasire.netty.kafka.chat.server.dto.MessageListDto;
import ru.vasire.netty.kafka.chat.server.entity.ChatMessage;
import ru.vasire.netty.kafka.chat.server.entity.Client;
import ru.vasire.netty.kafka.chat.server.mapper.ChatMessageMapper;
import ru.vasire.netty.kafka.chat.server.netty.ChannelRepository;
import ru.vasire.netty.kafka.chat.server.repository.ChatMessageRepository;
import ru.vasire.netty.kafka.chat.server.repository.RoomChannelRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public final class MessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final RoomChannelRepository roomChannelRepository;
    private final ClientService clientService;

    public static ChatMessage messageEncode(String requestJson) {
        try {
            ChatMessageDto chatMessageDto = new ObjectMapper().readValue(requestJson, ChatMessageDto.class);
            if (!validateRequest(chatMessageDto))
                throw new RuntimeException("ChatMessage is not valid");
            ChatMessage chatMessage = ChatMessageMapper.INSTANCE.ChatMessageDtoToChatMessage(chatMessageDto);
            return chatMessage;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void processRequest(String req) throws JsonProcessingException {
        ChatMessage chatMessage = MessageService.messageEncode(req);
        if(!chatMessageRepository.existsById(chatMessage.getId())) {
            chatMessageRepository.saveAndFlush(chatMessage);
        }
        ChatMessageDto res = ChatMessageMapper.INSTANCE.ChatMessageToChatMessageDto(chatMessage);
        String json = new ObjectMapper().writeValueAsString(res);
        roomChannelRepository.getRoomChannels(res.getRoomId()).forEach(c -> c.writeAndFlush(new TextWebSocketFrame(json)));
    }

    public void processMessageListRequest(String req, Channel channel) throws JsonProcessingException {
        MessageListDto chatMessageDto = new ObjectMapper().readValue(req, MessageListDto.class);
        roomChannelRepository.put(chatMessageDto.getRoomId(), channel, clientService.getRoomByClientId(chatMessageDto.getSenderId()));
        clientService.addClientToRoom(chatMessageDto.getSenderId(), chatMessageDto.getRoomId());
        List<ChatMessage> messages = chatMessageRepository.findByRoomId(chatMessageDto.getRoomId());
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
