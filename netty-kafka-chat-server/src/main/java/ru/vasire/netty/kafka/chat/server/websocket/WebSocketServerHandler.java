package ru.vasire.netty.kafka.chat.server.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import ru.vasire.netty.kafka.chat.server.websocket.dto.ResponseDto;
import ru.vasire.netty.kafka.chat.server.websocket.entity.ChatChannel;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Client;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Message;
import ru.vasire.netty.kafka.chat.server.websocket.service.MessageService;
import ru.vasire.netty.kafka.chat.server.websocket.service.ClientService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/websocket";
    private static final String HTTP_PARAM_REQUEST = "request";
    private static final Map<Long, ChannelGroup> CHANNEL_GROUP_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Long> CHAT_USERS = new ConcurrentHashMap<>();
    private static Long newClientIndex = 1L;

    private Client client;
    private WebSocketServerHandshaker handshaker;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest)
            handleHttpRequest(ctx, (HttpRequest)msg);
        else if (msg instanceof WebSocketFrame)
            handleWebSocketFrame(ctx, (WebSocketFrame)msg);
        else
            System.err.println("Unknown request type: " + msg.getClass().getName());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest request) {
        // Handle a bad request.
        if (!request.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (request.method() != GET) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        if ("/favicon.ico".equals(request.uri()) || "/".equals(request.uri())) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }

        Map<String, List<String>> requestParams = new QueryStringDecoder(request.uri()).parameters();

        if (requestParams.isEmpty() || !requestParams.containsKey(HTTP_PARAM_REQUEST)) {
            System.err.println(HTTP_PARAM_REQUEST + " parameters are not default");
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }

        client = ClientService.clientRegister(requestParams.get(HTTP_PARAM_REQUEST).get(0));

        if (CHAT_USERS.containsKey(client.getName())) {
            System.err.println("User name is not aviable");
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, CONFLICT));
            return;
        }

        if (client.getChatId() == 0) {
            System.err.println("Chat is not default");
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }

        // If it does not exist in the room list, it is the channel, then add a channel ChannelGroup
        if (!CHANNEL_GROUP_MAP.containsKey(client.getChatId())) {
            CHANNEL_GROUP_MAP.put(client.getChatId(), new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        }

        // Make sure there is a room number before adding the message to the channel
        CHANNEL_GROUP_MAP.get(client.getChatId()).add(new ChatChannel(newClientIndex, ctx.channel()));
        client.setId(newClientIndex);
        CHAT_USERS.put(client.getName(), newClientIndex);
        newClientIndex++;

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

    private void broadcast(ChannelHandlerContext ctx, WebSocketFrame frame) {
        try {
            if (client.getId() == 0) {
                ResponseDto res = new ResponseDto(1001, "You can't chat without logging in");
                String json = new ObjectMapper().writeValueAsString(res);
                ctx.channel().write(new TextWebSocketFrame(json));
            } else {
                String req = ((TextWebSocketFrame)frame).text();
                System.out.println("Received " + ctx.channel() + req);

                Message message = MessageService.messageEncode(client, req);

                ResponseDto res = MessageService.sendMessage(client, message);

                String json = new ObjectMapper().writeValueAsString(res);

                if (CHANNEL_GROUP_MAP.containsKey(client.getChatId())) {
                    if(message.getRecipient().isBlank()) {
                        CHANNEL_GROUP_MAP.get(client.getChatId()).writeAndFlush(new TextWebSocketFrame(json));
                    } else {
                        CHANNEL_GROUP_MAP.get(client.getChatId()).stream().filter(c -> ((ChatChannel) c).getClientId() == client.getId()).findFirst().get().writeAndFlush(new TextWebSocketFrame(json));
                    }
                    if (CHAT_USERS.containsKey(message.getRecipient())){
                        CHANNEL_GROUP_MAP.get(client.getChatId()).stream().filter(c->((ChatChannel)c).getClientId()==CHAT_USERS.get(message.getRecipient()))
                                .forEach(c->c.writeAndFlush(new TextWebSocketFrame(json)));
                    }
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
            System.out.println(ctx.channel() + " closed");
        } else if (frame instanceof PingWebSocketFrame) // binary date
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
        else if (!(frame instanceof TextWebSocketFrame)) // text data
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        else
            broadcast(ctx, frame);
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, FullHttpResponse res) {
        if (res.status().code() != HttpResponseStatus.OK.code()) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        ChannelFuture f = ctx.channel().writeAndFlush(res);

        if (!HttpUtil.isKeepAlive(req) || res.status().code() != HttpResponseStatus.OK.code()) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
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
        if (client != null && CHANNEL_GROUP_MAP.containsKey(client.getChatId())) {
            CHANNEL_GROUP_MAP.get(client.getChatId()).remove(ctx.channel());
            CHAT_USERS.remove(client.getName());
        }
    }

    private static String getWebSocketLocation(HttpRequest req) {
        String location = req.headers().get(HOST) + WEBSOCKET_PATH;
        return "ws://" + location;
    }

}
