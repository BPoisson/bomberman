package game.client.io;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SocketListener implements Runnable {
    int port;
    private boolean updateBuffer;
    private final DatagramSocket socket;
    private final InetAddress address;
    private List<JSONObject> buffer;
    Thread socketListenerThread;

    public SocketListener(DatagramSocket socket, InetAddress address, int port) {
        this.port = port;
        this.updateBuffer = true;
        this.socket = socket;
        this.address = address;
        this.buffer = Collections.synchronizedList(new LinkedList<>());
    }

    public void startSocketListenerThread() {
        socketListenerThread = new Thread(this);
        socketListenerThread.start();
    }

    private void listen() {
        System.out.println("Listening");
        if (!updateBuffer) {
            System.out.println("Not updating buffer.");
            return;
        }

        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        try {
            socket.receive(packet);
            String response = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();

            System.out.println("Client received:\n" + response);

            this.buffer.add(new JSONObject(response));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<JSONObject> receive() {
        updateBuffer = false;
        Iterator<JSONObject> iterator = buffer.iterator();
        List<JSONObject> messages = new LinkedList<>();

        while (iterator.hasNext()) {
            JSONObject message = iterator.next();
            messages.add(message);
            iterator.remove();
        }
        updateBuffer = true;
        return messages;
    }

    @Override
    public void run() {
        while (true) {
            if (!updateBuffer) {
                continue;
            }
            listen();
        }
    }
}
