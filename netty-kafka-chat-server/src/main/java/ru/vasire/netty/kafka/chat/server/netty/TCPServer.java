package ru.vasire.netty.kafka.chat.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
@RequiredArgsConstructor
public class TCPServer {

    private final ServerBootstrap serverBootstrap;

    private final InetSocketAddress tcpPort;
    private Channel serverChannel;

    public void start() throws Exception {
        serverChannel = serverBootstrap.bind(tcpPort).sync().channel().closeFuture().sync().channel();
    }

    @PreDestroy
    public void stop() throws Exception {
        if(serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close();
            serverChannel.parent().close();
        }
    }
}
