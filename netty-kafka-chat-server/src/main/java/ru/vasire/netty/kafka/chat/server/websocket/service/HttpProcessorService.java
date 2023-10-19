package ru.vasire.netty.kafka.chat.server.websocket.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import ru.vasire.netty.kafka.chat.server.websocket.entity.Client;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpProcessorService {
    private static final String HTTP_PARAM_REQUEST = "request";
    public static ChatWebSocketService processRequest(ChannelHandlerContext ctx, HttpRequest request){
        // Handle a bad request.
        if (!request.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST), null);
            return null;
        }

        // Allow only GET methods.
        if (request.method() != GET) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN), "Allow only GET methods");
            return null;
        }

        if ("/favicon.ico".equals(request.uri()) || "/".equals(request.uri())) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND), null);
            return null;
        }

        Map<String, List<String>> requestParams = new QueryStringDecoder(request.uri()).parameters();

        if (requestParams.isEmpty() || !requestParams.containsKey(HTTP_PARAM_REQUEST)) {
            System.err.println(HTTP_PARAM_REQUEST + " parameters are not default");
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND), null);
            return null;
        }

        Client client = ClientService.clientRegister(requestParams.get(HTTP_PARAM_REQUEST).get(0));

        if (client.getRoomId() == null) {
            System.err.println("Room is not default");
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND), "Room is not assign");
            return null;
        }

        ChatWebSocketService socketService = new ChatWebSocketService(client, ctx);
        socketService.createHandshaker(ctx, request);
        return socketService;
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, FullHttpResponse res, String messageText) {
        String body = res.status().toString()+ ((messageText!=null && !messageText.isEmpty()) ? String.format(" Error: %", messageText) : "");
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
}
