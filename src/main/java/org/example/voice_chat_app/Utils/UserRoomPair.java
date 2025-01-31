package org.example.voice_chat_app.Utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

public class UserRoomPair {
    private WebSocketSession session;

    @Setter
    @Getter
    private String room;
    public UserRoomPair(WebSocketSession session, String room) {
        this.session = session;
        this.room = room;
    }

}
