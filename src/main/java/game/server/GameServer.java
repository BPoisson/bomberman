package game.server;

import game.server.entities.Player;
import global.Constants;
import global.JSONCreator;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class GameServer {
    private List<Player> players;
    private byte[] buffer;
    private DatagramSocket socket;

    public GameServer() {
        this.players = new LinkedList<>();
        buffer = new byte[256];
        try {
            socket = new DatagramSocket(4445);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        getPlayers();
        sendGameStart();

        while (true) {
            System.out.println("Server running.");
//            handleNetworkIO();
//            player.expireBombs();
        }
    }

    private void sendGameStart() {
        for (Player player : players) {
            sendMessage(JSONCreator.gameStart().toString(), player.address, player.port);
        }
        System.out.println("Server sent game start.");
    }

    private void getPlayers() {
        System.out.println("Server getting players...");

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (players.size() < 2) {
            try {
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buffer, buffer.length, address, port);
                String playerData = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();

                System.out.println("Server received player:\n" + playerData);

                players.add(createPlayer(new JSONObject(playerData), address, port));
                sendMessage(JSONCreator.playerAck().toString(), address, port);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Server got 2 players: " + players.size());
    }

    private Player createPlayer(JSONObject playerData, InetAddress address, int port) {
        UUID playerUUID = UUID.fromString(playerData.getString(Constants.UUID));
        int playerX = playerData.getInt(Constants.X);
        int playerY = playerData.getInt(Constants.Y);

        return new Player(playerUUID, playerX, playerY, address, port);
    }

//    private void handleNetworkIO() {
//        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//        try {
//            socket.receive(packet);
//
//            InetAddress address = packet.getAddress();
//            int port = packet.getPort();
//            packet = new DatagramPacket(buffer, buffer.length, address, port);
//
//            String received = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();
//
//            System.out.println("Server received:\n" + received);
//
//            JSONObject request = new JSONObject(received);
//            String action = request.getString(Constants.ACTION);
//            if (action != null && action.equals(Constants.MOVE)) {
//                player.move(Direction.valueOf(request.getString(Constants.DIRECTION)));
//
//                sendMessage(JSONCreator.coord(player.x, player.y).toString(), address, port);
//            } else if (action != null && action.equals(Constants.BOMB)) {
//                int[] bombCoords = player.placeBomb();
//
//                if (bombCoords == null) {
//                    sendMessage(JSONCreator.bombNotPlaced().toString(), address, port);
//                } else {
//                    sendMessage(JSONCreator.bombPlaced(bombCoords[0], bombCoords[1]).toString(), address, port);
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public void sendMessage(String message, InetAddress address, int port) {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
