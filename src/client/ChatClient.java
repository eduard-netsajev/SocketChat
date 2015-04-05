package client;

import common.Message;
import common.StatusMessage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ChatClient extends Application implements ServerObserver {

    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;

    private TextField address;
    private TextField port;

    private Stage primary;

    private TextArea chatArea;
    private TextField chatInput;

    private String user;

    public void receiveMessage(Message message) {
        Platform.runLater(() -> {
            chatArea.appendText(message.toString() + '\n');
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primary = primaryStage;

        Scene userScene = createUserConfigScene();
        prepareHostConfigScene(userScene);

        primary.setOnCloseRequest(e -> System.exit(0));
        primary.show();
    }

    private Scene createUserConfigScene() {

        TextField usernameField = createUsernameField();
        HBox usernameInputBox = createUsernameBox(usernameField);

        Button proceedButton = new Button("OK");
        proceedButton.setOnAction(e -> launchChat(usernameField));

        HBox userInput = new HBox(usernameInputBox, proceedButton);
        return new Scene(userInput);
    }

    private HBox createUsernameBox(TextField usernameField) {
        Label usernameLabel = new Label("Username: ");
        return new HBox(usernameLabel, usernameField);
    }

    private TextField createUsernameField() {
        TextField username = new TextField();
        username.setOnAction(e -> launchChat(username));
        return username;
    }

    private void launchChat(TextField userField) {
        user = userField.getText().trim();
        createAndOpenChatScene();
        configureChatInputField();
        openSocketStreams();
        informServer();
        new Thread(new ServerListener(fromServer, this)).start();
    }

    private void informServer() {
        Message informationMessage = new StatusMessage(user, " connected to server.");
        sendMessage(informationMessage);
    }

    private void prepareHostConfigScene(Scene nextScene) {
        primary.setTitle("ChatClient ### Connection configuration");

        HBox addressInput = createAddressBox(nextScene);
        HBox portInput = createPortBox(nextScene);
        Button proceedButton = crerateButtonNext(nextScene);

        Scene serverScene = createServerScene(addressInput, portInput, proceedButton);
        primary.setScene(serverScene);
    }

    private Scene createServerScene(HBox addressInput, HBox portInput, Button proceedButton) {
        VBox serverInput = new VBox(10, addressInput, portInput, proceedButton);
        serverInput.setAlignment(Pos.CENTER);
        serverInput.setPadding(new Insets(10, 10, 10, 10));
        return new Scene(serverInput);
    }

    private Button crerateButtonNext(Scene nextScene) {
        Button proceedButton = new Button("OK");
        proceedButton.setOnAction(e -> primary.setScene(nextScene));
        return proceedButton;
    }

    private HBox createPortBox(Scene nextScene) {
        Label portLabel = new Label("Port: ");
        port = new NumericTextField("8000");
        port.setOnAction(e -> primary.setScene(nextScene));
        return new HBox(5, portLabel, port);
    }

    private HBox createAddressBox(Scene nextScene) {
        Label addressLabel = new Label("Server IP: ");
        address = new TextField("127.0.0.1");
        address.setOnAction(e -> primary.setScene(nextScene));
        return new HBox(5, addressLabel, address);
    }

    private void openSocketStreams() {
        try {
            Socket socket = new Socket(address.getText(), Integer.parseInt(port.getText()));
            fromServer = new ObjectInputStream(socket.getInputStream());
            toServer = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (Exception ex) {
            chatArea.appendText("Failed to connect to the server\n");
            chatArea.appendText("Restart application to try again\n");
            chatArea.appendText("\nCaused by: " + ex.toString());
        }
    }

    private void configureChatInputField() {
        chatInput.setOnAction(e -> {
            String text = chatInput.getText().trim();
            Message msg = new Message(user, text);
            sendMessage(msg);
        });
    }

    private void sendMessage(Message msg) {
        try {
            toServer.writeObject(msg);
            toServer.flush();
            chatInput.clear();
        } catch (IOException e) {
            System.err.println("Failed sending message to server: " + e.toString());
        }
    }

    private void createAndOpenChatScene() {
        initChatFields();
        BorderPane paneForTextField = createPaneForText();
        BorderPane mainPane = createMainPane(paneForTextField);

        Scene scene = new Scene(mainPane, 1350, 600);
        primary.setTitle(address.getText() + ":" + port.getText() + " ### ChatClient ### " + user);
        primary.setScene(scene);
    }

    private BorderPane createMainPane(BorderPane paneForTextField) {
        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(new ScrollPane(chatArea));
        mainPane.setTop(paneForTextField);
        return mainPane;
    }

    private void initChatFields() {
        chatInput = new TextField();
        chatInput.setAlignment(Pos.BOTTOM_LEFT);

        chatArea = new TextArea();
        chatArea.setEditable(false);
    }

    private BorderPane createPaneForText() {
        BorderPane paneForTextField = new BorderPane();
        paneForTextField.setPadding(new Insets(5, 5, 5, 5));
        paneForTextField.setLeft(new Label("Input message: "));
        paneForTextField.setCenter(chatInput);
        return paneForTextField;
    }
}