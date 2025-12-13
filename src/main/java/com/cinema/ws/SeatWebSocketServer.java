package com.cinema.ws;

import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 实时选座 WebSocket 服务
 * URL格式: ws://localhost:8081/ws/seats/{showId}
 */
@ServerEndpoint("/ws/seats/{showId}")
@Component
public class SeatWebSocketServer {

    // 存储每个场次对应的连接集合: Map<showId, Set<Session>>
    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Session>> showSessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("showId") String showId) {
        // 当用户打开某个场次的选座页面时，加入集合
        showSessions.computeIfAbsent(showId, k -> new CopyOnWriteArraySet<>()).add(session);
        System.out.println("用户进入场次 [" + showId + "] 选座, 当前在线: " + showSessions.get(showId).size());
    }

    @OnClose
    public void onClose(Session session, @PathParam("showId") String showId) {
        // 用户关闭页面时移除
        CopyOnWriteArraySet<Session> sessions = showSessions.get(showId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                showSessions.remove(showId);
            }
        }
        System.out.println("用户离开场次 [" + showId + "]");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    /**
     * 广播消息：通知该场次的所有用户刷新座位图
     * @param showId 场次ID
     * @param message 消息内容 (通常是 "UPDATE")
     */
    public static void fireUpdate(String showId, String message) {
        CopyOnWriteArraySet<Session> sessions = showSessions.get(showId);
        if (sessions != null) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        // 发送异步消息
                        session.getAsyncRemote().sendText(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}