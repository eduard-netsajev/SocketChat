package server;

import common.Message;

public interface ChatServer {
    void sendMessage(Message message);
    void registerUser(UserSession user);
    void unregisterUser(UserSession user);
}
