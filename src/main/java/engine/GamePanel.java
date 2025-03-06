package engine;

import game.Direction;
import game.client.GameClient;
import game.client.entities.Bomb;
import game.client.entities.Player;
import global.JSONCreator;
import global.Constants;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {
    Thread gameThread;
    KeyHandler keyHandler;
    Player player;
    Player enemy;
    GameClient gameClient;

    public GamePanel() {
        this.setPreferredSize(new Dimension(Constants.PANEL_WIDTH, Constants.PANEL_HEIGHT));
        this.setBackground(Color.GRAY);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
        this.keyHandler = new KeyHandler();
        this.gameClient = new GameClient();
        this.player = new Player();
        addKeyListener(this.keyHandler);
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
        String response = this.gameClient.sendMessage(JSONCreator.registerPlayer(player.uuid).toString());
        JSONObject responseObj = new JSONObject(response);

        // The server determines the initial player position.
        this.player.x = responseObj.getInt(Constants.X);
        this.player.y = responseObj.getInt(Constants.Y);

        if (!responseObj.getBoolean(Constants.ACK)) {
            throw new RuntimeException("Did not receive ACK from server upon registration.");
        }
        System.out.println("Player " + player.uuid + " registered.");
    }

    private void getGameStart() {
        JSONObject gameStart = this.gameClient.receiveMessage();

        if (!gameStart.getBoolean(Constants.START)) {
            throw new RuntimeException("Did not receive game start from server.");
        }
        System.out.println("Received game start");
    }

    public void update() {
       // handleInput();
        player.expireBombs();
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D playerGraphics2D = (Graphics2D) graphics;
        playerGraphics2D.setColor(player.color);
        playerGraphics2D.fillRect(player.x, player.y, Constants.TILE_SIZE, Constants.TILE_SIZE);

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
            String response = this.gameClient.sendMessage(JSONCreator.move(player.uuid, dir).toString());

            JSONObject responseObj = new JSONObject(response);
            player.x = responseObj.getInt(Constants.X);
            player.y = responseObj.getInt(Constants.Y);
        }

        if (keyHandler.spacePressed) {
            String response = this.gameClient.sendMessage(JSONCreator.bomb(player.uuid).toString());
            JSONObject responseObj = new JSONObject(response);

            if (responseObj.getBoolean(Constants.BOMB_PLACED)) {
                player.bombList.add(new Bomb(responseObj.getInt(Constants.X), responseObj.getInt(Constants.Y)));
            }
        }
    }
}