package game.server;

import engine.Entity;
import game.Direction;
import game.server.entities.Bomb;
import game.server.entities.GameMap;
import game.server.entities.Player;
import global.Constants;
import global.Coordinate;
import global.JSONCreator;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

public class GameServer {
    private GameMap gameMap;
    private List<Player> players;
    private Map<UUID, Player> playerMap;
    private byte[] buffer;
    private DatagramSocket socket;
    private ServerSocketListener serverSocketListener;

    public GameServer() {
        this.gameMap = new GameMap();
        this.players = new LinkedList<>();
        this.playerMap = new HashMap<>();
        this.buffer = new byte[256];
        try {
            this.socket = new DatagramSocket(4445);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        this.serverSocketListener = new ServerSocketListener(socket);
    }

    public void run() {
        getPlayers();
        sendGameMap();
        sendGameStart();

        serverSocketListener.startServerSocketListenerThread();
        while (true) {
            handleGameUpdates();
            expireBombs();
        }
    }

    private void getPlayers() {
        System.out.println("Server getting players...");

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (players.size() < 2) {
            try {
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                String playerData = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();
                UUID playerUUID = UUID.fromString(new JSONObject(playerData).getString(Constants.PLAYER_UUID));
                if (playerMap.containsKey(playerUUID)) {
                    continue;
                }
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

    private void sendGameMap() {
        JSONObject gameMapJson = JSONCreator.createGameMapJson(gameMap);
        Player player1 = players.get(0);
        Player player2 = players.get(1);

        sendMessage(gameMapJson.toString(), player1.address, player1.port);
        System.out.println("Server sent game map to: " + player1.uuid);

        sendMessage(gameMapJson.toString(), player2.address, player2.port);
        System.out.println("Server sent game map to: " + player2.uuid);
    }

    private void sendGameStart() {
        Player player1 = players.get(0);
        Player player2 = players.get(1);

        sendMessage(JSONCreator.gameStart(player2.uuid, player2.x, player2.y).toString(), player1.address, player1.port);
        System.out.println("Server sent game start to: " + player2.uuid);

        sendMessage(JSONCreator.gameStart(player1.uuid, player1.x, player1.y).toString(), player2.address, player2.port);
        System.out.println("Server sent game start to: " + player1.uuid);
    }

    private Player createPlayer(int playerNum, JSONObject playerData, InetAddress address, int port) {
        UUID playerUUID = UUID.fromString(playerData.getString(Constants.PLAYER_UUID));
        int playerX = playerNum == 1 ? Constants.PLAYER_1_X : Constants.PLAYER_2_X;
        int playerY = Constants.PLAYER_Y;

        return new Player(playerUUID, playerX, playerY, address, port);
    }

    private void handleGameUpdates() {
        List<JSONObject> messages = serverSocketListener.receive();

        for (JSONObject message : messages) {
            if (!message.has(Constants.ACTION)) {
                System.err.println("Server ignoring message: " + message);
                continue;
            }

            UUID playerUUID = UUID.fromString(message.getString(Constants.PLAYER_UUID));
            Player player = playerMap.get(playerUUID);
            String action = message.getString(Constants.ACTION);

            if (action == null) {
                return;
            }

            if (action.equals(Constants.MOVE)) {
                handleMovement(player, Direction.valueOf(message.getString(Constants.DIRECTION)));
            } else if (action.equals(Constants.BOMB)) {
                handleBomb(player);
            }
        }
    }

    private void handleMovement(Player player, Direction direction) {
        Coordinate playerNextCoord = player.getNextPosition(direction);

        if (checkMapCollision(playerNextCoord)) {
            return;
        }
        player.move(direction);

        for (Player p : players) {
            sendMessage(JSONCreator.playerMoved(player.uuid, player.x, player.y).toString(), p.address, p.port);
        }
    }

    private void handleBomb(Player player) {
        Bomb bomb = player.placeBomb();

        if (bomb == null) {
            sendMessage(JSONCreator.bombNotPlaced().toString(), player.address, player.port);
        } else {
            for (Player p : players) {
                sendMessage(JSONCreator.bombPlaced(player.uuid, bomb.uuid, bomb.x, bomb.y).toString(), p.address, p.port);
            }
        }
    }

    private void expireBombs() {
        for (Player player : players) {
            List<Bomb> expiredBombs = player.expireBombs();

            for (Bomb b : expiredBombs) {
                for (Player p : players) {
                    sendMessage(JSONCreator.bombExpired(player.uuid, b.uuid).toString(), p.address, p.port);
                }
            }
        }
    }
    
    private boolean checkMapCollision(Coordinate coordinate) {
        int playerXMin = coordinate.x + 1;
        int playerYMin = coordinate.y + 1;
        int playerXMax = coordinate.x + Constants.TILE_SIZE - 1;
        int playerYMax = coordinate.y + Constants.TILE_SIZE - 1;
        
        for (Entity entity : gameMap.mapEntities) {
            int entityXMin = entity.x;
            int entityYMin = entity.y;
            int entityXMax = entity.x + Constants.TILE_SIZE;
            int entityYMax = entity.y + Constants.TILE_SIZE;

            if ((entityXMin <= playerXMax && playerXMax <= entityXMax) || (entityXMin <= playerXMin && playerXMin <= entityXMax)) {
                if ((entityYMin <= playerYMax && playerYMax <= entityYMax) || (entityYMin <= playerYMin && playerYMin <= entityYMax)) {
                    System.out.println("Player: " + playerXMin + "," + playerYMin + " : " + playerXMax + "," + playerYMax);
                    System.out.println("Entity: " + entityXMin + "," + entityYMin + " : " + entityXMax + "," + entityYMax);
                    return true;
                }
            }
        }
        return false;
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
