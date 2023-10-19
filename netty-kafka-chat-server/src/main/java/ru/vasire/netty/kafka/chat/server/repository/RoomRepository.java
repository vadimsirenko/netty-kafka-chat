package ru.vasire.netty.kafka.chat.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Room;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findByName(String name);
}
