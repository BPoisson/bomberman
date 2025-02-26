package game.client;

import java.io.IOException;
import java.net.*;

public class GameClient {
    private DatagramSocket socket;
    private InetAddress address;

    public GameClient() {
        try {
            this.socket = new DatagramSocket();
            this.address = InetAddress.getByName("localhost");
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 4445);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
