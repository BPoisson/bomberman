package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class GameServer {
    private byte[] buffer;
    private DatagramSocket socket;

    public GameServer() {
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

                socket.send(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // socket.close();
    }
}
