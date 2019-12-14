package client;

import java.net.ServerSocket;

public class Client {

    private String username;
    private String passowrd_hash;
    private ServerSocket socket;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassowrd_hash() {
        return passowrd_hash;
    }

    public void setPassowrd_hash(String passowrd_hash) {
        this.passowrd_hash = passowrd_hash;
    }

    public ServerSocket getSocket() {
        return socket;
    }

    public void setSocket(ServerSocket socket) {
        this.socket = socket;
    }
    
    
}
