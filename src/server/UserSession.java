package server;

import common.Message;
import common.StatusMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UserSession implements Runnable {
    private ChatServer server;

    private boolean running;
    private String username;

    ObjectOutputStream outputToClient;
    ObjectInputStream inputFromClient;

    BlockingQueue<Message> messages;

    public UserSession(Socket socket, ChatServer server) {
        this.server = server;
        messages = new LinkedBlockingQueue<>();

        // Create data input and output streams
        try {
            outputToClient = new ObjectOutputStream(socket.getOutputStream());
            inputFromClient = new ObjectInputStream(socket.getInputStream());
            running = true;
        } catch (IOException e) {
            System.err.println("Error: " + e.toString() + "\nCaused by: " + socket.toString());
        }
    }

    private boolean userIsSet() {
        return username != null;
    }

    public void run() {
        try {
            new Thread(() -> {
                while(true) {
                    try {
                        Object obj = inputFromClient.readObject();
                        if (obj instanceof Message) {
                            Message msg = (Message) obj;

                            if (!userIsSet())
                                username = msg.getAuthor();

                            server.broadcastMessage(msg);
                        }
                    } catch (IOException|ClassNotFoundException ex) {
                        System.out.println(ex.toString());
                        running = false;
                        server.broadcastMessage(new StatusMessage(username, "has disconnected."));
                        server.unregisterUser(this);
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