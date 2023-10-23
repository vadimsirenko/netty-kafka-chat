package ru.vasire.netty.kafka.chat.server.netty.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vasire.netty.kafka.chat.server.entity.Client;

@Component
@RequiredArgsConstructor
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketHttpHandler webSocketHttpHandler;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws JsonProcessingException {
        if (msg instanceof HttpRequest)
            webSocketHttpHandler.processHttpRequest(ctx, (HttpRequest) msg);
        else if (msg instanceof WebSocketFrame)
            webSocketHttpHandler.processWebSocketRequest(ctx, (WebSocketFrame) msg);
        else
            System.err.println("Unknown request type: " + msg.getClass().getName());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("Received " + incoming.remoteAddress() + " handshake request");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        webSocketHttpHandler.handlerRemoved(ctx);
    }
}
