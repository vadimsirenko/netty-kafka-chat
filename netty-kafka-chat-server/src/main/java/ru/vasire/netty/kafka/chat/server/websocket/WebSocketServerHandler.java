package ru.vasire.netty.kafka.chat.server.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import ru.vasire.netty.kafka.chat.server.websocket.service.ChatWebSocketService;
import ru.vasire.netty.kafka.chat.server.websocket.service.HttpProcessorService;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private ChatWebSocketService chatWebSocketService;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest)
            chatWebSocketService = HttpProcessorService.processRequest(ctx, (HttpRequest) msg);
        else if (msg instanceof WebSocketFrame)
            chatWebSocketService.handleWebSocketFrame(ctx, (WebSocketFrame) msg);
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
        chatWebSocketService.handlerRemoved(ctx);
    }
}
