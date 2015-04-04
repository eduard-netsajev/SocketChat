package client;

import common.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

public class ChatClient extends Application {
    // IO streams
    ObjectOutputStream toServer;
    ObjectInputStream fromServer;

    String name = "Client " + new Random().nextInt(777);

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {

        Label lbl = new Label("Username: ");
        TextField username = new TextField();
        HBox hbox = new HBox(lbl, username);
        primaryStage.setTitle("ChatClient");
        primaryStage.setScene(new Scene(hbox));

        username.setOnAction(o -> {

            name = username.getText().trim();


            // Panel p to hold the label and text field
            BorderPane paneForTextField = new BorderPane();
            paneForTextField.setPadding(new Insets(5, 5, 5, 5));
            paneForTextField.setStyle("-fx-border-color: green");
            paneForTextField.setLeft(new Label("Input message: "));

            TextField tf = new TextField();
            tf.setAlignment(Pos.BOTTOM_LEFT);
            paneForTextField.setCenter(tf);

            BorderPane mainPane = new BorderPane();
            // Text area to display contents
            TextArea ta = new TextArea();
            mainPane.setCenter(new ScrollPane(ta));
            mainPane.setTop(paneForTextField);
            ta.setEditable(false);

            // Create a scene and place it in the stage
            Scene scene = new Scene(mainPane, 1350, 600);
            primaryStage.setTitle("ChatClient ### " + name); // Set the stage title
            primaryStage.setScene(scene); // Place the scene in the stage

            tf.setOnAction(e -> {
                try {
                    // Get the radius from the text field
                    String text = tf.getText().trim();
                    Message msg = new Message(name, text);

                    // Send the radius to the server
                    toServer.writeObject(msg);
                    toServer.flush();
                    System.out.println(msg);
                    tf.clear();
                }
                catch (IOException ex) {
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
                                ta.appendText(msg.getAuthor() + ": " + msg.getText() + "\n");
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
                // Create a socket to connect to the server
                Socket socket = new Socket("localhost", 8000);
                //Socket socket = new Socket("192.168.0.101", 8000);

                // Create an input stream to receive data from the server
                fromServer = new ObjectInputStream(socket.getInputStream());

                // Create an output stream to send data to the server
                toServer = new ObjectOutputStream(socket.getOutputStream());
            }
            catch (IOException ex) {
                ta.appendText(ex.toString() + '\n');
            }


        });

        primaryStage.show(); // Display the stage
    }
}