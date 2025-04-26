package game.client.io;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SocketListener implements Runnable {
    private int port;
    private final DatagramSocket socket;
    private final InetAddress address;
    private List<JSONObject> buffer;
    private Thread socketListenerThread;

    public SocketListener(DatagramSocket socket, InetAddress address, int port) {
        this.port = port;
        this.socket = socket;
        this.address = address;
        this.buffer = Collections.synchronizedList(new LinkedList<>());
    }

    public void startSocketListenerThread() {
        socketListenerThread = new Thread(this);
        socketListenerThread.start();
    }

    private void listen() {
        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        try {
            socket.receive(packet);
            String response = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();

//            System.out.println("Client received:\n" + response);

            this.buffer.add(new JSONObject(response));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<JSONObject> receive() {
        List<JSONObject> messages = new LinkedList<>(buffer);
        buffer.removeAll(messages);
        return messages;
    }

    @Override
    public void run() {
        while (true) {
            listen();
        }
    }
}
