package engine;

import game.Direction;
import game.client.GameClient;
import game.client.entities.Bomb;
import game.client.entities.Player;
import game.server.entities.Block;
import global.Constants;
import global.JSONCreator;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {
    Thread gameThread;
    KeyHandler keyHandler;
    FocusHandler focusHandler;
    Player player;
    Player enemy;
    Map<UUID, Player> playerMap;
    GameClient gameClient;
    List<Entity> gameMapEntities;

    public GamePanel() {
        keyHandler = new KeyHandler();
        focusHandler = new FocusHandler(keyHandler);
        gameClient = new GameClient();
        player = new Player();
        playerMap = new HashMap<>();
        playerMap.put(player.uuid, player);

        setPreferredSize(new Dimension(Constants.PANEL_WIDTH, Constants.PANEL_HEIGHT));
        setBackground(Color.WHITE);
        setDoubleBuffered(true);
        addKeyListener(keyHandler);
        addFocusListener(focusHandler);
        setFocusable(true);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        long nextDrawTime = System.nanoTime() + Constants.DRAW_INTERVAL;

        registerPlayer();
        this.gameMapEntities = getGameMap();
        getGameStart();
        gameClient.startSocketListener();

        while (gameThread != null) {
            long currTime = System.nanoTime();

            if (currTime < nextDrawTime) {
                continue;
            }
            update();
            repaint();
            nextDrawTime = currTime + Constants.DRAW_INTERVAL;
        }
    }

    private void registerPlayer() {
        gameClient.sendMessage(JSONCreator.registerPlayer(player.uuid).toString());
        JSONObject response = gameClient.receiveMessage();

        System.out.println("Client has received " + response.toString());

        // The server determines the initial player position.
        player.x = response.getInt(Constants.X);
        player.y = response.getInt(Constants.Y);

        if (!response.getBoolean(Constants.ACK)) {
            throw new RuntimeException("Did not receive ACK from server upon registration.");
        }
        System.out.println("Player " + player.uuid + " registered.");
    }

    private List<Entity> getGameMap() {
        System.out.println("Getting game map...");

        JSONObject response = gameClient.receiveMapMessage();

        while (!response.has(Constants.GAME_MAP)) {
            System.err.println("Client ignoring: " + response);
        }
        System.out.println("Client has received game map " + response);

        List<Entity> gameMapEntities = new LinkedList<>();
        JSONArray gameMapJson = response.getJSONArray(Constants.GAME_MAP);
        for (int i = 0; i < gameMapJson.length(); i++) {
            JSONObject jsonObject = gameMapJson.getJSONObject(i);

            if (jsonObject.getString(Constants.ENTITY).equals(Constants.BLOCK)) {
                UUID blockUUID = UUID.fromString(jsonObject.getString(Constants.UUID));
                int blockX = jsonObject.getInt(Constants.X);
                int blockY = jsonObject.getInt(Constants.Y);

                gameMapEntities.add(new Block(blockUUID, blockX, blockY));
            } else if (jsonObject.getString(Constants.ENTITY).equals(Constants.BOX)) {
                UUID boxUUID = UUID.fromString(jsonObject.getString(Constants.UUID));
                int boxX = jsonObject.getInt(Constants.X);
                int boxY = jsonObject.getInt(Constants.Y);

                gameMapEntities.add(new game.server.entities.Box(boxUUID, boxX, boxY));
            } else {
                throw new RuntimeException("Invalid game map entity type: " + jsonObject.getString(Constants.ENTITY));
            }
        }
        return gameMapEntities;
    }

    private void getGameStart() {
        JSONObject gameStart = gameClient.receiveMessage();

        while (!gameStart.has(Constants.START)) {
            gameStart = gameClient.receiveMessage();
        }

        if (!gameStart.getBoolean(Constants.START)) {
            throw new RuntimeException("Did not receive game start from server.");
        }
        enemy = new Player(UUID.fromString(gameStart.getString(Constants.PLAYER_UUID)), gameStart.getInt(Constants.X), gameStart.getInt(Constants.Y));
        playerMap.put(enemy.uuid, enemy);
        System.out.println("Received game start");
    }

    public void update() {
        handleInput();
        handleServerMessages();
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D playerGraphics2D = (Graphics2D) graphics;
        List<Graphics2D> playerBombGraphics2DList = new LinkedList<>();

        // Draw player.
        playerGraphics2D.setColor(player.color);
        playerGraphics2D.fillRect(player.x, player.y, Constants.TILE_SIZE, Constants.TILE_SIZE);

        // Draw player bombs.
        for (Bomb bomb : player.bombList) {
            Graphics2D bombGraphics2D = (Graphics2D) graphics;
            bombGraphics2D.setColor(bomb.color);
            bombGraphics2D.fillOval(bomb.x, bomb.y, Constants.TILE_SIZE, Constants.TILE_SIZE);
            playerBombGraphics2DList.add(bombGraphics2D);
        }

        Graphics2D enemyGraphics2D = (Graphics2D) graphics;
        List<Graphics2D> enemyBombGraphics2DList = new LinkedList<>();
        if (enemy != null) {
            // Draw enemy.
            enemyGraphics2D.setColor(enemy.color);
            enemyGraphics2D.fillRect(enemy.x, enemy.y, Constants.TILE_SIZE, Constants.TILE_SIZE);

            // Draw player bombs.
            for (Bomb bomb : enemy.bombList) {
                Graphics2D bombGraphics2D = (Graphics2D) graphics;
                bombGraphics2D.setColor(bomb.color);
                bombGraphics2D.fillOval(bomb.x, bomb.y, Constants.TILE_SIZE, Constants.TILE_SIZE);
                enemyBombGraphics2DList.add(bombGraphics2D);
            }
        }

        List<Graphics2D> mapGraphics2DList = new LinkedList<>();
        if (gameMapEntities != null) {
            for (Entity mapEntity : gameMapEntities) {
                Graphics2D mapGraphics2D = (Graphics2D) graphics;
                mapGraphics2D.setColor(mapEntity.color);
                mapGraphics2D.fillRect(mapEntity.x, mapEntity.y, Constants.TILE_SIZE, Constants.TILE_SIZE);
                mapGraphics2DList.add(mapGraphics2D);
            }
        }

        playerGraphics2D.dispose();
        enemyGraphics2D.dispose();
        playerBombGraphics2DList.forEach(Graphics::dispose);
        enemyBombGraphics2DList.forEach(Graphics::dispose);
        mapGraphics2DList.forEach(Graphics::dispose);
    }

    private void handleServerMessages() {
        List<JSONObject> messages = gameClient.receiveMessages();

        if (messages.isEmpty()) {
            return;
        }

        for (JSONObject message : messages) {
            String action = message.getString(Constants.ACTION);

            if (action == null) {
                continue;
            }

            if (action.equals(Constants.MOVE)) {
                handleMovement(message);
            } else if (action.equals(Constants.BOMB)) {
                if (!message.getBoolean(Constants.BOMB_PLACED)) {
                    continue;
                }
                handleBomb(message);
            } else if (action.equals(Constants.BOMB_EXPIRED)) {
                handleBombExpired(message);
            }
        }
    }

    private void handleMovement(JSONObject jsonObject) {
        UUID uuid = UUID.fromString(jsonObject.getString(Constants.PLAYER_UUID));
        int x = jsonObject.getInt(Constants.X);
        int y = jsonObject.getInt(Constants.Y);

        Player p = playerMap.get(uuid);
        p.x = x;
        p.y = y;
    }

    private void handleBomb(JSONObject jsonObject) {
        UUID playerUUID = UUID.fromString(jsonObject.getString(Constants.PLAYER_UUID));
        UUID bombUUID = UUID.fromString(jsonObject.getString(Constants.BOMB_UUID));
        int bombX = jsonObject.getInt(Constants.X);
        int bombY = jsonObject.getInt(Constants.Y);
        Bomb bomb = new Bomb(bombUUID, bombX, bombY);
        Player player = playerMap.get(playerUUID);
        player.bombList.add(bomb);
        player.bombMap.put(bombUUID, bomb);
    }

    private void handleBombExpired(JSONObject jsonObject) {
        UUID playerUUID = UUID.fromString(jsonObject.getString(Constants.PLAYER_UUID));
        UUID bombUUID = UUID.fromString(jsonObject.getString(Constants.BOMB_UUID));
        Player player = playerMap.get(playerUUID);
        Bomb bomb = player.bombMap.get(bombUUID);

        player.bombList.remove(bomb);
        player.bombMap.remove(bombUUID);
    }

    private void handleInput() {
        Direction dir = null;

        if (keyHandler.upPressed) {
            dir = Direction.UP;
        } else if (keyHandler.downPressed) {
            dir = Direction.DOWN;
        } else if (keyHandler.leftPressed) {
            dir = Direction.LEFT;
        } else if (keyHandler.rightPressed) {
            dir = Direction.RIGHT;
        }

        if (dir != null) {
            gameClient.sendMessage(JSONCreator.move(player.uuid, dir).toString());
        }

        if (keyHandler.spacePressed) {
            gameClient.sendMessage(JSONCreator.bomb(player.uuid).toString());
        }
    }
}