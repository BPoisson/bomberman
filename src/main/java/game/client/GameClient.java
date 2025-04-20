package game.client;

import game.client.io.SocketListener;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class GameClient {
    private int port;
    private DatagramSocket socket;
    private InetAddress address;
    private SocketListener socketListener;

    public GameClient() {
        try {
            this.port = 4445;
            this.socket = new DatagramSocket();
            this.address = InetAddress.getByName("localhost");
            this.socketListener = new SocketListener(socket, address, port);
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void startSocketListener() {
        socketListener.startSocketListenerThread();
    }

    public void sendMessage(String message) {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject receiveMessage() {
        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);

        try {
            socket.receive(packet);
            String response = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();

            System.out.println("Client received:\n" + response);

            return new JSONObject(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject receiveMapMessage() {
        byte[] buffer = new byte[32768];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);

        try {
            socket.receive(packet);
            String response = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();

            System.out.println("Client received:\n" + response);

            return new JSONObject(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<JSONObject> receiveMessages() {
        return socketListener.receive();
    }
}
