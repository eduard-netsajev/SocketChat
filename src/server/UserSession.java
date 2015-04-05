package server;

import common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UserSession implements Runnable {
    private Socket socket;
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
            System.err.println("Error: " + e.toString() + "\nCaused by: " + socket.toString());
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