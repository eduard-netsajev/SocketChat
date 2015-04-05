package client;

import common.Message;
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

public class ChatClient extends Application {
    ObjectOutputStream toServer;
    ObjectInputStream fromServer;

    TextField username;
    TextField address;
    TextField port;

    Stage primary;

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
        HBox usernameInputBox = createUsernameInputBox();

        Button proceedButton = new Button("OK");
        proceedButton.setOnAction(e -> openChat());

        HBox userInput = new HBox(usernameInputBox, proceedButton);
        return new Scene(userInput);
    }

    private HBox createUsernameInputBox() {
        Label usernameLabel = new Label("Username: ");
        username = new TextField();
        username.setOnAction(e -> openChat());
        return new HBox(usernameLabel, username);
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

    private void openChat() {

            String name = username.getText().trim();

            BorderPane paneForTextField = new BorderPane();
            paneForTextField.setPadding(new Insets(5, 5, 5, 5));
            paneForTextField.setLeft(new Label("Input message: "));

            TextField textField = new TextField();
            textField.setAlignment(Pos.BOTTOM_LEFT);
            paneForTextField.setCenter(textField);

            BorderPane mainPane = new BorderPane();
            TextArea textArea = new TextArea();
            mainPane.setCenter(new ScrollPane(textArea));
            mainPane.setTop(paneForTextField);
            textArea.setEditable(false);

            Scene scene = new Scene(mainPane, 1350, 600);
            primary.setTitle(address.getText() + ":" + port.getText() + "### ChatClient ### " + name);
            primary.setScene(scene);

            textField.setOnAction(e -> {
                try {

                    String text = textField.getText().trim();
                    Message msg = new Message(name, text);

                    toServer.writeObject(msg);
                    toServer.flush();
                    System.out.println(msg);
                    textField.clear();
                } catch (IOException ex) {
                    System.err.println(ex.toString());
                    System.err.println(1);
                }
            });

            new Thread(() -> {
                while(true) {
                    try {
                        Object obj = fromServer.readObject();
                        if (obj instanceof Message) {
                            Message msg = (Message) obj;
                            Platform.runLater(() -> {
                                textArea.appendText(msg.getAuthor() + ": " + msg.getText() + "\n");
                            });
                        }
                    } catch (NullPointerException e) {

                    } catch (IOException|ClassNotFoundException ex) {
                        System.err.println(ex.toString());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

            try {
                Socket socket = new Socket(address.getText(), Integer.parseInt(port.getText()));

                fromServer = new ObjectInputStream(socket.getInputStream());

                toServer = new ObjectOutputStream(socket.getOutputStream());
            }
            catch (IOException ex) {
                textArea.appendText(ex.toString() + '\n');
            }
    }
}