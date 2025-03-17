package game.server;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ServerSocketListener implements Runnable {
    private final DatagramSocket socket;
    private List<JSONObject> buffer;
    private Thread serverSocketListenerThread;

    public ServerSocketListener(DatagramSocket socket) {
        this.socket = socket;
        this.buffer = Collections.synchronizedList(new LinkedList<>());
    }

    public void startServerSocketListenerThread() {
        serverSocketListenerThread = new Thread(this);
        serverSocketListenerThread.start();
    }

    private void listen() {
        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(packet);
            String received = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();

            System.out.println("Server received:\n" + received);

            this.buffer.add(new JSONObject(received));
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
