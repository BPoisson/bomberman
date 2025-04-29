package game.server;

import engine.Entity;
import game.Direction;
import game.server.entities.*;
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
            Map<UUID, List<Bomb>> playerExplodedBombMap = expireBombs();
            propagateExplosions(playerExplodedBombMap);
            handleExplosionCollisions();
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
        sendMessage(gameMapJson.toString(), player2.address, player2.port);
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
        List<Entity> gameEntities = getGameEntities();

        if (player.checkCollision(playerNextCoord, gameEntities)) {
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

    private Map<UUID, List<Bomb>> expireBombs() {
        Map<UUID, List<Bomb>> playerExplodedBombMap = new HashMap<>();

        for (Player player : players) {
            List<Bomb> expiredBombs = player.expireBombs();
            playerExplodedBombMap.put(player.uuid, new LinkedList<>());

            for (Bomb bomb : expiredBombs) {
                for (Player p : players) {
                    sendMessage(JSONCreator.bombExpired(player.uuid, bomb.uuid).toString(), p.address, p.port);
                }
            }
            playerExplodedBombMap.get(player.uuid).addAll(expiredBombs);
        }
        return playerExplodedBombMap;
    }

    private void propagateExplosions(Map<UUID, List<Bomb>> playerExplodedBombMap) {
        List<Entity> gameEntities = getGameEntities();

        for (Player player : players) {
            List<Explosion> propagated = new LinkedList<>();
            List<Bomb> explosions = new LinkedList<>(playerExplodedBombMap.get(player.uuid));
            explosions.addAll(player.getExplosions());

            for (Bomb explosion : explosions) {
                propagated.addAll(explosion.propagate(gameEntities));
            }

            for (Explosion explosion : propagated) {
                for (Player p : players) {
                    sendMessage(JSONCreator.explosion(player.uuid, explosion.uuid, explosion.x, explosion.y).toString(), p.address, p.port);
                }
            }
            player.addExplosions(propagated);
        }
    }

    private void handleExplosionCollisions() {
        List<Entity> gameEntities = getGameEntities();
        List<Entity> explodedEntities = new LinkedList<>();

        for (Player player : players) {
            List<Explosion> explosions = player.getExplosions();

            for (Explosion explosion : explosions) {
                explodedEntities.addAll(explosion.checkExplodeCollision(gameEntities));
            }
        }

        for (Entity entity : explodedEntities) {
            if (entity instanceof Box) {
                gameMap.mapEntities.remove(entity); // Remove exploded Boxes.
            } else if (entity instanceof Player) {
                Player player = playerMap.get(entity.uuid);
                player.decrementHealth();
            }
            for (Player p : players) {
                sendMessage(JSONCreator.exploded(entity.uuid).toString(), p.address, p.port);
            }
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

    public List<Entity> getGameEntities() {
        List<Entity> gameEntities = new LinkedList<>(gameMap.mapEntities);
        gameEntities.addAll(players);

        return gameEntities;
    }
}
