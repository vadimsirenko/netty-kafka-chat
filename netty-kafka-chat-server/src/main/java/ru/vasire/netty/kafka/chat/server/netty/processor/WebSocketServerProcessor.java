package ru.vasire.netty.kafka.chat.server.netty.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vasire.netty.kafka.chat.server.dto.ErrorDto;
import ru.vasire.netty.kafka.chat.server.entity.Client;
import ru.vasire.netty.kafka.chat.server.service.ChatEngineService;
import ru.vasire.netty.kafka.chat.server.service.message.ClientService;
import ru.vasire.netty.kafka.chat.server.service.message.RequestService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Component
@RequiredArgsConstructor
public class WebSocketServerProcessor {
    private static final String HTTP_PARAM_REQUEST = "request";
    private final RequestService requestService;
    private final ChatEngineService chatEngineService;
    public Optional<Client> processHttpRequest(ChannelHandlerContext ctx, HttpRequest request) {
        // Handle a bad request.
        if (!request.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST), null);
            return Optional.empty();
        }

        // Allow only GET methods.
        if (request.method() != GET) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN), "Allow only GET methods");
            return Optional.empty();
        }

        if ("/favicon.ico".equals(request.uri()) || "/".equals(request.uri())) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND), null);
            return Optional.empty();
        }

        Map<String, List<String>> requestParams = new QueryStringDecoder(request.uri()).parameters();

        if (requestParams.isEmpty() || !requestParams.containsKey(HTTP_PARAM_REQUEST)) {
            System.err.println(HTTP_PARAM_REQUEST + " parameters are not default");
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND), null);
            return Optional.empty();
        }
        Client client = ClientService.clientRegister(requestParams.get(HTTP_PARAM_REQUEST).get(0));
/*
        if (client.getRoomId() == null) {
            System.err.println("Room is not default");
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND), "Room is not assign");
            return Optional.empty();
        }

 */
        chatEngineService.addClient(client, ctx.channel());
        return Optional.of(client);
    }
    public void handleWebSocketRequest(Client client, WebSocketServerHandshaker handshaker, ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            System.out.println(ctx.channel() + " closed");
        } else if (frame instanceof PingWebSocketFrame) // binary date
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
        else if (!(frame instanceof TextWebSocketFrame)) // text data
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        else
            broadcast(client, ctx, frame);
    }
    private void broadcast(Client client, ChannelHandlerContext ctx, WebSocketFrame frame) {
        try {
            if (client.getId() == null) {
                ErrorDto res = new ErrorDto(1001, "You can't chat without logging in");
                String json = new ObjectMapper().writeValueAsString(res);
                ctx.channel().write(new TextWebSocketFrame(json));
            } else {
                String req = ((TextWebSocketFrame) frame).text();
                System.out.println("Received " + ctx.channel() + req);
                requestService.processRequest(client, req, ctx.channel());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, FullHttpResponse res, String messageText) {
        String body = res.status().toString() + ((messageText != null && !messageText.isEmpty()) ? String.format(" Error: %s", messageText) : "");
        if (res.status().code() != HttpResponseStatus.OK.code()) {
            ByteBuf buf = Unpooled.copiedBuffer(body, CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != HttpResponseStatus.OK.code()) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
    public void removeClient(Client client) {
        chatEngineService.removeClient(client);
    }
}
