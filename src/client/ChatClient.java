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

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ChatClient ### Connection configuration");
        Label addressLabel = new Label("Server IP: ");
        address = new TextField("127.0.0.1");
        HBox addressInput = new HBox(5, addressLabel, address);

        Label portLabel = new Label("Port: ");
        port = new NumericTextField("8000");

        HBox portInput = new HBox(5, portLabel, port);

        Button proceedButton = new Button("OK");
        VBox serverInput = new VBox(10, addressInput, portInput, proceedButton);
        serverInput.setAlignment(Pos.CENTER);
        serverInput.setPadding(new Insets(10, 10, 10, 10));

        primaryStage.setScene(new Scene(serverInput));

        Label usernameLabel = new Label("Username: ");
        username = new TextField();

        HBox usernameInput = new HBox(usernameLabel, username);
        Button usernameButton = new Button("OK");
        HBox userInput = new HBox(usernameLabel, usernameInput, usernameButton);

        username.setOnAction(e -> primaryStage.setScene(new Scene(userInput)));
        port.setOnAction(e -> primaryStage.setScene(new Scene(userInput)));
        proceedButton.setOnAction(e -> primaryStage.setScene(new Scene(userInput)));

        usernameButton.setOnAction(e -> openChat(primaryStage));
        username.setOnAction(e -> openChat(primaryStage));

        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }

    private void openChat(Stage primaryStage) {

            String name = username.getText().trim();


            BorderPane paneForTextField = new BorderPane();
            paneForTextField.setPadding(new Insets(5, 5, 5, 5));
            paneForTextField.setStyle("-fx-border-color: green");
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
            primaryStage.setTitle(address.getText() + ":" + port.getText() + "### ChatClient ### " + name);
            primaryStage.setScene(scene);

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