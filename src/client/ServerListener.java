package client;

import common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;

public class ServerListener implements Runnable {

    ObjectInputStream fromServer;
    ServerObserver client;

    public ServerListener(ObjectInputStream fromServer, ServerObserver client) {
        this.fromServer = fromServer;
        this.client = client;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Object obj = fromServer.readObject();
                if (obj instanceof Message) {
                    Message msg = (Message) obj;
                    client.receiveMessage(msg);
                }
            } catch (NullPointerException e) {

            } catch (IOException |ClassNotFoundException ex) {
                System.err.println(ex.toString());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
