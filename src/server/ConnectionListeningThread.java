package server;

import common.StatusMessage;
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
            String messageText = "MultiThread ChatServer started at " + serverSocket + " on " + new Date() + '\n';
            server.sendMessage(new StatusMessage("SYSTEM", messageText));

            while (true) {
                // Listen for a new connection request
                Socket socket = serverSocket.accept();

                Platform.runLater( () -> {
                    // Display the client number
                    server.sendMessage(new StatusMessage("SYSTEM",
                            "Connection from " + socket.toString() + " at " + new Date()));
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
