package ru.vasire.netty.kafka.chat.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vasire.netty.kafka.chat.server.entity.Client;

import java.util.*;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByLogin(String login);
}
