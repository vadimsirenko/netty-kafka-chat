package ru.vasire.netty.kafka.chat.server.websocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import ru.vasire.netty.kafka.chat.server.websocket.dto.ResponseDto;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Client;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Message;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;

public class ChatWebSocketService {
    private static final String WEBSOCKET_PATH = "/websocket";
    private WebSocketServerHandshaker handshaker;
    private final Client client;

    public ChatWebSocketService(Client client, ChannelHandlerContext ctx) {
        this.client = client;
        ChatEngineService.addClient(client, ctx.channel());
    }

    public void createHandshaker(ChannelHandlerContext ctx, HttpRequest request) {

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, true);
        handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            ChannelFuture channelFuture = handshaker.handshake(ctx.channel(), request);

            // After the handshake is successful, the business logic
            if (channelFuture.isSuccess()) {
                if (client.getId() == 0) {
                    System.out.println(ctx.channel() + " tourist");
                }
            }
        }
    }

    public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            System.out.println(ctx.channel() + " closed");
        } else if (frame instanceof PingWebSocketFrame) // binary date
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
        else if (!(frame instanceof TextWebSocketFrame)) // text data
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        else
            broadcast(ctx, frame);
    }

    private void broadcast(ChannelHandlerContext ctx, WebSocketFrame frame) {
        try {
            if (client.getId() == 0) {
                ResponseDto res = new ResponseDto(1001, "You can't chat without logging in");
                String json = new ObjectMapper().writeValueAsString(res);
                ctx.channel().write(new TextWebSocketFrame(json));
            } else {
                String req = ((TextWebSocketFrame) frame).text();
                System.out.println("Received " + ctx.channel() + req);

                Message message = MessageService.messageEncode(client, req);

                ResponseDto res = MessageService.sendMessage(client, message);

                String json = new ObjectMapper().writeValueAsString(res);

                ChatEngineService.getChannelToSendMessage(client, message).forEach(c->c.writeAndFlush(new TextWebSocketFrame(json)));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private static String getWebSocketLocation(HttpRequest req) {
        String location = req.headers().get(HOST) + WEBSOCKET_PATH;
        return "ws://" + location;
    }

    public void handlerRemoved(ChannelHandlerContext ctx) {
        ChatEngineService.removeClient(client, ctx.channel());
    }
}
