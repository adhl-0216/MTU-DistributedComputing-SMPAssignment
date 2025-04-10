package server;

import protocol.SMPSessionManager;
import protocol.SMPStreamSocket;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AppSessionManager implements SMPSessionManager {
    private final Map<SMPStreamSocket, String> userSessions = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void loginUser(SMPStreamSocket socket, String username) {
        userSessions.put(socket, username);
        System.out.println(username + " logged in.");
    }

    @Override
    public Optional<String> getUsername(SMPStreamSocket socket) {
        return Optional.ofNullable(userSessions.get(socket));
    }

    @Override
    public void logoutUser(SMPStreamSocket socket) {
        String username = userSessions.remove(socket);
        if (username != null) {
            System.out.println(username + " logged out.");
        }
    }
}