package engine;

import game.Direction;
import game.client.GameClient;
import game.client.entities.Bomb;
import game.client.entities.Player;
import game.client.entities.Block;
import game.client.entities.Box;
import game.client.entities.Explosion;
import game.client.manager.BombManager;
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
    BombManager bombManager;

    public GamePanel() {
        keyHandler = new KeyHandler();
        focusHandler = new FocusHandler(keyHandler);
        gameClient = new GameClient();
        player = new Player();
        playerMap = new HashMap<>();
        playerMap.put(player.uuid, player);
        bombManager = new BombManager();

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

                gameMapEntities.add(new Box(boxUUID, boxX, boxY));
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

        List<Graphics2D> mapGraphics2DList = new LinkedList<>();
        if (gameMapEntities != null) {
            for (Entity mapEntity : gameMapEntities) {
                Graphics2D mapGraphics2D = (Graphics2D) graphics;
                mapGraphics2D.setColor(mapEntity.color);
                mapGraphics2D.fillRect(mapEntity.x, mapEntity.y, Constants.TILE_SIZE, Constants.TILE_SIZE);
                mapGraphics2DList.add(mapGraphics2D);
            }
        }

        Graphics2D playerGraphics2D = (Graphics2D) graphics;
        // Draw player. Flicker if immune.
        if (!player.isImmune() || new Random().nextBoolean()) {
            playerGraphics2D.setColor(player.color);
            playerGraphics2D.fillRect(player.x, player.y, Constants.TILE_SIZE, Constants.TILE_SIZE);
        }

        Graphics2D enemyGraphics2D = (Graphics2D) graphics;
        // Draw enemy. Flicker if immune.
        if (enemy != null && (!enemy.isImmune() || new Random().nextBoolean())) {
            enemyGraphics2D.setColor(enemy.color);
            enemyGraphics2D.fillRect(enemy.x, enemy.y, Constants.TILE_SIZE, Constants.TILE_SIZE);
        }

        List<Graphics2D> bombGraphics2DList = new LinkedList<>();
        // Draw player bombs.
        for (Bomb bomb : bombManager.getBombs()) {
            Graphics2D bombGraphics2D = (Graphics2D) graphics;
            bombGraphics2D.setColor(bomb.color);
            bombGraphics2D.fillOval(bomb.x, bomb.y, Constants.TILE_SIZE, Constants.TILE_SIZE);
            bombGraphics2DList.add(bombGraphics2D);
        }

        playerGraphics2D.dispose();
        enemyGraphics2D.dispose();
        mapGraphics2DList.forEach(Graphics::dispose);
        bombGraphics2DList.forEach(Graphics::dispose);
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

            switch (action) {
                case Constants.MOVE -> handleMovement(message);
                case Constants.BOMB -> {
                    if (!message.getBoolean(Constants.BOMB_PLACED)) {
                        continue;
                    }
                    handleBomb(message);
                }
                case Constants.BOMB_EXPIRED -> handleBombExpired(message);
                case Constants.EXPLOSION -> handleExplosion(message);
                case Constants.EXPLODED -> handleExploded(message);
                case Constants.PLAYER_HIT -> handlePlayerHit(message);
                case Constants.IMMUNITY_DISABLED -> handleImmunityDisabled(message);
                case Constants.PLAYER_LOST -> handlePlayerLost(message);
                default -> System.err.println("Unhandled message: " + message);
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
        UUID bombUUID = UUID.fromString(jsonObject.getString(Constants.BOMB_UUID));
        UUID playerUUID = UUID.fromString(jsonObject.getString(Constants.PLAYER_UUID));
        int bombX = jsonObject.getInt(Constants.X);
        int bombY = jsonObject.getInt(Constants.Y);
        Bomb bomb = new Bomb(bombUUID, playerUUID, bombX, bombY);
        bombManager.add(bomb);
    }

    private void handleBombExpired(JSONObject jsonObject) {
        UUID bombUUID = UUID.fromString(jsonObject.getString(Constants.BOMB_UUID));
        bombManager.remove(bombUUID);
    }

    private void handleExplosion(JSONObject jsonObject) {
        UUID explosionUUID = UUID.fromString(jsonObject.getString(Constants.EXPLOSION_UUID));
        UUID playerUUID = UUID.fromString(jsonObject.getString(Constants.PLAYER_UUID));
        int explosionX = jsonObject.getInt(Constants.X);
        int explosionY = jsonObject.getInt(Constants.Y);
        Explosion explosion = new Explosion(explosionUUID, playerUUID, explosionX, explosionY);
        bombManager.add(explosion);
    }

    private void handleExploded(JSONObject jsonObject) {
        UUID explodedUUID = UUID.fromString(jsonObject.getString(Constants.EXPLODED_UUID));
        List<Entity> explodedBoxes = new LinkedList<>();

        for (Entity entity : gameMapEntities) {
            if (entity instanceof Box && entity.uuid.equals(explodedUUID)) {
                explodedBoxes.add(entity);
            }
        }
        gameMapEntities.removeAll(explodedBoxes);
    }

    private void handlePlayerHit(JSONObject jsonObject) {
        UUID playerUUID = UUID.fromString(jsonObject.getString(Constants.PLAYER_UUID));
        playerMap.get(playerUUID).handleHit();

        if (player.uuid.equals(playerUUID)) {
            System.err.println("Player hit.");
        } else {
            System.err.println("Enemy hit.");
        }
    }

    private void handleImmunityDisabled(JSONObject jsonObject) {
        UUID playerUUID = UUID.fromString(jsonObject.getString(Constants.PLAYER_UUID));
        playerMap.get(playerUUID).disableImmunity();
    }

    private void handlePlayerLost(JSONObject jsonObject) {
        UUID playerUUID = UUID.fromString(jsonObject.getString(Constants.PLAYER_UUID));

        if (player.uuid.equals(playerUUID)) {
            System.err.println("Player lost.");
        } else {
            System.err.println("Enemy lost.");
        }
    }

    private void handleInput() {
        if (keyHandler.upPressed) {
            gameClient.sendMessage(JSONCreator.move(player.uuid, Direction.UP).toString());
        }
        if (keyHandler.downPressed) {
            gameClient.sendMessage(JSONCreator.move(player.uuid, Direction.DOWN).toString());
        }
        if (keyHandler.leftPressed) {
            gameClient.sendMessage(JSONCreator.move(player.uuid, Direction.LEFT).toString());
        }
        if (keyHandler.rightPressed) {
            gameClient.sendMessage(JSONCreator.move(player.uuid, Direction.RIGHT).toString());
        }
        if (keyHandler.spacePressed) {
            gameClient.sendMessage(JSONCreator.bomb(player.uuid).toString());
            keyHandler.spacePressed = false;
        }
    }
}