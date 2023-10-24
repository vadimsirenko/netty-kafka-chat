package ru.vasire.netty.kafka.chat.server.netty.handler;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vasire.netty.kafka.chat.server.dto.*;
import ru.vasire.netty.kafka.chat.server.entity.Client;
import ru.vasire.netty.kafka.chat.server.mapper.ClientMapper;
import ru.vasire.netty.kafka.chat.server.service.message.ClientService;
import ru.vasire.netty.kafka.chat.server.service.message.InfoService;
import ru.vasire.netty.kafka.chat.server.service.message.MessageService;
import ru.vasire.netty.kafka.chat.server.service.message.RoomService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/websocket";
    private static final String HTTP_PARAM_REQUEST = "request";

    private static final Map<UUID, ChannelGroup> CHANNEL_GROUP_MAP = new ConcurrentHashMap<>();
    private WebSocketServerHandshaker handshaker;
    private final ClientService clientService;
    private final MessageService messageService;
    private final RoomService roomService;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest)
            handleHttpRequest(ctx, (HttpRequest) msg);
        else if (msg instanceof WebSocketFrame)
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        else
            System.err.println("Unknown request type: " + msg.getClass().getName());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest request) {
        Client client;
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
        client = clientService.clientLogin(requestParams.get(HTTP_PARAM_REQUEST).get(0), ctx.channel().id().asLongText());

        if (client.getRoomId() == null) {
            System.err.println("Room number is not default");
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }
        joinToRoom(ctx);

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, true);
        handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            ChannelFuture channelFuture = handshaker.handshake(ctx.channel(), request);

            // After the handshake is successful, the business logic
            if (channelFuture.isSuccess()) {

                if (client.getId() == null) {
                    System.out.println(ctx.channel() + " tourist");
                } else {
                    String json = null;
                    try {
                        RoomListDto roomListDto = roomService.getRoomList(client.getId());
                        json = new ObjectMapper().writeValueAsString(roomListDto);
                        ctx.channel().writeAndFlush(new TextWebSocketFrame(json));

                        ClientDto clientDto = ClientMapper.INSTANCE.ClientToClientDto(client);
                        json = new ObjectMapper().writeValueAsString(clientDto);
                        ctx.channel().writeAndFlush(new TextWebSocketFrame(json));


                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }


            }
        }
    }

    private UserProfileDto getUserProfile(ChannelHandlerContext ctx){
        return clientService.getPfofile(ctx.channel().id().asLongText());
    }

    private void broadcast(ChannelHandlerContext ctx, WebSocketFrame frame) {
        try {
            if (getUserProfile(ctx).getId() == null) {
                ErrorDto res = new ErrorDto(1001, "You can't chat without logging in");
                String json = new ObjectMapper().writeValueAsString(res);
                ctx.channel().write(new TextWebSocketFrame(json));
            } else {
                String requestJson = ((TextWebSocketFrame) frame).text();
                System.out.println("Received " + ctx.channel() + requestJson);

                BaseDto baseDto = new ObjectMapper().readValue(requestJson, BaseDto.class);

                BaseDto res = switch (baseDto.getMessageType()) {
                    case MESSAGE -> messageService.processRequest(requestJson);
                    case ROOM -> roomService.processRequest(requestJson);
                    case MESSAGE_LIST -> {
                        MessageListDto messageListDto = messageService.processMessageListRequest(requestJson);
                        changeRoom(ctx, messageListDto.getRoomId());
                        yield messageListDto;
                    }
                    default -> new ErrorDto(100, "Unknown request type" + baseDto.getMessageType());
                };
                sendMessage(ctx, res);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void sendMessage(ChannelHandlerContext ctx, BaseDto message) {
        try {
            String json = new ObjectMapper().writeValueAsString(message);
        if(message instanceof RoomMessage)
        {
            sendToRoom(getUserProfile(ctx).getRoomId(), json);
        }else{
            sendToClient(ctx, json);
        }
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
    private void sendToRoom(UUID roomId, String messageJson) {
            if (CHANNEL_GROUP_MAP.containsKey(roomId)) {
                CHANNEL_GROUP_MAP.get(roomId).writeAndFlush(new TextWebSocketFrame(messageJson));
            }
    }

    private void sendToClient(ChannelHandlerContext ctx, String messageJson) {
        ctx.channel().writeAndFlush(new TextWebSocketFrame(messageJson));
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
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
        leaveTheRoom(ctx);
    }

    private void joinToRoom(ChannelHandlerContext ctx) {
        // If it does not exist in the room list, it is the channel, then add a channel ChannelGroup
        if (!CHANNEL_GROUP_MAP.containsKey(getUserProfile(ctx).getRoomId())) {
            CHANNEL_GROUP_MAP.put(getUserProfile(ctx).getRoomId(), new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        }
        // Make sure there is a room number before adding the client to the channel
        if(!CHANNEL_GROUP_MAP.get(getUserProfile(ctx).getRoomId()).contains(ctx.channel())) {
            CHANNEL_GROUP_MAP.get(getUserProfile(ctx).getRoomId()).add(ctx.channel());
            sendMessage(ctx, InfoService.getLogonInfo(getUserProfile(ctx).getNickName()));
        }
    }

    private void leaveTheRoom(ChannelHandlerContext ctx) {
        if (getUserProfile(ctx) != null && getUserProfile(ctx).getRoomId() != null && CHANNEL_GROUP_MAP.containsKey(getUserProfile(ctx).getRoomId())) {
            CHANNEL_GROUP_MAP.get(getUserProfile(ctx).getRoomId()).remove(ctx.channel());
            sendMessage(ctx, InfoService.getLogoffInfo(getUserProfile(ctx).getNickName()));
        }
    }

    public void changeRoom(ChannelHandlerContext ctx, UUID newRoomId) throws Exception {
        if (newRoomId != null) {
            leaveTheRoom(ctx);
            getUserProfile(ctx).setRoomId(newRoomId);
            joinToRoom(ctx);
        }
    }

    private static String getWebSocketLocation(HttpRequest req) {
        String location = req.headers().get(HOST) + WEBSOCKET_PATH;
        return "ws://" + location;
    }
}
