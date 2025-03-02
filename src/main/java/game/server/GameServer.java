package game.server;

import game.server.entities.Player;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class GameServer {
    private Player player;
    private byte[] buffer;
    private DatagramSocket socket;

    public GameServer() {
        this.player = new Player();
        buffer = new byte[256];
        try {
            socket = new DatagramSocket(4445);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buffer, buffer.length, address, port);

                String received = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();

                System.out.println("Server received:\n" + received);

                JSONObject request = new JSONObject(received);
                String action = (String) request.get("action");
                if (action != null && action.equals("move")) {
                    player.move((String) request.get("dir"));
                }
                String response = String.format("{\nx:%s,\ny:%s\n}", player.x, player.y);
                DatagramPacket responsePacket = new DatagramPacket(response.getBytes(), response.length(), address, port);
                socket.send(responsePacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
