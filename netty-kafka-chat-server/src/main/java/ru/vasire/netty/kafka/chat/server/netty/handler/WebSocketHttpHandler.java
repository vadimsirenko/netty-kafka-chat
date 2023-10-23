package ru.vasire.netty.kafka.chat.server.netty.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.vasire.netty.kafka.chat.server.entity.Client;
import ru.vasire.netty.kafka.chat.server.netty.processor.WebSocketServerProcessor;
import ru.vasire.netty.kafka.chat.server.service.message.ClientService;
import ru.vasire.netty.kafka.chat.server.service.message.MessageService;
import ru.vasire.netty.kafka.chat.server.service.message.RoomService;

import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;

@Component
@RequiredArgsConstructor
public class WebSocketHttpHandler {
    private static final String WEBSOCKET_PATH = "/websocket";
    Client client;
    private WebSocketServerHandshaker handshaker;

    private final WebSocketServerProcessor webSocketServerProcessor;
    private final RoomService roomService;
    private final ClientService clientService;

    public void processHttpRequest(ChannelHandlerContext ctx, HttpRequest request) throws JsonProcessingException {
        Optional<Client> clientOpt = webSocketServerProcessor.processHttpRequest(ctx, request);
        client = clientOpt.get();
        handshaker = createHandshaker(client, ctx, request);
    }
    public void processWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame frame) {
        webSocketServerProcessor.handleWebSocketRequest(client, handshaker, ctx, frame);
    }
    public void handlerRemoved(ChannelHandlerContext ctx) {
        webSocketServerProcessor.removeClient(client);
    }

    public WebSocketServerHandshaker createHandshaker(Client client, ChannelHandlerContext ctx, HttpRequest request) throws JsonProcessingException {
        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, true);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            ChannelFuture channelFuture = handshaker.handshake(ctx.channel(), request);

            // After the handshake is successful, the business logic
            if (channelFuture.isSuccess()) {
                if (client.getId() != null) {
                    //ctx.channel().
                    clientService.sendClientProfile(client, ctx.channel());
                    roomService.sendRoomList(client.getId(), ctx.channel());
                }
                else{
                    System.out.println(ctx.channel() + " tourist");
                }
            }
        }
        return handshaker;
    }
    private static String getWebSocketLocation(HttpRequest req) {
        String location = req.headers().get(HOST) + WEBSOCKET_PATH;
        return "ws://" + location;
    }
}
