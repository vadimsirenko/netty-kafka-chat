package ru.vasire.netty.kafka.chat.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vasire.netty.kafka.chat.server.entity.Room;

import java.util.Optional;
import java.util.UUID;


public interface RoomRepository extends JpaRepository<Room, UUID> {
    Optional<Room> findByName(String name);
}
