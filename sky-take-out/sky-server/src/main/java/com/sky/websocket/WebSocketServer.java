package com.sky.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务端点
 */
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 建立连接
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        SESSION_MAP.put(sid, session);
        log.info("客户端连接成功, sid: {}, 远程地址: {}", sid, session.getBasicRemote());
    }

    /**
     * 关闭连接
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        SESSION_MAP.remove(sid);
        log.info("客户端关闭连接, sid: {}", sid);
    }

    /**
     * 收到消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("收到来自客户端 {} 的消息: {}", sid, message);
    }

    /**
     * 连接异常
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 发生错误", error);
    }

    /**
     * 向所有在线客户端发送消息
     */
    public static void sendToAllClient(String message) {
        SESSION_MAP.forEach((sid, session) -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("向客户端 {} 发送消息失败", sid, e);
            }
        });
    }
}
