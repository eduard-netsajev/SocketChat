package server;

import javafx.application.Platform;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class ConnectionListeningThread extends Thread {

    private ChatServer server;
    private ServerSocket serverSocket;

    public ConnectionListeningThread(ChatServer server, ServerSocket serverSocket) {
        super();
        this.server = server;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            Platform.runLater(() -> {
                server.appendToTextArea(
                        "MultiThread ChatServer started at " + new Date() + '\n');
            });

            while (true) {
                // Listen for a new connection request
                Socket socket = serverSocket.accept();

                Platform.runLater( () -> {
                    // Display the client number
                    server.appendToTextArea(
                            "Connection from " + socket.toString() + " at " + new Date() + '\n');
                });

                // Create and start a new thread for the connection
                UserSession user = new UserSession(socket, server);
                server.registerUser(user);
                new Thread(user).start();
            }
        }
        catch(IOException ex) {
            System.err.println(ex.toString());
        }
    }
}
