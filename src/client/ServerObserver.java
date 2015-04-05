package client;

import common.Message;

public interface ServerObserver {
    void receiveMessage(Message message);
}
