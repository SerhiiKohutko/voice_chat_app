package org.example.voice_chat_app;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<WebSocketSession, String> userNames = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, String> userRoomPairs = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Queue<String>> roomMessages = new ConcurrentHashMap<>();

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm");


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        userRoomPairs.put(session, "general");
        rooms.putIfAbsent("general", new HashSet<>());
        rooms.get("general").add(session);

        roomMessages.putIfAbsent("general", new LinkedList<>());

        for (String message : roomMessages.get("general")) {
            session.sendMessage(new TextMessage(message));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        if (message.getPayload().equals("[join_voice]")) {
            switchRoom(session, "voice_chat");
            return;
        }

        if (message.getPayload().startsWith("[set_name]")) {
            String name = message.getPayload().split("\\.")[1];
            userNames.put(session, name);
            sendMessageToAllUsersInTheRoom(userNames.get(session) + " connected to the room", "general");
            return;
        }
        if (message.getPayload().startsWith("[join_room]")) {
            String roomName = message.getPayload().split("\\.")[1];
            switchRoom(session, roomName);
            return;
        }

        String[] destructedMessage = destructMessage(message);
        String roomName = destructedMessage[0];
        String userMessage = destructedMessage[1];

        roomMessages.putIfAbsent(roomName, new LinkedList<>());
        roomMessages.get(roomName).add(userMessage);

        sendMessageToAllUsersInTheRoom(userMessage, roomName);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        rooms.get(userRoomPairs.get(session)).remove(session);
        sendMessageToAllUsersInTheRoom(userNames.get(session) + " left", "general");
        userNames.remove(session);
        userRoomPairs.remove(session);
    }


    private void sendMessageToAllUsersInTheRoom(String message, String roomName) throws IOException {
        for (WebSocketSession session : rooms.get(roomName)) {
            session.sendMessage(new TextMessage(message));
        }
    }

    private void switchRoom(WebSocketSession session, String roomName) throws IOException {

        String prevRoom = userRoomPairs.get(session);

        if (prevRoom != null && rooms.containsKey(prevRoom)) {
            rooms.get(prevRoom).remove(session); // removing client from the previous room
        }

        rooms.putIfAbsent(roomName, new HashSet<>());
        rooms.get(roomName).add(session); // putting client into the new room

        userRoomPairs.put(session, roomName); // updating client state

        roomMessages.putIfAbsent(roomName, new LinkedList<>());
        for (String message : roomMessages.get(roomName)) {
            session.sendMessage(new TextMessage(message)); // sending all the messages of the room to refreshed client gui
        }
    }

    private String[] destructMessage(TextMessage message) {
        String date = dateFormat.format(new Date());
        String userMessage = message.getPayload();

        int lBracket = userMessage.indexOf("]");
        String roomName = userMessage.substring(1, lBracket);
        userMessage = userMessage.substring(lBracket + 1).trim();
        userMessage = "["+ date +"] " + userMessage;

        return new String[] {roomName, userMessage};
    }

}
