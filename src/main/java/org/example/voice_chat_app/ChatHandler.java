package org.example.voice_chat_app;

import org.example.voice_chat_app.Utils.UserRoomPair;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatHandler extends TextWebSocketHandler {
    private final ConcurrentHashMap<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Queue<String>> roomMessages = new ConcurrentHashMap<>();
    private UserRoomPair userRoomPair;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("Session established");
        userRoomPair = new UserRoomPair(session, "general");
        rooms.putIfAbsent(userRoomPair.getRoom(), new HashSet<>()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        if (message.getPayload().startsWith("[join_room]")) {
            String roomName = message.getPayload().split("\\.")[1];
            switchRoom(session, roomName);
            return;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        rooms.get(userRoomPair.getRoom()).remove(session);
    }


    private void switchRoom(WebSocketSession session, String roomName) {
        userRoomPair.setRoom(roomName);
    }

}
