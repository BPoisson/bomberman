package game.server;

import game.Direction;
import game.server.entities.Player;
import global.Constants;
import global.JSONCreator;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

public class GameServer {
    private List<Player> players;
    private Map<UUID, Player> playerMap;
    private byte[] buffer;
    private DatagramSocket socket;

    public GameServer() {
        this.players = new LinkedList<>();
        this.playerMap = new HashMap<>();
        this.buffer = new byte[256];
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
            handleGameUpdates();
//            player.expireBombs();
        }
    }

    private void sendGameStart() {
        Player player1 = players.get(0);
        Player player2 = players.get(1);

        sendMessage(JSONCreator.gameStart(player2.uuid, player2.x, player2.y).toString(), player1.address, player1.port);
        sendMessage(JSONCreator.gameStart(player1.uuid, player1.x, player1.y).toString(), player2.address, player2.port);
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
                int playerNum = players.isEmpty() ? 1 : 2;
                Player player = createPlayer(playerNum, new JSONObject(playerData), address, port);

                System.out.println("Server received player:\n" + playerData);

                players.add(player);
                playerMap.put(player.uuid, player);
                sendMessage(JSONCreator.playerAck(player.x, player.y).toString(), address, port);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Server got 2 players: " + players.size());
    }

    private Player createPlayer(int playerNum, JSONObject playerData, InetAddress address, int port) {
        UUID playerUUID = UUID.fromString(playerData.getString(Constants.UUID));
        int playerX = playerNum == 1 ? Constants.PLAYER_1_X : Constants.PLAYER_2_X;
        int playerY = Constants.PLAYER_Y;

        return new Player(playerUUID, playerX, playerY, address, port);
    }

    private void handleGameUpdates() {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(packet);

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buffer, buffer.length, address, port);

            String received = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();

            System.out.println("Server received:\n" + received);

            JSONObject request = new JSONObject(received);
            UUID playerUUID = UUID.fromString(request.getString(Constants.UUID));
            Player player = playerMap.get(playerUUID);
            String action = request.getString(Constants.ACTION);
            if (action != null && action.equals(Constants.MOVE)) {
                player.move(Direction.valueOf(request.getString(Constants.DIRECTION)));

                for (Player p : players) {
                    sendMessage(JSONCreator.playerMoved(player.uuid, player.x, player.y).toString(), p.address, p.port);
                }
            } else if (action != null && action.equals(Constants.BOMB)) {
                int[] bombCoords = player.placeBomb();

                if (bombCoords == null) {
                    sendMessage(JSONCreator.bombNotPlaced().toString(), address, port);
                } else {
                    // TODO: Handle bomb placement update for other player's client.
                    sendMessage(JSONCreator.bombPlaced(bombCoords[0], bombCoords[1]).toString(), address, port);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
