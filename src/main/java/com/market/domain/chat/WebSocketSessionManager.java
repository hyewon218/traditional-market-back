package com.market.domain.chat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketSessionManager {

    // Store sessions mapped by room ID (key: roomId, value: list of WebSocket sessions)
    private final Map<Long, CopyOnWriteArrayList<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // Add a session to a specific room
    public void addSessionToRoom(Long roomId, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    // Remove a session from a specific room
    public void removeSessionFromRoom(Long roomId, WebSocketSession session) {
        List<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
    }

    public List<WebSocketSession> getSessionsForRoom(Long roomId) { // 특정 채팅방의 모든 세션 가져오기
        return roomSessions.getOrDefault(roomId, new CopyOnWriteArrayList<>());
    }
}