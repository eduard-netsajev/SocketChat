package server;

import common.Message;
import common.NumericTextField;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ConfigurableChatServer extends Application implements ChatServer {

    public static final int TEXT_AREA_PREF_HEIGHT = 600;
    public static final int TEXT_AREA_PREF_WIDTH = 1350;

    private List<UserSession> userList = new ArrayList<>();

    private TextArea mainTextArea = new TextArea();
    private Label userCountLabel = new Label("Users online: 0");

    public void registerUser(UserSession user) {
        userList.add(user);
        updateCountLabel();
    }

    public void removeUser(UserSession user) {
        userList.remove(user);
        updateCountLabel();
    }

    private void updateCountLabel() {
        Platform.runLater(() -> userCountLabel.setText("Users online: " + userList.size()));
    }

    public void sendMessage(Message message) {
        Platform.runLater(() -> mainTextArea.appendText(message.toString() + '\n'));
        if (message.getAuthor().length() > 3)
            broadcastMessage(message);
    }

    private void broadcastMessage(Message msg) {
        for(UserSession user : userList) {
            user.sendMessage(msg);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        createAndShowPortConfig(primaryStage);
    }

    private void createAndShowPortConfig(Stage primaryStage) {
        TextField portField = new NumericTextField();

        Label portLabel = new Label("Port: ");
        HBox portInput = new HBox(portLabel, portField);

        Button proceedButton = new Button("OK");
        HBox userInputBox = new HBox(portInput, proceedButton);
        userInputBox.setPadding(new Insets(10));

        portField.setOnAction(e -> startServer(getPort(portField), primaryStage));

        proceedButton.setOnAction(e -> startServer(getPort(portField), primaryStage));

        primaryStage.setScene(new Scene(userInputBox));
        primaryStage.show();
    }

    private int getPort(TextField portField) {
        return Integer.parseInt(portField.getText());
    }

    private void startServer(int port, Stage primaryStage) {
        createAndShowMainScene(primaryStage);
        configureMainTextArea();

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            new ConnectionListeningThread(this, serverSocket).start();
        } catch (Exception e) {
            Platform.runLater(() -> mainTextArea.appendText("Couldn't open ServerSocket: " + e.getMessage()));

        }
    }

    private void createAndShowMainScene(Stage primaryStage) {
        VBox hbox = new VBox(30, mainTextArea, userCountLabel);
        userCountLabel.setPadding(new Insets(10, 30, 10, 30));
        Scene scene = new Scene(new ScrollPane(hbox), 1400, 750);
        primaryStage.setTitle("ChatServer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void configureMainTextArea() {
        mainTextArea.setEditable(false);
        mainTextArea.setPrefWidth(TEXT_AREA_PREF_WIDTH);
        mainTextArea.setPrefHeight(TEXT_AREA_PREF_HEIGHT);
    }
}