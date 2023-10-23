package ru.vasire.netty.kafka.chat.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vasire.netty.kafka.chat.server.entity.ChatMessage;
import ru.vasire.netty.kafka.chat.server.entity.Room;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findByRoomId(UUID roomId);
}
