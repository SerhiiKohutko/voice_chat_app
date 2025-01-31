package org.example.voice_chat_app;

public class Test {

    public static void main(String[] args) {

        String message = "[room] [test_name] test_message";

        int lBracket = message.indexOf("]");

        String roomName = message.substring(0, lBracket + 1);

        String userMessage = message.substring(lBracket + 1).trim();

        System.out.println(roomName);
        System.out.println(userMessage);


    }
}
