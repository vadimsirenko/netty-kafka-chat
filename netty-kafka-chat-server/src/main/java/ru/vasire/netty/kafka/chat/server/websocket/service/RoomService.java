package ru.vasire.netty.kafka.chat.server.websocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import ru.vasire.netty.kafka.chat.server.websocket.dto.OperationType;
import ru.vasire.netty.kafka.chat.server.websocket.dto.request.RequestRoomDto;
import ru.vasire.netty.kafka.chat.server.websocket.dto.response.ResponseDto;
import ru.vasire.netty.kafka.chat.server.websocket.dto.response.ResponseRoomDto;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Client;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Room;
import ru.vasire.netty.kafka.chat.server.repository.RoomRepository;

import java.util.Optional;

@RequiredArgsConstructor
public final class RoomService {

    private final RoomRepository roomRepository;

    public static ResponseRoomDto createResponse(OperationType operationType, Room room) {
        ResponseRoomDto res = new ResponseRoomDto(
                room.getId(),
                room.getName(),
                room.getMessageCount(),
                System.currentTimeMillis(),
                operationType);
        return res;
    }

    public void processRoom(Client client, String requestJson) throws JsonProcessingException {
        try {
            RequestRoomDto roomDto = new ObjectMapper().readValue(requestJson, RequestRoomDto.class);

            if (!validateRoom(roomDto))
                throw new RuntimeException("Message is not valid");

            if (roomDto.getOperationType() == OperationType.CREATE &&
                    roomRepository.findByName(roomDto.getName()).isEmpty()) {
                Room room = new Room();
                room.setName(roomDto.getName());
                room.setMessageCount(0);
                roomRepository.save(room);
                sendRoom(OperationType.CREATE, room);
            } else if (roomDto.getOperationType() == OperationType.UPDATE) {
                Optional<Room> existsRoom = roomRepository.findById(roomDto.getId());
                if (existsRoom.isPresent() && !existsRoom.get().getName().equals(roomDto.getName())) {
                    Room room = existsRoom.get();
                    room.setName(roomDto.getName());
                    roomRepository.save(room);
                    sendRoom(OperationType.UPDATE, room);
                }
            } else if (roomDto.getOperationType() == OperationType.DELETE) {
                Optional<Room> deletedRoom = roomRepository.findById(roomDto.getId());
                if (deletedRoom.isPresent()) {
                    Room room = deletedRoom.get();
                    roomRepository.delete(room);
                    sendRoom(OperationType.DELETE, room);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendRoom(OperationType operationType, Room room) throws JsonProcessingException {
        ResponseDto res = RoomService.createResponse(operationType, room);
        String json = new ObjectMapper().writeValueAsString(res);
        ChatEngineService.getChannelToSendRoom().forEach(c -> c.writeAndFlush(new TextWebSocketFrame(json)));
    }

    /**
     * Check that room structure is valid
     *
     * @param roomDto
     * @return
     */
    private static boolean validateRoom(RequestRoomDto roomDto) {
        return true;
    }
}
