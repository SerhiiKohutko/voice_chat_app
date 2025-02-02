package org.example.voice_chat_app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;


public class ChatClient extends Application {
    private WebSocketClient client;
    private final TextArea chatArea = new TextArea();
    private final Button startRecord = new Button("Record");
    private final Button stopRecord = new Button("Stop Record");

    private final ComboBox<String> roomSelector = new ComboBox<>();
    private final AudioRecorder audioRecorder = new AudioRecorder();
    private String username;

   @Override
    public void start(Stage primaryStage) {
       TextInputDialog dialog = new TextInputDialog();
       dialog.setTitle("Chat Client");
       dialog.setHeaderText(null);
       dialog.setContentText("Enter Username");

       Optional<String> result = dialog.showAndWait();
       username = result.map(String::trim).orElse("Anonymous");

       TextField inputFiled = new TextField();
       Button sendButton = new Button("Send");

       sendButton.setOnAction(event -> {
           String message = inputFiled.getText();
           String room = roomSelector.getSelectionModel().getSelectedItem();

           if(!message.isEmpty()){
               client.send("[" + room + "]" + "[" + username + "] " +message);
               inputFiled.clear();
           }
       });

       roomSelector.getItems().addAll("general", "games", "music", "voice_chat");
       roomSelector.setValue("general");
       roomSelector.setOnAction(event -> {
           String room = roomSelector.getSelectionModel().getSelectedItem();

           if (room.equals("voice_chat")) {
               client.send("[join_voice]");
               chatArea.clear();
               startRecord.setVisible(true);
               stopRecord.setVisible(true);
               return;
           }

           if(!room.isEmpty()){
               client.send("[join_room]." + room);
               chatArea.clear();
               startRecord.setVisible(false);
               stopRecord.setVisible(false);
           }
       });


       startRecord.setVisible(false);
       stopRecord.setVisible(false);
       startRecord.setOnAction(e -> {

           Task<Void> recordTask = new Task<>() {
               @Override
               protected Void call() {
                   audioRecorder.startRecording();
                   return null;
               }
           };


           Thread recordThread = new Thread(recordTask);
           recordThread.setDaemon(true);
           recordThread.start();


           startRecord.setDisable(true);
           stopRecord.setDisable(false);
       });


       stopRecord.setOnAction(e -> {

           Task<Void> stopTask = new Task<>() {
               @Override
               protected Void call() {
                   audioRecorder.stopRecording();
                   return null;
               }
           };


           Thread stopThread = new Thread(stopTask);
           stopThread.setDaemon(true);
           stopThread.start();


           startRecord.setDisable(false);
           stopRecord.setDisable(true);
       });


       VBox root = new VBox(10, chatArea, inputFiled, sendButton, roomSelector, startRecord, stopRecord);

       Scene rootScene = new Scene(root, 400, 300);
       primaryStage.setTitle("Chat Client");
       primaryStage.setScene(rootScene);
       primaryStage.show();

       System.out.println("Connecting socket");
       connectWebSocket();


   }

    private void connectWebSocket() {
        try {
            client = new WebSocketClient(new URI("ws://localhost:8080/chat")) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    chatArea.appendText("Connected to server\n");
                    client.send("[set_name]."+username);
                }

                @Override
                public void onMessage(String s) {
                    Platform.runLater(() -> chatArea.appendText(s + "\n"));
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    Platform.runLater(() -> chatArea.appendText("Connection closed\n"));
                }

                @Override
                public void onError(Exception e) {
                    Platform.runLater(() -> chatArea.appendText("Error: " + e.getMessage() + "\n"));
                }
            };
            client.connect();

            while (!client.isOpen()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
