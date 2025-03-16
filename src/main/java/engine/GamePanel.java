package engine;

import game.Direction;
import game.client.GameClient;
import game.client.entities.Bomb;
import game.client.entities.Player;
import global.Constants;
import global.JSONCreator;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class GamePanel extends JPanel implements Runnable {
    Thread gameThread;
    KeyHandler keyHandler;
    FocusHandler focusHandler;
    Player player;
    Player enemy;
    Map<UUID, Player> playerMap;
    GameClient gameClient;

    public GamePanel() {
        keyHandler = new KeyHandler();
        focusHandler = new FocusHandler(keyHandler);
        gameClient = new GameClient();
        player = new Player();
        playerMap = new HashMap<>();
        playerMap.put(player.uuid, player);

        setPreferredSize(new Dimension(Constants.PANEL_WIDTH, Constants.PANEL_HEIGHT));
        setBackground(Color.GRAY);
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

    private void getGameStart() {
        JSONObject gameStart = gameClient.receiveMessage();

        if (!gameStart.getBoolean(Constants.START)) {
            throw new RuntimeException("Did not receive game start from server.");
        }
        enemy = new Player(UUID.fromString(gameStart.getString(Constants.UUID)), gameStart.getInt(Constants.X), gameStart.getInt(Constants.Y));
        playerMap.put(enemy.uuid, enemy);
        System.out.println("Received game start");
    }

    public void update() {
        handleInput();
        handleServerMessages();
//        player.expireBombs();
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D playerGraphics2D = (Graphics2D) graphics;
        playerGraphics2D.setColor(player.color);
        playerGraphics2D.fillRect(player.x, player.y, Constants.TILE_SIZE, Constants.TILE_SIZE);

        if (enemy != null) {
            Graphics2D enemyGraphics2D = (Graphics2D) graphics;
            enemyGraphics2D.setColor(enemy.color);
            enemyGraphics2D.fillRect(enemy.x, enemy.y, Constants.TILE_SIZE, Constants.TILE_SIZE);
        }

        List<Graphics2D> bombGraphics2DList = new LinkedList<>();

        for (Bomb bomb : player.bombList) {
            Graphics2D bombGraphics2D = (Graphics2D) graphics;
            bombGraphics2D.setColor(bomb.color);
            bombGraphics2D.fillRect(bomb.x, bomb.y, Constants.TILE_SIZE, Constants.TILE_SIZE);
            bombGraphics2DList.add(bombGraphics2D);
        }
        playerGraphics2D.dispose();
        bombGraphics2DList.forEach(Graphics::dispose);
    }

    private void handleServerMessages() {
        List<JSONObject> messages = gameClient.receiveMessages();

        if (messages.isEmpty()) {
            return;
        }

        for (JSONObject message : messages) {
            String action = message.getString(Constants.ACTION);

            if (action != null && action.equals(Constants.MOVE)) {
                handleMovement(message);
            }
        }
    }

    private void handleMovement(JSONObject jsonObject) {
        UUID uuid = UUID.fromString(jsonObject.getString(Constants.UUID));
        int x = jsonObject.getInt(Constants.X);
        int y = jsonObject.getInt(Constants.Y);

        Player p = playerMap.get(uuid);
        p.x = x;
        p.y = y;
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
//            JSONObject responseObj = new JSONObject(response);
//
//            if (responseObj.getBoolean(Constants.BOMB_PLACED)) {
//                player.bombList.add(new Bomb(responseObj.getInt(Constants.X), responseObj.getInt(Constants.Y)));
//            }
        }
    }
}