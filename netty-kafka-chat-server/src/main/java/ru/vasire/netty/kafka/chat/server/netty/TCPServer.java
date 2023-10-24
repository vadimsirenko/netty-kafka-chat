package ru.vasire.netty.kafka.chat.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
@RequiredArgsConstructor
public class TCPServer {
    private final ServerBootstrap serverBootstrap;
    private final InetSocketAddress tcpPort;

    public void start() throws Exception {
        ChannelFuture f = serverBootstrap.bind(tcpPort).sync();
        f.channel().closeFuture().sync();
    }
}
