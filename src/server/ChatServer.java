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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatServer extends Application {
    // Text area for displaying contents
    private TextArea ta = new TextArea();
    private Label userCount = new Label("Users online: 0");

    private List<UserSession> userList;

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {
        // Create a scene and place it in the stage
        VBox hbox = new VBox(30, ta, userCount);
        userCount.setPadding(new Insets(10, 30, 10, 30));
        Scene scene = new Scene(new ScrollPane(hbox), 1400, 750);
        primaryStage.setTitle("ChatServer"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage
        ta.setEditable(false);
        ta.setPrefWidth(1350);
        ta.setPrefHeight(600);
        new Thread( () -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(8000);
                Platform.runLater(() -> {
                    ta.appendText("MultiThread ChatServer started at "
                            + new Date() + '\n');
                });

                userList = new ArrayList<>(16);

                while (true) {
                    // Listen for a new connection request
                    Socket socket = serverSocket.accept();
                    
                    Platform.runLater( () -> {
                        // Display the client number
                        ta.appendText("Connection from " + socket.toString() +
                                " at " + new Date() + '\n');
                    });

                    // Create and start a new thread for the connection
                    UserSession user = new UserSession(socket, this);
                    new Thread(user).start();
                    userList.add(user);
                    Platform.runLater(() -> {
                        userCount.setText("Users online: " + userList.size());
                    });
                }
            }
            catch(IOException ex) {
                System.err.println(ex.toString());
            }
        }).start();
    }

    public void broadcastMessage(Message msg) {
        Platform.runLater(() -> {
            ta.appendText(msg.getAuthor() + ": " + msg.getText() + "\n");
        });
        for(UserSession user : userList) {
            user.addMessage(msg);
        }
    }

    class UserSession implements Runnable {
        private Socket socket; // A connected socket
        private ChatServer server;

        private boolean running;

        ObjectOutputStream outputToClient;
        ObjectInputStream inputFromClient;

        BlockingQueue<Message> messages;

        /** Construct a thread */
        public UserSession(Socket socket, ChatServer server) {
            this.socket = socket;
            this.server = server;
            messages = new LinkedBlockingQueue<>();

            // Create data input and output streams
            try {
                outputToClient = new ObjectOutputStream(socket.getOutputStream());
                inputFromClient = new ObjectInputStream(socket.getInputStream());
                running = true;
            } catch (IOException e) {
                System.err.println(e.toString());
                System.err.println(socket.toString());
            }
        }

        /** Run a thread */
        public void run() {
            try {
                new Thread(() -> {
                    while(true) {
                        try {
                            Object obj = inputFromClient.readObject();
                            if (obj instanceof Message) {
                                Message msg = (Message) obj;
                                server.broadcastMessage(msg);
                            }
                        } catch (IOException|ClassNotFoundException ex) {
                            System.out.println(ex.toString());
                            running = false;
                            server.userList.remove(this);
                            Platform.runLater(() -> {
                                userCount.setText("Users online: " + userList.size());
                            });
                            break;
                        }
                    }
                }).start();
                // Continuously serve the client
                while (running) {
                    while(!messages.isEmpty()) {
                        outputToClient.writeObject(messages.poll());
                    }
                        outputToClient.flush();
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }

        void addMessage(Message msg) {
            messages.offer(msg);
        }
    }
}