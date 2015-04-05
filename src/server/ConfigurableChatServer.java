package server;

import common.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ConfigurableChatServer extends Application implements ChatServer {

    public static final int TEXT_AREA_PREF_HEIGHT = 600;
    public static final int TEXT_AREA_PREF_WITDH = 1350;

    private List<UserSession> userList = new ArrayList<>();

    private TextArea mainTextArea = new TextArea();
    private Label userCountLabel = new Label("Users online: 0");

    public void registerUser(UserSession user) {
        userList.add(user);
        updateCountLabel();
    }

    public void unregisterUser(UserSession user) {
        userList.remove(user);
        updateCountLabel();
    }

    private void updateCountLabel() {
        Platform.runLater(() -> {
            userCountLabel.setText("Users online: " + userList.size());
        });
    }

    public void sendMessage(Message message) {
        Platform.runLater(() -> {
            mainTextArea.appendText(message.toString() + '\n');
        });

        broadcastMessage(message);
    }

    private void broadcastMessage(Message msg) {
        for(UserSession user : userList) {
            user.sendMessage(msg);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Create a scene and place it in the stage
        VBox hbox = new VBox(30, mainTextArea, userCountLabel);
        userCountLabel.setPadding(new Insets(10, 30, 10, 30));
        Scene scene = new Scene(new ScrollPane(hbox), 1400, 750);
        primaryStage.setTitle("ChatServer"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage
        configureMainTextArea();

        try {
            ServerSocket serverSocket = new ServerSocket(8000);
            new ConnectionListeningThread(this, serverSocket).start();
        } catch (IOException e) {
            System.out.println("Couldn't open ServerSocket: " + e.getMessage());
        }
    }

    private void configureMainTextArea() {
        mainTextArea.setEditable(false);
        mainTextArea.setPrefWidth(TEXT_AREA_PREF_WITDH);
        mainTextArea.setPrefHeight(TEXT_AREA_PREF_HEIGHT);
    }
}