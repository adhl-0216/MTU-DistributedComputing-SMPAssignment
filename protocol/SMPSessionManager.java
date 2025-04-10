package protocol;

import java.util.Optional;

public interface SMPSessionManager {
    void loginUser(SMPStreamSocket socket, String username);
    Optional<String> getUsername(SMPStreamSocket socket);
    void logoutUser(SMPStreamSocket socket);
}