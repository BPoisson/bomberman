package game.server;

import engine.Entity;
import game.Direction;
import game.server.entities.*;
import game.server.manager.BombManager;
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
    private final GameMap gameMap;
    private final List<Player> players;
    private final Map<UUID, Player> playerMap;
    private final BombManager bombManager;
    private final byte[] buffer;
    private final DatagramSocket socket;
    private final ServerSocketListener serverSocketListener;

    public GameServer() {
        this.gameMap = new GameMap();
        this.players = new LinkedList<>();
        this.playerMap = new HashMap<>();
        this.bombManager = new BombManager();
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
            handlePlayerImmunity();
            List<Bomb> expiredBombs = expireBombs();
            propagateExplosions(expiredBombs);
            handleExplosionCollisions();

            if (checkWinCondition() > 0) {
                System.err.println("Game over.");
            }
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

    private void handlePlayerImmunity() {
        List<UUID> playerImmunityOver = new LinkedList<>();

        // Set player immunity off if time's-up.
        for (Player player : players) {
            boolean immuneDisabled = player.checkDisableImmunity();

            if (immuneDisabled) {
                playerImmunityOver.add(player.uuid);
            }
        }

        for (UUID playerUUID : playerImmunityOver) {
            for (Player p : players) {
                sendMessage(JSONCreator.playerImmunityDisabled(playerUUID).toString(), p.address, p.port);
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
            bombManager.add(bomb);
            for (Player p : players) {
                sendMessage(JSONCreator.bombPlaced(player.uuid, bomb.uuid, bomb.x, bomb.y).toString(), p.address, p.port);
            }
        }
    }

    private List<Bomb> expireBombs() {
        List<Bomb> expiredBombs = bombManager.expireBombs();
        for (Bomb bomb : expiredBombs) {
            for (Player p : players) {
                sendMessage(JSONCreator.bombExpired(bomb.uuid).toString(), p.address, p.port);
            }
        }
        return expiredBombs;
    }

    private void propagateExplosions(List<Bomb> expiredBombs) {
        List<Bomb> explosions = bombManager.propagateExplosions(expiredBombs, getGameEntities());

        for (Bomb explosion : explosions) {
            for (Player p : players) {
                sendMessage(JSONCreator.explosion(explosion.uuid, explosion.playerUUID, explosion.x, explosion.y).toString(), p.address, p.port);
            }
        }
    }

    private void handleExplosionCollisions() {
        List<Entity> explodedEntities = bombManager.handleExplosionCollisions(getGameEntities());

        for (Entity entity : explodedEntities) {
            if (entity instanceof Box box) {
                handleBoxExplosion(box);
            } else if (entity instanceof Player playerHit) {
                handlePlayerExplosion(playerHit);
            }

        }
    }

    private void handleBoxExplosion(Box box) {
        gameMap.mapEntities.remove(box); // Remove exploded Boxes.

        for (Player p : players) {
            sendMessage(JSONCreator.exploded(box.uuid).toString(), p.address, p.port);
        }
    }

    private void handlePlayerExplosion(Player player) {
        if (!player.isImmune) {
            player.handleHit();

            for (Player p : players) {
                sendMessage(JSONCreator.playerHit(player.uuid).toString(), p.address, p.port);
            }
        }
    }

    private int checkWinCondition() {
        List<UUID> losingPlayerUUIDs = new LinkedList<>();

        for (Player player : players) {
            if (player.getHealth() <= 0) {
                losingPlayerUUIDs.add(player.uuid);
            }
        }

        for (UUID losingPlayerUUID : losingPlayerUUIDs) {
            for (Player p : players) {
                sendMessage(JSONCreator.playerLost(losingPlayerUUID).toString(), p.address, p.port);
            }
        }
        return losingPlayerUUIDs.size();
    }

    public void sendMessage(String message, InetAddress address, int port) {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
//        System.err.println("Server sending: " + message);
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
